#!/usr/bin/env node
// Regenerate docs/COMPONENT_MAP.md — where the two sheets meet, where only one of them goes, and
// which Figma node each component answers to.
//
//   node scripts/component-map.mjs
//
// TWO INPUTS, AND THEY ARE DELIBERATELY DIFFERENT ONES.
//
//   1. The `@CatalogComponent` annotations in both modules, read straight from the Kotlin. That is
//      the same source `design-map.json` and the published sheets are projected from, so the map
//      cannot claim a pairing the catalogs do not have. Reading the annotations rather than
//      `design-map.json` is on purpose: the committed map belongs to `:catalog` alone (see
//      `scripts/design-map.sh`), and this doc has to speak for both.
//
//   2. The `catalog.json` of each published delivery branch, for the render paths. Those are
//      per-component and not derivable from the component id — the file name carries the variant,
//      state and size the exporter chose — so this fetches them rather than guessing a URL that
//      would 404 silently in a table of a hundred images.
//
// WHICH MEANS THIS NEEDS NETWORK, and is therefore NOT a CI-reconciled record like design-map.json
// or kit-cells.json — a `--check` gate would go red for the twenty minutes between a merge and the
// publish that gives its components renders, and again every time the sheets republish on their own
// schedule with no commit here at all. So it is refreshed rather than enforced. The images are
// branch-pinned rather than commit-pinned so they follow the sheets as those republish; the cost is
// that renaming a component leaves a dead image until this is re-run.
//
// THE REFRESH IS AUTOMATED, and the two inputs above are why it has to be. The annotations are
// current the moment a PR lands; the renders are not, so a map regenerated in the PR that ADDS a
// component names it with no image and nothing fills that in afterwards — which is what happened to
// the four one-handed gesture components in #258. `.github/workflows/component-map.yml` runs this
// after every `Design Artifacts` publish on main and opens a PR when the output moves. Running it
// by hand still works and is the right thing when you want the diff in the PR you are already
// writing; the workflow is what catches the half that could not have been there yet.
//
// The pairing key is `parallel` on the Remote side, falling back to an identical id. Several Remote
// components legitimately name one Wear component — the kit spells as one thing what Remote Compose
// reaches through separate functions — so the Wear column is grouped and those rows marked.

import fs from "node:fs";
import path from "node:path";

const KIT = "B24oss2tTeXAFykyeyusz0";
const REPO = "yschimke/wear-m3-catalog";
const RAW = `https://raw.githubusercontent.com/${REPO}`;
const SHEETS = [
  { module: "catalog", branch: "wear-m3-catalog" },
  { module: "remote-catalog", branch: "remote-m3" },
];

/**
 * The two mutually exclusive lanes `remote-catalog` builds against, and the file that picks one.
 *
 * `settings.gradle.kts` and `remote-catalog/build.gradle.kts` both resolve the lane the same way
 * and put exactly ONE of these source sets on the source path — never both, never neither. Walking
 * both here would name components that the sheet does not draw, which is precisely the skew that
 * made run #164 declare eleven snapshot-only cells with no sticker behind them.
 *
 * `-PremoteSnapshot=<id>` overrides the file for one Gradle invocation and is deliberately not
 * mirrored: this script is run by `component-map.yml` with no arguments, and the render it is a
 * record of went through the same reusable workflow with no way to pass one either. The pin file
 * is what both of them see.
 */
const LANES = new Set(["released", "snapshot"]);
const SNAPSHOT_PIN = ".github/ci/remote-snapshot-pin";

function activeLane() {
  const pin = fs.existsSync(SNAPSHOT_PIN) ? fs.readFileSync(SNAPSHOT_PIN, "utf8").trim() : "";
  return pin ? "snapshot" : "released";
}

/**
 * Every `.kt` in a module's production source sets.
 *
 * DISCOVERED, NOT LISTED. `catalog` was a single `src/main/kotlin` until #437 made it one
 * multiplatform source set, and this script went on reading a directory that no longer existed —
 * so every `Refresh component map` run failed for four days and the map went stale in silence,
 * which is the exact failure the workflow exists to prevent. Reading whatever `src/<set>/kotlin`
 * the module actually has means the next source set costs nothing.
 *
 * `CatalogInventoryTest` lists its two source sets explicitly and says why: it asserts COVERAGE,
 * and a scan that silently widened would report success by looking at more. This is the opposite
 * job — a RECORD of what the sheets publish — where a scan that silently narrows is the bug, so
 * the two disagree on purpose.
 *
 * Two kinds of source set are filtered out rather than walked:
 *
 *   - TESTS. Defensively: today `catalog/src/androidHostTest` and `remote-catalog/src/test` only
 *     MENTION `@CatalogComponent` (they parse it — see `CatalogInventoryTest`) and declare no id,
 *     so nothing of theirs would have been picked up anyway. A fixture that did declare one would
 *     put a component in the map that no sheet publishes.
 *   - THE LANE NOT IN USE, per [LANES] above.
 */
