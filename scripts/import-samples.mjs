#!/usr/bin/env node
/**
 * Vendor the AndroidX samples named by `samples/import.json` into this repository.
 *
 * See `docs/design/ANDROIDX_SAMPLES.md` for why vendoring is the only option: the sample modules are
 * **not published as artifacts** — `material3-samples`, `compose-material3-samples` and
 * `foundation-samples` all 404 on Google Maven — so there is nothing to depend on, and the source
 * has to come out of the AndroidX tree.
 *
 * ## How the fetch works, and why it is not a clone
 *
 * `platform/frameworks/support` is enormous; a plain clone would cost every CI run minutes and
 * gigabytes for two directories. This uses a **blobless sparse clone** instead:
 *
 *     git clone --filter=blob:none --no-checkout --depth 1 <repo> <cache>
 *     git sparse-checkout set <paths…>
 *     git checkout <ref>
 *
 * Only the trees, and only the blobs under the sparse paths, are ever transferred — **13 MB** for
 * the two sample subtrees, measured. It needs no API token and no directory listing, which is what
 * makes it work identically on a CI runner and a laptop.
 *
 * `ref` in the manifest is a **commit SHA**, never a branch. A branch would make the published
 * catalog irreproducible and turn an upstream bump into an invisible event rather than a reviewable
 * diff.
 *
 * ## Small fixes are patches, never edits
 *
 * The vendored tree stays byte-identical to upstream except for the patches in `samples/patches/`,
 * each applied here after the copy and each carrying its reason in its own header. That is the whole
 * of "make small fixes when needed": a fix is a patch with a stated reason, so the next import
 * re-applies it and a patch that stops applying FAILS rather than silently reverting the fix.
 *
 * A sample that cannot be imported at all — it needs an Activity, a permission, a real `Context` —
 * is listed in `samples/quarantine.json` with a reason and skipped. `--check` fails when a
 * quarantined sample becomes importable again, so the gap cannot rot into a permanent exclusion
 * nobody revisits. Same declared-and-checked-gap contract `kit-unauthorable.json` carries here.
 *
 * Vendored sources must NOT be reformatted: they are upstream's bytes, and ktfmt would rewrite them
 * into a diff against every future import. The module that compiles them excludes this directory
 * from formatting.
 *
 *     node scripts/import-samples.mjs --out <dir>     # vendor into <dir>
 *     node scripts/import-samples.mjs --check         # re-import and diff against the committed tree
 */

