// The pure half of `import-figma-pages.mjs`: which pages an import covers, and what each one is
// called on the way out. Both answers are URLs — `/{system}/pages/{id}` — so they are worth pinning
// rather than discovering from a diff after the fact.

import assert from "node:assert/strict";
import test from "node:test";

import {
  collectNodes,
  indexDesignMap,
  resolvePages,
  slugForPage,
} from "./import-figma-pages.mjs";

const page = (nodeId, name) => ({ nodeId, name });

test("slugs a page name into a route-safe id", () => {
  assert.equal(slugForPage("Shape", "58548:7093"), "shape");
  assert.equal(slugForPage("Date & time pickers", "55141:14175"), "date-time-pickers");
  assert.equal(slugForPage("Table of contents", "55879:3580"), "table-of-contents");
});

test("falls back to the node id when a name slugs to nothing usable", () => {
  assert.equal(slugForPage("—", "55141:14176"), "55141-14176");
  assert.equal(slugForPage("", "55141:14176"), "55141-14176");
  // `.svg` is the export route, and `.`/`..` are path segments — a page id may be none of them.
  assert.equal(slugForPage("shape.svg", "58548:7093"), "shape-svg");
  assert.equal(slugForPage("..", "1:2"), "1-2");
});

test("discovers every page in the file, in document order", () => {
  assert.deepEqual(
    resolvePages({
      discovered: [page("11:1833", "Getting started"), page("55141:14175", "Date & time pickers")],
    }),
    [
      { id: "getting-started", nodeId: "11:1833", name: "Getting started" },
      { id: "date-time-pickers", nodeId: "55141:14175", name: "Date & time pickers" },
    ],
  );
});

test("a pin fixes the id of its node, wherever discovery finds it", () => {
  const resolved = resolvePages({
    pins: [{ id: "shape", nodeId: "58548:7093", name: "Shape" }],
    discovered: [page("11:1833", "Getting started"), page("58548:7093", "Shape v2")],
  });
  // The published URL stays `shape` even though the designer renamed the tab; the *name* follows
  // the file, because that is what the index reads.
  assert.deepEqual(resolved[1], {
    id: "shape",
    nodeId: "58548:7093",
    name: "Shape",
    // Pinned: a human named this page, so its failure is fatal where a discovered page's is a skip.
    pinned: true,
  });
});

test("a pin whose node the file does not carry is still imported", () => {
  assert.deepEqual(resolvePages({ pins: [{ id: "shape", nodeId: "58548-7093" }] }), [
    { id: "shape", nodeId: "58548:7093", name: "shape", pinned: true },
  ]);
});

test("two pages sharing a name both survive, the first keeping the bare slug", () => {
  assert.deepEqual(
    resolvePages({ discovered: [page("1:1", "Buttons"), page("2:2", "Buttons")] }).map((p) => p.id),
    ["buttons", "buttons-2"],
  );
});

test("a discovered page never steals a pinned id", () => {
  assert.deepEqual(
    resolvePages({
      pins: [{ id: "shape", nodeId: "9:9" }],
      discovered: [page("1:1", "Shape"), page("9:9", "Shape set")],
    }).map((p) => p.id),
    ["shape-2", "shape"],
  );
});

test("excludes by node id or by name, case-insensitively", () => {
  const discovered = [page("55594:2480", "Examples"), page("58548:7093", "Shape")];
  assert.deepEqual(
    resolvePages({ discovered, exclude: ["55594:2480"] }).map((p) => p.id),
    ["shape"],
  );
  assert.deepEqual(
    resolvePages({ discovered, exclude: ["examples"] }).map((p) => p.id),
    ["shape"],
  );
  // A pin is not a licence to import: excluding its node drops it too.
  assert.deepEqual(
    resolvePages({
      pins: [{ id: "shape", nodeId: "58548:7093", name: "Shape" }],
      discovered,
      exclude: ["Shape"],
    }).map((p) => p.id),
    ["examples"],
  );
});

// The node walk. Shaped after the kit's real `Switch` sheet, which is what showed the bug: a
// component set of variants, each variant carrying an `Icon` instance and the focused ones a
// `Focus indicator`, none of which a `design-map.json` reference can name.
const set = (id, name, children) => ({ id, name, type: "COMPONENT_SET", children });
const variant = (id, name, children = []) => ({ id, name, type: "COMPONENT", children });
const instance = (id, name, children = []) => ({ id, name, type: "INSTANCE", children });
const frame = (children) => ({ id: "0:1", name: "Switch", type: "CANVAS", children });

