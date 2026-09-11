#!/usr/bin/env node
/**
 * Generate a `@Preview` wrapper for every vendored Wear sample that does not carry one.
 *
 * ## Why this exists here and not in the phone repo
 *
 * The phone import needs no wrappers: **298 of the 317** `@Sampled` functions in
 * `androidx.compose.material3.samples` already carry `@Preview` upstream, so discovery finds them
 * directly. That finding is what deleted this stage from `docs/design/ANDROIDX_SAMPLES.md`'s plan.
 *
 * It does not hold on Wear. `androidx.wear.compose.material3.samples` carries a preview annotation
 * on **34 of 170** — measured, not assumed. Without wrappers this catalog publishes 34 components
 * and drops the other 136 samples on the floor, which is most of the corpus and most of the point.
 *
 * So the plan's original shape was right for one repo and wrong for the other, and the difference
 * is upstream's own habit rather than anything about the two platforms.
 *
 * ## What it generates, and what it refuses to
 *
 * One wrapper per sample, in this repo's own package and its own generated file — never inside the
 * vendored tree, which stays byte-identical to upstream:
 *
 * ```kotlin
 * @Preview @Composable fun AppCardSamplePreview() = AppCardSample()
 * ```
 *
 * A sample is wrapped only when it is a **zero-argument `@Composable`**. Anything else is skipped
 * and reported:
 *
 *  - a `@Sampled` function taking parameters is a helper a sample calls, not a call site anyone can
 *    render on its own;
 *  - a sample that already carries a preview annotation is left alone, or discovery would find the
 *    same composable twice and publish it twice;
 *  - a sample named in `samples/quarantine.json`'s `previews` list compiles but cannot RUN here, and
 *    a wrapper for it would fail the whole render job rather than just itself.
 *
 * Refusing rather than guessing matters: a wrapper that does not compile fails the whole module,
 * and a wrapper that renders something meaningless is worse than a missing card.
 *
 *     node scripts/samples-previews.mjs            # regenerate
 *     node scripts/samples-previews.mjs --check    # fail if the generated file is stale
 */

import { existsSync, readdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const VENDORED = "samples-catalog/src/main/kotlin/upstream";
const QUARANTINE = "samples/quarantine.json";
const OUT = "samples-catalog/src/main/kotlin/generated/SamplePreviews.kt";
const PACKAGE = "ee.schimke.wearm3catalog.samples";
const UPSTREAM_PACKAGE = "androidx.wear.compose.material3.samples";

/** Every `.kt` file under [dir], recursively — the vendored tree has an `icons/` subpackage. */
function kotlinSources(dir) {
  const out = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const path = join(dir, entry.name);
    if (entry.isDirectory()) out.push(...kotlinSources(path));
    else if (entry.name.endsWith(".kt")) out.push(path);
  }
  return out.sort();
}

/**
 * The sample function names `samples/quarantine.json` declares unrenderable, mapped to their
 * reasons.
 *
 * The per-FILE `samples` list is the importer's unit and is not read here: a file that does not
 * compile never reaches this tree at all. This is the other failure mode — a sample that compiles
 * and then throws at composition, where taking out its whole file would drop the eight siblings
 * that render perfectly well.
 */
export function quarantinedPreviews(path = QUARANTINE) {
  if (!existsSync(path)) return new Map();
  const parsed = JSON.parse(readFileSync(path, "utf8"));
  return new Map(
    (parsed.previews ?? []).map((entry) => [entry.sample, entry.reason ?? "(no reason given)"]),
  );
}

/**
 * Classify every `@Sampled` function in the vendored tree.
 *
 * @returns {{wrap: string[], hasPreview: string[], takesArguments: string[], quarantined: string[]}}
 */
export function classifySamples(dir = VENDORED, skip = quarantinedPreviews()) {
  const wrap = [];
  const hasPreview = [];
  const takesArguments = [];
  const quarantined = [];
  for (const file of kotlinSources(dir)) {
    const text = readFileSync(file, "utf8");
    // Annotations immediately preceding a `fun`, then its parameter list. The compiler's own rule,
    // so a commented-out sample cannot slip in and a KDoc `@sample` cannot be mistaken for one.
    for (const match of text.matchAll(/((?:@\w+(?:\([^)]*\))?\s*)+)fun\s+(\w+)\s*\(([^)]*)\)/g)) {
      const [, annotations, fn, parameters] = match;
      if (!annotations.includes("@Sampled")) continue;
      if (!annotations.includes("@Composable")) continue;
      if (annotations.includes("@Preview")) {
        hasPreview.push(fn);
        continue;
      }
      if (skip.has(fn)) {
        quarantined.push(fn);
        continue;
      }
      if (parameters.trim() !== "") {
        takesArguments.push(fn);
        continue;
      }
      wrap.push(fn);
    }
  }
  return {
    wrap: [...new Set(wrap)].sort(),
    hasPreview: [...new Set(hasPreview)].sort(),
    takesArguments: [...new Set(takesArguments)].sort(),
    quarantined: [...new Set(quarantined)].sort(),
  };
}

/** The generated Kotlin source for [names]. */
export function renderPreviews(names) {
  const header = `// GENERATED by scripts/samples-previews.mjs — do not edit.
//
// A \`@Preview\` wrapper for every vendored Wear sample that does not carry one upstream. Only 34 of
// the 170 \`@Sampled\` functions in \`androidx.wear.compose.material3.samples\` are annotated, so
// without these the catalog would publish a fifth of the corpus. The phone repo needs no equivalent:
// 298 of its 317 samples are already annotated.
//
// The wrappers live HERE, in this repo's own package, and never inside \`upstream/\` — that tree is
// upstream's bytes and is re-fetched byte-identically on every import.

package ${PACKAGE}

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
`;
  const body = names
    .map(
      (fn) =>
        `\n@Preview\n@Composable\nfun ${fn}Preview() = ${UPSTREAM_PACKAGE}.${fn}()\n`,
    )
    .join("");
  return header + body;
}

function main(argv) {
  const skip = quarantinedPreviews();
  const { wrap, hasPreview, takesArguments, quarantined } = classifySamples(VENDORED, skip);
  const source = renderPreviews(wrap);

  if (argv.includes("--check")) {
    if (readFileSync(OUT, "utf8") !== source) {
      console.error(`${OUT} is stale: regenerate it with \`node scripts/samples-previews.mjs\`.`);
      process.exit(1);
    }
    console.log(`${OUT} is current (${wrap.length} wrapper(s)).`);
    return;
  }

  writeFileSync(OUT, source);
  console.log(`${OUT}: ${wrap.length} wrapper(s) generated.`);
  console.log(`  ${hasPreview.length} sample(s) already carry @Preview upstream and are left alone.`);
  for (const fn of quarantined) {
    console.log(`  quarantined: ${fn} — ${skip.get(fn)}`);
  }
  if (takesArguments.length > 0) {
    console.log(
      `  ${takesArguments.length} @Sampled function(s) take arguments and are not renderable on ` +
        `their own: ${takesArguments.slice(0, 6).join(", ")}${takesArguments.length > 6 ? " …" : ""}`,
    );
  }
}

if (process.argv[1] && process.argv[1].endsWith("samples-previews.mjs")) main(process.argv.slice(2));
