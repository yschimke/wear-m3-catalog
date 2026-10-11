# Typed Wear renderer pilot

The Wear M3 catalog's components, authored as Kotlin adapter definitions over compiled entry points
instead of hand-read JSON properties, generated into a parallel `wear-m3-typed-pilot` catalog pair and
packaged as a separate browser runtime. Nothing in ordinary discovery, rendering or delivery reads it;
production keeps `wear-m3`, its committed policy and the `wear-m3-p2-…` runtime.

## Shape

`catalog-typed-adapters` (opt-in, `-PtypedWearCatalog=true`):

- `WearComponents.kt` — one top-level composable per component (`WearButton`, `WearCard`, …) whose
  wire parameters are plain `String` / `Boolean` / `Float` / `Int`. Each body calls the real Wear
  Compose Material 3 component the production adapter calls, with the same defaults and the same
  row-transformation rule.
- `WearAdapters.kt` — a `TypedComponentAdapter` per entry point: property references, slots, click
  events and two-way state changes, checked by `freeze()` against the signature `TypedAdapterDiscovery`
  reads from bytecode.
- `TypedWearRenderer.kt` — the typed registrations, through the builder's experimental
  `TypedCanvasNodeScope` bridge. `WearWireValues.kt` maps the spellings saved designs already use
  (`enum`, `colorToken`, `typographyToken`, `float`, …) onto each codec's literal wrapper first; any
  other value still reaches the bridge's strict check and draws a node-local diagnostic.
- `GenerateTypedCatalog.kt` — discovers the entry points, generates the portable pair and the full
  pilot catalog (production builtins, menu, templates and capabilities kept; records and canvases from
  the typed definitions), compiled source-export fixtures and `readiness.json`.
- `wasmJsMain/Main.kt` — the pilot runtime: production foundation layouts and screen structure (the
  round frame and the transforming list) plus the typed registrations. Components passing their own
  `onClick` are registered as handling clicks, so the SDK adds no second clickable.

Ids are the production ids, so saved designs and templates address the pilot unchanged.

## Coverage

17 shelf components and 5 builtins are typed: text, icon, list headers, card, buttons (plain, edge,
icon, text, group), the three selection buttons, slider, stepper, both pickers, the three dialogs,
progress indicators and variable-font text. The screen scaffold and transforming list stay the
production adapters. `readiness.json` lists what remains: the five policy components production gives
no canvas either (`app-scaffold`, `time-text`, `circular-progress-indicator`,
`alert-dialog-edge-button`, `open-on-phone-dialog-icon`).

## Evidence

- `TypedWearRendererTest` renders every typed registration beside the production adapter for the same
  id from the same node, with every offered property set and once per offered value of each
  enumerated property (the 11 431-key icon vocabulary is sampled at eight spread keys): 113 cases,
  pixel-identical. The pickers' production adapters are browser-only, so the typed pickers are compared
  with the real Wear pickers instead. It also checks click callbacks, state write-back with the change
  event, and the strict diagnostic.
- `:catalog-typed-consumer` compiles the generated default, explicit and state-read source fixtures
  against the public entry points.
- `scripts/verify-typed-wear-runtime.mjs` loads the packaged ZIP in a sandboxed Chromium iframe over
  protocol v2: archive integrity, rejection of the production runtime pin, both templates at 192 and
  227dp, every owned component, and a pointer click on a bound checkbox row writing its state back.

## What it does not prove

`readyForCutover` stays false. Still open: production source export with state/event write-back
(the generic generator projects state reads, not callback assignments), production delivery of the
pair and runtime, and the components above that have no canvas. Known differences are recorded in
`readiness.json`: an authored letter spacing of exactly 0 reads as unset, and selection-button labels
take the label string where production also accepts a label slot.

## Running it

```sh
scripts/validate-typed-wear-renderer.sh ../compose-ai-tools ../compose-ui-builder
node scripts/verify-typed-wear-runtime.mjs ../compose-ui-builder
```

The tools checkout is v2.43.0 (`cb9e0d53`), the release this catalog already pins; the builder
checkout is `5dbe7a4f`, the first commit whose bridge accepts the `bool` / `int` literal wrappers that
release generates. Both are source-only until the bridge leaves its experiment.
`.github/workflows/typed-catalog.yml` runs the same against those commits.
