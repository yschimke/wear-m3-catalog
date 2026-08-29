// The pure half of `kit-cells.mjs`: what counts as a claimed cell, and what the record says about
// a set neither sheet fully draws. Both are worth pinning rather than discovering from a diff of a
// 30-set record after the fact.

import assert from "node:assert/strict";
import test from "node:test";

import { cellCoverage, claimsBySet, nodeOf, publishedCells } from "./kit-cells.mjs";

const index = {
  sets: {
    "1:1": {
      name: "Card",
      variants: [
        { id: "1:10", name: "Layout type=Title Card 1, Content type=Text" },
        { id: "1:11", name: "Layout type=Title Card 1, Content type=Image" },
        { id: "1:12", name: "Layout type=Title Card 3, Content type=Text" },
      ],
    },
    "2:1": { name: "Stepper", variants: [{ id: "2:10", name: "Disabled=No" }] },
  },
};

const sets = {
  fileKey: "FILE",
  sets: [
    { page: "Cards", set: "Card", node: "1:1", components: ["TitleCard"] },
    { page: "Steppers", set: "Stepper", node: "2:1", components: ["Stepper"] },
  ],
};

test("reads the node id out of a design-parity reference", () => {
  assert.equal(nodeOf("figma:FILE/38437:5746"), "38437:5746");
  assert.equal(nodeOf("38437:5746"), "38437:5746");
});

test("collects claims from all three shapes `ref` takes", () => {
  const claims = claimsBySet({
    components: [
      { refSet: "figma:FILE/1:1", ref: "figma:FILE/1:10" },
      { refSet: "figma:FILE/1:1", ref: ["figma:FILE/1:11"] },
      { refSet: "figma:FILE/2:1", ref: [{ ref: "figma:FILE/2:10", state: "disabled" }] },
    ],
  });
  assert.deepEqual([...claims.get("1:1")].sort(), ["1:10", "1:11"]);
  assert.deepEqual([...claims.get("2:1")], ["2:10"]);
});

test("a component with no referenceSet claims nothing", () => {
  // Door 2 of AGENTS.md — a `noReference` component is not a cell of anything, and counting its
  // captures against a set would inflate every sheet that has one.
  const claims = claimsBySet({ components: [{ ref: "figma:FILE/1:10" }] });
  assert.equal(claims.size, 0);
});

test("names the published cells of a set, and nothing for a set the walk missed", () => {
  assert.deepEqual([...publishedCells(index, "2:1").values()], ["Disabled=No"]);
  assert.equal(publishedCells(index, "9:9"), null);
});

test("records what each sheet draws and names the cells it does not", () => {
  const record = cellCoverage({
    index,
    sets,
    maps: {
      catalog: {
        components: [{ refSet: "figma:FILE/1:1", ref: ["figma:FILE/1:10", "figma:FILE/1:11"] }],
      },
      "remote-catalog": {
        components: [{ refSet: "figma:FILE/1:1", ref: "figma:FILE/1:10" }],
      },
    },
  });
  assert.equal(record.fileKey, "FILE");
  assert.equal(record.sets.length, 1, "Stepper is drawn by neither sheet, so it is not a row");
  const card = record.sets[0];
  assert.equal(card.published, 3);
  assert.deepEqual(card.sheets.catalog, {
    drawn: 2,
    uncovered: ["Layout type=Title Card 3, Content type=Text"],
  });
  assert.deepEqual(card.sheets["remote-catalog"].drawn, 1);
  assert.equal(card.sheets["remote-catalog"].uncovered.length, 2);
});

test("a claim on a node the set does not publish is recorded as stray, not as drawn", () => {
  // The shape of a reference that has rotted: the node is still named, the kit no longer publishes
  // it under this set. Counting it as drawn would hold the number up while the comparison is gone.
  const record = cellCoverage({
    index,
    sets,
    maps: {
      catalog: { components: [{ refSet: "figma:FILE/1:1", ref: ["1:10", "9:99"] }] },
    },
  });
  assert.equal(record.sets[0].sheets.catalog.drawn, 1);
  assert.deepEqual(record.sets[0].sheets.catalog.stray, ["9:99"]);
});

test("a sheet that names none of a set gets no entry at all", () => {
  const record = cellCoverage({
    index,
    sets,
    maps: {
      catalog: { components: [{ refSet: "figma:FILE/1:1", ref: ["1:10"] }] },
      "remote-catalog": { components: [] },
    },
  });
  assert.deepEqual(Object.keys(record.sets[0].sheets), ["catalog"]);
});
