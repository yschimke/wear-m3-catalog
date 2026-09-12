#!/usr/bin/env node
/**
 * Generate `samples-catalog/catalog.spec.json` — the `wear-m3-samples` inventory — from `sample-map.json`
 * and the vendored sources.
 *
 * ## Why the inventory is in the spec here, and annotations elsewhere
 *
 * `AGENTS.md` makes annotations the rule: the catalog inventory lives next to the `@Preview`s, and
 * growing mapping JSON is a signal of a missing annotation. That rule is about **this repository's
 * own code**. The samples are upstream's bytes under upstream's package, vendored from a pinned
 * commit and re-fetched byte-identically on every import — an `@CatalogComponent` written into one
 * would be destroyed by the next import, or would have to become a patch per sample, which is 300
 * patches carrying no fix.
 *
 * So the inventory is generated instead, and `catalog.spec.json` is where an IMPORTED project's
 * inventory belongs: the schema says `groups` is optional precisely because a first-party catalog
 * supplies it from annotations, and this is the other case. Generated and committed, with the
 * regenerate-and-diff contract `design-map.json` already carries here.
 *
 * ## What it produces
 *
 * One **group per Compose API** the samples demonstrate (`Button`, `NavigationBar`, …), read from
 * `sample-map.json`, which was itself read from the KDoc of the artifact this module compiles
 * against. One **component per sample function**, because each sample is its own call site and the
 * thing a reader came to see; a sample is not a state of another sample, so nothing folds as a
 * variant.
 *
 * A sample the map names but the vendored sources do not render is skipped and reported. That is
 * the ordinary case for the ~19 `@Sampled` functions carrying no `@Preview` upstream: discovery has
 * nothing to find, so a spec entry naming one would fail the publish gate rather than draw anything.
 *
 *     node scripts/samples-spec.mjs            # regenerate
 *     node scripts/samples-spec.mjs --check    # fail if the committed spec is stale
 */

import { existsSync, readdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const SAMPLE_MAP = "sample-map.json";
const VENDORED = "samples-catalog/src/main/kotlin/upstream";
const GENERATED = "samples-catalog/src/main/kotlin/generated/SamplePreviews.kt";
const SPEC = "samples-catalog/catalog.spec.json";
/** The kit catalog these samples are call sites for: the system name, and where its ids live. */
const KIT_SYSTEM = "wear-m3-catalog";
const KIT_SOURCES = ["catalog/src/commonMain/kotlin", "catalog/src/androidMain/kotlin"];

/**
 * Compose APIs whose samples belong to a kit family spelled differently.
 *
 * Deliberately EMPTY, and deliberately present. The join below is exact — a sample of `Button`
 * links to the `Button/…` family — which reaches 37 of the kit's 50 families. The rest are two
 * kinds. Some are first-party catalog material with no upstream API at all (`Auth`, `Media`, the
 * two demo tracks) and must never have an entry. The others are real one-to-many relationships
 * where the kit's taxonomy is its author's call and not this script's to guess: `Scaffold` could
 * claim `AppScaffold`, `ScreenScaffold` and both pager scaffolds, or the pager ones could belong to
 * `Pager`. `run` reports every unjoined API so those can be added deliberately, with the reasoning,
 * rather than inferred by string distance.
 */
export const API_TO_KIT_FAMILY = new Map([]);

/**
 * Every function in the vendored sources that is BOTH `@Sampled` and `@Preview`, mapped to the file
 * it lives in.
 *
 * Both halves matter. `@Sampled` is what makes it a sample rather than a helper the sample calls —
 * `FancyIndicator` sits beside `FancyIndicatorTabs` and is not itself a sample. `@Preview` is what
 * makes it renderable: discovery scans for that annotation, so a `@Sampled` function without one
 * draws nothing and must not reach the spec.
 */
export function renderableSamples(dir = VENDORED, generated = GENERATED) {
  const found = new Map();
  const walk = (d) => {
    for (const entry of readdirSync(d, { withFileTypes: true })) {
      const path = join(d, entry.name);
      if (entry.isDirectory()) {
        walk(path);
        continue;
      }
      if (!entry.name.endsWith(".kt")) continue;
      const text = readFileSync(path, "utf8");
      for (const match of text.matchAll(/((?:@\w+(?:\([^)]*\))?\s*)+)fun\s+(\w+)\s*\(/g)) {
        const [, annotations, fn] = match;
        if (!annotations.includes("@Sampled")) continue;
        if (!annotations.includes("@Preview")) continue;
        // Renders as itself: upstream annotated it.
        if (!found.has(fn)) found.set(fn, fn);
      }
    }
  };
  walk(dir);

  // …and the generated wrappers, which is how the other 136 reach the sheet. A wrapper is named
  // `<Sample>Preview` and is what discovery actually finds, so THAT is the name the spec's
  // `preview` field must carry — naming the sample itself would resolve to no @Preview at all and
  // fail the publish gate.
  if (existsSync(generated)) {
    const text = readFileSync(generated, "utf8");
    for (const match of text.matchAll(/fun\s+(\w+)Preview\(\)\s*=\s*[\w.]*\.(\w+)\(\)/g)) {
      const [, , sample] = match;
      if (!found.has(sample)) found.set(sample, `${sample}Preview`);
    }
  }
  return found;
}

/**
 * `family -> the kit component id declared FIRST in it`, read off `@CatalogComponent(id = …)`.
 *
 * A sample is about an API (`Button`); the kit splits an API into cells (`Button/Filled`,
 * `Button/Outlined`, …). A link has to name one, and the family's first-declared cell is the one a
 * catalog author reaches for first — it is the cell the section opens on.
 *
 * "First" is DETERMINISTIC rather than incidental: files in sorted path order, declarations in file
 * order. That makes it a convention rather than an accident, and the regenerate-and-diff `--check`
 * gate is what keeps it honest — reordering a section's components changes the committed spec and
 * shows up as a diff to review, instead of silently re-pointing every sample of that family.
 */
export function kitFirstCellByFamily(dirs = KIT_SOURCES) {
  const byFamily = new Map();
  const walk = (d) => {
    if (!existsSync(d)) return;
    for (const entry of readdirSync(d, { withFileTypes: true }).sort((a, b) =>
      a.name.localeCompare(b.name),
    )) {
      const path = join(d, entry.name);
      if (entry.isDirectory()) {
        walk(path);
        continue;
      }
      if (!entry.name.endsWith(".kt")) continue;
      const text = readFileSync(path, "utf8");
      for (const match of text.matchAll(/@CatalogComponent\s*\(([\s\S]*?)\)/g)) {
        const id = /\bid\s*=\s*"([^"]+)"/.exec(match[1])?.[1];
        // A family is the id's first segment. An id with no `/` is its own family and its own
        // first cell, which is the single-cell component's ordinary shape.
        if (!id) continue;
        const family = id.split("/")[0];
        if (!byFamily.has(family)) byFamily.set(family, id);
      }
    }
  };
  for (const dir of dirs) walk(dir);
  return byFamily;
}

