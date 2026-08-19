#!/usr/bin/env node
// Import whole PAGES of the M3 Wear OS Apps Design Kit as cached SVG, with the node id of every
// component on them joined back to the code that implements it.
//
// WHAT THIS REPLACES
//
// The first cut of this surface imported *one composed screen* (`Examples` → Upcoming-Mobile) as a
// flat PNG and drew a rectangle per component instance on it. `docs/FIGMA_PAGES.md` records why
// that never paid off: `Examples` is the only page in the file with instances on it, most of each
// screen is hand-drawn rather than assembled from the kit, and the densest screen in the whole file
// yields eleven placements of which two are OS chrome.
//
// The kit's *value* is on the other thirty pages — the component definition sheets. A definition
// page is a specimen: `Shapes` is the 35-shape Expressive
// library, `Buttons` is every button variant, laid out as the designer intends them to be read. That is the
// thing worth putting our renders on top of, because a definition sheet is exactly the claim this
// catalog is trying to reproduce.
//
// SO: A PAGE, AS SVG, WITH IDS
//
// Two REST calls per page:
//
//   1. `/v1/files/:key/nodes?ids=<page>` — the node tree. Every COMPONENT / COMPONENT_SET /
//      INSTANCE under the page becomes a `nodes` entry, carrying its node id and layer name.
//   2. `/v1/images/:key?ids=<page>&format=svg&svg_include_node_id=true` — the page as one SVG,
//      with `data-node-id` on every element.
//
// `svg_include_node_id` is the whole trick. It means the cached SVG is not a picture but a
// *document we can address*: given a node id, a consumer can find that shape in the markup, hide
// it, and put our own render in the hole it leaves — which is what the preview server's
// `/{system}/pages/` surface now does.
//
// NO GEOMETRY IS RECORDED, DELIBERATELY
//
// A node carries no bounding box. The old PNG manifest had to carry one — a flat raster has no
// structure to ask. An SVG does: the element is right there, and its box is whatever the browser
// measures. Recording Figma's `absoluteBoundingBox` alongside would introduce a second, weaker
// answer to the same question — weaker because the export box is the *render* bounds (it includes
// effect bleed), so the two disagree by a few pixels on anything with a shadow, and a consumer
// choosing between them would silently pick the wrong one. One source of truth: the SVG.
//
// EVERY PAGE, NOT A HAND-KEPT LIST
//
// The kit has ~31 pages and `docs/FIGMA_PAGES.md` could name only half of them: page ids are as
// undiscoverable as node ids, and fourteen of them were listed there as bare numbers because naming
// one costs a full subtree dump through the MCP server. Requiring a human to type an id, a name and
// a slug per page is what kept this import at one page.
//
// So `design-pages.json` can say `"discover": true`, and the importer asks the file itself — the
// same one request `design-parity-pages list` makes (`GET /v1/files/:key?depth=1`, the document
// truncated to its pages). Each page it finds becomes an entry whose **id is a slug of the page's
// own name** (`Date & time pickers` → `date-time-pickers`), so the published URL reads like the
// design file rather than like a node id.
//
// `pages` is still honoured, and now means *pinned*: an entry there fixes the id (and optionally
// the name) for that node, wherever discovery finds it. `shape` is pinned for exactly that reason —
// its URL is already published, and a slug is only stable while the designer leaves the page name
// alone. `exclude` drops a page by node id or by name.
//
// USAGE
//
//   FIGMA_TOKEN=figd_... node scripts/import-figma-pages.mjs
//   FIGMA_TOKEN=figd_... node scripts/import-figma-pages.mjs --page shape
//
// Reads `design-pages.json` (which pages, and where to write them) and `design-map.json` (the
// node → code join, itself derived from the `@CatalogComponent(reference = …)` annotations). Writes
// `<outDir>/pages.json` and one `<outDir>/<id>.svg` per page.
//
// This script is READ-ONLY against Figma, like every other Figma interaction in this repo. The
// token needs `file_content:read` — the same scope `design-parity-propose-refs` and
// `design-parity-pages list` already document.

import { mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

/** The manifest version this importer writes. Mirrored by `DesignPagesManifest` in the server. */
const PAGES_VERSION = 2;

/** Node types that become nodes on the page: the things a definition sheet is *made of*. */
const PLACEABLE_TYPES = new Set(["COMPONENT", "COMPONENT_SET", "INSTANCE"]);

/**
 * How many nodes one page may carry. The server caps at 500 and drops the rest; refusing here as
 * well means the cache never carries a node the consumer will silently discard.
 */
const MAX_NODES = 500;

/**
 * How large one page's export may be before it is skipped rather than cached, in bytes.
 *
 * Importing every page changes the arithmetic here. The `Shape` sheet is ~0.8 MB, but the kit's
 * `Buttons` page carries a few thousand component nodes and `Examples` fourteen whole screens, and
 * the cache is *committed* — to this repo and then, on every regeneration, to the
 * `design-artifacts/wear-m3-catalog` delivery branch, whose history is append-only by design. A page
 * nobody can open (the server caps at 500 nodes, so a 3000-node sheet is mostly undrawable anyway)
 * is not worth tens of megabytes in two histories.
 *
 * A page over the cap is *skipped with a warning*, not fatal: with discovery on, an enormous sheet
 * is a fact about the design file, not a mistake in the config. Override with `maxSvgBytes`.
 */
const MAX_SVG_BYTES = 12 * 1024 * 1024;

/** A page id is a URL path segment on `/{system}/pages/{id}` — `ServeDesignPageStore.SAFE_ID`. */
const SAFE_ID = /^[A-Za-z0-9._-]{1,160}$/;

function arg(name, fallback) {
  const i = process.argv.indexOf(`--${name}`);
  return i >= 0 && process.argv[i + 1] ? process.argv[i + 1] : fallback;
}

const configPath = arg("config", "design-pages.json");
const designMapPath = arg("design-map", "design-map.json");
const onlyPage = arg("page", null);
const token = process.env.FIGMA_TOKEN;

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** `123-456` and `123:456` are the same node. Figma accepts both on input and answers with `:`. */
export function canonicalNodeId(id) {
  return String(id ?? "").replace(/-/g, ":");
}

/**
 * A route-safe page id from the page's own name — `Date & time pickers` → `date-time-pickers`.
 *
 * The id is the published URL (`/{system}/pages/{id}`) and the join key a pin uses, so it is
 * deliberately derived from the *name* rather than the node id: `55141:14175` tells a reader
 * nothing, and the names are what `docs/FIGMA_PAGES.md` is a table of. A name that slugs to nothing
 * — punctuation only, or an alphabet this regex does not survive — falls back to the node id with
 * its colon dashed, which is ugly but addressable, and never empty.
 */
export function slugForPage(name, nodeId) {
  const slug = String(name ?? "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 160)
    .replace(/-+$/g, "");
  if (slug !== "" && SAFE_ID.test(slug) && !/^\.{1,2}$/.test(slug) && !/\.svg$/i.test(slug)) {
    return slug;
  }
  return canonicalNodeId(nodeId).replace(/:/g, "-");
}

/** The ids the consumer refuses: not route-safe, a path segment, or shadowed by an export URL. */
function assertUsableId(id, where) {
  if (!SAFE_ID.test(id) || /^\.{1,2}$/.test(id) || /\.svg$/i.test(id)) {
    throw new Error(`${where} declares the id ${JSON.stringify(id)}, which the server will refuse`);
  }
}

/**
 * The pages to import: the file's own page list, with `pages` pins applied and `exclude` removed.
 *
 * `discovered` is what `GET /v1/files/:key?depth=1` returned (document order, which is the order
 * the designer put the tabs in — the most useful order for the published index, and a stable one to
 * diff the manifest against). `pins` is `design-pages.json`'s `pages` array: an entry there fixes
 * the id — and the name, if it gives one — for its node id, so a published URL never moves because
 * a designer renamed a tab. A pin whose node the file does not contain is kept and appended, so the
 * importer behaves exactly as it did before discovery existed when `discovered` is empty.
 *
 * `exclude` matches a node id or a page name (case-insensitively), because a human writing that
 * list has the names in front of them and the ids nowhere.
 */
export function resolvePages({ pins = [], discovered = [], exclude = [] } = {}) {
  const excludedIds = new Set();
  const excludedNames = new Set();
  for (const entry of exclude) {
    const text = String(entry ?? "").trim();
    if (text === "") continue;
    if (/^\d+[-:]\d+$/.test(text)) excludedIds.add(canonicalNodeId(text));
    else excludedNames.add(text.toLowerCase());
  }
  const isExcluded = (nodeId, name) =>
    excludedIds.has(canonicalNodeId(nodeId)) || excludedNames.has(String(name ?? "").toLowerCase());

  const pinsByNode = new Map();
  for (const pin of pins) {
    const nodeId = canonicalNodeId(pin?.nodeId);
    if (nodeId === "") throw new Error(`a pin in "pages" names no nodeId`);
    const id =
      typeof pin?.id === "string" && pin.id !== "" ? pin.id : slugForPage(pin?.name, nodeId);
    assertUsableId(id, `the pin for ${nodeId}`);
    if (pinsByNode.has(nodeId)) throw new Error(`"pages" pins ${nodeId} twice`);
    pinsByNode.set(nodeId, { id, nodeId, ...(pin?.name ? { name: String(pin.name) } : {}) });
  }

  const taken = new Set([...pinsByNode.values()].map((pin) => pin.id));
  const usedPins = new Set();
  const resolved = [];

  for (const page of discovered) {
    const nodeId = canonicalNodeId(page?.nodeId ?? page?.id);
    if (nodeId === "") continue;
    const name = String(page?.name ?? "");
    const pin = pinsByNode.get(nodeId);
    if (isExcluded(nodeId, pin?.name ?? name)) continue;
    if (pin) {
      usedPins.add(nodeId);
      // The pin fixes the id; the *name* still comes from the file unless the pin overrode it, so a
      // renamed page reads correctly in the index while keeping its published URL.
      resolved.push({ id: pin.id, nodeId, name: pin.name ?? name, pinned: true });
      continue;
    }
    let id = slugForPage(name, nodeId);
    if (taken.has(id)) {
      // Two tabs may share a name. Suffixing keeps both importable; the first one found keeps the
      // bare slug so an already-published URL is not the one that moves.
      let n = 2;
      while (taken.has(`${id}-${n}`)) n += 1;
      id = `${id}-${n}`;
    }
    taken.add(id);
    resolved.push({ id, nodeId, name });
  }

  for (const [nodeId, pin] of pinsByNode) {
    if (usedPins.has(nodeId) || isExcluded(nodeId, pin.name)) continue;
    resolved.push({ id: pin.id, nodeId, name: pin.name ?? pin.id, pinned: true });
  }
  return resolved;
}

/**
 * One REST call, with backoff on 429/5xx.
 *
 * Retrying matters more here than in `design-parity-pages list`: a page import is two calls plus an
 * asset download, and the asset host is a different origin with its own limits.
 */
async function get(url, { headers = {}, attempt = 0 } = {}) {
  const res = await fetch(url, { headers });
  if (res.ok) return res;
  const retryable = res.status === 429 || res.status >= 500;
  if (!retryable || attempt >= 4) {
    throw new Error(`${url.replace(/\?.*$/, "")} → HTTP ${res.status} ${await res.text()}`);
  }
  const after = Number(res.headers.get("retry-after"));
  await sleep(Number.isFinite(after) && after > 0 ? after * 1000 : 1000 * 2 ** attempt);
  return get(url, { headers, attempt: attempt + 1 });
}

async function figma(pathAndQuery) {
  const res = await get(`https://api.figma.com${pathAndQuery}`, {
    headers: { "X-Figma-Token": token },
  });
  return res.json();
}

/**
 * The variant slot a resolved `design-map.json` entry was tagged with, or `null` for the base one.
 *
 * `@design-parity/kit-index` tags each resolved variant with ONE of `state` / `size` / `theme`, and
 * which one it picks depends on the axis it matched — a button's sizes come back as `size`, an
 * interaction as `state`. The two sides of an entry are always tagged the same way, so the join
 * below only needs "the tag this entry carries", not a guess at which slot it should have been.
 *
 * Reading `state` alone is what this used to do, and it silently degraded every `size`-tagged
 * entry: no state, so every ref fell through to the component's base preview. Folding the 35-shape
 * `Shape Set` onto one component is what surfaced it — the shapes resolve as `size` (their axis is
 * neither an interaction nor a theme), so all 35 kit symbols would have swapped in the Circle
 * render, which is the same picture 35 times and reads as a working page.
 */
function variantSlot(tagged) {
  if (!tagged || typeof tagged === "string") return null;
  return tagged.state ?? tagged.size ?? tagged.theme ?? null;
}

/**
 * `design-map.json` keyed by design ref — the join this whole surface hangs on.
 *
 * The map is a projection of the `@CatalogComponent(reference = …)` annotations, so a page node
 * links to code exactly when some component named that node id. Both the scalar and the per-variant
 * array forms are read; a variant array contributes one entry per ref, each paired with the preview
 * id of the *same* variant, because that is the render a consumer will draw and pairing by position
 * alone would silently mismatch them.
 *
 * Exported for its own test: every failure mode here is quiet. A ref that pairs with the wrong
 * preview still renders something, and a page of 35 identical silhouettes looks like a page.
 */
export function indexDesignMap(map) {
  const byRef = new Map();
  for (const entry of map?.components ?? []) {
    const code = entry.code;
    if (typeof code !== "string" || code === "") continue;

    const previewsByVariant = new Map();
    let basePreview = null;
    for (const p of asArray(entry.previewId)) {
      if (typeof p === "string") basePreview ??= p;
      else if (p?.previewId) {
        const slot = variantSlot(p);
        if (slot) previewsByVariant.set(slot, p.previewId);
        else basePreview ??= p.previewId;
      }
    }
    for (const r of asArray(entry.ref)) {
      const ref = typeof r === "string" ? r : r?.ref;
      if (typeof ref !== "string" || ref === "") continue;
      const slot = variantSlot(r);
      const previewId = (slot && previewsByVariant.get(slot)) || basePreview || null;
      // First writer wins: two components naming one node is a mapping bug, and picking the later
      // one silently would make which of them shows depend on file order.
      if (!byRef.has(ref)) byRef.set(ref, { code, previewId });
    }
  }
  return byRef;
}

function readDesignMap(file) {
  try {
    return indexDesignMap(JSON.parse(readFileSync(file, "utf8")));
  } catch (error) {
    console.warn(`import-figma-pages: no usable ${file} (${error.message}); nothing will link`);
    return new Map();
  }
}

/** The pages already in the cache, so a scoped refresh adds to it rather than replacing it. */
function readCachedPages(outDir) {
  try {
    const cached = JSON.parse(readFileSync(path.join(outDir, "pages.json"), "utf8"));
    return Array.isArray(cached.pages) ? cached.pages : [];
  } catch {
    return [];
  }
}

function asArray(value) {
  if (value == null) return [];
  return Array.isArray(value) ? value : [value];
}

/** Every COMPONENT / COMPONENT_SET / INSTANCE under `node`, depth-first, with its nesting depth. */
export function collectNodes(node, depth = 0, out = []) {
  if (out.length >= MAX_NODES) return out;
  if (depth > 0 && PLACEABLE_TYPES.has(node.type)) {
    out.push({
      nodeId: canonicalNodeId(node.id),
      name: String(node.name ?? ""),
      depth,
      // The node's own type, which is what lets the consumer tell a CONTAINER from the components
      // inside it: a `COMPONENT_SET` is the box a family came in, its variants are the components,
      // and both are listed. `DesignPage.coverageGaps` reads this, and without it falls back to
      // inferring containment from nesting depth — an inference an unlisted frame between two
      // components can fool, since only components are listed. A fact is cheaper than a judgement.
      type: String(node.type ?? ""),
    });
    // A specimen's INSIDES are not specimens, whatever the node type says. Its children are that
    // other component's internals: they carry ids nothing in `design-map.json` can name — a
    // reference names a variant, never a part of one — so every last one of them publishes as "no
    // code behind this", and the page then paints red inside a node this catalog *does* implement.
    // The Switch sheet is the case that named this: each `Icon=True` variant carries an `Icon`
    // instance and each `State=Focused` variant a `Focus indicator`, so four of its red boxes sat
    // inside the enabled/disabled switches we implement, and ten more inside the states we don't.
    //
    // A COMPONENT_SET is the one exception, and the only one: its children ARE the variants — the
    // things a definition sheet is a grid of, and the things a reference points at — so the walk
    // continues through it and stops at each variant it finds.
    //
    // This was `node.type === "INSTANCE"` and so only held for instances, which let the walk
    // descend through a component set's variants (`COMPONENT`) into their internals: 736 of the
    // kit's 5,991 imported nodes were parts of a node already listed above them.
    if (node.type !== "COMPONENT_SET") return out;
  }
  for (const child of node.children ?? []) collectNodes(child, depth + 1, out);
  return out;
}

/**
 * The SVG's own coordinate space, read off its root element.
 *
 * This is the page's frame: the aspect ratio a consumer lays the stage out with, and the space
 * every `data-node-id` element is positioned in. Taken from the export rather than computed from
 * the node tree precisely so that the number a consumer draws with is the number the picture was
 * drawn at.
 */
function frameOf(svg) {
  const root = /<svg\b[^>]*>/i.exec(svg)?.[0] ?? "";
  const viewBox = /viewBox\s*=\s*"([^"]*)"/i.exec(root)?.[1];
  if (viewBox) {
    const parts = viewBox.trim().split(/[\s,]+/).map(Number);
    if (parts.length === 4 && parts.every(Number.isFinite) && parts[2] > 0 && parts[3] > 0) {
      return { width: parts[2], height: parts[3] };
    }
  }
  const width = Number(/\bwidth\s*=\s*"(\d+(?:\.\d+)?)"/i.exec(root)?.[1]);
  const height = Number(/\bheight\s*=\s*"(\d+(?:\.\d+)?)"/i.exec(root)?.[1]);
  if (width > 0 && height > 0) return { width, height };
  throw new Error("the exported SVG declares no usable viewBox or size");
}

