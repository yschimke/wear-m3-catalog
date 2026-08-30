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
// or kit-cells.json. It is documentation: run it when the inventory moves, commit the result. The
// images are branch-pinned rather than commit-pinned so they follow the sheets as those republish;
// the cost is that renaming a component leaves a dead image until this is re-run.
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

/** Every `.kt` under a module's main source set. */
function sources(module) {
  const root = path.join(module, "src/main/kotlin");
  const out = [];
  const walk = (dir) => {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const p = path.join(dir, entry.name);
      if (entry.isDirectory()) walk(p);
      else if (entry.name.endsWith(".kt")) out.push(p);
    }
  };
  walk(root);
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
  "> `design-artifacts/*` delivery branches and follow them as those republish.\n",
);

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
