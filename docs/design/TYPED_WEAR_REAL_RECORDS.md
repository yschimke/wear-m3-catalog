# Typed Wear definitions on the real records

The next step for the typed pilot ([`TYPED_WEAR_RENDERER.md`](TYPED_WEAR_RENDERER.md)): its definitions
move off the catalog-owned wrapper entry points (`WearButton(variant: String, …)`) onto the real
Wear Compose Material 3 composables, each carrying an `ExportProjection` the builder interprets. The
plan, and the structure around the calls, is compose-ai-tools' `docs/design/TYPED_REAL_RECORD_ADAPTERS.md`
and `docs/design/TYPED_STRUCTURAL_TEMPLATES.md`; this note is what applying it to Wear needs.

## Records

`RealWearRecords` reads the real composables from the library metadata on `catalog-typed-adapters`'
JVM classpath (`TypedAdapterDiscovery.discoverLibrary`, compose-ai-tools 2.50.0). The multiplatform
port publishes the same `androidx.wear.compose` API the Android export calls. Every non-deprecated
overload is published on the record (`Button` has four: `label` or `content`, each with and without
`containerPainter`); the record's own parameters are the overload a `LibraryCallable` picks:

| Component | Record | Picked by |
| --- | --- | --- |
| `wear-m3/text` | `material3.Text` | `text: String`, not `AnnotatedString` |
| `wear-m3/button` | `material3.Button`, `FilledTonalButton`, `OutlinedButton`, `ChildButton` | `label`, not `containerPainter` |
| `wear-m3/list-header` | `material3.ListHeader` | its only overload |
| `wear-m3/transforming-lazy-column` | `foundation.lazy.TransformingLazyColumn` | `state` |
| `wear-m3/icon` | `material3.Icon` | `imageVector`, not `painter` |
| `wear-m3/edge-button` | `material3.EdgeButton` | its only overload |
| `wear-m3/card` | `TitleCard`, `AppCard`, `OutlinedCard`, `Card` | not `containerPainter` |
| `wear-m3/icon-button` | `IconButton`, `FilledIconButton`, `FilledTonalIconButton`, `OutlinedIconButton` | its only overload |
| `wear-m3/button-group` | `material3.ButtonGroup` | its only overload |
| `wear-m3/alert-dialog` | `material3.AlertDialog` | `confirmButton`, not `transformationSpec` |
| `layout/column`, `layout/row`, `layout/box` | `foundation.layout.Column`, `Row`, `Box` | `content` |

The colour factories the projections call (`ButtonDefaults`, `CardDefaults` and
`IconButtonDefaults` colours) are read with `discoverFactories`. The three layouts are not
published: a host joins compose-ui-builder's foundation pack, which records them under the shared
`layout/…` ids, and `RealWearExportTest` joins the discovered ones in its place.

## What a row in the list writes

From compose-ui-builder's `WearContentEmitter`, each as data on the target's `list-item` context:

- `Modifier.transformedHeight(this, spec)`: `androidx.wear.compose.material3.lazy.transformedHeight`,
  the item scope read as `this`, `spec` a frame local.
- `minimumVerticalContentPadding(…)`: a member of `TransformingLazyColumnItemScope`
  (`ChainLink.receiverScopeFqn`), with the component's own `…Defaults.minimumVerticalListContentPadding`
  (`ListHeaderDefaults` and `TextDefaults` take a top and bottom pair).
- `transformation = SurfaceTransformation(spec)`: an item-scope extension in
  `androidx.wear.compose.material3`, written only to records that declare `transformation`.

## The definitions

`real/RealWearAdapters.kt` defines the components on those records, each with the projection the
builder lowers a design through (`RealWearRecords.adapters()` builds them from what discovery read).
They sit beside the pilot's wrapper adapters: the pilot pair publishes each definition's projection
on the wrapper's shelf entry, the real records as excluded export targets, the builtins they cover
(the list, the dialog) with that record as their `implementation`, the `code` block and the
`compose-component-records` export. `RealWearAdaptersTest` validates each against its record and
lowers the properties the fixture designs set; `RealWearExportTest` exports every fixture design
through compose-ui-builder's `ScreenExportGate`.

