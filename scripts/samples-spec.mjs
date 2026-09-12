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
const GENERATED =
  "samples-catalog/src/main/kotlin/ee/schimke/wearm3catalog/samples/SamplePreviews.kt";
const SPEC = "samples-catalog/catalog.spec.json";
/** The kit catalog these samples are call sites for: the system name, and where its ids live. */
const KIT_SYSTEM = "wear-m3-catalog";
const KIT_SOURCES = ["catalog/src/commonMain/kotlin", "catalog/src/androidMain/kotlin"];

/**
 * Compose APIs whose samples belong to a kit component this catalog spells differently.
 *
 * A value is a **kit lookup key** in the sense [kitCellIndex] defines: a full cell id
 * (`Button/Outlined`) links that cell, a bare family name (`CheckboxButton`) links the family's
 * first-declared cell. Both shapes are in use below and neither is a fallback for the other.
 *
 * Most entries name a cell rather than a family, and that is the point of the two shapes. These
 * APIs are *variants* — `OutlinedButton` is a Wear API in its own right, and the kit models it as
 * `Button/Outlined` inside the `Button` section. Mapping it to the family `Button` would resolve to
 * `Button/Filled`: a confidently wrong destination, worse than the honest nothing an unmapped API
 * produces. The family shape is right only where the kit publishes ONE cell for the whole API
 * (`CheckboxButton`), so first-cell and only-cell are the same thing.
 *
 * A mapped value that resolves to no cell THROWS rather than falling through to null. An unmapped
 * API reaching nothing is the ordinary case; a hand-written override reaching nothing is a typo or
 * a cell someone renamed, and the two must not fail the same way.
 *
 * What is deliberately NOT here: APIs with no kit component at all (`AppScaffold`,
 * `SurfaceTransformation`), APIs that are not components (`scrollAway`, `rememberTransformationSpec`,
 * every `Local*` and threshold constant), and `animatedShapes` / `curvedText` — the first because
 * its samples span `IconButton` and `TextButton` with no one subject, the second because the kit
 * publishes no curved-text cell. Those reach nothing correctly, and `run` lists them on every
 * regeneration so the set stays reviewed rather than assumed.
 */
export const API_TO_KIT_COMPONENT = new Map([
  // Variants the kit models as cells of a larger section.
  ["OutlinedButton", "Button/Outlined"],
  ["FilledTonalButton", "Button/Tonal"],
  ["ChildButton", "Button/Child"],
  ["FilledIconButton", "IconButton/Filled"],
  ["FilledTonalIconButton", "IconButton/Tonal"],
  ["OutlinedIconButton", "IconButton/Outlined"],
  ["OutlinedCard", "Card/Outlined"],
  ["SuccessConfirmationDialog", "ConfirmationDialog/Success"],
  ["FailureConfirmationDialog", "ConfirmationDialog/Failure"],
  ["VerticalPageIndicator", "PageIndicator/Vertical"],
  // Colour factories whose single sample renders one named cell — the API is not a composable, but
  // the sample is unambiguously a call site for that cell.
  ["filledVariantButtonColors", "Button/FilledVariant"],
  ["filledVariantIconButtonColors", "IconButton/FilledVariant"],
  // …and the ones whose cell is the whole family, so the family shape says it more plainly.
  ["filledTextButtonColors", "TextButton"],
  ["filledTonalTextButtonColors", "TextButton"],
  ["filledVariantTextButtonColors", "TextButton"],
  ["outlinedTextButtonColors", "TextButton"],
  ["variantSliderColors", "Slider"],
  // Split* is the kit's same cell with a second tap target; it publishes no separate one.
  ["SplitCheckboxButton", "CheckboxButton"],
  ["SplitRadioButton", "RadioButton"],
  ["SplitSwitchButton", "SwitchButton"],
  // `*Content` is the card's content slot, demonstrated on the card itself.
  ["AppCardContent", "AppCard"],
  ["TitleCardContent", "TitleCard"],
  // The placeholder FEATURE rather than one of its three cells: `placeholderShimmer`'s samples span
  // a button and a text, so the family's first cell is the honest "open the section here".
  ["placeholder", "Placeholder"],
  ["placeholderShimmer", "Placeholder"],
]);

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