function sources(module) {
  const src = path.join(module, "src");
  const lane = activeLane();
  const roots = fs
    .readdirSync(src, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => entry.name)
    .filter((name) => !/test/i.test(name))
    .filter((name) => !LANES.has(name) || name === lane)
    .map((name) => path.join(src, name, "kotlin"))
    .filter((root) => fs.existsSync(root))
    // Sorted, and so is each directory below, because two source sets can declare the same id and
    // the last one read wins. Traversal order does not reach the tables — every one of them sorts
    // by id — but it does decide which of a duplicate pair is described, and that should not come
    // down to what order a filesystem happened to hand back.
    .sort();
  // LOUDLY, because discovery turns the old crash into a silent wrong answer. Reading a path that
  // had moved raised ENOENT, which is at least a stack trace; reading whatever is there instead
  // yields no annotations, drops every one of the module's components, and makes the next refresh
  // read as a deletion rather than as a bug.
  if (roots.length === 0) {
    throw new Error(
      `${module}: no production source root under ${src} — the module's layout has moved and ` +
        `this script has to move with it.`,
    );
  }
  const out = [];
  const walk = (dir) => {
    const entries = fs
      .readdirSync(dir, { withFileTypes: true })
      .sort((a, b) => a.name.localeCompare(b.name));
    for (const entry of entries) {
      const p = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(p);
      else if (entry.name.endsWith(".kt")) out.push(p);
    }
  };
  for (const root of roots) walk(root);
  return out;
}

/**
 * The `@CatalogComponent(...)` blocks in one module, by component id.
 *
 * Brace-matched rather than regex-matched to the closing paren: the annotations carry concatenated
 * strings with parentheses inside them, and a non-greedy `\)` stops at the first one — which yields
 * a truncated block whose `caption` and `noReference` silently go missing.
 */
function components(module) {
  const found = new Map();
  for (const file of sources(module)) {
    const s = fs.readFileSync(file, "utf8");
    for (const m of s.matchAll(/@CatalogComponent\(/g)) {
      let i = m.index + m[0].length;
      let depth = 1;
      while (depth > 0 && i < s.length) {
        if (s[i] === "(") depth++;
        else if (s[i] === ")") depth--;
        i++;
      }
      const block = s.slice(m.index + m[0].length, i);
      const id = block.match(/\bid\s*=\s*"([^"]+)"/)?.[1];
      if (!id) continue;
      found.set(id, {
        id,
        node: block.match(/\breference\s*=\s*"figma:[^/]+\/([^"]+)"/)?.[1] ?? null,
        parallel: block.match(/\bparallel\s*=\s*"([^"]+)"/)?.[1] ?? null,
        caption: block.match(/\bcaption\s*=\s*"([^"]*)"/)?.[1] ?? "",
      });
    }
  }
  return found;
}

/** componentId → first published render path, from a delivery branch's `catalog.json`. */
async function renders(branch) {
  const res = await fetch(`${RAW}/design-artifacts/${branch}/catalog.json`);
  if (!res.ok) throw new Error(`${branch}: catalog.json ${res.status} — has it published yet?`);
  const map = new Map();
  for (const c of (await res.json()).components ?? []) {
    const first = (c.images ?? [])[0];
    if (first?.path) map.set(c.componentId, first.path);
  }
  return map;
}

const img = (id, paths, branch, alt) => {
  const p = paths.get(id);
  return p ? `<img src="${RAW}/design-artifacts/${branch}/${p}" width="150" alt="${alt}">` : "—";
};
const node = (n) =>
  n ? `[\`${n}\`](https://www.figma.com/design/${KIT}/?node-id=${n.replace(":", "-")})` : "_stated absence_";

const wear = components("catalog");
const remote = components("remote-catalog");
const [wearImg, remoteImg] = await Promise.all([renders("wear-m3-catalog"), renders("remote-m3")]);

// Group the Remote components under the Wear one they pair with; what is left is one-sided.
const paired = new Map();
const remoteOnly = [];
for (const r of [...remote.values()].sort((a, b) => a.id.localeCompare(b.id))) {
  const key = r.parallel ?? (wear.has(r.id) ? r.id : null);
  if (key && wear.has(key)) {
    if (!paired.has(key)) paired.set(key, []);
    paired.get(key).push(r);
  } else remoteOnly.push(r);
}
const wearOnly = [...wear.values()]
  .filter((w) => !paired.has(w.id))
  .sort((a, b) => a.id.localeCompare(b.id));
const pairedRemote = [...paired.values()].reduce((n, rs) => n + rs.length, 0);

