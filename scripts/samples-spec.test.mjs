import { test } from "node:test";
import assert from "node:assert/strict";
import { existsSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

import {
  API_TO_KIT_COMPONENT,
  buildGroups,
  buildSpec,
  kitComponentFor,
  kitCellIndex,
  renderableSamples,
  sampleSources,
} from "./samples-spec.mjs";

/** A throwaway kit source tree: `<root>/<name>` for each entry. */
function kitSources(files) {
  const root = mkdtempSync(join(tmpdir(), "samples-spec-test-"));
  for (const [name, text] of Object.entries(files)) {
    const path = join(root, name);
    mkdirSync(join(path, ".."), { recursive: true });
    writeFileSync(path, text);
  }
  return root;
}

const COMPONENT = (id, extra = "") =>
  `@CatalogComponent(\n  id = "${id}",\n  caption = "A cell."${extra}\n)\n@Preview\n@Composable\nfun P${id.replace(/\\W/g, "")}() {}\n`;

test("a family's FIRST declared cell is the one a sample links to", () => {
  const root = kitSources({
    "Buttons.kt": COMPONENT("Button/Filled") + COMPONENT("Button/Outlined"),
  });
  try {
    assert.equal(kitCellIndex([root]).get("Button"), "Button/Filled");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("declaration order is file order, and files are walked in sorted path order", () => {
  // Determinism is the whole claim: "first" has to be a convention someone can rely on, not
  // whatever order the filesystem happened to hand back.
  const root = kitSources({
    "Zzz.kt": COMPONENT("Button/Last"),
    "Aaa.kt": COMPONENT("Button/First"),
  });
  try {
    assert.equal(kitCellIndex([root]).get("Button"), "Button/First");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("an id with no slash is its own family and its own first cell", () => {
  const root = kitSources({ "Dialogs.kt": COMPONENT("AlertDialog") });
  try {
    assert.equal(kitCellIndex([root]).get("AlertDialog"), "AlertDialog");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("a multi-line annotation with other fields still yields its id", () => {
  // The real ones carry `reference`, `referenceSet`, `caption` and more across several lines.
  const root = kitSources({
    "Swipe.kt": COMPONENT("SwipeToReveal/Card", `,\n  reference = "figma:abc/1:2"`),
  });
  try {
    assert.equal(kitCellIndex([root]).get("SwipeToReveal"), "SwipeToReveal/Card");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("an API the kit publishes no component for reaches nothing", () => {
  // The ordinary case, and not a failure: most of the map's APIs are ones this catalog has no
  // component for, and their samples are still worth publishing.
  assert.equal(kitComponentFor("NoSuchApi", new Map([["Button", "Button/Filled"]])), null);
});

test("a sample component carries the link, and one with no kit family carries none", () => {
  const map = [
    { api: "Button", samples: ["androidx.wear.compose.material3.samples.ButtonSample"] },
    { api: "Orphan", samples: ["androidx.wear.compose.material3.samples.OrphanSample"] },
  ];
  const renderable = new Map([
    ["ButtonSample", "ButtonSamplePreview"],
    ["OrphanSample", "OrphanSamplePreview"],
  ]);
  const { groups, unjoined } = buildGroups(
    map,
    renderable,
    new Map([["Button", "Button/Filled"]]),
  );
  const components = groups.flatMap((g) => g.components);
  const button = components.find((c) => c.componentId === "Button/ButtonSample");
  const orphan = components.find((c) => c.componentId === "Orphan/OrphanSample");

  assert.deepEqual(button.related, [
    { system: "wear-m3-catalog", componentId: "Button/Filled" },
  ]);
  // Absent, not empty: "declared and empty" and "not declared" mean the same thing downstream, and
  // the shorter one does not make an absent link look declared.
  assert.equal(orphan.related, undefined);
  assert.deepEqual(unjoined, ["Orphan"]);
});

test("the spec declares what kind of catalog this is", () => {
  // The server reads this to shape the pages (comparison lanes dropped, source beside the render)
  // and to name this catalog in the kit's back-links. It cannot infer it: which catalogs exist is
  // the deployment's business, not the server's.
  const { spec } = buildSpec([], new Map(), new Map());
  assert.equal(spec.display.role, "samples");
});

test("no label is emitted, so the destination names the component", () => {
  const { groups } = buildGroups(
    [{ api: "Button", samples: ["a.b.ButtonSample"] }],
    new Map([["ButtonSample", "ButtonSamplePreview"]]),
    new Map([["Button", "Button/Filled"]]),
  );
  const link = groups.flatMap((g) => g.components)[0].related[0];
  assert.equal("label" in link, false);
});

test("samples are found under the package directories they are vendored into", () => {
  // The vendored tree mirrors the samples' own package, so nothing sits at its root. A flat scan
  // finds no sample at all and reports it as "none carries @Preview upstream" — an empty spec that
  // reads as an upstream fact rather than as a walk that never descended. Caught exactly that way.
  const root = kitSources({
    "androidx/wear/compose/material3/samples/ButtonSamples.kt":
      "@Sampled\n@Preview\n@Composable\nfun ButtonSample() {}\n" +
      "@Composable\nfun FancyHelper() {}\n",
  });
  try {
    // No generated file: this test is about the vendored walk, and the wrappers would only add
    // names it is not asking about.
    const found = renderableSamples(root, join(root, "no-such-generated.kt"));
    assert.deepEqual([...found.keys()], ["ButtonSample"]);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("a cell id in the index maps to itself, so an override can name one directly", () => {
  const root = kitSources({
    "Buttons.kt": COMPONENT("Button/Filled") + COMPONENT("Button/Outlined"),
  });
  try {
    const index = kitCellIndex([root]);
    // Both granularities, from one walk: the family opens on its first cell, and each cell is
    // addressable on its own.
    assert.equal(index.get("Button"), "Button/Filled");
    assert.equal(index.get("Button/Outlined"), "Button/Outlined");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("an override naming a cell links THAT cell, not its family's first", () => {
  // The whole reason values may be cell ids. `OutlinedButton` mapped to the family `Button` would
  // resolve to `Button/Filled` — a confidently wrong destination.
  const index = new Map([
    ["Button", "Button/Filled"],
    ["Button/Outlined", "Button/Outlined"],
  ]);
  assert.equal(kitComponentFor("OutlinedButton", index), "Button/Outlined");
});

test("an override naming a family links the family's first cell", () => {
  // The other shape, right where the kit publishes ONE cell for the whole API: first-cell and
  // only-cell are then the same thing, and the family name says so more plainly than the id.
  const index = new Map([["CheckboxButton", "CheckboxButton"]]);
  assert.equal(kitComponentFor("SplitCheckboxButton", index), "CheckboxButton");
});

test("an override that resolves to nothing throws, naming the value", () => {
  // A hand-written override reaching nothing is a typo or a renamed cell. An UNMAPPED api reaching
  // nothing is the ordinary case, so the two must not fail the same way — this is the whole
  // difference between the two branches of `kitComponentFor`.
  assert.throws(
    () => kitComponentFor("OutlinedButton", new Map([["Button", "Button/Filled"]])),
    (error) =>
      error.message.includes("OutlinedButton") && error.message.includes("Button/Outlined"),
  );
});

test("every override in the shipped map resolves against the real kit", () => {
  // The regression that matters on a rename: this is the test that turns a broken link into a red
  // build rather than a `--check` diff nobody reads closely.
  const index = kitCellIndex();
  for (const [api, value] of API_TO_KIT_COMPONENT) {
    assert.equal(typeof index.get(value), "string", `${api} -> ${value} resolves to no kit cell`);
  }
});

test("a sample's declaration site is indexed as a module-relative path and its `fun` line", () => {
  const root = kitSources({
    "samples-catalog/src/main/kotlin/upstream/a/b/ButtonSample.kt":
      "package a.b\n\n@Sampled\n@Composable\nfun ButtonSample() {}\n",
  });
  try {
    const found = sampleSources(join(root, "samples-catalog/src/main/kotlin/upstream"));
    // Not the repository path: the server resolves `sourceFile` against the producing MODULE.
    assert.equal(found.get("ButtonSample").sourceFile.startsWith("src/main/kotlin/"), false);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("the indexed line is the `fun`, not the first annotation above it", () => {
  // The panel should open on the declaration a reader recognises, with the annotations above it as
  // context rather than as the subject.
  const root = kitSources({
    "S.kt": "package a\n\n@Sampled\n@Preview\n@Composable\nfun ButtonSample() {}\n",
  });
  try {
    assert.equal(sampleSources(root).get("ButtonSample").bodyLine, 6);
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("a @Sampled function with no @Preview is still indexed", () => {
  // This indexes where a sample is DECLARED — a fact about the vendored source — while
  // renderability is a fact about what discovery can invoke. A wrapper is generated for exactly
  // these, and the wrapper's component needs this path most of all.
  const root = kitSources({ "S.kt": "@Sampled\n@Composable\nfun OnlySampled() {}\n" });
  try {
    assert.equal(typeof sampleSources(root).get("OnlySampled")?.bodyLine, "number");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("a component carries the sample's source, not the generated wrapper's", () => {
  // The whole point. `apply-source-files.mjs` prefers a declared path over the one discovery
  // recorded, which for 115 of these is `generated/SamplePreviews.kt`.
  const { groups } = buildGroups(
    [{ api: "Button", samples: ["a.b.ButtonSample"] }],
    new Map([["ButtonSample", "ButtonSamplePreview"]]),
    new Map([["Button", "Button/Filled"]]),
    new Map([["ButtonSample", { sourceFile: "src/main/kotlin/upstream/B.kt", bodyLine: 77 }]]),
  );
  const component = groups.flatMap((g) => g.components)[0];
  assert.equal(component.sourceFile, "src/main/kotlin/upstream/B.kt");
  assert.equal(component.bodyLine, 77);
});

test("a sample the index does not know carries no source at all", () => {
  // Absent rather than guessed: a half-declared component would send a reader to a line of a file
  // chosen by default, which is worse than the server rendering no link.
  const { groups } = buildGroups(
    [{ api: "Button", samples: ["a.b.ButtonSample"] }],
    new Map([["ButtonSample", "ButtonSamplePreview"]]),
    new Map(),
    new Map(),
  );
  const component = groups.flatMap((g) => g.components)[0];
  assert.equal("sourceFile" in component, false);
  assert.equal("bodyLine" in component, false);
});

test("every declared path in the shipped spec exists on disk", () => {
  // The declaration overrides discovery, so nothing downstream will catch a path that has moved —
  // an upstream re-import that renames a file would otherwise publish 150 dead source links.
  const spec = JSON.parse(readFileSync("samples-catalog/catalog.spec.json", "utf8"));
  for (const group of spec.groups) {
    for (const component of group.components) {
      if (!component.sourceFile) continue;
      assert.equal(
        existsSync(join("samples-catalog", component.sourceFile)),
        true,
        `${component.componentId} declares ${component.sourceFile}, which does not exist`,
      );
    }
  }
});
