import { test } from "node:test";
import assert from "node:assert/strict";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

import { classifySamples, quarantinedPreviews, renderPreviews } from "./samples-previews.mjs";

/** A throwaway vendored tree: `<root>/<name>` for each entry, nested paths allowed. */
function vendored(files) {
  const root = mkdtempSync(join(tmpdir(), "samples-previews-test-"));
  for (const [name, text] of Object.entries(files)) {
    const path = join(root, name);
    mkdirSync(join(path, ".."), { recursive: true });
    writeFileSync(path, text);
  }
  return root;
}

const SAMPLE = (fn, annotations = "@Sampled\n@Composable", parameters = "") =>
  `${annotations}\nfun ${fn}(${parameters}) {}\n`;

test("wraps a zero-argument @Sampled @Composable", () => {
  const root = vendored({ "ButtonSamples.kt": SAMPLE("ButtonSample") });
  try {
    const { wrap, hasPreview, takesArguments, quarantined } = classifySamples(root, new Map());
    assert.deepEqual(wrap, ["ButtonSample"]);
    assert.deepEqual([hasPreview, takesArguments, quarantined], [[], [], []]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("leaves a sample that already carries @Preview alone", () => {
  // Wrapping it would publish the same composable twice: discovery finds both the upstream
  // annotation and the wrapper.
  const root = vendored({
    "A.kt": SAMPLE("AnnotatedSample", "@Sampled\n@Preview\n@Composable"),
  });
  try {
    const { wrap, hasPreview } = classifySamples(root, new Map());
    assert.deepEqual(wrap, []);
    assert.deepEqual(hasPreview, ["AnnotatedSample"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("refuses a @Sampled function that takes arguments", () => {
  // It is a helper a sample calls, not a call site anything can render on its own — and a wrapper
  // for it would not compile.
  const root = vendored({ "A.kt": SAMPLE("HelperSample", "@Sampled\n@Composable", "text: String") });
  try {
    const { wrap, takesArguments } = classifySamples(root, new Map());
    assert.deepEqual(wrap, []);
    assert.deepEqual(takesArguments, ["HelperSample"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("ignores a function that is not @Sampled", () => {
  const root = vendored({ "A.kt": SAMPLE("FancyIndicator", "@Composable") });
  try {
    const { wrap } = classifySamples(root, new Map());
    assert.deepEqual(wrap, []);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("descends into a sample subpackage", () => {
  // `androidx.wear.compose.material3.samples` keeps shared glyphs in `samples/icons/`; a flat scan
  // is the bug that cost the import 24 compile errors.
  const root = vendored({ "icons/SampleIcons.kt": SAMPLE("IconSample") });
  try {
    assert.deepEqual(classifySamples(root, new Map()).wrap, ["IconSample"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("skips a quarantined sample and reports it separately", () => {
  // The load-bearing behaviour: a sample that compiles but cannot RUN here is taken out ALONE. One
  // failed preview fails the whole render job, and quarantining its file would drop its siblings.
  const root = vendored({
    "OneHandedGestureSamples.kt": SAMPLE("OneHandedGestureButtonSample") +
      SAMPLE("OneHandedGestureButtonInAmbientSample"),
  });
  try {
    const skip = new Map([["OneHandedGestureButtonInAmbientSample", "needs the Wear SDK"]]);
    const { wrap, quarantined } = classifySamples(root, skip);
    assert.deepEqual(wrap, ["OneHandedGestureButtonSample"]);
    assert.deepEqual(quarantined, ["OneHandedGestureButtonInAmbientSample"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("quarantinedPreviews() maps sample names to their stated reasons", () => {
  const dir = mkdtempSync(join(tmpdir(), "q-"));
  const path = join(dir, "quarantine.json");
  writeFileSync(
    path,
    JSON.stringify({ samples: [{ file: "Ignored.kt" }], previews: [{ sample: "A", reason: "why" }, { sample: "B" }] }),
  );
  try {
    const map = quarantinedPreviews(path);
    assert.equal(map.get("A"), "why");
    // A missing reason is recorded rather than dropped: the entry still excludes the sample, and
    // the absence of a reason is itself worth seeing in the generator's output.
    assert.equal(map.get("B"), "(no reason given)");
    // The per-FILE list is the importer's unit and must not leak into this one.
    assert.equal(map.has("Ignored.kt"), false);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("quarantinedPreviews() treats an absent file and an absent list as empty", () => {
  assert.equal(quarantinedPreviews(join(tmpdir(), "definitely-not-here.json")).size, 0);
  const dir = mkdtempSync(join(tmpdir(), "q-"));
  const path = join(dir, "quarantine.json");
  writeFileSync(path, JSON.stringify({ samples: [] }));
  try {
    assert.equal(quarantinedPreviews(path).size, 0);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("the generated wrapper calls the sample by its fully qualified upstream name", () => {
  // The wrappers live in this repo's package and the samples in upstream's; an unqualified call
  // would not resolve, and the `<Sample>Preview` name is what the spec's `preview` field carries.
  const source = renderPreviews(["ButtonSample"]);
  assert.match(source, /package ee\.schimke\.wearm3catalog\.samples/);
  assert.match(
    source,
    /@Preview\n@Composable\nfun ButtonSamplePreview\(\) = androidx\.wear\.compose\.material3\.samples\.ButtonSample\(\)/,
  );
});

test("generation is deterministic — the same names produce the same bytes", () => {
  assert.equal(renderPreviews(["A", "B"]), renderPreviews(["A", "B"]));
});