import { execFileSync } from "node:child_process";
import {
  cpSync,
  existsSync,
  mkdirSync,
  mkdtempSync,
  readdirSync,
  readFileSync,
  rmSync,
  writeFileSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { basename, dirname, join } from "node:path";

const MANIFEST = "samples/import.json";
const PATCH_DIR = "samples/patches";
const QUARANTINE = "samples/quarantine.json";

const run = (cmd, args, opts = {}) =>
  execFileSync(cmd, args, { encoding: "utf8", stdio: ["ignore", "pipe", "pipe"], ...opts });

/**
 * Fetch the manifest's subtrees at its pinned ref into [cache], reusing an existing checkout when
 * it already sits on that exact commit — an import is then a no-op rather than a re-download.
 */
export function fetchUpstream(manifest, cache) {
  const atRef =
    existsSync(join(cache, ".git")) &&
    (() => {
      try {
        return run("git", ["-C", cache, "rev-parse", "HEAD"]).trim() === manifest.ref;
      } catch {
        return false;
      }
    })();

  if (!atRef) {
    rmSync(cache, { recursive: true, force: true });
    mkdirSync(dirname(cache), { recursive: true });
    run("git", [
      "clone",
      "--filter=blob:none",
      "--no-checkout",
      "--depth",
      "1",
      manifest.repo,
      cache,
    ]);
    run("git", ["-C", cache, "sparse-checkout", "init", "--cone"]);
    // BOTH lists. `resourcePaths` is as much a subtree this import needs as `paths` is: leaving it
    // out of the sparse set makes `vendorResources` find nothing to copy, and since that function
    // clears its destination first, every committed drawable is deleted by a successful-looking
    // import. Kept here rather than worked around there, because the cache not holding a subtree
    // the manifest names is the actual defect.
    run("git", [
      "-C",
      cache,
      "sparse-checkout",
      "set",
      ...manifest.paths,
      ...(manifest.resourcePaths ?? []),
    ]);
    // The pinned commit may not be the shallow tip, so fetch it by id before checking it out.
    try {
      run("git", ["-C", cache, "fetch", "--depth", "1", "origin", manifest.ref]);
    } catch {
      // A ref already present in the shallow pack needs no fetch; checkout below is the real test.
    }
    run("git", ["-C", cache, "checkout", manifest.ref]);
  }
  return cache;
}

/** The quarantined sample file names, mapped to their stated reasons. */
export function quarantined(path = QUARANTINE) {
  if (!existsSync(path)) return new Map();
  const parsed = JSON.parse(readFileSync(path, "utf8"));
  return new Map(
    (parsed.samples ?? []).map((entry) => [entry.file, entry.reason ?? "(no reason given)"]),
  );
}

/**
 * Copy every `.kt` file under the manifest's paths into [out], skipping quarantined ones.
 *
 * RECURSIVE, and that is not incidental. The Wear samples keep their shared glyphs in a
 * `samples/icons/` subpackage, and a flat copy silently left it behind — 24 of the first compile's
 * 58 errors were `Unresolved reference 'icons'` and the `VolumeUpIcon` / `WifiOnIcon` helpers it
 * holds. A sample tree is a package, not a directory of files, so the whole package travels.
 *
 * Quarantine matches on the file's name, not its path, because that is the unit a reader names in
 * `samples/quarantine.json` and sample file names are unique within a corpus.
 */
export function vendor(cache, manifest, out, skip = new Map()) {
  rmSync(out, { recursive: true, force: true });
  mkdirSync(out, { recursive: true });
  const copied = [];
  const skipped = [];
  const walk = (from, relative) => {
    for (const entry of readdirSync(from, { withFileTypes: true })) {
      const source = join(from, entry.name);
      const target = relative ? `${relative}/${entry.name}` : entry.name;
      if (entry.isDirectory()) {
        walk(source, target);
        continue;
      }
      if (!entry.isFile() || !entry.name.endsWith(".kt")) continue;
      if (skip.has(entry.name)) {
        skipped.push(entry.name);
        continue;
      }
      mkdirSync(dirname(join(out, target)), { recursive: true });
      cpSync(source, join(out, target));
      copied.push(target);
    }
  };
  for (const path of manifest.paths) walk(join(cache, path), "");
  return { copied: copied.sort(), skipped: skipped.sort() };
}

/**
 * Copy the manifest's `resourcePaths` verbatim into [out] — the Android resource directory the
 * samples resolve `R.drawable.*` against.
 *
 * Only meaningful for an ANDROID samples module. The phone catalog renders through Compose
 * Multiplatform desktop, where there is no `R` class to generate whatever is vendored, which is why
 * its carousel samples are quarantined rather than fixed by copying these. Here the module is
 * Android, so the drawables resolve and the samples that draw artwork keep working.
 *
 * Returns the number of files copied; absent `resourcePaths` is a no-op, so the phone repo's
 * manifest shape stays valid against this same script.
 */
export function vendorResources(cache, manifest, out) {
  const paths = manifest.resourcePaths ?? [];
  if (paths.length === 0) return 0;
  // Before deleting anything. The destination is CLEARED below so a resource dropped upstream
  // disappears here too, which makes a missing source catastrophic rather than merely unhelpful:
  // skipping it quietly wipes the committed drawables and reports success. A manifest naming a
  // subtree the cache does not hold is a broken fetch, and the import has to stop.
  const missing = paths.filter((path) => !existsSync(join(cache, path)));
  if (missing.length > 0) {
    throw new Error(
      `the upstream checkout has no ${missing.join(", ")}. The manifest's resourcePaths must be ` +
        `in the sparse-checkout set — see fetchUpstream. Refusing to clear ${out}, which would ` +
        `delete the committed resources and report success.`,
    );
  }
  rmSync(out, { recursive: true, force: true });
  let copied = 0;
  for (const path of paths) {
    const from = join(cache, path);
    cpSync(from, out, { recursive: true });
    const count = (dir) =>
      readdirSync(dir, { withFileTypes: true }).reduce(
        (n, e) => n + (e.isDirectory() ? count(join(dir, e.name)) : 1),
        0,
      );
    copied += count(from);
  }
  return copied;
}

/**
 * Apply every patch in `samples/patches/`, in name order.
 *
 * A patch that does not apply is an ERROR and not a warning: it means upstream changed under a fix
 * whose reason may or may not still hold, and the one thing that must not happen is the fix quietly
 * disappearing from the vendored tree while the patch file sits there looking authoritative.
 *
 * `--unsafe-paths` because [out] is regularly OUTSIDE the working tree: `--check` vendors into a
 * `mkdtemp` to diff against the committed copy, and the documented way to cut a patch is
 * `--out /tmp/...`. Without it `git apply --directory` rejects every absolute destination as an
 * `invalid path`, which arrives here as "upstream has moved under this patch" — the one message
 * that is certainly wrong, because the patch was never compared against anything. Harmless in the
 * default case: writing outside the tree is the whole job of an importer whose output directory is
 * a parameter.
 */
export function applyPatches(out, dir = PATCH_DIR) {
  if (!existsSync(dir)) return [];
  const patches = readdirSync(dir)
    .filter((name) => name.endsWith(".patch"))
    .sort();
  for (const patch of patches) {
    try {
      run("git", ["apply", "--unsafe-paths", "--directory", out, join(dir, patch)]);
    } catch (error) {
      const detail = error.stderr?.toString().trim() || error.message;
      throw new Error(
        `patch ${patch} does not apply to the freshly imported tree:\n${detail}\n\n` +
          `Upstream has moved under it. Re-cut the patch against ${out}, or drop it and say why in ` +
          `the commit — never leave a patch that cannot apply, because the fix it carries is then ` +
          `silently absent from the vendored sources.`,
      );
    }
  }
  return patches;
}

/** Record where the bytes came from, beside them. */
export function writeProvenance(out, manifest, result, patches) {
  const provenance = {
    $comment:
      "GENERATED by scripts/import-samples.mjs — do not edit. Records exactly which upstream " +
      "commit these vendored sources came from, so a published render can be traced back to it.",
    repo: manifest.repo,
    ref: manifest.ref,
    paths: manifest.paths,
    artifact: `${manifest.artifactCoordinates}:${manifest.artifactVersion}`,
    files: result.copied.length,
    quarantined: result.skipped,
    patches,
  };
  writeFileSync(join(out, "PROVENANCE.json"), `${JSON.stringify(provenance, null, 2)}\n`);
}

function main(argv) {
  const args = new Map();
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i].startsWith("--")) args.set(argv[i].slice(2), argv[i + 1]);
  }
  const manifest = JSON.parse(readFileSync(MANIFEST, "utf8"));
  const check = argv.includes("--check");
  const committed = args.get("out") ?? "samples-catalog/src/main/kotlin/upstream";
  const cache = args.get("cache") ?? join(tmpdir(), `androidx-samples-${basename(manifest.ref)}`);

  console.log(`Fetching ${manifest.repo} at ${manifest.ref.slice(0, 12)} …`);
  fetchUpstream(manifest, cache);

  const skip = quarantined();
  const out = check ? mkdtempSync(join(tmpdir(), "samples-import-")) : committed;
  const result = vendor(cache, manifest, out, skip);
  const resourcesOut = args.get("res") ?? "samples-catalog/src/main/res";
  const resources = check ? 0 : vendorResources(cache, manifest, resourcesOut);
  let patches;
  try {
    patches = applyPatches(out);
  } catch (error) {
    // The message already says which patch and what to do about it; a stack trace on top only
    // buries it, and this is a failure a human has to read and act on.
    console.error(`\n${error.message}`);
    if (check) rmSync(out, { recursive: true, force: true });
    process.exit(1);
  }
  writeProvenance(out, manifest, result, patches);

  console.log(
    `  ${result.copied.length} file(s) vendored, ${result.skipped.length} quarantined, ` +
      `${patches.length} patch(es) applied` +
      (resources > 0 ? `, ${resources} resource(s) copied.` : "."),
  );
  for (const name of result.skipped) console.log(`    quarantined: ${name} — ${skip.get(name)}`);

  if (check) {
    const diff = (() => {
      try {
        run("diff", ["-ru", committed, out]);
        return "";
      } catch (error) {
        return error.stdout?.toString() ?? "differs";
      }
    })();
    rmSync(out, { recursive: true, force: true });
    if (diff) {
      console.error("\nThe committed sources differ from a fresh import at the pinned ref:\n");
      console.error(diff.slice(0, 4000));
      console.error("Re-run `node scripts/import-samples.mjs` and commit the result.");
      process.exit(1);
    }
    console.log("The vendored sources match a fresh import at the pinned ref.");
  }
}

if (process.argv[1] && process.argv[1].endsWith("import-samples.mjs")) main(process.argv.slice(2));
