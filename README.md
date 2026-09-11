# Wear Material 3 Samples — design artifacts

Importable sticker-sheet for **`androidx.wear.compose:compose-material3`**, rendered from the committed
`@Preview` catalog in [`yschimke/wear-m3-catalog`](https://github.com/yschimke/wear-m3-catalog). This branch is a
**generated delivery artifact** — browse it in the page below, or pull it into
Figma / Stitch / Claude Design.

## 🔎 Browse the catalog

**[▶ Open the rendered catalog (htmlpreview)](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/index.html)**

A self-contained gallery — one card per component with its rendered PNG,
dimensions, accessibility greenlines, and a link to an editable SVG wireframe.

## 🔬 Compare SVG vs PNG

**[▶ Open the SVG↔PNG comparison (htmlpreview)](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/compare.html)**

Every component on one row: its editable **figma-svg** re-rasterized by the
browser beside the rendered **PNG** it is measured against — the design vector on
the left, the render on the right, as everywhere else the two are shown together
— plus a live **structural-similarity (SSIM)** match
score — so you can eyeball vector fidelity across the whole system at once and
spot which stickers drift. The score is pre-blurred and downscaled, so a
half-pixel rasterizer offset doesn't read as a mismatch.

## ↔ Compare across systems

**[▶ Open the Wear Material 3 Samples ↔ M3 Wear OS Apps Design Kit matches (htmlpreview)](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/matches.html)**

Every component paired with its counterpart in **M3 Wear OS Apps Design Kit**, side by side — the
authored `parallel` mapping in the catalog spec, rendered as a cross-system contact sheet. Both
sides are static thumbnails — this branch's baked render on the left, the wear-m3-catalog
render baked from its own `design-artifacts/wear-m3-catalog` branch on the right — and each
links to the live preview server on click.

## 🎛 Customise live

**[▶ Open this catalog in the live preview server](https://preview.coo.ee/wear-m3-samples/)**

The same rendered components, served live by `compose-preview serve --catalogs wear-m3-samples` —
open one, then change the theme, locale, font scale, or device and watch it
re-render. Every entry in `catalog.json` carries a per-variant `livePreview`
deep link to its exact preview on the same server, so browsing this branch and
customising the live render are two ends of one workflow.

## At a glance

| | |
| --- | --- |
| Components | **149** |
| Rendered images (PNG) | **149** |
| Editable wireframes (SVG) | **149** |
| Editable design vectors (figma-svg) | **149** |
| Components with a11y greenlines | **95** |
| Library | `androidx.wear.compose:compose-material3` |
| Renderer | compose-preview 2.10.0 |
| Schema | `design-parity-catalog/v1` |
| Generated | 2026-09-11 |

## Components by group

| Group | Count |
| --- | ---: |
| TitleCard | 7 |
| AlertDialog | 6 |
| AppCard | 5 |
| Card | 5 |
| CircularProgressIndicator | 5 |
| SwipeToReveal | 5 |
| oneHandedGesture | 4 |
| SegmentedCircularProgressIndicator | 4 |
| Stepper | 4 |
| TimePicker | 4 |
| animatedShapes | 3 |
| AnimatedText | 3 |
| DatePicker | 3 |
| Slider | 3 |
| SurfaceTransformation | 3 |
| TimeText | 3 |
| TransformationSpec | 3 |
| ButtonGroup | 2 |
| ChildButton | 2 |
| ConfirmationDialog | 2 |
| Content | 2 |
| curvedText | 2 |
| EdgeButton | 2 |
| FailureConfirmationDialog | 2 |
| FilledTonalButton | 2 |
| filledVariantButtonColors | 2 |
| OutlinedButton | 2 |
| OutlinedCard | 2 |
| Picker | 2 |
| placeholder | 2 |
| rememberTransformationSpec | 2 |
| scrollDownToNextItem | 2 |
| TextButton | 2 |
| TextToggleButton | 2 |
| animateScrollToOption | 1 |
| AppCardContent | 1 |
| AppScaffold | 1 |
| ArcProgressIndicator | 1 |
| Button | 1 |
| CheckboxButton | 1 |
| drawCircularProgressIndicator | 1 |
| dynamicColorScheme | 1 |
| FadingExpandingLabel | 1 |
| FilledIconButton | 1 |
| filledTextButtonColors | 1 |
| FilledTonalIconButton | 1 |
| filledTonalTextButtonColors | 1 |
| filledVariantIconButtonColors | 1 |
| filledVariantTextButtonColors | 1 |
| firstVisibleItemLayoutItemInfo | 1 |
| HighSnapPositionalThreshold | 1 |
| IconButton | 1 |
| IconToggleButton | 1 |
| layoutItemInfoOf | 1 |
| LevelIndicator | 1 |
| ListHeader | 1 |
| LocalOneHandedGestureEnabled | 1 |
| LowSnapPositionalThreshold | 1 |
| minimumTopListContentPadding | 1 |
| minimumVerticalListContentPadding | 1 |
| OneHandedGestureScrollIndicator | 1 |
| OpenOnPhoneDialog | 1 |
| OutlinedIconButton | 1 |
| outlinedTextButtonColors | 1 |
| PickerGroup | 1 |
| placeholderShimmer | 1 |
| RadioButton | 1 |
| scrollAway | 1 |
| ScrollIndicator | 1 |
| scrollToOption | 1 |
| SplitCheckboxButton | 1 |
| SplitRadioButton | 1 |
| SplitSwitchButton | 1 |
| SuccessConfirmationDialog | 1 |
| SwipeToDismissBox | 1 |
| SwitchButton | 1 |
| TitleCardContent | 1 |
| variantSliderColors | 1 |
| VerticalPageIndicator | 1 |

## What's in this branch

| Path | What it is |
| --- | --- |
| `index.html` | Self-contained gallery — [open via htmlpreview](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/index.html) |
| `compare.html` | SVG↔PNG comparison with a live structural-similarity score — [open via htmlpreview](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/compare.html) |
| `matches.html` | Cross-system component pairing vs `wear-m3-catalog` — [open via htmlpreview](https://htmlpreview.github.io/?https://github.com/yschimke/wear-m3-catalog/blob/design-artifacts/wear-m3-samples/matches.html) |
| `catalog.json` | Machine-readable catalog (`design-parity-catalog/v1`): components, variants, design tokens, greenlines, and per-variant `livePreview` deep links |
| `images/` | Rendered PNGs — the source of truth for each variant |
| `wireframes/` | One editable SVG per component (layout-inspector tree → token-styled shapes) |

## Using it

- **Figma / Stitch / Claude Design** — import `catalog.json` + `images/` as a sticker sheet.
- **Browse** — open `index.html` through htmlpreview (link above), or clone the branch and open it locally.
- **Customise** — open the [live preview server](https://preview.coo.ee/wear-m3-samples/) (or any image's `livePreview` link in `catalog.json`) to re-render a component under different themes / locales / devices.
- **Adopt structure** — the `wireframes/*.svg` are plain vector files; drop one into any editor to start from the real layout instead of tracing a screenshot.

## Provenance

Generated by the [`Design Artifacts`](https://github.com/yschimke/wear-m3-catalog/actions/workflows/design-artifacts.yml)
workflow: `compose-preview bundle pack` → catalog-export driver → force-push to
this branch. The render is the source of truth.

> ⚠️ **This branch is regenerated (force-pushed) from `main`** — weekly and after
> catalog/renderer changes. Don't commit work here by hand; it will be
> overwritten on the next run.
