#!/usr/bin/env node
// Assemble the `design-artifacts/wear-m3-typed-pilot` delivery tree from the typed pilot's own
// build outputs, in the layout a preview server reads from a production catalog branch:
//
//   catalog.json                 the manifest: componentsFile, uiBuilderFile, uiBuilderRuntime, …
//   components.json              the pilot's component record (generateTypedCatalog)
//   ui-builder.json              the pilot's builder catalog, catalog id `wear-m3-typed-pilot`
//   ui-builder.guidelines.json   the production guidelines, re-addressed to the pilot's catalog id
//   ui-builder/designs/*.json    the pilot's templates, already pinned to the pilot catalog
//   ui-builder/runtime.zip       the pilot runtime (rendererArchive), verified before it is copied
//   images/<case>/typed.png      the typed renderer's JVM parity evidence, as baked stickers
//   readiness.json               the readiness report after the browser runtime check
//
// The pilot is not a rendered catalog, so it does not go through the reusable
// design-artifacts workflow's render pipeline. Every file the server reads is written by the
// export driver's own publishers (the same `@design-parity/export-driver` that workflow runs), so
// the copy, template containment, guidelines check and runtime integrity check are the
// production ones rather than a second implementation of them.
//
// The baked stickers are not decoration. A server stages a catalog's executable runtime only when
// it loads that catalog's generation, and it refuses to load one with no usable images; without
// them the branch would publish a runtime no server ever serves.
//
//   node scripts/assemble-typed-pilot-delivery.mjs --driver <export-driver dir> --out <dir>
//     [--pair catalog-typed-adapters/build/catalog]
//     [--runtime catalog-typed-adapters/build/distributions/wear-m3-typed-pilot-renderer.zip]
//     [--evidence catalog-typed-adapters/build/render-evidence/parity]
//     [--guidelines ui-builder.guidelines.json]
//     [--readiness catalog-typed-adapters/build/browser-evidence/readiness.json]
import { mkdir, readdir, readFile, rm, writeFile, copyFile } from "node:fs/promises";
import { join, resolve, relative, sep } from "node:path";
import { pathToFileURL } from "node:url";
import { parseArgs } from "node:util";

export const PILOT_CATALOG_ID = "wear-m3-typed-pilot";
const COMPONENT_RECORDS = { adapter: "compose-component-records", version: 1 };

const root = resolve(new URL("..", import.meta.url).pathname);
const { values } = parseArgs({
  options: {
    driver: { type: "string" },
    out: { type: "string" },
    pair: { type: "string", default: join(root, "catalog-typed-adapters/build/catalog") },
    runtime: {
      type: "string",
      default: join(
        root,
        "catalog-typed-adapters/build/distributions/wear-m3-typed-pilot-renderer.zip",
      ),
    },
    evidence: {
      type: "string",
      default: join(root, "catalog-typed-adapters/build/render-evidence/parity"),
    },
    guidelines: { type: "string", default: join(root, "ui-builder.guidelines.json") },
    readiness: {
      type: "string",
      default: join(root, "catalog-typed-adapters/build/browser-evidence/readiness.json"),
    },
  },
});
if (!values.driver || !values.out) {
  console.error("usage: assemble-typed-pilot-delivery.mjs --driver <dir> --out <dir> [options]");
  process.exit(2);
}

const driver = (file) => import(pathToFileURL(join(resolve(values.driver), file)).href);
const { publishComponentRecord } = await driver("catalog-component-record.mjs");
const { publishUiBuilderCatalog, uiBuilderGuidelinesManifestFields, uiBuilderGuidelinesWarning } =
  await driver("catalog-ui-builder.mjs");
const { publishUiBuilderRuntime } = await driver("catalog-ui-builder-runtime.mjs");

const pair = resolve(values.pair);
const out = resolve(values.out);
await rm(out, { recursive: true, force: true });
await mkdir(out, { recursive: true });

function fail(message) {
  console.error(`::error title=Typed pilot delivery::${message}`);
  process.exit(1);
}

// Every regular file under the pair, keyed by its pair-relative path: the bundle-entry shape the
// driver's publishers read. Generated Kotlin fixtures are review evidence, not delivery.
async function entriesOf(dir) {
  const entries = {};
  async function walk(current) {
    for (const entry of await readdir(current, { withFileTypes: true })) {
      const path = join(current, entry.name);
      const key = relative(dir, path).split(sep).join("/");
      if (key === "generated-source") continue;
      if (entry.isDirectory()) await walk(path);
      else if (entry.isFile()) entries[key] = await readFile(path);
    }
  }
  await walk(dir);
  return entries;
}
const entries = await entriesOf(pair);

// The two facts the A/B comparison rests on. A pilot published as `wear-m3`, or one exporting
// through the production adapter, would be a second production catalog rather than a trial.
const builder = JSON.parse(Buffer.from(entries["ui-builder.json"] ?? "null").toString("utf8"));
if (builder?.catalog?.id !== PILOT_CATALOG_ID)
  fail(`ui-builder.json names catalog '${builder?.catalog?.id}', not '${PILOT_CATALOG_ID}'`);
