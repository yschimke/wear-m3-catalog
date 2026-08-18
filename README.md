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

Standing up. The pipeline runs end to end — build, discover, render, publish, serve — and the kit's
**Shapes** page is complete: one `Shape/MaterialShapes` component carrying all 35 silhouettes as
folded cells. The component sweep across the rest of the kit follows, and it needs a `FIGMA_TOKEN`
on this repository first: every published component names an exact kit node, and node ids are not
discoverable without API access. See [`AGENTS.md`](AGENTS.md) for the conventions any addition has
to hold.

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

design-parity is wired but **inert** until a `FIGMA_TOKEN` repository secret exists (a read-only PAT
with `file_content:read`) and design-map.json carries a component; it skips with a notice rather
than failing while either is missing.

Dependencies update themselves via Renovate ([`renovate.json`](.github/renovate.json)).

## Licence

Apache 2.0 — see [LICENSE](LICENSE).
