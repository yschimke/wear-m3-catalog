import { test } from "node:test";
import assert from "node:assert/strict";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

import { buildGroups, kitComponentFor, kitFirstCellByFamily } from "./samples-spec.mjs";

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
    assert.equal(kitFirstCellByFamily([root]).get("Button"), "Button/Filled");
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
    assert.equal(kitFirstCellByFamily([root]).get("Button"), "Button/First");
  } finally {
    rmSync(root, { recursive: true, force: true });
  }
});

test("an id with no slash is its own family and its own first cell", () => {
  const root = kitSources({ "Dialogs.kt": COMPONENT("AlertDialog") });
  try {
    assert.equal(kitFirstCellByFamily([root]).get("AlertDialog"), "AlertDialog");
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
    assert.equal(kitFirstCellByFamily([root]).get("SwipeToReveal"), "SwipeToReveal/Card");
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

test("no label is emitted, so the destination names the component", () => {
  const { groups } = buildGroups(
    [{ api: "Button", samples: ["a.b.ButtonSample"] }],
    new Map([["ButtonSample", "ButtonSamplePreview"]]),
    new Map([["Button", "Button/Filled"]]),
  );
  const link = groups.flatMap((g) => g.components)[0].related[0];
  assert.equal("label" in link, false);
});
