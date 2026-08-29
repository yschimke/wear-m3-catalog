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

## Two renditions of the same surface

This repo publishes **two** catalogs, and the pairing between them is the point.

| Module | System | Library | Delivery branch |
| --- | --- | --- | --- |
| [`:catalog`](catalog) | `wear-m3-catalog` | `androidx.wear.compose:compose-material3` (+ Horologist) | `design-artifacts/wear-m3-catalog` |
| [`:remote-catalog`](remote-catalog) | `remote-m3` | `androidx.wear.compose.remote:remote-material3` (+ `remote-creation-compose`, Glance Wear) | `design-artifacts/remote-m3` |

`:catalog` draws the kit with Wear Compose Material 3. `:remote-catalog` draws the same components
as **Remote Compose documents** — each sticker is a real `RemoteDocument`, rasterised by the player,
which is the path a watch face, tile or widget takes on-device. Every Remote component names its
`:catalog` counterpart, so the published compare page reads as three columns: **the kit**, the Wear
Compose rendition, and the Remote one. Two implementations can only tell you that they differ; the
kit is what says which one is wrong.

The Remote catalog moved here from `:samples:design-catalog-remote-m3` in
[compose-ai-tools](https://github.com/yschimke/compose-ai-tools) — see
[issue #4588](https://github.com/yschimke/compose-ai-tools/issues/4588). It is a separate Gradle
module rather than a source set because it is on the alpha Remote Compose line at `compileSdk 37`
with no Compose BOM, and none of that may reach the catalog that reproduces the kit.

**Both are design-led.** `.design-parity.json` is repo-wide and the parity workflow runs a job per
module, so each sheet is compared against the kit under the same policy: a divergence is a defect in
this code. The Remote sheet's kit mapping is partial by design — eleven components share an id with
their `:catalog` counterpart and inherit its kit node; the rest record why they are not mapped yet
rather than pointing at a cell they do not draw.

## Status

**Every published set in the kit is accounted for.** 33 of the kit's 42 published component sets are
reproduced by a catalog component; the other 9 are excluded, each with a stated reason.
[`kit-sets.json`](kit-sets.json) is that record — one row per set, carrying either the components
that reproduce it or why it is absent — and `CatalogKitCoverageTest` holds it to the annotations in
both directions, so a set cannot be quietly dropped and an exclusion cannot outlive the limitation
that earned it.

**How much of each set is drawn is a second question, and it now has an answer too.**
[`kit-cells.json`](kit-cells.json) counts it cell by cell, for both sheets: `:catalog` draws 550 of
the 888 cells published by the 33 sets it reproduces, `:remote-catalog` 196 of the 331 published by
the 9 it names. Fifteen of those 42 sheet-rows draw their set in full. The record is projected from
each module's resolved design map and reconciled by CI, so a cell that stops being drawn moves a
number in a reviewable diff — the check that was missing when the Remote sheet drew 15 of the `Card`
set's 45 cells with everything green
([#158](https://github.com/yschimke/wear-m3-catalog/issues/158)).

**And every gap on both sheets now says why.** All 29 short rows carry a written reason on their
`kit-sets.json` row, and `KitCellCoverageTest` fails on a gap that states none — so a cell that goes
missing cannot go quiet. Most of those reasons are a library declining to draw a distinction the kit
does: Wear resolves the three filled styles' disabled colours to one `onSurface` pair, so 28 cells
across `Text-Button`, `Button-Compact` and `Edge-Button` are one picture under two or three names —
a comparison that cannot fail. The Remote line says the same thing in its own accent, and adds
absences of its own: no outlined title or app card, no segmented progress ring.

**Where the library draws the wrong thing, the sheet draws it anyway.** A cell whose API exists is
called and published even when the result is blank or identical to its neighbour — an image-backed
button that renders a black pill with no image in it, a text button that draws nothing at all when
disabled, a disabled tonal button that is the disabled filled one to the byte. Withdrawing those
would leave the set reading as unreproduced, which is indistinguishable from nobody having got to
it: the sheet would look finished and the defect would be nowhere. `StickerBakeCoverageTest`'s
`knownBlank`, `RemoteRenderTest`'s `knownDuplicate` and `CatalogRenderTest`'s record each one
against the call that causes it, and all three fail in the other direction too — the day the
library starts drawing, the exemption is what announces it.

Sixteen components enter through the **library's** door instead — components carrying `noReference`
with the reason there is nothing to compare against. A sheet whose reader is looking for the
component set should not omit a component because a design file did. Four are Wear Compose Material 3
(`ButtonGroup`, `ArcProgressIndicator`, `TransformingLazyColumn`, `Scaffold`); the other twelve are
Horologist's, below.

### Two libraries

Wear Compose Material 3 is the first library here. **Horologist is the second**, and it is on the
sheet because the kit does not stop where the platform library does: `Media-Player` is a whole
screen, and Wear Compose ships no media player. That set used to be an exclusion reading "assembled
by an app (or by Horologist), not a library component" — true of Wear Compose, wrong about the
ecosystem. [Horologist][horologist] publishes the screen and its parts as library components, so the
catalog calls them.

The player is still excluded, but for a different and narrower reason: the kit's cell **exports** as
its own album-artwork overlay rather than as the player, so there is no faithful reference image to
compare against. The component ships; the comparison does not. See
[`MediaControls.kt`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/sections/MediaControls.kt).

Everything Horologist is filed under a `Horologist` section, so a reader can always tell which
library a card's composable comes from:

| Group | Components |
| --- | --- |
| Media controls | `PlayerScreen` (the kit's `Media-Player`), the transport rows, the play/pause progress button, the track header, the playlist action |
| Sign-in | the sign-in and guest buttons, the account picker, the placeholder screen, the signed-in confirmation |
| Fast scrolling | `FastScrollingTransformingLazyColumn` — the rotary section-skimming long list |

Only Horologist's `*-material3` artifacts are used; the un-suffixed ones are its Material 2 line.
The ViewModel-driven `auth-ui-material3` screens stay out — a sticker for one would be a sticker for
a fake repository, not for the component.

[horologist]: https://github.com/google/horologist

What is excluded, and why:

| Kit set | Why |
| --- | --- |
| `Button-ImageBackground-Round` | Compose puts the image container painter on `Button` and `Card`; `IconButton` takes no painter |
| `Media-Player` | implemented (`Media/PlayerScreen`) but not comparable — the kit's cell exports as its album-artwork overlay, not as the player |
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

`display.hero` is `Media/PlayerScreen` — a whole round watch face, not a component swatch. It is
the picture the preview server's index shows for this catalog, and on a sheet of Wear stickers a
running media player says "this is a watch design system" at a glance where an isolated shape or
button cannot. It names a `@CatalogComponent` id; `CatalogInventoryTest` holds it to one that
exists, because a hero naming nothing does not fail anything — the server just quietly features
its own pick instead.

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

Nine recordings live in
[`Motion.kt`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/sections/Motion.kt), published as
GIFs beside the sticker sheet: the indeterminate progress ring, the switch thumb travelling, the
toggle button's shape morph, swipe-to-reveal revealing, the edge button rising out of a scroll, the
media transport row pressed button by button, and the button, icon button and card placeholders
shimmering and then wiping off to reveal real content. They carry no `@CatalogComponent` — a
recording is not a component, and membership is still the kit's call — but each is **claimed** by
the component it records, through `motionPreview` on that component's `@CatalogComponent`. One
function per component, so a recording covering several axes covers them in one window.

Most are driven by the component's own animation or by a `LaunchedEffect` state change. The media
transport row is the exception and the first here to use **`@InteractionPreview`**, which dispatches
a real pointer at nodes resolved from the live semantics tree: the row's buttons respond through
their own wiring, and pressing the middle one genuinely pauses playback rather than a preview
setting `playing` on its behalf. That annotation was desktop-only when this file's Motion notes were
first written; it has run on Robolectric since compose-ai-tools 1.25.0, below the version this repo pins. See
[`AGENTS.md`](AGENTS.md) and the notes in `Motion.kt` for when to reach for which, and for why a
press that dispatches cleanly still needs measuring before you call it motion.

## Building

```sh
./gradlew :catalog:assembleDebug :remote-catalog:assembleDebug              # compile
./gradlew :catalog:composePreviewDiscover :remote-catalog:composePreviewDiscover
./gradlew test                                                             # inventory invariants
./gradlew ktfmtFormat                                                      # format
```

`composePreviewDiscover` is the real contract: it is what turns the annotations into the published
inventory. A component that compiles but is not discovered vanishes from the sheet silently.

## CI

| Workflow | Does |
| --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) | compile, run preview discovery, unit tests, `ktfmtCheck`, and the build-free catalog-spec pre-flight |
| [`compose-preview.yml`](.github/workflows/compose-preview.yml) | renders the previews and posts a before/after visual diff on every PR |
| [`design-artifacts.yml`](.github/workflows/design-artifacts.yml) | renders and publishes both bundles — `design-artifacts/wear-m3-catalog` and `design-artifacts/remote-m3` — scoped so a push that moves one catalog does not re-render the other |
| [`design-parity.yml`](.github/workflows/design-parity.yml) | compares each catalog's render against the Figma kit — `:catalog` to `design-parity/main`, `:remote-catalog` to `design-parity/remote-m3` |
| [`design-parity-import.yml`](.github/workflows/design-parity-import.yml) | owns the Figma traffic: refreshes the reference cache on `design-parity/reference` |
| [`figma-pages.yml`](.github/workflows/figma-pages.yml) | imports the kit's page SVGs and commits the cache under `design/pages` |
| [`figma-refs.yml`](.github/workflows/figma-refs.yml) | manual, read-only: proposes a kit node per component and rebuilds the kit index |
| [`no-agent-attribution.yml`](.github/workflows/no-agent-attribution.yml) | blocks agent `Co-authored-by:` trailers and agent commit identities from reaching `main` |

design-parity runs **hourly** and weekly — the `FIGMA_TOKEN` secret is set and `design-map.json` is
committed, so the two prerequisites this section used to describe as missing are both met. It is
deliberately not per-push: a run takes ~35-45 min and merges land every few minutes, so the
per-push trigger could never drain, and the concurrency lane resolved that by cancelling queued
runs before they started. Hourly is the cadence the lane can actually serve, and the cache check
makes a quiet hour a ~40s no-op. See the comment at the top of
[`design-parity.yml`](.github/workflows/design-parity.yml).

Dependencies update themselves via Renovate ([`renovate.json`](.github/renovate.json)).

## Licence

Apache 2.0 — see [LICENSE](LICENSE).
