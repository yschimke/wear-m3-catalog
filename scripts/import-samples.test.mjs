import { test } from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

import {
  applyPatches,
  packageDirOf,
  quarantined,
  vendor,
  writeProvenance,
} from "./import-samples.mjs";

/** The package the fixture's samples declare — and so the directories they vendor into. */
const PKG = "androidx/compose/material3/samples";

/** A throwaway upstream checkout: `<root>/<path>/*.kt`. */
function fakeUpstream(files) {
  const root = mkdtempSync(join(tmpdir(), "import-samples-test-"));
  const path = `compose/material3/material3/samples/src/main/java/${PKG}`;
  mkdirSync(join(root, path), { recursive: true });
  for (const [name, text] of Object.entries(files)) {
    writeFileSync(join(root, path, name), text);
  }
  return { root, manifest: { paths: [path], repo: "r", ref: "abc123", artifactCoordinates: "g:a", artifactVersion: "1" } };
}

test("vendors every .kt file under the manifest's paths", () => {
  const { root, manifest } = fakeUpstream({
    "ButtonSamples.kt": "fun a() {}",
    "CardSamples.kt": "fun b() {}",
    "README.md": "not kotlin",
  });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    const result = vendor(root, manifest, out);
    assert.deepEqual(result.copied, [`${PKG}/ButtonSamples.kt`, `${PKG}/CardSamples.kt`]);
    assert.deepEqual(result.skipped, []);
    // Byte-identical: the vendored tree is upstream's bytes, never reformatted.
    assert.equal(readFileSync(join(out, PKG, "ButtonSamples.kt"), "utf8"), "fun a() {}");
  } finally {
    rmSync(root, { recursive: true, force: true });
    rmSync(out, { recursive: true, force: true });
  }
});