/** How many `data-node-id` attributes the export actually carries — the check that matters most. */
function countNodeIds(svg) {
  return (svg.match(/\bdata-node-id\s*=/g) ?? []).length;
}

async function importPage(page, { fileKey, byRef, outDir, maxSvgBytes = MAX_SVG_BYTES }) {
  const nodeId = canonicalNodeId(page.nodeId);
  const encoded = encodeURIComponent(nodeId);

  const tree = await figma(`/v1/files/${fileKey}/nodes?ids=${encoded}`);
  const document = tree?.nodes?.[nodeId]?.document;
  if (!document) throw new Error(`node ${nodeId} is not in file ${fileKey}`);

  // `svg_include_node_id` is the reason this surface exists at all — without it the export is a
  // picture. `svg_outline_text` is left at its default (true): outlined text renders identically
  // everywhere, and a specimen sheet is mostly labels, so a font substitution on the consumer's box
  // would make the design half of a comparison wrong in exactly the way it is meant to be right.
  const images = await figma(
    `/v1/images/${fileKey}?ids=${encoded}&format=svg&svg_include_node_id=true`,
  );
  const url = images?.images?.[nodeId];
  if (typeof url !== "string" || url === "") {
    throw new Error(`Figma rendered no SVG for ${nodeId}: ${images?.err ?? "no url"}`);
  }
  const svg = await (await get(url)).text();
  if (!/^\s*<svg\b/i.test(svg)) throw new Error("the export did not start with an <svg> element");

  const bytes = Buffer.byteLength(svg, "utf8");
  if (bytes > maxSvgBytes) {
    // Skipped, not thrown: see MAX_SVG_BYTES. Reported at the same level as a successful page so
    // the run log says which sheets the cache does *not* carry, rather than leaving that to be
    // inferred from a shorter list.
    console.log(
      `${page.id}: SKIPPED — ${(bytes / 1024 / 1024).toFixed(1)} MB SVG exceeds the ` +
        `${(maxSvgBytes / 1024 / 1024).toFixed(0)} MB cap`,
    );
    return null;
  }

  const nodes = collectNodes(document).map((node) => {
    const ref = `figma:${fileKey}/${node.nodeId}`;
    const mapped = byRef.get(ref);
    return {
      ...node,
      ref,
      link: mapped ? "manifest" : "unlinked",
      ...(mapped?.code ? { code: mapped.code } : {}),
      ...(mapped?.previewId ? { previewId: mapped.previewId } : {}),
      ...(mapped ? { confidence: "high" } : {}),
    };
  });

  const id = page.id;
  writeFileSync(path.join(outDir, `${id}.svg`), svg);
  const linked = nodes.filter((n) => n.link !== "unlinked").length;
  console.log(
    `${id}: ${(svg.length / 1024).toFixed(0)} KB SVG, ${countNodeIds(svg)} addressable nodes, ` +
      `${linked}/${nodes.length} nodes linked` +
      // The kit's biggest sheets hold thousands of components, and both this walk and the server
      // stop at 500. Say so on the page it happens to: a truncated sheet still renders whole, and
      // the missing rows are only visible as an absence otherwise.
      (nodes.length >= MAX_NODES ? ` (truncated at the ${MAX_NODES}-node cap)` : ""),
  );

  return {
    id,
    name: String(page.name ?? document.name ?? id),
    nodeId,
    frame: frameOf(svg),
    image: { uri: `${id}.svg`, format: "svg" },
    // `nodes`, NOT `placements`. This is the contract's field name
    // (`DesignPage.nodes` in compose-ai-tools' `DesignPages.kt`, read by
    // `emit-design-pages.mjs`), and getting it wrong is silent: the page still
    // publishes, still renders its sheet, and simply has nothing addressable on
    // it — no outlines, no swap, no click-through. The first real import wrote
    // `placements` and produced exactly that.
    nodes,
  };
}