| Component | Writes | In a `list-item` |
| --- | --- | --- |
| `wear-m3/text` | `text`, `style` (a Wear typography role), `color`, weight, style, sizes in `sp`, alignment, decoration, line limits, `overflow` | `transformedHeight`, `TextDefaults`' top and bottom list padding |
| `wear-m3/list-header` | its label as a synthesized `wear-m3/text` (`text`, `maxLines`, `overflow`) | the same, with `ListHeaderDefaults`, and `SurfaceTransformation(spec)` |
| `wear-m3/button` | the variant's composable and `…ButtonColors(…)`; an optional icon and one or two lines as `icon` / `label` / `secondaryLabel`, anything else to the `content` overload | `transformedHeight`, `ButtonDefaults.minimumVerticalListContentPadding`, `SurfaceTransformation(spec)` |
| `wear-m3/transforming-lazy-column` | the frame's `listState` and `contentPadding`, `Arrangement.spacedBy(verticalSpacingDp.dp)`, each row in `item { }` | opens `list-item` unless `transformation = none` |
| `wear-m3/icon` | `Icons.<style>.<Name>` from the host's icon table, `contentDescription`, `tint`, `Modifier.size(sizeDp.dp)` | `transformedHeight` |
| `wear-m3/edge-button` | `buttonSize = EdgeButtonSize.…`, `enabled`, `ButtonDefaults.buttonColors(…)` | (the frame's `edgeButton`) |
| `wear-m3/card` | the variant's card; a title card's first line as `title`, the rest its content; an app card's app name, title and content; `CardDefaults` colours | `transformedHeight`, `CardDefaults` padding, `SurfaceTransformation(spec)` |
| `wear-m3/icon-button` | the variant's icon button and `IconButtonDefaults` colours | `transformedHeight`, `IconButtonDefaults` padding |
| `wear-m3/button-group` | its children as `content`, where `Modifier.weight` is `ButtonGroupScope`'s | `transformedHeight`, `ButtonGroupDefaults` padding, `SurfaceTransformation(spec)` |
| `wear-m3/alert-dialog` | `visible`, the title and text as synthesized texts, the design's own confirm and dismiss buttons | (the frame's `overlays`) |

The screen frame is published once per set of the scaffold's filled slots: `screen-root`,
`screen-root+edgeButton`, `screen-root+overlays` and `screen-root+edgeButton+overlays`.

All seven fixture designs export.

What the three open items needed landed in compose-ai-tools (#5877: item roles, `exportRefuses`, a
property only another target writes is refused) and compose-ui-builder (#728: the screen-root
builtin unwrapped into the frame). Writing the definitions needed two more tools fixes: a target's
overload is validated as the generator calls it, not against its own overload's recorded call site
(#5879), and a factory call passes where its return type's supertype is taken (`spacedBy`'s
`HorizontalOrVertical` for `Arrangement.Vertical`), with a call's only argument counted as always
written (#5880).

## Still different from the hand-written export

Each is refused by name rather than written differently, or written differently on purpose:

- `wear-list.json`'s cards set a `shape` the card no longer declares and the canvas never drew;
  the hand exporter dropped it silently and the projection refused it, so it is gone from the
  template.
- An `app` card needs three lines (app name, title, content): the hand exporter writes an empty
  content lambda for two, and the generator never invents an empty slot.
- `filled-variant` icon buttons: `FilledVariantIconButton` is not in the Wear Compose this module
  discovers.
- An icon button's `enabled = false` is written; the hand exporter drops it.
- A dialog is written with the authored `visible` and no-op dismiss handlers, and keeps the design's
  own buttons; the hand exporter hoists a `remember` nothing sets and writes
  `AlertDialogDefaults.ConfirmButton` / `DismissButton` in their place.

- A Material 3 `headline…` style role. Wear has no headline scale; the hand exporter writes the
  matching `title…` role, and a token codec cannot rename yet.
- A text's `fontVariationSettings` / `fontFeatureSettings`, which reach the call only through a
  `TextStyle` copy (`exportRefuses`).
- An outlined or child button's `containerColor`, which their colour factories do not take (as the
  hand exporter refuses it).
- A recoloured button's `iconColor`: the projection writes `contentColor` there too, as the canvas
  draws it (`WearScreenAdapters`); the hand exporter writes only `contentColor`, so an untinted icon
  keeps the variant's colour in its export.
- A list row's width: the fixtures author `fillMaxWidth` on each button and card, which both write
  before the row's links; the canvas fills every row whether or not it is authored.
- A `minLines` below one is written as stated rather than clamped.

## Order

1. Records: discovery, with a test that pins what it reads.
2. The three open items, in compose-ai-tools and compose-ui-builder (done).
3. Definitions with projections for the four components.
4. The catalog's `code` block (the frame, `listState` / `spec` / `contentPadding` locals, the
   `list-item` scope) and the definitions published in the pilot pair.
5. The components the fixtures use, the frame's `edgeButton` and `overlays`, and every fixture
   exported through `ScreenExportGate` (this change).
6. The remaining shelf (selection buttons, sliders, steppers, pickers, progress indicators, text
   buttons), then cutover: the production policy, runtime and `composeSourceExport` switch together.
