#!/usr/bin/env node
/**
 * Decide which vendored samples are DEVICE-BOUND, by measuring a render.
 *
 * ## Why this is measured and not inferred
 *
 * A sample is device-bound when it fills the watch screen — a `ScreenScaffold`, a
 * `TransformingLazyColumn`, a dialog, a pager — and wrap-content when it is one component that
 * crops to its own bounds. The two want opposite treatment: the first belongs on a round device
 * with a black face, because that is the surface it draws on and a square canvas shows corners a
 * watch clips; the second must stay device-less, or a `FilledIconButton` becomes a 454x454 watch
 * face with a button adrift in it (the plugin's own `retargetWearStickers` KDoc records that
 * regression).
 *
 * A static rule over the source was tried first and is not good enough. Matching layout signals in
 * the body — `fillMaxSize`, `ScreenScaffold`, `Pager`, `Picker` — reproduced all 86 device-bound
 * samples with ZERO false negatives but five false positives, and each is a different kind of
 * wrong: `PickerGroupSample` and `PickerScrollToOption` use a `Picker` that does not fill the
 * screen, and `ButtonWithIconAndLabelAndPlaceholders` carries `fillMaxSize` on an element INSIDE
 * the button. Separating those needs to know which call is the root, which means parsing Kotlin,
 * for a question the renderer answers exactly.
 *
 * So this reads the answer off a render instead. The Wear retarget measures every device-less
 * preview against the 227dp watch screen with BOTH AXES WRAPPED, so a PNG that came out the full
 * sandbox in both directions is a composable that filled it — the definition, not a proxy for it.
 *
 * ## What `--check` can and cannot catch
 *
 * A first cut keyed on one size — the 227dp sandbox square — and was wrong, which its own `--check`
 * caught immediately: once a sample is ON a device its render is the DEVICE's square instead, so
 * every listed sample read as "no longer device-bound" on the next run. So the signal is *either*
 * square: the wrap sandbox (a device-less preview that filled it) or the pinned round device (one
 * this list already put there). Both mean the same thing, and the classification is then stable
 * across the change it causes.
 *
 * The asymmetry that leaves is worth stating rather than hiding. A NEWLY imported sample renders
 * device-less, so if it fills the screen `--check` sees the sandbox square, finds it unlisted and
 * fails — which is the case worth catching, and the reason this is checked at all. The reverse is
 * invisible: a listed sample that upstream rewrites to no longer fill the screen keeps rendering at
 * the device's square, because the device is pinned. Catching that would mean re-rendering with the
 * devices stripped, and it is not worth a second render of the whole module for a change that
 * arrives with a reviewable import diff anyway.
 *
 *     ./gradlew :samples-catalog:composePreviewRender
 *     node scripts/samples-device-bound.mjs            # rewrite samples/device-bound.json
 *     node scripts/samples-device-bound.mjs --check    # fail if the render disagrees
 */

import { existsSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const RENDERS = "samples-catalog/build/compose-previews/renders";
const GENERATED = "samples-catalog/src/main/kotlin/generated/SamplePreviews.kt";
const OUT = "samples/device-bound.json";

/** The PNG's pixel dimensions, straight out of the IHDR chunk. */
export function pngSize(path) {
  const head = readFileSync(path).subarray(0, 24);
  return { width: head.readUInt32BE(16), height: head.readUInt32BE(20) };
}

/**
 * The sample names whose render is a full screen — the wrap sandbox, or the device this list pins.
 *
 * [squares] are those two edges in px: 227dp at the Wear retarget's 2.0x density, and 225dp at the
 * round device's 320dpi. Passed rather than hardcoded so a density or device change is a caller's
 * problem and not a silent reclassification of every sample at once.
 */
export function measure(dir = RENDERS, generated = GENERATED, squares = [454, 450]) {
  // A generated wrapper is named `<Sample>Preview`; an upstream-annotated sample renders under its
  // own name. Map back so the list names SAMPLES either way — the wrapper name is an artefact of
  // how this repo reaches the sample, and would churn if the generator's naming ever changed.
  const wrappers = new Map();
  if (existsSync(generated)) {
    for (const m of readFileSync(generated, "utf8").matchAll(/fun (\w+)Preview\(\) =/g)) {
      wrappers.set(`${m[1]}Preview`, m[1]);
    }
  }
  const bound = [];
  for (const name of readdirSync(dir)) {
    if (!name.endsWith(".png")) continue;
    // A device-bound render carries the device spec in its name (`…_width_225dp_…`); strip it, so
    // the list names samples the same way whether or not one is pinned yet.
    const render = name.replace(/-[0-9a-f]+\.png$/, "").replace(/_width_\d+dp.*$/, "");
    // The theme specimen is the renderer's own sheet, not a sample.
    if (render.startsWith("wearthemecatalog__") || render.startsWith("themecatalog__")) continue;
    const { width, height } = pngSize(join(dir, name));
    if (width === height && squares.includes(width)) bound.push(wrappers.get(render) ?? render);
  }
  return [...new Set(bound)].sort();
}

function main(argv) {
  if (!existsSync(RENDERS)) {
    console.error(
      `No renders under ${RENDERS}. Run \`./gradlew :samples-catalog:composePreviewRender\` first — ` +
        `this script reads the answer off a render rather than guessing it from the sources.`,
    );
    process.exit(1);
  }
  const samples = measure();
  const json = `${JSON.stringify(
    {
      $comment:
        "GENERATED by scripts/samples-device-bound.mjs from a render — do not edit. The samples " +
        "that FILL the watch screen, and therefore render on a round device with a black face " +
        "rather than cropping to their own bounds. Measured rather than inferred from the " +
        "sources; the script's header says why a static rule is not good enough.",
      samples,
    },
    null,
    2,
  )}\n`;

  if (argv.includes("--check")) {
    const committed = existsSync(OUT) ? readFileSync(OUT, "utf8") : "";
    if (committed !== json) {
      const was = committed ? new Set(JSON.parse(committed).samples ?? []) : new Set();
      const now = new Set(samples);
      const added = [...now].filter((s) => !was.has(s));
      const gone = [...was].filter((s) => !now.has(s));
      console.error(`${OUT} disagrees with the render.`);
      if (added.length > 0) console.error(`  now device-bound, not listed: ${added.join(", ")}`);
      if (gone.length > 0) console.error(`  listed, but no longer device-bound: ${gone.join(", ")}`);
      console.error(`Re-run \`node scripts/samples-device-bound.mjs\` and commit the result.`);
      process.exit(1);
    }
    console.log(`${OUT} is current (${samples.length} device-bound sample(s)).`);
    return;
  }

  writeFileSync(OUT, json);
  console.log(`${OUT}: ${samples.length} device-bound sample(s) measured.`);
}

if (process.argv[1] && process.argv[1].endsWith("samples-device-bound.mjs")) {
  main(process.argv.slice(2));
}