const exportAdapter = builder.statusSemantics?.composeSourceExport;
if (
  exportAdapter?.adapter !== COMPONENT_RECORDS.adapter ||
  exportAdapter?.version !== COMPONENT_RECORDS.version
)
  fail(`ui-builder.json composeSourceExport is ${JSON.stringify(exportAdapter)}`);

// The production guidelines, re-addressed: a host accepts guidelines only for the catalog id they
// name, and the pilot's designs are the same Wear screens the production rules were written for.
const guidelines = JSON.parse(await readFile(resolve(values.guidelines), "utf8"));
entries["ui-builder.guidelines.json"] = Buffer.from(
  JSON.stringify({ ...guidelines, catalog: PILOT_CATALOG_ID }, null, 2) + "\n",
);

const record = await publishComponentRecord(entries, out);
if (!record) fail("components.json is missing or is not a component record");
const published = await publishUiBuilderCatalog(entries, out);
if (!published) fail("ui-builder.json is not a builder catalog");
for (const [kind, paths] of [
  ["missing", published.missingTemplates],
  ["unsafe", published.unsafeTemplates],
  ["unreadable", published.unreadableTemplates],
]) {
  if (paths.length > 0) fail(`${kind} template(s): ${paths.join(", ")}`);
}
const guidelinesWarning = uiBuilderGuidelinesWarning(published, PILOT_CATALOG_ID);
if (guidelinesWarning) fail(guidelinesWarning);
const runtime = await publishUiBuilderRuntime(resolve(values.runtime), out);
if (!runtime.runtimeId.startsWith(`${PILOT_CATALOG_ID}-`))
  fail(`runtime id '${runtime.runtimeId}' is not a pilot runtime id`);
// The readiness report AFTER the browser check (verify-typed-wear-runtime.mjs), not the
// generator's: only that one records the packaged runtime as exercised. Required, and required to
// name the runtime being published, so the branch never claims a verification of other bytes.
let readiness;
try {
  readiness = JSON.parse(await readFile(resolve(values.readiness), "utf8"));
} catch (error) {
  fail(`no browser-verified readiness report at ${values.readiness} (${error.message})`);
}
const verified = readiness.browserVerification;
if (
  verified?.runtimeId !== runtime.runtimeId ||
  verified?.integritySha256 !== runtime.integritySha256
)
  fail(
    `readiness.json verified runtime ${verified?.runtimeId}/${verified?.integritySha256}, ` +
      `not the published ${runtime.runtimeId}/${runtime.integritySha256}`,
  );
await writeFile(join(out, "readiness.json"), JSON.stringify(readiness, null, 2) + "\n");

// The typed side of each JVM parity case, one sticker per case. The production side is already
// served by `wear-m3-catalog`; carrying it twice would only make the two catalogs harder to tell
// apart.
const slugOf = (value) =>
  value
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
const evidence = resolve(values.evidence);
const cases = (await readdir(evidence))
  .filter((name) => name.endsWith("-typed.png"))
  .sort();
const components = [];
const slugs = new Set();
for (const name of cases) {
  const parityCase = name.slice(0, -"-typed.png".length);
  let slug = slugOf(parityCase) || "case";
  for (let n = 2; slugs.has(slug); n++) slug = `${slugOf(parityCase)}-${n}`;
  slugs.add(slug);
  const path = `images/${slug}/typed.png`;
  await mkdir(join(out, "images", slug), { recursive: true });
  await copyFile(join(evidence, name), join(out, path));
  const png = await readFile(join(evidence, name));
  components.push({
    componentId: `typed-pilot/${slug}`,
    caption: `Typed renderer, parity case ${parityCase}`,
    section: "Typed pilot",
    group: "JVM parity",
    images: [
      {
        variant: "typed",
        path,
        state: "default",
        // IHDR: width and height are the big-endian words at 16 and 20.
        width: png.readUInt32BE(16),
        height: png.readUInt32BE(20),
      },
    ],
  });
}
if (components.length === 0)
  fail(`no *-typed.png parity evidence under ${evidence}; a server would refuse the catalog`);

// No `source` and no live bundle: either would offer trusted server-side re-render, and the pilot's
// stickers are evidence from its own JVM test, not previews a server can rebuild.
const manifest = {
  schema: "design-parity-catalog/v1",
  system: PILOT_CATALOG_ID,
  title: builder.catalog.title ?? PILOT_CATALOG_ID,
  components,
  renderer: "wear-m3-catalog catalog-typed-adapters",
  generatedAt: new Date().toISOString(),
  componentsFile: record.path,
  uiBuilderFile: published.path,
  ...uiBuilderGuidelinesManifestFields(published),
  uiBuilderRuntime: runtime,
};
await writeFile(join(out, "catalog.json"), JSON.stringify(manifest, null, 2) + "\n");
console.log(
  `typed pilot delivery: ${record.components} record component(s), ${published.components} ` +
    `builder component(s), ${published.templates.length} template(s), runtime ` +
    `${runtime.runtimeId}, ${components.length} sticker(s) -> ${out}`,
);
