# M3 Wear OS Apps Design Kit — as code

The [M3 Wear OS Apps Design Kit][kit] rebuilt as **Jetpack Compose `@Preview`s**, published as an
importable design catalog. The Wear-side sibling of [yschimke/m3-catalog][m3], and the same
posture: the kit is the source of truth, the code is what moves.

**The Figma kit is the source of truth.** A divergence between the two is a bug in this code, and
the code is what changes — that is what `direction: "design-led"` in
[`.design-parity.json`](.design-parity.json) says. That is the opposite of the `wear-m3` catalog in
compose-ai-tools, which publishes a system whose own render is authoritative; this one exists to
*reproduce* a published kit. The direction has teeth beyond reporting: design-parity's
Code-to-Canvas push-back is gated on `code-led`, so `design-led` makes writing back to the Figma
file structurally impossible rather than merely forbidden by convention.

**Nothing in this repo writes to Figma.** Every Figma interaction is read-only: the REST API for
node ids and reference images, and the MCP server for variables and metadata.

- **Browse it:** the published catalog is served at `preview.coo.ee/wear-m3-catalog/`.
- **Import it:** the generated bundle lives on the `design-artifacts/wear-m3-catalog` branch —
  `catalog.json` (the inventory), raster `images/`, `code-connect.json`, and a browsable
  `index.html`. Regenerated from the code on every change.

[kit]: https://www.figma.com/design/B24oss2tTeXAFykyeyusz0/M3-Wear-OS-Apps-Design-Kit--Community-
[m3]: https://github.com/yschimke/m3-catalog

## Status

**Every published set in the kit is accounted for.** 33 of the kit's 42 published component sets are
reproduced by a catalog component; the other 9 are excluded, each with a stated reason.
[`kit-sets.json`](kit-sets.json) is that record — one row per set, carrying either the components
that reproduce it or why it is absent — and `CatalogKitCoverageTest` holds it to the annotations in
both directions, so a set cannot be quietly dropped and an exclusion cannot outlive the limitation
that earned it.

Four components enter through the **library's** door instead — real Wear Compose Material 3
components the kit never published, each carrying `noReference` with the reason: `ButtonGroup`,
`ArcProgressIndicator`, `TransformingLazyColumn` and `Scaffold`. A sheet whose reader is looking for
the component set should not omit a component because a design file did.

What is excluded, and why:

| Kit set | Why |
| --- | --- |
| `Button-ImageBackground-Round` | Compose puts the image container painter on `Button` and `Card`; `IconButton` takes no painter |
| `Media-Player` | Wear Compose publishes no media player — the kit's set is a composition an app assembles |
| the six `Avatar-*` components | avatars are app content; the kit draws the shapes an app fills, and there is no composable to invoke |
| `Confirmation-Overlay` | `ConfirmationDialogContent` animates its children in from `alpha = 0`; the renderer pauses the clock, so a still capture is an empty ring. Back in when a capture can settle first |

Out of scope and not listed: the kit's own internals (names beginning `.`, and the `Base
components` each page builds its published set from) and the 1072-component **Icons** page, which is
an icon set rather than a component inventory.

See [`AGENTS.md`](AGENTS.md) for the conventions any addition has to hold.

## Annotation-first, by design

The catalog's whole inventory — sections, groups, component ids, captions and variants — lives in
**annotations next to the composables**. There is no hand-maintained JSON mapping components to
previews: a name-keyed mapping file drifts the moment a preview is renamed, and it fails silently
(the render succeeds; the sticker just never appears).

```kotlin
@file:CatalogGroup(name = "Shapes", section = "Styles")

