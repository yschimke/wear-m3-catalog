#!/usr/bin/env node
// Hold the two sheets' `parallel` declarations against each other.
//
//   node scripts/parallel-map.mjs --previews <sheet>=<previews.json> --previews <sheet>=<…> \
//        [--from <sheet>] [--to <sheet>]
//
// Normally driven by `scripts/parallel-map.sh`, which discovers both modules first.
//
// WHY THIS EXISTS. The compare page reads the two columns component by component through
// `parallel`, and AGENTS.md settles what that declaration means: the two sheets have ONE taxonomy,
// so where they draw the same kit node the component id is the SAME STRING and the `parallel` is a
// restatement rather than a translation. Nothing checked it. The rule was applied by hand across
// #116 and the renames that followed (#292, #299, #300), and every way of breaking it is silent:
//
//   * a `parallel` naming an id the other sheet no longer has drops that row off the compare page,
//     which looks exactly like a component nobody has written yet;
//   * two paired components naming DIFFERENT kit nodes compare two different pictures under one
//     heading, and report the difference between the kit's own cells as divergence in this code;
//   * an id both sheets publish with no `parallel` behind it is a pairing that happens to work by
//     spelling and will stop working the next time somebody renames either side.
//
// A rename is exactly when this goes wrong, and a rename is exactly when nothing here fails. So
// this is a GATE and not a record: there is no file to regenerate, nothing to commit, and the only
// output on a healthy tree is the count.
//
// WHAT IT DOES NOT DECIDE is which cells each sheet draws. That is `kit-cells.json`'s question, it
// is answered against the kit rather than against the other sheet, and the two sheets legitimately
// differ where one library can draw a cell and the other cannot (`RemoteTitleCard` takes no
// painter, so every `background-image` cell is one-sided). Cell pairing is REPORTED here, under a
// heading that keeps it apart from a failure, because a growing list of one-sided cells is worth
// seeing in the log of the job that already holds both manifests.

import { readFileSync } from "node:fs";

/** The `_VARIANT_<name>` suffix the discovery gives an `@OverrideVariant` capture. */
const VARIANT = /_VARIANT_(.+)$/;

/**
 * One sheet's components and cells, out of its discovered preview manifest.
 *
 * The manifest is the right input rather than `design-map.json`: the map is projected per module
 * into ONE committed path (see `scripts/design-map.sh`), so a checkout can only ever hold one
 * sheet's, while both manifests exist side by side in the job that discovers them. It also carries
 * `parallel` itself, which the map does not.
 */
export const readSheet = (manifest) => {
  const components = new Map();
  const cells = new Map();
  for (const preview of manifest.previews ?? []) {
    const catalog = preview.catalog;
    if (!catalog?.componentId) continue;
    const id = catalog.componentId;
    const variant = VARIANT.exec(preview.id ?? "");
    if (variant) {
      if (!cells.has(id)) cells.set(id, new Set());
      cells.get(id).add(variant[1]);
    } else if (!components.has(id)) {
      components.set(id, catalog);
    }
  }
  return { components, cells };
};

/**
 * Every way the two sheets can disagree about a pairing, as one flat list of prose findings.
 *
 * `from` is the sheet that AUTHORS the declaration — the Remote one, whose `catalog.spec.json`
 * carries the `compareWith` the page is built from. `to` is the sheet it points at. The direction
 * matters for the first rule and not for the rest: a dangling `parallel` is a defect on the sheet
 * that wrote it, while a disagreement about a kit node is a defect in the pair.
 */
export const findings = ({ from, to, fromName, toName }) => {
  const out = [];
  for (const [id, component] of from.components) {
    const target = component.parallel;
    if (target) {
      const twin = to.components.get(target);
      if (!twin) {
        out.push(
          `${fromName} ${id}: parallel = "${target}", which ${toName} does not publish. ` +
            `The compare page drops the row.`,
        );
        continue;
      }
      if ((component.reference ?? null) !== (twin.reference ?? null)) {
        out.push(
          `${fromName} ${id} and ${toName} ${target} are parallel but name different kit ` +
            `nodes: ${component.reference ?? "none"} against ${twin.reference ?? "none"}.`,
        );
      }
      if ((component.referenceSet ?? null) !== (twin.referenceSet ?? null)) {
        out.push(
          `${fromName} ${id} and ${toName} ${target} are parallel but name different kit ` +
            `SETS: ${component.referenceSet ?? "none"} against ${twin.referenceSet ?? "none"}.`,
        );
      }
      continue;
    }
    if (to.components.has(id)) {
      out.push(
        `${fromName} ${id}: both sheets publish this id and neither says so. Author the ` +
          `parallel — it is what the pairing walks, even where the two ids are identical.`,
      );
    }
  }
  return out;
};

/** One-sided cells of a paired component, reported rather than failed. See the header. */
export const unpairedCells = ({ from, to, fromName, toName }) => {
  const out = [];
  for (const [id, component] of from.components) {
    const target = component.parallel ?? (to.components.has(id) ? id : null);
    if (!target) continue;
    const mine = from.cells.get(id) ?? new Set();
    const theirs = to.cells.get(target) ?? new Set();
    const onlyMine = [...mine].filter((c) => !theirs.has(c)).sort();
    const onlyTheirs = [...theirs].filter((c) => !mine.has(c)).sort();
    if (onlyMine.length || onlyTheirs.length) {
      out.push({ id, target, [fromName]: onlyMine, [toName]: onlyTheirs });
    }
  }
  return out;
};

const usage = () => {
  console.error(
    "usage: parallel-map.mjs --previews <sheet>=<previews.json> [--previews …] " +
      "[--from <sheet>] [--to <sheet>]",
  );
  process.exit(2);
};

const main = (argv) => {
  const sheets = {};
  let fromName = "remote-catalog";
  let toName = "catalog";
  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    if (arg === "--previews") {
      const spec = argv[++i] ?? usage();
      const eq = spec.indexOf("=");
      if (eq < 0) usage();
      sheets[spec.slice(0, eq)] = readSheet(JSON.parse(readFileSync(spec.slice(eq + 1), "utf8")));
    } else if (arg === "--from") fromName = argv[++i] ?? usage();
    else if (arg === "--to") toName = argv[++i] ?? usage();
    else usage();
  }
  const from = sheets[fromName];
  const to = sheets[toName];
  if (!from || !to) usage();

  const broken = findings({ from, to, fromName, toName });
  const paired = [...from.components.values()].filter(
    (c) => c.parallel && to.components.has(c.parallel),
  ).length;

  const oneSided = unpairedCells({ from, to, fromName, toName });
  if (oneSided.length) {
    console.log(`Cells drawn on one sheet only (reported, not a failure):`);
    for (const row of oneSided) {
      const mine = row[fromName].length ? `${fromName}-only ${row[fromName].join(", ")}` : "";
      const theirs = row[toName].length ? `${toName}-only ${row[toName].join(", ")}` : "";
      console.log(`  ${row.id} -> ${row.target}: ${[mine, theirs].filter(Boolean).join("; ")}`);
    }
    console.log("");
  }

  if (broken.length) {
    for (const line of broken) console.error(`::error::${line}`);
    process.exit(1);
  }
  console.log(
    `✓ ${paired} parallel pair(s) between ${fromName} and ${toName} name the same kit node.`,
  );
};

if (import.meta.url === `file://${process.argv[1]}`) main(process.argv.slice(2));