test("walks a component set's variants but never a variant's insides", () => {
  const page = frame([
    set("1:1", "Switch", [
      variant("1:2", "Selected=True, State=Enabled, Icon=True", [
        instance("1:3", "Icon", [{ id: "1:4", name: "vector", type: "VECTOR" }]),
      ]),
      variant("1:5", "Selected=True, State=Focused, Icon=False", [
        instance("1:6", "Focus indicator"),
      ]),
    ]),
    instance("1:7", "Header"),
  ]);
  assert.deepEqual(
    collectNodes(page).map((n) => [n.nodeId, n.name, n.depth]),
    [
      ["1:1", "Switch", 1],
      ["1:2", "Selected=True, State=Enabled, Icon=True", 2],
      ["1:5", "Selected=True, State=Focused, Icon=False", 2],
      ["1:7", "Header", 1],
    ],
  );
  // Each node carries its own type, which is how the consumer tells the container from the
  // components inside it without inferring containment from nesting depth.
  assert.deepEqual(
    collectNodes(page).map((n) => n.type),
    ["COMPONENT_SET", "COMPONENT", "COMPONENT", "INSTANCE"],
  );
});

test("finds a set however deeply the sheet nests it, and dashes its ids", () => {
  const row = { id: "2:0", name: "row", type: "FRAME" };
  const page = frame([{ ...row, children: [set("2-1", "Switch", [variant("2-2", "A")])] }]);
  assert.deepEqual(
    collectNodes(page).map((n) => n.nodeId),
    ["2:1", "2:2"],
  );
});

test("refuses a pin the server would refuse", () => {
  assert.throws(() => resolvePages({ pins: [{ id: "shape.svg", nodeId: "1:1" }] }), /refuse/);
  assert.throws(() => resolvePages({ pins: [{ id: "..", nodeId: "1:1" }] }), /refuse/);
  assert.throws(() => resolvePages({ pins: [{ id: "a/b", nodeId: "1:1" }] }), /refuse/);
  assert.throws(() => resolvePages({ pins: [{ id: "shape" }] }), /nodeId/);
  assert.throws(
    () => resolvePages({ pins: [{ id: "a", nodeId: "1:1" }, { id: "b", nodeId: "1-1" }] }),
    /twice/,
  );
});

// The design-map join. Every failure here is quiet — a ref paired with the wrong preview still
// renders something, so a page of 35 identical silhouettes looks exactly like a page that works.

const ref = (id) => `figma:AbCdEf/${id}`;
const preview = (name) => `com.example.ShapesKt.${name}`;

test("pairs each ref with the preview of the same variant, whatever slot it was tagged with", () => {
  // `size` is the slot the kit resolver picks for a shape axis; reading only `state` used to send
  // every one of these to the base preview.
  const byRef = indexDesignMap({
    components: [
      {
        code: "Shapes.kt#MaterialShapesSticker",
        ref: [
          { ref: ref("1:1") },
          { ref: ref("1:2"), size: "square" },
          { ref: ref("1:3"), size: "very sunny" },
        ],
        previewId: [
          { previewId: preview("Sticker_Light") },
          { previewId: preview("Sticker_Light_VARIANT_square"), size: "square" },
          { previewId: preview("Sticker_Light_VARIANT_very-sunny"), size: "very sunny" },
        ],
      },
    ],
  });
  assert.equal(byRef.get(ref("1:1")).previewId, preview("Sticker_Light"));
  assert.equal(byRef.get(ref("1:2")).previewId, preview("Sticker_Light_VARIANT_square"));
  assert.equal(byRef.get(ref("1:3")).previewId, preview("Sticker_Light_VARIANT_very-sunny"));
});

test("still pairs a state-tagged entry, and falls back to the base preview when unmatched", () => {
  const byRef = indexDesignMap({
    components: [
      {
        code: "Buttons.kt#FilledButton",
        ref: [
          { ref: ref("2:1") },
          { ref: ref("2:2"), state: "disabled" },
          { ref: ref("2:3"), state: "hovered" },
        ],
        previewId: [
          { previewId: preview("Button_Light") },
          { previewId: preview("Button_Light_VARIANT_disabled"), state: "disabled" },
        ],
      },
    ],
  });
  assert.equal(byRef.get(ref("2:2")).previewId, preview("Button_Light_VARIANT_disabled"));
  // No `hovered` render: the base is the honest answer, not another variant's picture.
  assert.equal(byRef.get(ref("2:3")).previewId, preview("Button_Light"));
});

test("reads the scalar form, and lets the first component to name a node win", () => {
  const byRef = indexDesignMap({
    components: [
      { code: "A.kt#First", ref: ref("3:1"), previewId: preview("First_Light") },
      { code: "B.kt#Second", ref: ref("3:1"), previewId: preview("Second_Light") },
    ],
  });
  assert.equal(byRef.get(ref("3:1")).code, "A.kt#First");
  assert.equal(byRef.size, 1);
});