@CatalogComponent(
  id = "Shape/MaterialShapes",
  reference = "figma:B24oss2tTeXAFykyeyusz0/42284:176650",
  caption = "The expressive shape library, with each named shape folded in as a variant.",
)
@CatalogModes
@OverrideVariant(name = "square", strings = ["shape=square"])
@Composable
fun MaterialShapesSticker() = Sticker { /* … */ }
```

[`catalog.spec.json`](catalog.spec.json) carries only cover-sheet fields the code has no opinion
about: the system slug, title, primary modes, the round-size breakpoints and the front-door hero.

## Android, not desktop — and why

The phone catalog is a Compose **Multiplatform desktop** module, which is what lets the preview
server hold a live Compose session over Skiko. This one cannot be:
`androidx.wear.compose:compose-material3` ships only for Android. So `:catalog` is an Android
application module and the render goes through **Robolectric**, the same lane the `wear-m3` catalog
in compose-ai-tools uses — and the live re-render lane needs a serve host carrying the Android
daemon rather than the desktop one.

One dependency deserves naming: the module pulls in **mobile** `androidx.compose.material3` for
`MaterialShapes` and `RoundedPolygon.toShape()` alone. The kit's Shapes page publishes the 35
expressive shapes and Wear Compose names none of them — `androidx.wear.compose.material3` ships
corner radii and a morph shape, and `androidx.graphics:graphics-shapes` ships only the primitives
they are built from. `MaterialShapes` is plain `RoundedPolygon` data, so the specimen sheet draws
Material's own finished polygons rather than this repo's arithmetic, which is the point of a
design-led catalog.

## Dark-first

Wear draws its components on a black watch face, so a sticker is a **single dark capture** with a
transparent background rather than the light/dark pair the phone catalog publishes
(`@CatalogModes`, `showBackground = false`). `catalog.spec.json` says `modes: ["dark"]` and
`display.surface: "dark"`, so the preview server's front door stages the hero on dark too.

That single mode has one consequence worth knowing about before you go looking for a parity report:
see [`docs/DESIGN_MAP.md`](docs/DESIGN_MAP.md).

## Themes

The kit publishes one theme — the stock Wear M3 dark palette every sticker on this sheet is drawn
in. Six more are declared as `@WearThemeCatalog` providers in
[`CatalogThemes.kt`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogThemes.kt), which puts
them in the preview server's **Theme** select (any sticker re-renders under any of them) and bakes
one specimen sheet per theme showing the Wear roles and type scale it resolves to.

Five are [Confetti Wear](https://github.com/joreilly/Confetti)'s: its stock theme plus the four
curated conference identities — KotlinConf, AndroidMakers, Droidcon, DevFest. They are built the way
Confetti builds them, a seed colour through `materialkolor`'s dynamic dark scheme mapped onto the
Wear roles, rather than transcribed as a table of resolved hex values that would drift the first
time either side moved. Each carries Confetti's typography too: a theme is a typeface pairing as
much as a palette, and KotlinConf's JetBrains Mono titles over an Inter body are as much of that
identity as the purple.

The sixth is not Confetti's. It is the stock Wear palette with only the type scale re-pointed at
**Google Sans Flex**, so a side-by-side against an un-themed sticker reads as a pure type comparison
rather than a type *and* colour change.

These answer to no kit node and are not inventory — membership is still the kit's call. Every
typeface resolves as a downloadable Google font, so no TTF is vendored here.

These are why a sticker frame installs its theme through `CatalogMaterialTheme` rather than a bare
`MaterialTheme { … }`: a provider wraps the sticker from the outside, and an inner theme would
shadow it. That failure is silent and convincing — every entry in the switcher renders identical
pixels — so the frame stands down when a provider has already installed one.

## Motion

Four recordings live in
[`Motion.kt`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/sections/Motion.kt), published as
GIFs beside the sticker sheet: the indeterminate progress ring, the switch thumb travelling, the
toggle button's shape morph, and swipe-to-reveal revealing. They carry no `@CatalogComponent` — a
recording is not a component, and membership is still the kit's call.

They are driven by the component's own animation or by a `LaunchedEffect` state change rather than
by a scripted tap: `@InteractionPreview` is implemented in the desktop renderer only, and this is an
Android module. See [`AGENTS.md`](AGENTS.md) for what that costs and how it fails.

## Building

```sh
./gradlew :catalog:assembleDebug              # compile
./gradlew :catalog:composePreviewDiscover     # the annotation-derived inventory
./gradlew test                                # inventory invariants
./gradlew ktfmtFormat                         # format
```

`composePreviewDiscover` is the real contract: it is what turns the annotations into the published
inventory. A component that compiles but is not discovered vanishes from the sheet silently.

## CI

| Workflow | Does |
| --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) | compile, run preview discovery, unit tests, `ktfmtCheck`, and the build-free catalog-spec pre-flight |
| [`compose-preview.yml`](.github/workflows/compose-preview.yml) | renders the previews and posts a before/after visual diff on every PR |
| [`design-artifacts.yml`](.github/workflows/design-artifacts.yml) | renders and publishes the importable bundle to `design-artifacts/wear-m3-catalog` |
| [`design-parity.yml`](.github/workflows/design-parity.yml) | compares the render against the Figma kit and publishes the report to `design-parity/main` |
| [`figma-refs.yml`](.github/workflows/figma-refs.yml) | manual, read-only: proposes a kit node per component and rebuilds the kit index |

design-parity is wired but **manual-only** for now: it needs a `FIGMA_TOKEN` repository secret (a
read-only PAT with `file_content:read`), which it skips with a notice while absent, and a
design-map.json carrying a component, which it does *not* — an empty map fails the run outright.
This catalog cannot project one yet, for a reason that is upstream and documented in
[`docs/DESIGN_MAP.md`](docs/DESIGN_MAP.md).

Dependencies update themselves via Renovate ([`renovate.json`](.github/renovate.json)).

## Licence

Apache 2.0 — see [LICENSE](LICENSE).
