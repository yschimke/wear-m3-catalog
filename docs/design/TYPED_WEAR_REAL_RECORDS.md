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

The colour factories the projections call (`ButtonDefaults.buttonColors` and its variants) are read
with `discoverFactories`.

## What a row in the list writes

From compose-ui-builder's `WearContentEmitter`, each as data on the target's `list-item` context:

- `Modifier.transformedHeight(this, spec)`: `androidx.wear.compose.material3.lazy.transformedHeight`,
  the item scope read as `this`, `spec` a frame local.
- `minimumVerticalContentPadding(…)`: a member of `TransformingLazyColumnItemScope`
  (`ChainLink.receiverScopeFqn`), with the component's own `…Defaults.minimumVerticalListContentPadding`
  (`ListHeaderDefaults` and `TextDefaults` take a top and bottom pair).
- `transformation = SurfaceTransformation(spec)`: an item-scope extension in
  `androidx.wear.compose.material3`, written only to records that declare `transformation`.

## Open before the first projections publish

1. **The screen root.** Wear designs are rooted on `wear-m3/screen-scaffold` (`timeText`,
   `scrollIndicator`; `content` and `edgeButton` slots), with the list as its content. Under the
   structural design the scaffold is the catalog's `screen-root` frame, not a record call: its
   `scrollIndicator` lambda and `AppScaffold`'s `timeText` are not expressible as arguments. The
   builder therefore has to recognise the catalog's screen-root builtin, write the frame from it and
   make its `content` child the generated root. An `edgeButton` or a `false` `scrollIndicator` is
   refused until the frame grows those holes.
2. **A list without transformation.** The list's `transformation` property (`spec` or `none`) decides
   whether its rows take the `list-item` context at all. A role is opened by the list's item member,
   so the item member has to say which role it opens. `items` would name a role per variant, so that
   `none` wraps rows in `item` without the role.
3. **Properties with no parameter.** `Text`'s `fontVariationSettings` and `fontFeatureSettings` reach
   the call only through a `TextStyle` copy, which a projection cannot write. Projection validation
   requires every declared property to be exported, so a definition needs a way to declare one the
   export refuses when set, rather than dropping it from the shelf.

## Order

1. Records (this change): discovery, with a test that pins what it reads.
2. The three open items above, in compose-ai-tools and compose-ui-builder.
3. Definitions with projections for the four components, the catalog's `code` block (the frame,
   `listState` / `spec` / `contentPadding` locals, the `list-item` scope), published in the pilot pair.
4. Structural equivalence against `WearScreenCodeExporter` over the six Wear fixture designs, then
   the rest of the components.