test("skips a quarantined file and reports it", () => {
  const { root, manifest } = fakeUpstream({
    "ButtonSamples.kt": "fun a() {}",
    "NeedsActivitySamples.kt": "fun b() {}",
  });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    const skip = new Map([["NeedsActivitySamples.kt", "needs a real Activity"]]);
    const result = vendor(root, manifest, out, skip);
    assert.deepEqual(result.copied, [`${PKG}/ButtonSamples.kt`]);
    // Reported by NAME, not by path: quarantine.json names files, and the reader who wrote that
    // entry should see back the string they wrote.
    assert.deepEqual(result.skipped, ["NeedsActivitySamples.kt"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
    rmSync(out, { recursive: true, force: true });
  }
});

test("vendoring is idempotent — a second run reproduces the tree exactly", () => {
  const { root, manifest } = fakeUpstream({ "A.kt": "fun a() {}" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    vendor(root, manifest, out);
    const first = readFileSync(join(out, PKG, "A.kt"), "utf8");
    vendor(root, manifest, out);
    assert.equal(readFileSync(join(out, PKG, "A.kt"), "utf8"), first);
  } finally {
    rmSync(root, { recursive: true, force: true });
    rmSync(out, { recursive: true, force: true });
  }
});

test("vendoring clears a file that upstream no longer has", () => {
  // The destination is rebuilt rather than merged into, so a sample deleted upstream disappears
  // here too instead of lingering as a render nothing can trace back to a commit.
  const { root, manifest } = fakeUpstream({ "A.kt": "fun a() {}" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    mkdirSync(out, { recursive: true });
    writeFileSync(join(out, "Stale.kt"), "fun stale() {}");
    const result = vendor(root, manifest, out);
    assert.deepEqual(result.copied, [`${PKG}/A.kt`]);
    assert.throws(() => readFileSync(join(out, "Stale.kt"), "utf8"));
  } finally {
    rmSync(root, { recursive: true, force: true });
    rmSync(out, { recursive: true, force: true });
  }
});

test("quarantined() maps file names to their stated reasons", () => {
  const dir = mkdtempSync(join(tmpdir(), "q-"));
  const path = join(dir, "quarantine.json");
  writeFileSync(
    path,
    JSON.stringify({ samples: [{ file: "A.kt", reason: "needs a Context" }, { file: "B.kt" }] }),
  );
  try {
    const map = quarantined(path);
    assert.equal(map.get("A.kt"), "needs a Context");
    // A missing reason is recorded rather than dropped: the entry still excludes the file, and the
    // absence of a reason is itself worth seeing in the import's output.
    assert.equal(map.get("B.kt"), "(no reason given)");
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("quarantined() treats an absent list as an empty one", () => {
  assert.equal(quarantined(join(tmpdir(), "definitely-not-here.json")).size, 0);
});

test("applyPatches is a no-op when there are no patches", () => {
  const dir = mkdtempSync(join(tmpdir(), "p-"));
  try {
    assert.deepEqual(applyPatches(dir, join(dir, "missing")), []);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a patch that applies is applied, to an out dir outside the working tree", () => {
  // The counterpart of the test below, and the one that was missing: with only a NEGATIVE test,
  // `applyPatches` passed for years while being unable to apply anything at all. `--directory`
  // rejected every absolute destination as an `invalid path`, and the only mode that exercised it
  // — `--check`, which vendors into a mkdtemp — reported that as "upstream has moved under this
  // patch". A tmpdir here rather than a fixture inside the repo, because outside the tree is
  // exactly the case that broke.
  const { root, manifest } = fakeUpstream({ "A.kt": "fun a() {}\n" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  const patches = mkdtempSync(join(tmpdir(), "patches-"));
  writeFileSync(
    join(patches, "0001-real.patch"),
    `diff --git a/${PKG}/A.kt b/${PKG}/A.kt\n--- a/${PKG}/A.kt\n+++ b/${PKG}/A.kt\n` +
      "@@ -1 +1 @@\n-fun a() {}\n+fun a() { pinned() }\n",
  );
  try {
    vendor(root, manifest, out);
    assert.deepEqual(applyPatches(out, patches), ["0001-real.patch"]);
    assert.equal(readFileSync(join(out, PKG, "A.kt"), "utf8"), "fun a() { pinned() }\n");
  } finally {
    for (const d of [root, out, patches]) rmSync(d, { recursive: true, force: true });
  }
});

test("a patch that does not apply throws, naming the patch and what to do", () => {
  // The load-bearing behaviour: upstream moving under a fix must FAIL, never silently drop the fix.
  const { root, manifest } = fakeUpstream({ "A.kt": "fun a() {}" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  const patches = mkdtempSync(join(tmpdir(), "patches-"));
  writeFileSync(
    join(patches, "0001-bogus.patch"),
    "diff --git a/Nope.kt b/Nope.kt\n--- a/Nope.kt\n+++ b/Nope.kt\n@@ -1,1 +1,1 @@\n-nope\n+yes\n",
  );
  try {
    vendor(root, manifest, out);
    assert.throws(
      () => applyPatches(out, patches),
      (error) => error.message.includes("0001-bogus.patch") && error.message.includes("silently absent"),
    );
  } finally {
    for (const d of [root, out, patches]) rmSync(d, { recursive: true, force: true });
  }
});

test("provenance records the ref, the artifact and what was skipped", () => {
  const { root, manifest } = fakeUpstream({ "A.kt": "fun a() {}" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    const result = vendor(root, manifest, out, new Map([["B.kt", "why"]]));
    writeProvenance(out, manifest, { ...result, skipped: ["B.kt"] }, ["0001-fix.patch"]);
    const provenance = JSON.parse(readFileSync(join(out, "PROVENANCE.json"), "utf8"));
    assert.equal(provenance.ref, "abc123");
    assert.equal(provenance.artifact, "g:a:1");
    assert.equal(provenance.files, 1);
    assert.deepEqual(provenance.quarantined, ["B.kt"]);
    assert.deepEqual(provenance.patches, ["0001-fix.patch"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
    rmSync(out, { recursive: true, force: true });
  }
});

test("a sample vendors into the directories its package names", () => {
  // The whole reason this shape is load-bearing. Discovery resolves a preview back to its file by
  // asking which source path ENDS WITH the package-qualified path it reads off the class; vendored
  // flat, nothing did, and every sample's `sourceFile` fell back to a string naming no file in the
  // repository — a dead usage panel and a 404 on the page's "source" link.
  const { root, manifest } = fakeUpstream({ "ButtonSamples.kt": "fun a() {}" });
  const out = mkdtempSync(join(tmpdir(), "out-"));
  try {
    const [copied] = vendor(root, manifest, out).copied;
    assert.equal(copied.endsWith("androidx/compose/material3/samples/ButtonSamples.kt"), true);
  } finally {
    for (const d of [root, out]) rmSync(d, { recursive: true, force: true });
  }
});

test("the package directory is whatever follows the module's source root", () => {
  assert.equal(
    packageDirOf("wear/compose/compose-material3/samples/src/main/java/androidx/wear/x/samples"),
    "androidx/wear/x/samples",
  );
  assert.equal(packageDirOf("a/b/src/main/kotlin/com/example"), "com/example");
});

test("a subtree under no source root keeps the flat shape", () => {
  // Not a failure: a manifest may one day point at a directory that is not a module's source root,
  // and vendoring it flat is better than guessing at a package it does not declare.
  assert.equal(packageDirOf("some/loose/directory"), "");
});
