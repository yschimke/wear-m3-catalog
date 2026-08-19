#!/usr/bin/env node
// Re-shape the COMMITTED kit page walk (design/pages/pages.json) into the inventory that
// `@design-parity/kit-index build` reads, so the kit index can be rebuilt without a Figma token.
//
// WHY THIS EXISTS. The sanctioned producer of the inventory is `kit-index dump`, which walks the
// Figma REST API and therefore needs `FIGMA_TOKEN` — a secret only CI holds
// (`.github/workflows/figma-refs.yml`). But `figma-pages.yml` has ALREADY walked the same file with
// the same credential and committed what it found: `design/pages/pages.json` lists every
// COMPONENT / COMPONENT_SET / INSTANCE under each imported page, with its node id, layer name,
// type and nesting depth. That is the same vocabulary the index is a projection of, read from the
// same kit, on the same walk — so an inventory built from it is a re-shaping of committed data
// rather than a second source of truth.
//
// This is deliberately NOT a reimplementation of the index. It emits only the inventory, and
// `kit-index build` still decides what the index keeps; the two steps stay where they are.
//
// WHAT IT CANNOT SEE, and why that is safe. The page import records a node's id, name and type and
// nothing else, so the inventory it can reconstruct carries no component PROPERTIES and no
// configured INSTANCE property vectors. `kit-index build` treats both as optional — they are the
// enrichment it fetches when a token is present — so the index this produces is a strict subset of
// the one CI produces: same sets, same variants, same ids, minus the property tables. A token-less
// rebuild therefore resolves every AXIS-shaped variant cell and leaves the property-shaped ones
// unresolved, which is the honest answer rather than a guessed one.
//
// Re-running `figma-refs.yml` with the token remains the way to get the full index, and its output
// supersedes this one wholesale.
//
//   node scripts/kit-inventory-from-pages.mjs [--pages design/pages/pages.json] [--out figma-inventory.json]

import { readFileSync, writeFileSync } from "node:fs";

function arg(name, fallback) {
  const i = process.argv.indexOf(`--${name}`);
  return i >= 0 && i + 1 < process.argv.length ? process.argv[i + 1] : fallback;
}

const pagesPath = arg("pages", "design/pages/pages.json");
const outPath = arg("out", "figma-inventory.json");

const pages = JSON.parse(readFileSync(pagesPath, "utf8"));

/**
 * A page's flat node list back into the shallow tree the inventory wants.
 *
 * The import writes nodes depth-first with an explicit `depth`, and stops descending at everything
 * except a COMPONENT_SET — so a set's variants are exactly the runs of deeper nodes that follow it,
 * and nothing else nests. Reading the runs is therefore enough; no id bookkeeping is needed.
 *
 * `level` is the walk depth `dump` records, and `hidden` is unknowable from the import — it is only
 * ever used to pick a render alias for a hidden set, and claiming `true` without evidence would
 * invent one. `false` is the safe answer: the definition id stays the render handle, which is what
 * every visible set uses anyway.
 */
function walk(page) {
  const nodes = page.nodes ?? [];
  const components = [];
  for (let i = 0; i < nodes.length; i++) {
    const node = nodes[i];
    if (node.type !== "COMPONENT_SET" && node.type !== "COMPONENT") continue;
    const children = [];
    if (node.type === "COMPONENT_SET") {
      for (let j = i + 1; j < nodes.length && nodes[j].depth > node.depth; j++) {
        if (nodes[j].depth === node.depth + 1 && nodes[j].type === "COMPONENT") {
          children.push({ name: nodes[j].name, id: nodes[j].nodeId, w: 0, h: 0, radius: null });
        }
      }
    }
    // A COMPONENT listed at the top of a page (not under a set) is a standalone; one listed under a
    // set is that set's variant and is already carried as a child, so it is not a component of its
    // own. The import only ever descends INTO a set, so "has a shallower COMPONENT_SET covering it"
    // is the test.
    if (node.type === "COMPONENT") {
      let covered = false;
      for (let j = i - 1; j >= 0; j--) {
        if (nodes[j].depth >= node.depth) continue;
        covered = nodes[j].type === "COMPONENT_SET";
        break;
      }
      if (covered) continue;
    }
    components.push({
      name: node.name,
      id: node.nodeId,
      type: node.type,
      level: node.depth,
      hidden: false,
      w: 0,
      h: 0,
      radius: null,
      trail: `${page.name} / ${node.name}`,
      children,
    });
  }
  return components;
}

const inventory = {
  fileKey: pages.fileKey,
  generatedBy: "wear-m3-catalog/scripts/kit-inventory-from-pages.mjs",
  pages: (pages.pages ?? []).map((page) => {
    const components = walk(page);
    return {
      page: page.name,
      pageId: page.nodeId,
      deepest: (page.nodes ?? []).reduce((max, n) => Math.max(max, n.depth ?? 0), 0),
      components,
      // Neither kind of instance survives the page import: it records an INSTANCE's id and name but
      // none of the property values that make one worth keeping, and a render alias is only ever
      // chosen for a hidden set, which this walk cannot identify. Empty is the truthful answer.
      renderInstances: [],
      propertyInstances: [],
    };
  }),
};

writeFileSync(outPath, `${JSON.stringify(inventory, null, 2)}\n`);

const sets = inventory.pages.flatMap((p) => p.components.filter((c) => c.type === "COMPONENT_SET"));
const variants = sets.reduce((n, s) => n + s.children.length, 0);
const standalone = inventory.pages.flatMap((p) =>
  p.components.filter((c) => c.type === "COMPONENT"),
).length;
console.log(
  `Wrote ${outPath} from ${pagesPath}: ${inventory.pages.length} page(s), ` +
    `${sets.length} component set(s), ${variants} variant(s), ${standalone} standalone ` +
    `component(s), 0 instances (not recoverable from the page import).`,
);