const L = [];
L.push("# Component map: `wear-m3-catalog` ↔ `remote-m3`\n");
L.push(
  `Both sheets in this repository reproduce the [M3 Wear OS Apps Design Kit](https://www.figma.com/design/${KIT}/) —`,
  "`wear-m3-catalog` in Wear Compose, `remote-m3` in Remote Compose. This is where they meet, where",
  "only one of them goes, and which Figma node each component answers to.\n",
);
L.push("| | Wear | Remote |", "| --- | ---: | ---: |");
L.push(`| Components | **${wear.size}** | **${remote.size}** |`);
L.push(`| Paired with the other sheet | **${paired.size}** | **${pairedRemote}** |`);
L.push(`| Only on this sheet | **${wearOnly.length}** | **${remoteOnly.length}** |`, "");
L.push(
  "> Generated by [`scripts/component-map.mjs`](../scripts/component-map.mjs) from the",
  "> `@CatalogComponent` annotations in both modules — the same source the published sheets and",
  "> `design-map.json` are projected from, so it cannot drift from what ships. Renders come from the",
  "> `design-artifacts/*` delivery branches and follow them as those republish — which is why",
  "> [`component-map.yml`](../.github/workflows/component-map.yml) re-runs this after every publish",
  "> and opens a PR when the output moves.\n",
);

// The two halves are read at DIFFERENT freshness, and a reader deserves to be told which rows that
// costs. Annotations come from this checkout; renders come from whatever the delivery branch last
// published. So a component renamed or added since that publish is correctly listed and has no
// picture yet — a `—` that means "not published under this name yet", not "draws nothing". Naming
// them beats leaving a bare dash to be misread as a missing sticker.
const unpublished = [
  ...[...paired.values()].flat().map((r) => [r.id, remoteImg, "remote-m3"]),
  ...remoteOnly.map((r) => [r.id, remoteImg, "remote-m3"]),
  ...[...paired.keys()].map((id) => [id, wearImg, "wear-m3-catalog"]),
  ...wearOnly.map((w) => [w.id, wearImg, "wear-m3-catalog"]),
].filter(([id, paths]) => !paths.has(id));
if (unpublished.length) {
  const one = unpublished.length === 1;
  L.push(
    `> **${one ? "One component has" : `${unpublished.length} components have`} no render yet.**`,
    `> ${one ? "Its" : "Their"} sheet has not republished since`,
    `> ${one ? "it was" : "they were"} named, so the image cell reads \`—\`, meaning "not published`,
    "> under this name yet\" rather than \"draws nothing\". It fills in on the next publish:",
    ...unpublished.map(([id, , branch]) => `> - \`${id}\` (${branch})`),
    "",
  );
}

L.push(`## Common — ${paired.size} Wear components facing ${pairedRemote} Remote ones\n`);
L.push(
  "Paired through `parallel` on the Remote side. Where a Wear component faces **more than one**",
  "Remote component the row is marked ⑂: the kit spells as one thing something Remote Compose",
  "reaches through separate functions, or the Remote sheet documents a capability with no Wear",
  "equivalent.\n",
);
L.push("| | Wear component | Kit node | Remote component | |", "| --- | --- | --- | --- | --- |");
for (const id of [...paired.keys()].sort()) {
  const w = wear.get(id);
  const rs = paired.get(id);
  rs.forEach((r, i) => {
    const first = i === 0;
    L.push(
      `| ${first ? img(id, wearImg, "wear-m3-catalog", `${id} on Wear`) : ""} ` +
        `| ${first ? `${rs.length > 1 ? "⑂ " : ""}\`${id}\`` : "↳"} ` +
        `| ${first ? node(w.node) : ""} ` +
        `| \`${r.id}\`<br>${node(r.node)} ` +
        `| ${img(r.id, remoteImg, "remote-m3", `${r.id} on Remote`)} |`,
    );
  });
}
L.push("");

L.push(`## Remote only — ${remoteOnly.length} components\n`);
L.push(
  "Not gaps. These document what Remote Compose can do that has no Wear Material 3 peer and no kit",
  "counterpart: document-level shaders, colour and typography token specimens, downloadable-font",
  "axes, and the Glance Wear widget host frame. None can name a kit node — which is why this",
  "sheet's component coverage cannot reach 100% by construction.\n",
);
L.push("| | Component | What it documents |", "| --- | --- | --- |");
for (const r of remoteOnly)
  L.push(`| ${img(r.id, remoteImg, "remote-m3", r.id)} | \`${r.id}\` | ${r.caption} |`);
L.push("");

L.push(`## Wear only — ${wearOnly.length} components\n`);
L.push(
  "The Wear sheet is older and wider. Most of these are Wear Compose surfaces `remote-material3`",
  "has not published yet — pickers, dialogs, the media stack, swipe-to-reveal, placeholders —",
  "rather than deliberate omissions on the Remote side.\n",
);
L.push("| | Component | Kit node | Caption |", "| --- | --- | --- | --- |");
for (const w of wearOnly)
  L.push(
    `| ${img(w.id, wearImg, "wear-m3-catalog", w.id)} | \`${w.id}\` | ${node(w.node)} | ${w.caption} |`,
  );
L.push("");

fs.writeFileSync("docs/COMPONENT_MAP.md", L.join("\n") + "\n");
console.log(
  `component-map: ${paired.size} paired (${pairedRemote} Remote), ` +
    `${remoteOnly.length} Remote-only, ${wearOnly.length} Wear-only`,
);