/**
 * The kit component a sample of [api] is a call site for, or null when the kit has none.
 *
 * Null is the common answer and not a failure: 66 of the map's 103 APIs are ones this catalog
 * publishes no component for, and a sample of one is still worth publishing — it just has nothing
 * to link back to.
 */
export function kitComponentFor(api, firstCellByFamily) {
  const family = API_TO_KIT_FAMILY.get(api) ?? api;
  return firstCellByFamily.get(family) ?? null;
}

/** `sampleFunctionName -> api`, inverted from the map's `api -> samples[]`. */
export function apiBySample(map) {
  const byFunction = new Map();
  for (const { api, samples } of map) {
    for (const fqn of samples) {
      const fn = fqn.split(".").pop();
      // First API wins: a sample demonstrating several APIs is filed under the first that claims
      // it, deterministically, because the map is sorted. Grouping is presentation, not a claim of
      // exclusivity, and the map itself keeps the full relation.
      if (!byFunction.has(fn)) byFunction.set(fn, api);
    }
  }
  return byFunction;
}

/** Build the `groups` array: one group per API, one component per renderable sample. */
export function buildGroups(map, renderable, firstCellByFamily = new Map()) {
  const byApi = apiBySample(map);
  const groups = new Map();
  const unmapped = [];
  const unjoined = new Set();

  for (const [fn, previewFn] of [...renderable].sort((a, b) => a[0].localeCompare(b[0]))) {
    const api = byApi.get(fn);
    if (!api) {
      // Renderable, `@Sampled`, but no `@sample` tag points at it — upstream declared a sample that
      // no KDoc references. It still renders, and it is still a legitimate call site, so it is
      // published under the group its file implies rather than dropped.
      unmapped.push(fn);
    }
    const group = api ?? "Other";
    if (!groups.has(group)) groups.set(group, []);
    // The kit component this sample is a call site for. Declared HERE, on the generated side,
    // rather than on the kit component pointing back: this file is rewritten from `sample-map.json`
    // on every import, so the link cannot go stale, while the same statement written into the kit's
    // `@CatalogComponent` would be a hand-kept second copy of a map that moves whenever upstream
    // renames a sample. The server derives the other direction at read time.
    //
    // No `label`: the destination catalog's own name for the component is better than one invented
    // here, and an absent label is what tells the server to use it.
    const kitComponentId = api ? kitComponentFor(api, firstCellByFamily) : null;
    if (api && !kitComponentId) unjoined.add(api);
    groups.get(group).push({
      componentId: `${group}/${fn}`,
      preview: previewFn,
      caption: `${fn} — the sample \`${api ?? "upstream"}\`'s KDoc points at.`,
      ...(kitComponentId
        ? { related: [{ system: KIT_SYSTEM, componentId: kitComponentId }] }
        : {}),
    });
  }

  return {
    groups: [...groups.entries()]
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([name, components]) => ({ name, components })),
    unmapped,
    unjoined: [...unjoined].sort(),
  };
}