/** The module the vendored sources belong to, stripped to make a path module-relative. */
const MODULE_PREFIX = "samples-catalog/";

/**
 * `sampleFunctionName -> { sourceFile, bodyLine }` for every `@Sampled` function in the vendored
 * tree, as a MODULE-relative path and the line its `fun` is declared on.
 *
 * This is what the component's `sourceFile` / `bodyLine` are declared from, and the reason they are
 * declared at all. 115 of this catalog's 150 previews are generated wrappers —
 * `fun ButtonSamplePreview() = androidx.wear…ButtonSample()` — so discovery correctly records the
 * generated file as the preview's source, and a reader opening the Source panel gets three lines of
 * delegation instead of the sample. The catalog is the only party that knows which sample a wrapper
 * it generated stands for, so the catalog says so.
 *
 * Declared for the upstream-annotated samples too, where it agrees with discovery. Stating it
 * uniformly costs one line each and keeps the spec readable as "every component points at its
 * sample", rather than as a list with 34 silent exceptions whose silence means something.
 *
 * `@Sampled` and not `@Preview`: this indexes where a sample is DECLARED, which is a fact about the
 * vendored source, while renderability is a fact about what discovery can invoke.
 */
export function sampleSources(dir = VENDORED) {
  const found = new Map();
  const walk = (d) => {
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
      const sourceFile = path.startsWith(MODULE_PREFIX)
        ? path.slice(MODULE_PREFIX.length)
        : path;
      // Line numbers are 1-based and counted over the WHOLE file, so the offset has to come from
      // the match index rather than from a per-match line walk.
      for (const match of text.matchAll(/((?:@\w+(?:\([^)]*\))?\s*)+)fun\s+(?:<[^>]*>\s*)?(\w+)\s*\(/g)) {
        const [, annotations, fn] = match;
        if (!annotations.includes("@Sampled")) continue;
        // The `fun` keyword, not the first annotation: the panel should open on the declaration a
        // reader recognises, with the annotations above it as context rather than as the subject.
        const funAt = match.index + annotations.length;
        const bodyLine = text.slice(0, funAt).split("\n").length;
        if (!found.has(fn)) found.set(fn, { sourceFile, bodyLine });
      }
    }
  };
  walk(dir);
  return found;
}

/**
 * `kit lookup key -> the cell id it names`, read off `@CatalogComponent(id = …)`.
 *
 * TWO kinds of key, because a link wants to name either granularity:
 *
 *  - a **family** (`Button`) maps to the cell declared FIRST in it (`Button/Filled`) — the cell the
 *    section opens on, and the right destination for a sample about the API in general;
 *  - a **full cell id** (`Button/Outlined`) maps to itself — the right destination for a sample
 *    about that one variant, which the family shape cannot express.
 *
 * A single-cell component (`CheckboxButton`) is both keys at once and resolves identically either
 * way, so the two shapes never disagree.
 *
 * "First" is DETERMINISTIC rather than incidental: files in sorted path order, declarations in file
 * order. That makes it a convention rather than an accident, and the regenerate-and-diff `--check`
 * gate is what keeps it honest — reordering a section's components changes the committed spec and
 * shows up as a diff to review, instead of silently re-pointing every sample of that family.
 */
export function kitCellIndex(dirs = KIT_SOURCES) {
  const index = new Map();
  // Which families have already taken their first cell. A separate set rather than `index.has`,
  // because a single-cell component writes its id and its family name as the SAME key — so asking
  // the index "is this family assigned?" cannot tell that apart from the cell key it just wrote.
  const familyAssigned = new Set();
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
        if (!familyAssigned.has(family)) {
          familyAssigned.add(family);
          index.set(family, id);
        }
        // The cell under its own id too, so a `Button/Outlined` override resolves without a second
        // structure to keep in step with this one. After the family, so a bare id that IS its
        // family ends up mapped to itself either way.
        index.set(id, id);
      }
    }
  };
  for (const dir of dirs) walk(dir);
  return index;
}

