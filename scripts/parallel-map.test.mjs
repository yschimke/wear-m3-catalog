// The pure half of `parallel-map.mjs`: what it reads out of a preview manifest, and which of the
// four ways a pairing breaks it calls a failure. Each of those failures is silent in production —
// a dropped compare row looks like an unwritten component — so each is worth pinning here rather
// than trusting the gate to have meant it.

import assert from "node:assert/strict";
import test from "node:test";

import { findings, readSheet, unpairedCells } from "./parallel-map.mjs";

/** A manifest of one component plus its `@OverrideVariant` captures, in discovery's own shape. */
const manifest = (components) => ({
  previews: components.flatMap(({ id, cells = [], ...catalog }) => [
    { id: `pkg.Kt.${id.replace(/\W/g, "")}`, catalog: { componentId: id, ...catalog } },
    ...cells.map((cell) => ({
      id: `pkg.Kt.${id.replace(/\W/g, "")}_VARIANT_${cell}`,
      catalog: { componentId: id },
    })),
  ]),
});

const wear = readSheet(
  manifest([
    { id: "Button/Filled", reference: "figma:F/1", referenceSet: "figma:F/0", cells: ["disabled"] },
    { id: "TitleCard", reference: "figma:F/2", cells: ["background-image"] },
    { id: "Slider", reference: "figma:F/3" },
  ]),
);

const names = { fromName: "remote-catalog", toName: "catalog" };

test("reads components and their cells apart", () => {
  const sheet = readSheet(
    manifest([{ id: "Button/Filled", reference: "figma:F/1", cells: ["disabled", "icon"] }]),
  );
  assert.deepEqual([...sheet.components.keys()], ["Button/Filled"]);
  assert.deepEqual([...sheet.cells.get("Button/Filled")].sort(), ["disabled", "icon"]);
});

test("a matching pair is no finding", () => {
  const remote = readSheet(
    manifest([
      {
        id: "Button/Filled",
        parallel: "Button/Filled",
        reference: "figma:F/1",
        referenceSet: "figma:F/0",
      },
    ]),
  );
  assert.deepEqual(findings({ from: remote, to: wear, ...names }), []);
});

test("a parallel the other sheet does not publish fails", () => {
  const remote = readSheet(manifest([{ id: "Progress/Circular", parallel: "Progress/Circular" }]));
  const [only] = findings({ from: remote, to: wear, ...names });
  assert.match(only, /catalog does not publish/);
});

test("a pair naming different kit nodes fails, and so does a different set", () => {
  const remote = readSheet(
    manifest([
      {
        id: "Button/Filled",
        parallel: "Button/Filled",
        reference: "figma:F/9",
        referenceSet: "figma:F/9",
      },
    ]),
  );
  const broken = findings({ from: remote, to: wear, ...names });
  assert.equal(broken.length, 2);
  assert.match(broken[0], /different kit nodes/);
  assert.match(broken[1], /different kit SETS/);
});

test("an id both sheets publish with no parallel behind it fails", () => {
  const remote = readSheet(manifest([{ id: "Slider", reference: "figma:F/3" }]));
  const [only] = findings({ from: remote, to: wear, ...names });
  assert.match(only, /neither says so/);
});

test("a component only one sheet has is not a pairing at all", () => {
  const remote = readSheet(manifest([{ id: "Theme/Typography" }]));
  assert.deepEqual(findings({ from: remote, to: wear, ...names }), []);
});

test("one-sided cells are reported, not failed", () => {
  const remote = readSheet(
    manifest([{ id: "TitleCard", parallel: "TitleCard", reference: "figma:F/2" }]),
  );
  assert.deepEqual(findings({ from: remote, to: wear, ...names }), []);
  assert.deepEqual(unpairedCells({ from: remote, to: wear, ...names }), [
    { id: "TitleCard", target: "TitleCard", "remote-catalog": [], catalog: ["background-image"] },
  ]);
});
