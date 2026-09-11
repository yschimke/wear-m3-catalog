import { test } from "node:test";
import assert from "node:assert/strict";
import { mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

import { measure } from "./samples-device-bound.mjs";

/** A PNG whose IHDR says [width]x[height]. Only the first 24 bytes are ever read. */
function png(dir, name, width, height) {
  const head = Buffer.alloc(24);
  Buffer.from("\x89PNG\r\n\x1a\n").copy(head, 0);
  head.writeUInt32BE(width, 16);
  head.writeUInt32BE(height, 20);
  writeFileSync(join(dir, name), head);
}

function fixture(files, generated = "") {
  const dir = mkdtempSync(join(tmpdir(), "device-bound-"));
  for (const [name, [w, h]] of Object.entries(files)) png(dir, name, w, h);
  const gen = join(dir, "SamplePreviews.kt");
  writeFileSync(gen, generated);
  return { dir, gen };
}

test("a render filling the wrap sandbox is device-bound", () => {
  const { dir, gen } = fixture({ "ScaffoldSample-abc123.png": [454, 454] });
  try {
    assert.deepEqual(measure(dir, gen), ["ScaffoldSample"]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a render cropped to its own bounds is not", () => {
  // The regression this exists to prevent: pinning a one-component sticker to a device makes it a
  // watch canvas with the button adrift in it.
  const { dir, gen } = fixture({
    "FilledIconButtonSamplePreview-abc123.png": [104, 104],
    "CardSamplePreview-def456.png": [454, 128],
  });
  try {
    assert.deepEqual(measure(dir, gen), []);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a sample already pinned to the round device stays device-bound", () => {
  // THE IDEMPOTENCE THIS TURNS ON. Keying on the sandbox square alone was the first cut and was
  // wrong: once a sample is on a device it renders at the DEVICE's square with the spec in its
  // name, so every listed sample read as "no longer device-bound" on the very next run.
  const { dir, gen } = fixture({
    "ScaffoldSample_width_225dp_height_225dp_dpi_320_isRound_true-abc123.png": [450, 450],
  });
  try {
    assert.deepEqual(measure(dir, gen), ["ScaffoldSample"]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a device suffix truncated by the renderer still resolves to the sample", () => {
  // Long names get cut short on disk — `…SamplePreview_width_225dp` with the rest elided — so the
  // suffix strip cannot require the segments after `225dp`.
  const gen = "fun OneHandedGestureTransformingLazyColumnScrollToNextItemSamplePreview() =";
  const { dir, gen: genPath } = fixture(
    { "OneHandedGestureTransformingLazyColumnScrollToNextItemSamplePreview_width_225dp-6dd8.png": [450, 450] },
    gen,
  );
  try {
    assert.deepEqual(measure(dir, genPath), [
      "OneHandedGestureTransformingLazyColumnScrollToNextItemSample",
    ]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a generated wrapper is reported under the SAMPLE's name, not the wrapper's", () => {
  // The wrapper name is an artefact of how this repo reaches the sample; the list must not churn if
  // the generator's naming ever changes.
  const { dir, gen } = fixture(
    { "ButtonSamplePreview-abc123.png": [454, 454] },
    "fun ButtonSamplePreview() = androidx.wear.compose.material3.samples.ButtonSample()",
  );
  try {
    assert.deepEqual(measure(dir, gen), ["ButtonSample"]);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("the renderer's own theme specimen is not a sample", () => {
  const { dir, gen } = fixture({ "wearthemecatalog__Material-abc123.png": [450, 450] });
  try {
    assert.deepEqual(measure(dir, gen), []);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

test("a square that is neither the sandbox nor the device is not device-bound", () => {
  const { dir, gen } = fixture({ "SomeSample-abc123.png": [300, 300] });
  try {
    assert.deepEqual(measure(dir, gen), []);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});
