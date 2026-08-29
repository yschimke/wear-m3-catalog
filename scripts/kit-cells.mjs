#!/usr/bin/env node
// Count how much of each published kit SET each sheet actually draws, and write it down.
//
//   node scripts/kit-cells.mjs --map catalog=DIR/design-map.json --map remote-catalog=DIR/... \
//        [--check]
//
// Normally driven by `scripts/kit-cells.sh`, which projects both maps first.
//
// WHY THIS EXISTS. `kit-sets.json` records what this catalog did about every published set, and
// `CatalogKitCoverageTest` holds the two together — but both work at the granularity of the SET. A
// set counts as reproduced when a component exists for it, and nothing asks how much of it is
// drawn. That is how `:remote-catalog` came to draw 15 of the `Card` set's 45 cells with every
// test green ([#158](https://github.com/yschimke/wear-m3-catalog/issues/158)): two thirds of the
// set missing, the `Content type` axis absent entirely, and no file anywhere that would have said
// so.
//
// This is the missing number. One row per set per sheet: how many cells the kit publishes, how
// many this sheet draws, and — named in full, one line each — which ones it does not. The record
// is COMMITTED and CI reconciles it (`scripts/kit-cells.sh --check`), so a cell that stops being
// drawn moves a number in a reviewable diff instead of vanishing quietly.
//
// WHAT COUNTS AS DRAWN is not decided here. The numerator is the RESOLVED design map — the same
// artifact design-parity compares against — so a cell counts exactly when parity would compare
// something to it. Re-deriving it from the annotations was the first attempt and it was wrong in
// eight sets out of thirty-five: `@OverrideVariant(name = "square")` carries no `kitProps` and
// still resolves, because matching a variant name onto a kit axis value is `@design-parity/
// kit-index`'s job and its heuristics live there. Reimplementing them here would have produced a
// record that quietly undercounted — worse than no record, since it would be believed.
//
// The DENOMINATOR is `figma-kit-index.json`, the committed kit walk. Both halves therefore move
// only when something upstream of this repo moves, and both are checked in.

import { readFileSync, writeFileSync } from "node:fs";

/** `figma:<fileKey>/<nodeId>` -> `<nodeId>`. Node ids are unique within a file. */
export const nodeOf = (ref) => {
  const slash = ref.indexOf("/");
  return slash < 0 ? ref : ref.slice(slash + 1);
};

/**
 * Every kit node a design map claims, grouped by the set the claiming component belongs to.
 *
 * `ref` has three shapes in the wild — a bare string, a list of strings, a list of
 * `{ref, state}` — because the variant resolution is what turns one into the other and a
 * component with no variant axis never goes through it.
 */
export const claimsBySet = (map) => {
  const out = new Map();
  for (const component of map.components ?? []) {
    if (!component.refSet) continue;
    const set = nodeOf(component.refSet);
    if (!out.has(set)) out.set(set, new Set());
    const bucket = out.get(set);
    const refs = typeof component.ref === "string" ? [component.ref] : (component.ref ?? []);
    for (const entry of refs) {
      bucket.add(nodeOf(typeof entry === "string" ? entry : entry.ref));
    }
  }
  return out;
};

/**
 * The published cells of one set, keyed by node id, valued by the vector the kit names them with.
 *
 * The vector is kept as the kit's own `Axis=Value, Axis=Value` string rather than parsed: it is
 * what a reader recognises, it is stable under a re-walk, and nothing here needs to reason about
 * an individual axis.
 */
export const publishedCells = (index, node) => {
  const set = index.sets?.[node];
  if (!set) return null;
  return new Map(set.variants.map((v) => [v.id, v.name]));
};

/**
 * The record itself: one row per set that at least one sheet reproduces, in `kit-sets.json` order.
 *
 * A set no sheet touches is NOT a row. Its whole answer is the stated reason on its `kit-sets.json`
 * row, and repeating it here as `drawn: 0` would invite the reading that it is a gap rather than a
 * decision.
 */
export const cellCoverage = ({ index, sets, maps }) => {
  const rows = [];
  for (const row of sets.sets) {
    const published = publishedCells(index, row.node);
    if (!published) continue; // the walk saw no variants under it; kit-sets.json owns that case
    const sheets = {};
    for (const [sheet, map] of Object.entries(maps)) {
      const claimed = claimsBySet(map).get(row.node);
      if (!claimed) continue;
      const drawn = [...claimed].filter((id) => published.has(id));
      const stray = [...claimed].filter((id) => !published.has(id)).sort();
      const uncovered = [...published.entries()]
        .filter(([id]) => !claimed.has(id))
        .map(([, name]) => name)
        .sort();
      sheets[sheet] = { drawn: drawn.length, uncovered, ...(stray.length ? { stray } : {}) };
    }
    if (Object.keys(sheets).length === 0) continue;
    rows.push({
      page: row.page,
      set: row.set,
      node: row.node,
      published: published.size,
      sheets,
    });
  }
  return {
    $comment:
      "HOW MUCH OF EACH KIT SET EACH SHEET DRAWS, cell by cell. Generated by " +
      "scripts/kit-cells.sh from the resolved design map of each module joined to " +
      "figma-kit-index.json; CI reconciles it, so a cell that stops being drawn moves a number " +
      "in a reviewable diff. `uncovered` names the kit's own vector for every published cell the " +
      "sheet does not draw. WHY a set is short is not recorded here — that is prose, and it " +
      "belongs on the matching kit-sets.json row under `cells`, which KitCellCoverageTest holds " +
      "against these numbers.",
    fileKey: sets.fileKey,
    generatedBy: "scripts/kit-cells.sh",
    sets: rows,
  };
};

const usage = () => {
  console.error("usage: kit-cells.mjs --map <sheet>=<design-map.json> [--map …] [--check] [--out F]");
  process.exit(2);
};

const main = (argv) => {
  const maps = {};
  let check = false;
  let out = "kit-cells.json";
  let index = "figma-kit-index.json";
  let sets = "kit-sets.json";
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    if (arg === "--check") check = true;
    else if (arg === "--map") {
      const spec = argv[++i] ?? usage();
      const eq = spec.indexOf("=");
      if (eq < 0) usage();
      maps[spec.slice(0, eq)] = JSON.parse(readFileSync(spec.slice(eq + 1), "utf8"));
    } else if (arg === "--out") out = argv[++i] ?? usage();
    else if (arg === "--index") index = argv[++i] ?? usage();
    else if (arg === "--sets") sets = argv[++i] ?? usage();
    else usage();
  }
  if (Object.keys(maps).length === 0) usage();

  const record = cellCoverage({
    index: JSON.parse(readFileSync(index, "utf8")),
    sets: JSON.parse(readFileSync(sets, "utf8")),
    maps,
  });
  const text = `${JSON.stringify(record, null, 2)}\n`;

  if (!check) {
    writeFileSync(out, text);
    return;
  }
  let current = "";
  try {
    current = readFileSync(out, "utf8");
  } catch {
    /* absent counts as out of date */
  }
  if (current !== text) {
    console.error(`::error::${out} is out of date — regenerate with scripts/kit-cells.sh`);
    process.exit(1);
  }
};

if (import.meta.url === `file://${process.argv[1]}`) main(process.argv.slice(2));