async function main() {
  if (!token) {
    console.error("FIGMA_TOKEN is not set. A read-only PAT with `file_content:read` is enough.");
    process.exit(1);
  }

  const config = JSON.parse(readFileSync(configPath, "utf8"));
  if (config.enabled !== true) {
    console.log(`import-figma-pages: ${configPath} is not enabled; nothing to do`);
    return;
  }
  const fileKey = config.fileKey;
  if (typeof fileKey !== "string" || fileKey === "") {
    console.error(`import-figma-pages: ${configPath} names no fileKey`);
    process.exit(2);
  }
  const outDir = path.resolve(config.outDir || "design/pages");
  const maxSvgBytes = Number.isFinite(config.maxSvgBytes) ? config.maxSvgBytes : MAX_SVG_BYTES;

  // One request for the whole page list — the same call `design-parity-pages list` makes. Only made
  // when the config asks for discovery, so a repo that wants a hand-kept list still costs two calls
  // per page and nothing else.
  let discovered = [];
  if (config.discover === true) {
    const doc = await figma(`/v1/files/${fileKey}?depth=1`);
    discovered = (doc?.document?.children ?? [])
      .filter((node) => node?.type === "CANVAS")
      .map((node) => ({ nodeId: canonicalNodeId(node.id), name: String(node.name ?? "") }));
    console.log(`import-figma-pages: the file declares ${discovered.length} page(s)`);
  }

  const resolved = resolvePages({
    pins: config.pages ?? [],
    discovered,
    exclude: config.exclude ?? [],
  });
  const wanted = resolved.filter((p) => !onlyPage || p.id === onlyPage);
  if (wanted.length === 0) {
    console.error(
      onlyPage
        ? `import-figma-pages: no page is called ${onlyPage}`
        : `import-figma-pages: ${configPath} declares no pages to import`,
    );
    process.exit(2);
  }

  const byRef = readDesignMap(designMapPath);
  mkdirSync(outDir, { recursive: true });

  const pages = [];
  const skipped = [];
  for (const page of wanted) {
    // A PINNED page's failure still fails the run: it is in the config because a human put it
    // there, and a broken node id must not quietly shrink the cache to nothing.
    //
    // A DISCOVERED page's failure is a skip. Discovery imports whatever the file happens to hold,
    // and the kit holds pages Figma itself declines to export — `/v1/images` answers with no url
    // for at least one of them. Aborting there would mean one unrenderable sheet costs the other
    // thirty their import, which is precisely the fragility discovery exists to remove.
    try {
      const imported = await importPage(page, { fileKey, byRef, outDir, maxSvgBytes });
      if (imported) pages.push(imported);
      else skipped.push(page.id);
    } catch (error) {
      if (page.pinned) throw error;
      console.log(`${page.id}: SKIPPED — ${error.message}`);
      skipped.push(page.id);
    }
  }
  // Every page failing is not a partial result, it is an outage — an expired token, a file that
  // moved, Figma down. Reporting "refreshed 0 page(s)" and exiting 0 would let the workflow commit
  // an emptied cache over a good one.
  if (pages.length === 0) {
    console.error(`import-figma-pages: no page imported (${skipped.length} skipped)`);
    process.exit(1);
  }

  // `--page` refreshes ONE page without discarding the rest of the cache. Rewriting the manifest
  // from just what this run fetched would silently delete the others' entries while leaving their
  // SVGs on disk — a cache that disagrees with itself. Order follows the resolved page list, so the
  // manifest diffs cleanly however the run was scoped.
  const merged = new Map(readCachedPages(outDir).map((page) => [page.id, page]));
  for (const page of pages) merged.set(page.id, page);
  // A page that has just been refused for its size must lose its cached entry *and* its export.
  // Keeping either would leave the cache advertising a sheet this run deliberately declined to
  // carry, and the delivery branch would publish the stale bytes forever.
  for (const id of skipped) {
    merged.delete(id);
    rmSync(path.join(outDir, `${id}.svg`), { force: true });
  }
  const ordered = resolved.map((p) => merged.get(p.id)).filter(Boolean);

  writeFileSync(
    path.join(outDir, "pages.json"),
    `${JSON.stringify({ version: PAGES_VERSION, source: "figma", fileKey, pages: ordered }, null, 2)}\n`,
  );
  console.log(
    `import-figma-pages: refreshed ${pages.length} of ${ordered.length} page(s) in ` +
      `${path.relative(".", outDir)}` +
      (skipped.length > 0 ? `; skipped ${skipped.length} (${skipped.join(", ")})` : ""),
  );
}

// Only when run as a script: `resolvePages` and `slugForPage` are pure and unit-tested
// (`import-figma-pages.test.mjs`), and importing this module must not start talking to Figma.
if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}