/**
 * The kit component a sample of [api] is a call site for, or null when the kit has none.
 *
 * Null is the common answer and not a failure: most of the map's APIs are ones this catalog
 * publishes no component for, and a sample of one is still worth publishing — it just has nothing
 * to link back to.
 *
 * An override from [API_TO_KIT_COMPONENT] that resolves to nothing THROWS instead. Someone wrote
 * that key by hand against a cell they had read; if it no longer resolves, the cell was renamed or
 * mistyped, and degrading to "this sample has no kit component" would hide a broken link behind
 * the same silence as the ordinary case.
 */
export function kitComponentFor(api, cellIndex) {
  const override = API_TO_KIT_COMPONENT.get(api);
  if (override !== undefined) {
    const resolved = cellIndex.get(override);
    if (!resolved) {
      throw new Error(
        `API_TO_KIT_COMPONENT maps ${api} to "${override}", which this kit declares no cell for. ` +
          `A value is either a full @CatalogComponent id or a family name; check the id in ` +
          `${KIT_SOURCES.join(", ")} and fix the entry rather than dropping it.`,
      );
    }
    return resolved;
  }
  return cellIndex.get(api) ?? null;
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
export function buildGroups(map, renderable, cellIndex = new Map(), sources = new Map()) {
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
    const kitComponentId = api ? kitComponentFor(api, cellIndex) : null;
    if (api && !kitComponentId) unjoined.add(api);
    // Where a reader should be sent for this component's source. The `@Preview` is in the generated
    // wrapper for 115 of these, and the wrapper is machinery; `apply-source-files.mjs` takes a
    // declared path in preference to the one discovery recorded, which is what makes the Source
    // panel show the sample rather than its delegation.
    const source = sources.get(fn);
    groups.get(group).push({
      componentId: `${group}/${fn}`,
      preview: previewFn,
      caption: `${fn} — the sample \`${api ?? "upstream"}\`'s KDoc points at.`,
      ...(source ? { sourceFile: source.sourceFile, bodyLine: source.bodyLine } : {}),
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

export function buildSpec(map, renderable, cellIndex = new Map(), sources = new Map()) {
  const { groups, unmapped, unjoined } = buildGroups(map, renderable, cellIndex, sources);
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
      // WHAT KIND of catalog this is, which the preview server reads to shape the pages: a catalog
      // of CALL SITES drops every comparison lane — a sample is not a rendition of a reference, so
      // a difference between the two is not a defect and offering the lane would invite a reader to
      // read it as one — and stands the source beside the render instead of behind a chip, because
      // here the code is what the page is for. It also names this catalog in the kit's back-links,
      // so a component points at "Samples" rather than at this sheet's title.
      //
      // Declared, because only the catalog knows. The server may not infer it from a system name:
      // which catalogs exist is a deployment's business, and `ui-builder-catalog-literals.sh` there
      // exists to keep that knowledge out of its Kotlin.
      display: { role: "samples" },
      // NO `themes[]`, deliberately, and it used to be here.
      //
      // The block is for "themes an IMPORTED project has but cannot declare for itself", and the
      // reasoning was that the vendored sources are upstream's bytes and cannot carry an
      // annotation. True of `upstream/`, false of the module: a file outside it is first-party
      // code that survives the next import untouched. So the theme is declared where `:catalog`
      // declares its own -- `samples-catalog/src/main/kotlin/ee/schimke/wearm3catalog/samples/SamplesTheme.kt`, a
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
  const { spec, unmapped, unjoined } = buildSpec(map, renderable, kitCellIndex(), sampleSources());
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
    // belong in `API_TO_KIT_COMPONENT`, added by someone who knows the taxonomy.
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
