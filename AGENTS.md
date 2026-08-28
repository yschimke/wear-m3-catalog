# Repository instructions for AI agents

Read [README.md](README.md) first — it describes what this repo is and how it is laid out. This
file records the conventions that are easy to violate by accident. It is the Wear-side sibling of
[yschimke/m3-catalog](https://github.com/yschimke/m3-catalog) and inherits its rules; where they
differ, it is because Wear differs.

## Two modules, and which one you are in

`:catalog` is the kit rendition (Wear Compose Material 3 + Horologist). `:remote-catalog` is the
**Remote Compose** rendition of the same surface — every sticker a real `RemoteDocument` rasterised
by the player. It publishes the `remote-m3` system and moved here from
`:samples:design-catalog-remote-m3` in compose-ai-tools
([issue #4588](https://github.com/yschimke/compose-ai-tools/issues/4588)).

Three rules follow, and they are the ones easiest to break by accident:

- **The alpha line stays in `:remote-catalog`.** It is on `compileSdk 37`, the alpha Remote Compose
  trio, prerelease Compose UI and **no Compose BOM**, all declared in its own `dependencies` block.
  `:catalog` is on the stable BOM. That separation is the reason there are two modules rather than
  two source sets; do not "unify" the dependency lines.
- **The Remote trio moves together.** `compose-remote`, `wear-compose-remote` and `glance-wear` are
  built on the same `remote-creation*`, and a skewed pair fails inside the player at render time,
  not at compile time. Bump all three in one PR and read the visual diff.
- **Both catalogs are design-led, and both are in the parity scan.** `.design-parity.json` is
  repo-wide, and `design-parity.yml` now runs a job per module — `:catalog` publishing
  `design-parity/main`, `:remote-catalog` publishing `design-parity/remote-m3`. Same policy for
  both: a divergence from the kit is a defect in this code. Where the Remote library genuinely
  cannot draw what the kit specifies, say so in the caption or the component's KDoc — the same rule
  `:catalog` follows for a Wear Compose gap — rather than silently rendering something else.
- **One design map per checkout.** design-parity reads `<repoRoot>/design-map.json` and nothing
  else, so the two modules cannot each commit one. `scripts/design-map.sh [<module-dir>]` projects
  the named module into that one path; each parity job regenerates it for its own module in its own
  workspace. The committed map is `:catalog`'s. If you project the Remote one locally, restore it
  (`git checkout -- design-map.json`) before committing. Which you will, if you run a parity check
  locally — the recipe, and the three other things that waste an hour, are in
  [docs/PARITY_LOCAL.md](docs/PARITY_LOCAL.md).

## Annotation-first is the rule, not a preference

The catalog inventory lives in annotations next to the composables. **Do not** add a `groups` array
to `catalog.spec.json` to add, rename or recaption a component — put it on the `@CatalogComponent` /
`@CatalogVariant`. The spec is layered over the annotations as a field-level override and exists for
cover-sheet fields only.

**That now holds for both modules.** `:remote-catalog` arrived from compose-ai-tools with its
inventory in `catalog.spec.json` as a `groups` array and has been migrated onto annotations, so
neither spec carries an inventory any more. Both specs are cover-sheet only.

**The two sheets have ONE taxonomy, and it is the kit's.** The Remote sheet used to have its own:
every size, style and slot as a separate top-level component, 51 of them and not one
`@OverrideVariant`, against `:catalog`'s 49-with-cells
([#116](https://github.com/yschimke/wear-m3-catalog/issues/116)). The compare page reads the two
columns component by component through `parallel`, so that was two different answers to the same
question. It is folded now: an axis that is an **argument** to a function is a cell on both sheets,
under the **same cell name**, because the columns pair cell by cell as well as card by card.

Where the two sheets legitimately differ is which axes have a function behind them, and the
call-site test decides it exactly as it does on the Wear side. `Style=` on `Text-Button` folds on
both, because each library ships one text button taking its emphasis as colours. `Style=` on
`Button` and `Icon-Button` stays split on both — `remote-material3` publishes one `RemoteButton`
and one `RemoteIconButton`, so it would fold by the letter of the rule, but those cards pair with
`Button/Tonal`, `IconButton/Filled` and `IconButton/Outlined`, which are separate Wear Compose
functions and therefore separate cards. Folding them would leave those facing nothing.

**A cell must resolve to a kit node; a component may say why it does not.** That is what decides
the shape of anything left over. A cell's kit vector is matched against the set, and a cell that
resolves to nothing is compared against nothing with no diagnostic anywhere — so a render the kit
publishes no cell for stays a top-level component carrying `noReference`, however variant-shaped it
looks (`Button/CustomShape`, `Progress/Circular-Indeterminate`, `TitleCard/Subtitle`). And where
the kit's axes are coupled — `Icon`, `Icon size` and `Alignment` on `Button` are one choice spelled
three ways — a cell declares its WHOLE vector through `kitProps`, because naming one value asks for
a node between the ones the kit drew.

If you find yourself writing a lot of mapping config to express something, that is a signal the
upstream libraries are missing an annotation — **raise it in
[compose-ai-tools](https://github.com/yschimke/compose-ai-tools) and add the annotation there**
rather than growing a JSON file here. The same rule applies to the CI pipeline: a capability any
catalog could want belongs as a generic input on the reusable `design-artifacts-reusable.yml`
workflow, never as a forked copy of the pipeline in this repo. The open dark-mode gap in the
design-map projector ([docs/DESIGN_MAP.md](docs/DESIGN_MAP.md)) is exactly that rule applied: it is
being fixed upstream, not worked around here.

## Direction: design-led, and Figma is read-only

**The kit is the source of truth.** `.design-parity.json` says `design-led`, so a parity finding is
a defect in this code, not a note about the kit. Where Compose genuinely cannot express what the kit
specifies, say so in the caption or the component's KDoc rather than silently rendering something
else.

**Never write to Figma.** Every interaction with the kit is read-only: the REST API for node ids and
reference images, the MCP server for variables and metadata. Do not call `use_figma`,
`create_new_file`, `upload_assets`, `add_code_connect_map`, `send_code_connect_mappings`, or any
other mutating Figma tool, and do not enable design-parity's Code-to-Canvas push-back.

## What enters the inventory, and what it is called

**Membership has two doors, and every component walks through one of them.**

1. **The kit's door.** A component that reproduces a published kit set names one exact, renderable
   kit node in its `reference` — **the VARIANT it draws, never the set frame** — and names the set
   it is a cell of in `referenceSet`. This is the default and the majority.

   **Renderable means it EXPORTS as the component, and that is not the same as looking right on the
   canvas.** Check the exported image before mapping a node, not the Figma viewport. A child that
   composites against its backdrop — a full-bleed artwork, a blend mode, a scrim — stops compositing
   when the node is exported on its own and simply covers everything under it. The kit's
   `Media-Player` cell is the worked example: valid node, correct on canvas, and its reference image
   is an opaque purple wash with the player invisible beneath it, so a comparison against it reports
   the entire frame and finds nothing. A mapping like that is worse than no mapping, because the
   sheet then claims a comparison it is not making. Withdraw it, say why on the component and on the
   `kit-sets.json` row, and name any sibling cell that does export cleanly so the mapping is cheap to
   restore.

   The distinction is not cosmetic. A kit component set is a grid of axis vectors (the `Button` set
   is 50 cells), and its frame's geometry is an editor artifact. Point `reference` at the set and
   three things break silently: parity diffs one 52dp button against a 1068×928 board, every
   `@OverrideVariant(kitAxis = …)` cell resolves to nothing because there is no base vector to vary
   one axis from, and the imported kit pages link 33 nodes instead of 181. `referenceSet` is the
   join key for `kit-sets.json` and `CatalogKitCoverageTest`, which are per-set.
   See [docs/DESIGN_MAP.md](docs/DESIGN_MAP.md).
2. **The library's door.** A component of one of the two libraries here (see **Two libraries**
   below) that the kit never published as a set still belongs on a sheet whose reader is looking for
   *the component set*, and it enters with `noReference = "<why the kit has none>"`. `ButtonGroup` is
   the plain case: real API, no kit set.

What is NOT allowed is silence. A `@CatalogComponent` with neither fails
`CatalogInventoryTest.every component is either mapped to the kit or says why not` — so "I forgot to
look" cannot masquerade as "the kit has nothing". `scripts/design-map.sh` fails the same way, before
a render is attempted: it passes `--strict --allow-stated-absence`, which is fatal on a missing
reference and on captures that pair with none, while accepting an absence a `noReference` explains.
Plain `--strict` would reject the sixteen door-2 components too, which is why the pair is what runs.

Door 2 is deliberately narrower than it sounds: it is for a **component of a library**, not for
anything a screen can be built from. App content (the kit's `Avatar-*`) is still out: the kit draws
the shapes an app fills and there is no composable to invoke.

## Two libraries, and which one a component comes from

**Wear Compose Material 3 is the first library; Horologist is the second.** The kit does not stop at
what `androidx.wear.compose:compose-material3` publishes — its `Media-Player` set is a whole screen,
and Wear Compose ships no media player — so a catalog that only ever calls Wear Compose has to
exclude the parts of the kit the platform library does not reach. It used to:
`Media-Player`'s coverage row read "assembled by an app (or by Horologist), not a library
component", which was true of Wear Compose and wrong about the ecosystem.
[Horologist](https://github.com/google/horologist) publishes exactly that screen, and its parts, as
library components — plus the sign-in surfaces and the fast-scrolling list, neither of which Wear
Compose has. (That row is excluded again now, but on a narrower and truer ground: the component
exists, the kit's cell just does not export as anything comparable.)

So Horologist components are in, under three rules:

- **The `*-material3` artifacts only.** Horologist still ships its original Material 2 line under
  the un-suffixed names (`horologist-media-ui`, `horologist-auth-composables`, …). A sticker drawn
  from those compares the kit against the wrong design system, silently.
- **They live under `section = "Horologist"`.** The other sections are the kit's contents pages, and
  a reader has to be able to tell at a glance which library a card's composable comes from — a
  `PlayerScreen` filed under "Containment" is indistinguishable from Wear Compose API that does not
  exist. Groups within it are the surface: `Media controls`, `Sign-in`, `Fast scrolling`.
- **Both doors are open to them, and the kit's door is preferred where the kit can actually be
  compared against.** Today none of them go through it, for three different reasons worth keeping
  apart: the kit has no such set at all (the sign-in screens, the fast-scrolling list); the only node
  that draws it is one of the kit's own **private** `.Base / Media / …` sets, which the kit walk does
  not publish and no coverage row can join to (the media parts); or the set is published but its cell
  does not export as the component (`Media/PlayerScreen` — see door 1 above, and the note in
  `MediaControls.kt`). A `noReference` for either of the last two should name the node it is talking
  about, so the correspondence is written down where a reader will find it and the mapping is cheap
  to restore.

**Composition alone is still not a reason to exclude.** What matters is whether *a library*
publishes the thing as a component you call, not how many components it is built from.

Stateful, ViewModel-driven entry points stay out either way: `auth-ui-material3`'s
`SignInPromptScreen` takes a `SignInPromptViewModel` and drives a real auth repository, so a sticker
for it would be a sticker for a fake. The stateless half — `auth-composables-material3` — is the
half a catalog can publish honestly. The same test rules out Horologist's Lottie-animated media
buttons here for a different reason: the Robolectric renderer does not resolve a Lottie composition.

**Naming is Compose's call.** Ids follow the Wear Compose API surface, because that is what a reader
of a Compose catalog greps for. The one hard rule is not to borrow a kit word for something the kit
uses differently. Where Compose has no name of its own, take the kit's.

## The coverage record

[`kit-sets.json`](kit-sets.json) lists **every published set in the kit** with either the catalog
components that reproduce it or a stated reason it is absent, and `CatalogKitCoverageTest` fails in
both directions: a set with neither, and an exclusion whose node something now references. Adding a
component means adding it to the row that names its kit node; deciding a set stays out means writing
down why.

The rows come from the kit walk in [`figma-refs.yml`](.github/workflows/figma-refs.yml) — re-run it
when the kit itself moves, and reconcile the file in the same commit. It records only what the kit
publishes: private sets (names beginning `.`, and each page's `Base components`) and the Icons page
are out of scope.

## Sticker conventions

- **A sticker says what the kit says.** Content is not decoration here: a button the kit labels
  `Primary label` must not be labelled `Filled`, because the difference is reported as a difference
  and — since a sticker is cropped to what it draws — it changes the outline too, which then gets
  the reference squashed into the wrong frame to be compared. Take every string from
  [`KitCopy`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogCopy.kt) and read it through
  `kitCopy(key, kit)`, never as a literal: the baked capture stays the kit's words and `key` becomes
  a live knob on the preview server, so the realistic copy is still one keystroke away for a reader
  browsing the sheet. Where a kit cell truncates its text, the constant is the **full** string it is
  truncating. Adding a component means adding its kit strings there, with the node they came from.
  `Motion.kt` is outside this: its recordings answer to no kit node (see below), so they keep copy
  that reads like an app.
- **The reference must be the same SHAPE of artwork as the render.** The kit publishes three kinds
  of cell and only sizes tell them apart: *component* (`172×52`, `192×59` — pair with `Sticker`),
  *display* (`192×192` — pair with `FullScreenSticker` on a round device), and *long scroll*
  (`192×354`…`192×500` — a scrolling screen unrolled, which **nothing here can be diffed against**).
  Where a set publishes both, as `Dialog` does behind `Scrolling=`, take the display cell: pointed at
  a scroll cell the alert dialog reported its entire frame as a difference, because the comparison
  squashes the reference to the render's aspect first.
- **Which means the FRAME follows the cell, and it is the half that gets forgotten.** Naming the
  right cell and then publishing it in the wrong frame lands in exactly the same place as naming the
  wrong cell, because the reference is fitted to the render's frame before it is diffed. It has gone
  wrong in both directions: `EdgeButton` published a whole watch face against a `192×59` component
  cell ([#31](https://github.com/yschimke/wear-m3-catalog/issues/31)) — it renders through
  `EdgeButtonSticker` now — while both `SwipeToReveal` components and `CircularProgressIndicator`
  published cropped stickers against `192×192` display cells, the ring at a fixed 120dp rather than
  around the bezel. **A component "feeling" full-screen is not the test; the cell's size is** — the
  ring and the segmented ring are one API family drawn on two different shapes of cell, and the
  frame follows the cell.
  So is the *size within* a cell: the kit's four `Edge-Button` sizes are `EdgeButtonSize` plus its
  3dp floor (`49=46+3` … `99=96+3`), so they line up one-to-one, and reading the two lists off in
  parallel put every cell a step too big. See [docs/DESIGN_MAP.md](docs/DESIGN_MAP.md).
- **A full-screen sticker renders at every screen size the kit recognises, and they fold.** The kit
  enumerates its sizes in `.WatchPuck` on the Meta Components page — 192 (its own "legacy"), 204,
  216, 225 (the breakpoint), 240 — and `CatalogFullScreenModes` draws all five. **192 is the base**,
  because whatever the puck table calls it, the kit *draws* every screen cell at 192×192, so a base
  comparison is only like-for-like there; the other four fold under it as `<dp>dp` cells, since a
  size is an argument to the same screen rather than a different screen. **Do not add
  `id:wearos_large_round`** — it is 227dp, a size the kit does not draw, and rendering there is what
  put a scale difference under every full-screen comparison. Tooling has named device ids for only
  two of the five; the rest are `spec:` strings at the same 2.0 density, which render fine.
- One file per component **group**, opening with `@file:CatalogGroup(name = …, section = …)`.
- Every `@CatalogComponent` carries a `caption`. A component with no caption publishes as a bare
  picture, and a test fails the build for it.
- **Fold variants behind defaults.** A state / content axis is an `@OverrideVariant` cell (or a
  `@CatalogVariant(of = …)`) under its parent, not a new top-level component. A sheet this size is
  only browsable because the card count tracks components, not renders.
- **A cell that turns ONE knob is primary; a CROSSING is `secondary = true`.** Folding fixed the
  card count and moved the problem down a level: drawing the kit exhaustively (#101) put 89 cells
  on `SegmentedProgress` and 47 on `ScreenEdgeButton`, and a component subtree nobody can read is
  the same failure as a grid nobody can read.
  `@OverrideVariant(secondary = true)` is the tier for that
  ([compose-ai-tools#4734](https://github.com/yschimke/compose-ai-tools/pull/4734), 1.46.0).

  **Only the listing changes.** The cell still renders, still bakes, still keeps its own `/p/` URL
  and still pairs with its kit node — so nothing is traded away for the shorter menu. It stays
  reachable by every route that does not go through the tree: an imported kit page, a design-map
  pairing, a search result, and the viewer keeps the render on screen with the primary cells under
  it as the way back. **Never reach for `secondary` to hide a cell that is wrong.** A cell that
  resolves to nothing, or draws a picture its node is not, is a defect; demoting it only makes it
  harder to find.

  The line is what a reader BROWSES BY. One knob off the base is a question somebody asks —
  "what does the disabled one look like", "the small one", "the pentagon" — so all 35 shape cells
  and all 14 `Segments=` cells stay primary. Two or more knobs at once is a crossing: it exists to
  be compared against its kit node, and nobody navigates to `segments-11-small-stroke-overflow`
  by name.

  Count the KNOBS, not the `kitProps` entries — the kit spells one choice as several properties
  wherever its axes are coupled, and four cells here are exactly that: `Button`'s `icon`
  (the kit has no `Icon=Yes, Alignment=Center` node), `ArcProgressIndicator`'s `overflow`
  (`allowProgressOverflow` is the flag that permits the value), the toggle buttons' `disabled`
  (Wear draws checked and unchecked disabled differently and the kit publishes the unchecked one),
  and `Date Picker`'s `year-first` (the kit publishes that type only under a limit). Each turns two
  knobs and is one choice, so each stays primary.
- **One kit component set is one catalog component — unless the axis is a different function.**
  The kit's set boundary decides the taxonomy, so a kit variant property folds in as a cell: all 35
  shapes are cells of one `Shape/MaterialShapes` component, because the kit models them as one set
  varying `Shape=`.

  The carve-out is an axis whose values are **separate Wear Compose functions**. `Style=` on the
  kit's `Button` set is `Button` / `FilledTonalButton` / `OutlinedButton` / `ChildButton`, and
  `Type=` on `Toggle+Selection-Buttons` is `CheckboxButton` / `SwitchButton` / `RadioButton` —
  which one you call is the choice a reader of this catalog is making, so each is a component and
  they share the set's node. What stays folded is what is an **argument** to whichever function you
  picked: `enabled`, size, whether there is an icon, split vs whole.

  The test is the call site, not the word. `Style=` on `Button-Compact` and on `Text-Button` folds,
  because Compose ships one `CompactButton` and one `TextButton` that take their emphasis as
  `colors` — there is no second function to choose, so there is nothing to split.
- **A knob per parameter, named after the parameter, and a `previewOverrideChoice` wherever the
  values are a closed set.** The controls panel on the live sheet is built from the
  `previewOverride*` calls a sticker makes, and it is all a reader browsing that sheet has: a
  parameter the sticker pins is a parameter they cannot reach, and a knob named after anything but
  its parameter is one they cannot look up in the API. So expose every argument that changes the
  picture — `segmented`, `valueRange`, an icon slot, a start angle — and spell the key exactly as
  Compose spells the parameter (`value`, not the kit's `level`; `segmentCount`, not `segments`).
  The kit's word for the same axis belongs on the cell as `kitAxis` / `kitValue`, which is the next
  bullet. Where the value set is closed, `previewOverrideChoice(key, default, listOf(…))` renders a
  picker; a plain `previewOverrideString` renders a text box that shows the current value and hides
  every alternative, so `extra-small` is reachable only by someone who has read the source. Two
  things stay off the panel: `colors` / `shape` / `modifier`, which are theme-level objects rather
  than scalars a reader can type (the theme switcher is where those are chosen), and a value the
  component genuinely does not take.

  Knobs are additive — keep each default at what the sticker already rendered and the baked
  captures, and their kit comparisons, do not move. **Check the state a knob feeds is keyed on it**:
  `remember { … }` and `rememberSaveable { … }` read their initial value once, so a knob wired into
  one moves nothing in a live session while looking perfectly correct in every baked render, which
  is a fresh composition each time. `remember(knob)` or `key(knob) { … }` around the state.
- **Name the kit's word on the cell, not only in the seed.** `@OverrideVariant(kitAxis = …,
  kitValue = …)` carries the kit's own axis and value across to the resolver, which is what lets a
  Compose-shaped knob (`enabled=false`, a boolean) resolve against the kit's `Disabled=Yes`. Use it
  by default on a cell whose knob is not already spelled the kit's way.
- **A cell's seed must spell what the kit says** when it carries no `kitValue`. A variant's props are matched against the kit's own
  variant *values*, so `shape=pentagon` resolves to nothing against the kit's `Shape=Pantagon` (its
  own spelling) and drops that node from the comparison with no diagnostic anywhere. Where the two
  genuinely disagree, `@OverrideVariant(kitAxis = …, kitValue = …)` names the kit's spelling directly
  and lets the seed keep Compose's.
- Component ids are the published sticker's URL and the join key for `@CatalogVariant(of = …)`.
  Renaming one moves a published URL — do it deliberately.
- **A live click answers with the component, not with the label.** `counted` returns the label it
  was given and a handler that is live-lane only; the ripple, state layer and pressed shape are
  what a click is supposed to show. The `(n)` tally is the `clickCount` knob now, off by default —
  reach for it when the question really is "did the handler run?", never as a sticker's standing
  answer to a press ([#32](https://github.com/yschimke/wear-m3-catalog/issues/32)). See
  `CatalogInteractive.kt`.
- **Dark-first, transparent.** A component sticker is a single dark capture on a transparent
  background (`@CatalogModes`). A component the kit draws on a display cell — scaffolds, lists,
  dialogs, pickers, the indicator rails, swipe-to-reveal — takes `FullScreenSticker` and
  `@CatalogFullScreenModes` instead: the round device frame plus the breakpoint fan-out.
- **A control the kit draws across its content column takes `Modifier.kitRowWidth()`.** Wear's
  `Button` applies no `fillMaxWidth` of its own — `Card`, `Slider` and `Stepper` do — so a button
  given no width hugs its label, and `Button/Filled` published at 120dp against a 172dp kit cell.
  That is not a trimmable edge: design-parity rasterises the reference at the *candidate's* width,
  so a narrow candidate rescales the whole comparison
  ([#138](https://github.com/yschimke/wear-m3-catalog/issues/138)). `fillMaxWidth()` is not the fix
  either — the sandbox is bounded at 227dp, so it resolves to 211dp, a different wrong answer.
  Components that are *supposed* to size to their content — icon buttons, the compact button, the
  text specimens — must NOT take it: pinning those would publish a component wider than it is.
- Renders must be **deterministic**: a `TimeText` is pinned to a fixed instant, never the system
  clock. An unpinned clock would make every nightly render differ from the last, which turns the
  delivery branch's history into noise.
- Every published comparison must invoke the actual named composable — the Wear Material 3 one, or
  the Horologist one where that is the library that publishes it (see **Two libraries** above).
  Rebuilding a component from `Box` and its `*Defaults` can make a replica line up, but it cannot
  test the library and therefore does not belong in the comparison inventory. The shape specimens are the
  documented exception in the other direction: they draw mobile `MaterialShapes` because Wear
  publishes no shape library of its own (see README).

## Themes

The declared themes in `CatalogThemes.kt` are **not inventory** — no `@CatalogComponent`, no kit
node, no `kit-sets.json` row. Membership is still the kit's call; a theme is a re-skin of what is
already a member.

- **A sticker frame installs its theme through `CatalogMaterialTheme`, never a bare
  `MaterialTheme { … }`.** A `@WearThemeCatalog` provider wraps the sticker from the outside, and an
  inner theme silently shadows it: every entry in the switcher then renders identical pixels and
  every specimen sheet reports the stock palette. A new full-screen frame that reaches for
  `MaterialTheme` directly reintroduces that, and nothing fails.
- **`@WearThemeCatalog`, not the mobile `@ThemeCatalog`.** The two are not interchangeable — the
  mobile one's specimen reads `androidx.compose.material3.MaterialTheme`, which these providers
  never install, so the sheet reports the mobile baseline instead of the theme. Enforced by
  `CatalogInventoryTest`.
- **A theme carries a type scale, not only a palette.** Re-point every role explicitly:
  `Typography(defaultFontFamily = …)` is a no-op on Wear (it fills in a family only where a style
  has none, and every stock role already declares one), so a theme built that way renders in the
  stock face no matter what it declares.
- **Reproduce a borrowed theme by its recipe, not its output.** The Confetti palettes run the same
  seed through the same library Confetti uses rather than transcribing the roles it resolved to,
  because a transcribed table drifts the first time either side moves and nothing notices.
- **Both modules declare the SAME theme set.** `CatalogThemes.kt` and
  `remote-catalog/…/RemoteThemeCatalogs.kt` publish the same six names in the same two groups, built
  from the same four seeds through the same `materialkolor` recipe — because the compare page reads
  the two columns theme by theme, and a Theme select offering "Droidcon" on one column and "Coral"
  on the other cannot be read at all (#99). Add, rename or reseed a theme in **both**, in one PR.
  The seeds are duplicated (the two modules are on different dependency lines and cannot share a
  constant); `CatalogInventoryTest` and `RemoteCatalogThemeTest` each pin the literals, so a
  one-sided edit fails the other side.
- **What a Remote theme can carry is narrower, and that is not a licence to diverge.** A recorded
  document is re-themed by overriding named colour state (`USER:WearM3.<role>`), so the Remote side
  publishes a theme's *colours* mapped onto those 29 roles and its *faces* as data for a player lane
  to resolve — never a `Typography`. Same names, same palettes; only the mechanism differs.

## Motion

A sticker is one frame, and a lot of what a design system *is* lives in the motion. `Motion.kt`
carries the recordings, and they are **outside the component inventory** on purpose — no
`@CatalogComponent`, so they answer to no kit node and change no taxonomy. They are recordings *of*
components catalogued elsewhere.

Three rules, each learned the hard way:

- **Do not put `@AnimatedPreview` on a component that has cells.** It rides every `@OverrideVariant`
  too, and the animated path does not apply a cell's knobs — the three placeholder styles came out
  as three byte-identical copies of the base recording under three different names.
- **Pin the canvas.** A motion capture needs `widthDp` AND `heightDp`; the component stickers wrap
  and are cropped, and an unpinned capture fails with "produced no GIF".
- **`@InteractionPreview` works here now — and it is the right tool when the motion IS the press.**
  It used to be desktop-only: on Robolectric nothing wrote the animated file and the still then
  failed to decode `<id>.apng: file is missing on disk`, which also cost the component its ordinary
  PNG. That is compose-ai-tools issue #4215, closed by #4240 with the ripple's clock fixed in #4315,
  both shipped in **1.25.0** — below the version this repo pins. Prefer it over a `LaunchedEffect` for
  anything a finger provokes: it dispatches a real pointer at nodes resolved from the live semantics
  tree, so the component responds through its own wiring rather than through state a preview set on
  its behalf, and the Android backend advances the **main looper** per frame, which is the only way
  a platform `RippleDrawable` moves at all. A `LaunchedEffect` is still correct where there is no
  finger — a spinner, a shimmer, a switch shown resolving both ways.
- **A press is not automatically recordable — measure before claiming it is.** Two Horologist
  transport rows, same annotation, same targets: `PodcastControlButtons` opts its side buttons into
  `ButtonGroupScope.animateWidth` and gives 111 pixel-distinct frames of 178, while
  `MediaControlButtons` never calls it and gives **1 of 178** — a literal still. A capture that
  dispatched cleanly and wrote a file is not evidence the component moved.
- **A placeholder only animates under an `AppScaffold`.** `PlaceholderState` reads its frame clock
  from the library's internal `AnimationCoordinator`, and `AppScaffold` is the one thing in Wear
  Compose that composes that coordinator's looper — so a shimmer drawn in a bare `Sticker` stands
  perfectly still, on a watch as much as here. `AnimatedSticker` is that scaffold and nothing else;
  the placeholder recordings use it. This is the worked example of the rule below: a still recording
  looks exactly like a renderer limitation, and this one was written down as one for a while.

A recording must actually move: `CatalogRenderTest` fails a GIF with fewer than six distinct frames.
If a component does not animate under this renderer, publish no recording rather than one that
implies motion nobody would see — but rule out a missing wrapper first, because that is what the
placeholder's "3 distinct frames in 46" turned out to be.

## Kotlin

- ktfmt Google style, 100 columns. `./gradlew ktfmtFormat`.
- Kotlin block comments **nest**, so `/*` inside a KDoc opens a nested comment and swallows the rest
  of the file.

## Git

- **`main` is protected — every change goes through a PR.** The `Protect Main` ruleset requires a
  pull request (0 approvals) with all CI checks green, and squash is the only merge method. Branch
  names are `agent/…`.
- Conventional commit subjects (`feat:`, `fix:`, `docs:`, `chore:`). The squash commit is built from
  the **PR title**, so write the PR title as the commit subject.
- **Never attribute a commit to an AI agent** — no `Co-authored-by:` trailer naming an agent, and no
  agent author/committer identity. Links to an agent session and the `_Generated by [Claude Code]_`
  footer are fine; they don't claim authorship. Enforced in two places sharing one detector
  ([`.github/scripts/agent-attribution-scan.sh`](.github/scripts/agent-attribution-scan.sh)): the
  hooks in `.githooks/` (install with `scripts/install-git-hooks.sh`) and the
  [`No Agent Attribution`](.github/workflows/no-agent-attribution.yml) CI gate.
  **The hooks alone are not enough, and `main` has the scar to prove it.** `main` is squash-only,
  and GitHub builds the squash message from the **PR title + PR body** and credits every distinct
  *branch commit author* as a co-author — all of it server-side, after the last hook has run. That
  is how `7e203c38` (#59) landed `Co-authored-by: Claude <noreply@anthropic.com>` on `main` with
  clean local hooks. So scrub the **PR description** too, not just the commits.
  The CI gate only *blocks* once it is a **required status check** on the Protect Main ruleset;
  until then its `drift` job makes a breach loud rather than preventing it.

## Dependencies

- **Renovate owns the version bumps; don't hand-bump.** `.github/renovate.json` automerges anything
  that is not a major once CI is green. Change the config instead when the policy is wrong.
- Two groups are deliberately **not** automerged: **majors**, and **Compose** (Wear Compose plus the
  mobile Compose BOM and the material3 pin). Both change what the catalog renders, and the render is
  the product — a human reads the visual diff before it lands.
- **Horologist is grouped and held**, like Compose and for the same reason: its artifacts ship as
  one release (a skew between them is a compile error), and they draw the media player, the sign-in
  screens and the fast-scrolling list — so the visual diff is the review. It is on an alpha line;
  expect that diff to be real. Only the `*-material3` artifacts are dependencies here.
- `compose-ai-tools` is the exception in the other direction: the Gradle plugin marker, the
  annotation coordinates and the pinned CI action ref are one release and move together in a single
  PR, unscheduled and automerged. A skew between them breaks preview discovery outright.
- **The alpha Remote line is watched, not bumped.** `remote-snapshot-probe.yml` builds
  `:remote-catalog` against the newest androidx.dev snapshot every Monday and comments on
  [#95](https://github.com/yschimke/wear-m3-catalog/issues/95) only when the picture moves — it
  stops compiling, a tracked upstream bug's capture stops matching its known-broken one, or a render
  changes. It never touches `main`: the overlay that repoints the Remote trio at a snapshot is
  applied to the runner's checkout and thrown away, because a committed snapshot pin is exactly the
  skew the top of this file forbids. State lives on `snapshot-probe/remote-m3`.
- Repository settings — squash-only merges, auto-merge, and the `Protect Main` ruleset — are applied
  by `scripts/setup-repo-protection.sh`. They need an admin token, so no workflow (and no agent
  session) can set them; the script is the record of what they are meant to be, and re-running it
  repairs drift. `DRY_RUN=1` prints without writing.

## Verifying a change

```sh
./gradlew :catalog:assembleDebug :catalog:composePreviewDiscover \
          :remote-catalog:assembleDebug :remote-catalog:composePreviewDiscover \
          test ktfmtCheck
```

`composePreviewDiscover` is the real contract: it is what turns the annotations into the published
inventory. A component that compiles but is not discovered vanishes from the sheet silently.