export function buildSpec(map, renderable, firstCellByFamily = new Map()) {
  const { groups, unmapped, unjoined } = buildGroups(map, renderable, firstCellByFamily);
  return {
    spec: {
      $schema:
        "https://raw.githubusercontent.com/yschimke/compose-ai-tools/main/scripts/design-artifacts/catalog.spec.schema.json",
      $comment:
        "GENERATED by scripts/samples-spec.mjs from sample-map.json and the vendored sources — do " +
        "not edit. The inventory is generated rather than annotated because the sources are " +
        "upstream's bytes, re-fetched byte-identically on every import: an @CatalogComponent " +
        "written into one would not survive it. See docs/design/ANDROIDX_SAMPLES.md.",
      system: "wear-m3-samples",
      title: "Wear Material 3 Samples",
      library: ["androidx.wear.compose:compose-material3"],
      module: ":samples-catalog",
      modes: ["light", "dark"],
      // NO `themes[]`, deliberately, and it used to be here.
      //
      // The block is for "themes an IMPORTED project has but cannot declare for itself", and the
      // reasoning was that the vendored sources are upstream's bytes and cannot carry an
      // annotation. True of `upstream/`, false of the module: a file outside it is first-party
      // code that survives the next import untouched. So the theme is declared where `:catalog`
      // declares its own -- `samples-catalog/src/main/kotlin/themes/SamplesTheme.kt`, a
      // `@WearThemeCatalog` provider -- and that file carries the full reasoning.
      //
      // Not a style preference: `generate-theme-catalogs.mjs` always emits the MOBILE
      // `@ThemeCatalog`, whose specimen composes `androidx.compose.material3.MaterialTheme`. A
      // Wear-only module has no such class, so the generated provider took the whole sheet down
      // with a NoClassDefFoundError on the first publish.
      // The kit catalog these samples are the call sites for. Pairing lands on CANONICAL — samples
      // publish no kit node — which is the right reading: the kit cell beside how you call it.
      compareWith: { system: "wear-m3-catalog", spec: "../catalog.spec.json" },
      groups,
    },
    unmapped,
    unjoined,
  };
}

function main(argv) {
  const map = JSON.parse(readFileSync(SAMPLE_MAP, "utf8"));
  const renderable = renderableSamples();
  const { spec, unmapped, unjoined } = buildSpec(map, renderable, kitFirstCellByFamily());
  const json = `${JSON.stringify(spec, null, 2)}\n`;

  const components = spec.groups.reduce((n, g) => n + g.components.length, 0);
  const mapped = new Set(map.flatMap((e) => e.samples.map((s) => s.split(".").pop())));
  const notRenderable = [...mapped].filter((fn) => !renderable.has(fn)).sort();

  if (argv.includes("--check")) {
    if (readFileSync(SPEC, "utf8") !== json) {
      console.error(`${SPEC} is stale: regenerate it with \`node scripts/samples-spec.mjs\`.`);
      process.exit(1);
    }
    console.log(`${SPEC} is current (${components} component(s) in ${spec.groups.length} group(s)).`);
    return;
  }

  writeFileSync(SPEC, json);
  console.log(`${SPEC}: ${components} component(s) in ${spec.groups.length} group(s).`);
  if (unmapped.length > 0) {
    console.log(`  ${unmapped.length} renderable sample(s) no @sample tag points at, filed under Other.`);
  }
  const linked = spec.groups.reduce(
    (n, g) => n + g.components.filter((c) => c.related).length,
    0,
  );
  console.log(`  ${linked} component(s) link back to a ${KIT_SYSTEM} component.`);
  if (unjoined.length > 0) {
    // Reported every run, never inferred. Most of these are APIs this catalog publishes no
    // component for, which is the ordinary case; the few that are a kit family under another name
    // belong in `API_TO_KIT_FAMILY`, added by someone who knows the taxonomy.
    console.log(
      `  ${unjoined.length} API(s) reach no kit family: ` +
        `${unjoined.slice(0, 8).join(", ")}${unjoined.length > 8 ? " …" : ""}`,
    );
  }
  if (notRenderable.length > 0) {
    console.log(
      `  ${notRenderable.length} mapped sample(s) carry no @Preview upstream and are not published: ` +
        `${notRenderable.slice(0, 8).join(", ")}${notRenderable.length > 8 ? " …" : ""}`,
    );
  }
}

if (process.argv[1] && process.argv[1].endsWith("samples-spec.mjs")) main(process.argv.slice(2));
