# Repository instructions for AI agents

Read [README.md](README.md) first — what this repo is and how it is laid out. This file records the
conventions that are easy to violate by accident. It is the Wear-side sibling of
[yschimke/m3-catalog](https://github.com/yschimke/m3-catalog) and inherits its rules; where they
differ, it is because Wear differs.

## Two modules, and which one you are in

`:catalog` is the kit rendition (Wear Compose Material 3 + Horologist). `:remote-catalog` is the
**Remote Compose** rendition of the same surface — every sticker a real `RemoteDocument` rasterised
by the player. It publishes the `remote-m3` system.

- **The alpha line stays in `:remote-catalog`**: `compileSdk 37`, the alpha Remote Compose trio,
  prerelease Compose UI, **no Compose BOM**, all in its own `dependencies` block. `:catalog` is on the
  stable BOM. That separation is why there are two modules rather than two source sets; do not
  "unify" the dependency lines.
- **The Remote trio moves together.** `compose-remote`, `wear-compose-remote` and `glance-wear` share
  a `remote-creation*` base, and a skewed pair fails inside the player at render time, not compile
  time. Bump all three in one PR and read the visual diff. The snapshot lane is the one deliberate
  exception (see **Dependencies**).
- **Both catalogs are design-led, and both are in the parity scan.** `design-parity.yml` runs a job
  per module — `:catalog` publishing `design-parity/main`, `:remote-catalog` publishing
  `design-parity/remote-m3`. A divergence from the kit is a defect in this code. Where the Remote
  library genuinely cannot draw what the kit specifies, say so in the caption or KDoc rather than
  silently rendering something else.
- **One design map per checkout.** design-parity reads `<repoRoot>/design-map.json` and nothing else,
  so the two modules cannot each commit one. `scripts/design-map.sh [<module-dir>]` projects the named
  module into that path; each parity job regenerates it in its own workspace. The committed map is
  `:catalog`'s — restore it (`git checkout -- design-map.json`) after projecting the Remote one
  locally. Local-run traps: [docs/PARITY_LOCAL.md](docs/PARITY_LOCAL.md).
- **Everything projected FROM that map is per-module too, including the page join.** The committed
  `design/pages/pages.json` is `:catalog`'s; the Remote sheet gets its own via
  `node scripts/import-figma-pages.mjs --relink` after `scripts/design-map.sh remote-catalog`, in its
  own publish job. Without it the publisher falls back to component-level `reference`s alone — the base
  cell of each component and nothing under it — and the sheet reads as unwritten. CI checks the Remote
  relink reaches its whole map (`--require-full-join`).
- **The two sheets pair through `parallel`, and `scripts/parallel-map.sh` holds them to it.** A
  `parallel` naming an id the other sheet does not publish, a pair naming two different kit nodes, and
  an id both sheets publish with no `parallel` behind it are all silent, and a rename is when they
  happen. The gate needs both modules discovered; it fails on those three and only REPORTS a cell one
  sheet draws and the other cannot — that question belongs to `kit-cells.json`.

## Annotation-first is the rule, not a preference

The inventory lives in annotations next to the composables, in **both** modules. **Do not** add a
`groups` array to `catalog.spec.json` to add, rename or recaption a component — put it on the
`@CatalogComponent` / `@CatalogVariant`. Both specs are cover-sheet only.

**The two sheets have ONE taxonomy and ONE vocabulary, and both are the kit's and the Wear sheet's
respectively.** The compare page reads the two columns component by component through `parallel`, so
an axis that is an **argument** to a function is a cell on both sheets under the **same cell name**,
and where both draw the same kit node the component id is the same string (`IconButton/Filled`, not
`Button/Icon-Filled`). A `parallel` that has to translate the name is a mapping table in disguise; one
pointing a whole card at somebody else's cell is usually a fold waiting to happen. Keep authoring
`parallel` even where the ids match — it is what the pairing walks.

**Cell names read `<layout>-<style>-<content/size>`**, each segment dropped when it is the default:
`icon-outlined-gallery-1`, `with-subtitle-outlined-content-image`, `outlined-icon-only`. The order
matters because a cell that names no kit node has nothing else to pair on.

What does NOT converge is the render-name breakpoint segment. Remote stickers must name their frame —
a Remote Compose document rasterises the whole `@Preview` — while Wear stickers are device-less.
**Preview ids agree** (`CircularProgressRemote_192dp` against `CircularProgress_192dp`, no `__compact`
on either side), and that is what the projector reads its base breakpoint from, so do not reintroduce a
size-class frame for a shorter name. **Render names diverge**: the delivery branch publishes
`ideal__default__compact.png` for Remote against `ideal__default.png` for Wear, which is why every
Remote thumbnail in [docs/COMPONENT_MAP.md](docs/COMPONENT_MAP.md) has the suffix.

Where the sheets legitimately differ is which axes have a function behind them, decided by the same
call-site test. `Style=` on `Text-Button` folds on both. `Style=` on `Button` and `Icon-Button` stays
split on both: `remote-material3` publishes one `RemoteButton` and one `RemoteIconButton`, so it would
fold by the letter of the rule, but those cards pair with `Button/Tonal`, `IconButton/Filled` and
`IconButton/Outlined`, which are separate Wear Compose functions.

**A cell should resolve to a kit node**, so an unresolved cell is nearly always a mis-authored vector.
The exception: where the library takes the axis as an argument to a call the kit publishes under one
name, the render is a cell even if the kit drew no node for it.
`CircularProgressIndicator`'s `indeterminate` is the case to reason from — both overloads are the same
function name, so a separate card would spell one component two ways. The cost: `@OverrideVariant` has
no `noReference`, so a folded cell cannot state its absence and is indistinguishable from a typo'd
`kitValue`. Until that is fixed upstream
([compose-ai-tools#4875](https://github.com/yschimke/compose-ai-tools/issues/4875)) **a nodeless cell
must carry its reason in a source comment at the annotation**. What stays a top-level component with
`noReference` is a render with no kit call site to fold onto at all. Where the kit's axes are coupled —
`Icon`, `Icon size` and `Alignment` on `Button` are one choice spelled three ways — a cell declares its
WHOLE vector through `kitProps`.

A lot of mapping config to express something signals a missing upstream annotation — **raise it in
[compose-ai-tools](https://github.com/yschimke/compose-ai-tools)** rather than growing a JSON file
here. Same for CI: a capability any catalog could want belongs as a generic input on the reusable
`design-artifacts-reusable.yml` workflow, never as a forked pipeline here.

## Direction: design-led, and Figma is read-only

**The kit is the source of truth.** `.design-parity.json` says `design-led`, so a parity finding is a
defect in this code, not a note about the kit.

**Never write to Figma.** Every interaction with the kit is read-only: the REST API for node ids and
reference images, the MCP server for variables and metadata. Do not call `use_figma`,
`create_new_file`, `upload_assets`, `add_code_connect_map`, `send_code_connect_mappings`, or any other
mutating Figma tool, and do not enable design-parity's Code-to-Canvas push-back.

## What enters the inventory, and what it is called

**Membership has two doors, and every component walks through one of them.**

1. **The kit's door.** A component reproducing a published kit set names one exact, renderable kit node
   in its `reference` — **the VARIANT it draws, never the set frame** — and names the set it is a cell
   of in `referenceSet`. This is the default and the majority.

   **Renderable means it EXPORTS as the component, which is not the same as looking right on the
   canvas.** Check the exported image before mapping a node: a child compositing against its backdrop
   stops compositing when exported alone and covers everything under it, so the comparison reports the
   entire frame and finds nothing. That is worse than no mapping, because the sheet claims a comparison
   it is not making. Withdraw it, say why on the component and on the `kit-sets.json` row, and name any
   sibling cell that does export cleanly.

   Pointing `reference` at the SET breaks three things silently: parity diffs one 52dp button against a
   1068×928 board, every `@OverrideVariant(kitAxis = …)` cell resolves to nothing for want of a base
   vector, and the imported kit pages link 33 nodes instead of 181. `referenceSet` is the join key for
   `kit-sets.json` and `CatalogKitCoverageTest`. See [docs/DESIGN_MAP.md](docs/DESIGN_MAP.md).
2. **The library's door.** A component of one of the two libraries here that the kit never published as
   a set enters with `noReference = "<why the kit has none>"`. `ButtonGroup` is the plain case: real
   API, no kit set. This door is for a **component of a library**, not anything a screen can be built
   from — app content (the kit's `Avatar-*`) is out, because there is no composable to invoke.

What is NOT allowed is silence. A `@CatalogComponent` with neither fails
`CatalogInventoryTest.every component is either mapped to the kit or says why not`, so "I forgot to
look" cannot masquerade as "the kit has nothing". `scripts/design-map.sh` fails the same way before a
render is attempted, passing `--strict --allow-stated-absence`.

## Two libraries, and which one a component comes from

**Wear Compose Material 3 is the first library; Horologist is the second.** The kit does not stop at
what `androidx.wear.compose:compose-material3` publishes — its `Media-Player` set is a whole screen,
and Wear Compose ships no media player. [Horologist](https://github.com/google/horologist) publishes
that screen and its parts, plus the sign-in surfaces and the fast-scrolling list. They are in, under
three rules:

- **The `*-material3` artifacts only.** Horologist still ships its Material 2 line under the
  un-suffixed names; a sticker drawn from those compares the kit against the wrong design system,
  silently.
- **They live under `section = "Horologist"`.** The other sections are the kit's contents pages, and a
  reader must be able to tell at a glance which library a card's composable comes from. Groups within
  it: `Media controls`, `Sign-in`, `Fast scrolling`.
- **Both doors are open, and the kit's door is preferred where the kit can be compared against.** Today
  none go through it, for three reasons worth keeping apart: the kit has no such set; the only node
  drawing it is one of the kit's **private** `.Base / Media / …` sets, which the kit walk does not
  publish; or the set is published but its cell does not export as the component. A `noReference` for
  either of the last two should name the node it is talking about.

**Composition alone is not a reason to exclude** — what matters is whether *a library* publishes the
thing as a component you call. Stateful, ViewModel-driven entry points stay out either way:
`auth-ui-material3`'s `SignInPromptScreen` drives a real auth repository, so a sticker for it would be
a sticker for a fake; the stateless `auth-composables-material3` half is publishable. Horologist's
Lottie-animated media buttons stay out because the Robolectric renderer does not resolve a Lottie
composition.

**Naming is Compose's call.** Ids follow the Wear Compose API surface, because that is what a reader
greps for. The one hard rule is not to borrow a kit word for something the kit uses differently. Where
Compose has no name of its own, take the kit's.

## The coverage record

[`kit-sets.json`](kit-sets.json) lists **every published set in the kit** with either the components
that reproduce it or a stated reason it is absent, and `CatalogKitCoverageTest` fails in both
directions: a set with neither, and an exclusion whose node something now references. The rows come
from the kit walk in [`figma-refs.yml`](.github/workflows/figma-refs.yml) — re-run it when the kit
moves and reconcile the file in the same commit. Private sets (names beginning `.`, and each page's
`Base components`) and the Icons page are out of scope.

**That record works at the level of the SET, and the level below it is where things go missing.**
[`kit-cells.json`](kit-cells.json) is that missing number, for **both sheets**: per set, how many cells
the kit publishes, how many each sheet draws, and the kit's own vector for every cell it does not. It
is an OUTPUT — `scripts/kit-cells.sh` projects it from each module's resolved design map joined to the
kit index. Do not hand-edit it, and do not re-derive the numerator from the annotations: whether a cell
resolves is `@design-parity/kit-index`'s judgement. **WHY a sheet falls short is prose, and it goes on
the `kit-sets.json` row** under `cells`, keyed by sheet — a written reason in a generated file is a
merge conflict waiting to happen. `KitCellCoverageTest` fails on a gap with no reason and on a reason
that has outlived its gap.

**A cell whose API exists is drawn even when the library draws it wrong.** Publishing a blank, or a
picture identical to its neighbour, puts the defect where a reader meets it and lets a design-led scan
score it; withdrawing it leaves the set reading as unreproduced, which looks like nobody having got to
it. `StickerBakeCoverageTest.knownBlank` and the two `knownDuplicate` maps are how such a cell is
published rather than hidden. Each entry names the call that causes it, and each fails in the other
direction when the library is fixed — so **re-test a gap held open by a library limitation rather than
re-reading it.** Withdraw a cell only when there is no API to call at all (`RemoteTitleCard` takes no
painter argument), never because the result is ugly.

## Sticker conventions

- **A sticker says what the kit says.** A button the kit labels `Primary label` must not be labelled
  `Filled` — the difference is reported as a difference and, since a sticker is cropped to what it
  draws, it changes the outline too, which gets the reference squashed into the wrong frame. Take every
  string from [`KitCopy`](catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogCopy.kt) through
  `kitCopy(key, kit)`, never as a literal; where a kit cell truncates its text, the constant is the
  **full** string. Adding a component means adding its kit strings there, with the node they came from.
  `Motion.kt` is outside this — its recordings answer to no kit node.
- **The reference must be the same SHAPE of artwork as the render.** The kit publishes three kinds of
  cell and only sizes tell them apart: *component* (`172×52`, `192×59` — pair with `Sticker`), *display*
  (`192×192` — `FullScreenSticker` on a round device), and *long scroll* (`192×354`…`192×500` — which
  **nothing here can be diffed against**). Where a set publishes both, as `Dialog` does behind
  `Scrolling=`, take the display cell.
- **The FRAME follows the cell, and it is the half that gets forgotten.** The reference is fitted to the
  render's frame before it is diffed, so the wrong frame lands exactly where the wrong cell does. **A
  component "feeling" full-screen is not the test; the cell's size is.** So is the *size within* a cell:
  the kit's four `Edge-Button` sizes are `EdgeButtonSize` plus its 3dp floor (`49=46+3` … `99=96+3`),
  one-to-one. See [docs/DESIGN_MAP.md](docs/DESIGN_MAP.md).
- **A full-screen sticker renders at every screen size the kit recognises, and they fold.** The kit
  enumerates its sizes in `.WatchPuck` — 192, 204, 216, 225 (the breakpoint), 240 — and
  `CatalogFullScreenModes` draws all five. **192 is the base**, because the kit *draws* every screen
  cell at 192×192; the rest fold under it as `<dp>dp` cells. **Do not add `id:wearos_large_round`** — it
  is 227dp, a size the kit does not draw, and rendering there puts a scale difference under every
  full-screen comparison.
- One file per component **group**, opening with `@file:CatalogGroup(name = …, section = …)`.
- Every `@CatalogComponent` carries a `caption`; a test fails the build for one that does not.
- **Fold variants behind defaults.** A state / content axis is an `@OverrideVariant` cell (or a
  `@CatalogVariant(of = …)`) under its parent, not a new top-level component. A sheet this size is only
  browsable because the card count tracks components, not renders.
- **A cell that turns ONE knob is primary; a CROSSING is `secondary = true`.** Drawing the kit
  exhaustively puts 89 cells on `SegmentedProgress`, and a component subtree nobody can read is the same
  failure as a grid nobody can read. **Only the listing changes** — the cell still renders, bakes, keeps
  its own `/p/` URL and pairs with its kit node. **Never reach for `secondary` to hide a cell that is
  wrong**; that is a defect, and demoting it only makes it harder to find.

  The line is what a reader BROWSES BY. One knob off the base is a question somebody asks — "what does
  the disabled one look like", "the pentagon" — so all 35 shape cells and all 14 `Segments=` cells stay
  primary. Two or more at once is a crossing: nobody navigates to `segments-11-small-stroke-overflow` by
  name. Count the KNOBS, not the `kitProps` entries — the kit spells one choice as several properties
  wherever its axes are coupled, and `Button`'s `icon`, `ArcProgressIndicator`'s `overflow`, the toggle
  buttons' `disabled` and `Date Picker`'s `year-first` are each one choice and stay primary.
- **One kit component set is one catalog component — unless the axis is a different function.** A kit
  variant property folds in as a cell: all 35 shapes are cells of one `Shape/MaterialShapes`. The
  carve-out is an axis whose values are **separate Wear Compose functions** — `Style=` on the kit's
  `Button` set is `Button` / `FilledTonalButton` / `OutlinedButton` / `ChildButton`, and `Type=` on
  `Toggle+Selection-Buttons` is `CheckboxButton` / `SwitchButton` / `RadioButton`. What stays folded is
  what is an **argument** to whichever function you picked: `enabled`, size, whether there is an icon,
  split vs whole. The test is the call site, not the word.
- **A knob per parameter, named after the parameter, and a `previewOverrideChoice` wherever the values
  are a closed set.** The controls panel is built from the `previewOverride*` calls a sticker makes, and
  it is all a reader has: a parameter the sticker pins is one they cannot reach, and a knob named after
  anything but its parameter is one they cannot look up in the API. Spell the key exactly as Compose
  does (`value`, not the kit's `level`) — the kit's word belongs on the cell as `kitAxis` / `kitValue`.
  A plain `previewOverrideString` renders a text box that hides every alternative. Off the panel:
  `colors` / `shape` / `modifier`, theme-level objects rather than scalars a reader can type, and any
  value the component does not take.

  Knobs are additive — keep each default at what the sticker already rendered, so the baked captures and
  their kit comparisons do not move. **Check the state a knob feeds is keyed on it**: `remember` and
  `rememberSaveable` read their initial value once, so a knob wired into one moves nothing in a live
  session while looking correct in every baked render. Use `remember(knob)` or `key(knob) { … }`.
- **Name the kit's word on the cell, not only in the seed.** `@OverrideVariant(kitAxis = …,
  kitValue = …)` is what lets a Compose-shaped knob (`enabled=false`) resolve against the kit's
  `Disabled=Yes`. Use it by default on a cell whose knob is not already spelled the kit's way. Without
  it the seed must spell what the kit says: `shape=pentagon` resolves to nothing against the kit's
  `Shape=Pantagon` (its own spelling) and drops that node from the comparison with no diagnostic.
- Component ids are the published sticker's URL and the join key for `@CatalogVariant(of = …)`. Renaming
  one moves a published URL — do it deliberately.
- **A live click answers with the component, not with the label.** `counted` returns the label it was
  given and a live-lane-only handler; the ripple, state layer and pressed shape are what a click is
  supposed to show. The `(n)` tally is the `clickCount` knob, off by default. See
  `CatalogInteractive.kt`.
- **A wrap sticker is cropped tight — no decorative padding in the capture.** design-parity rasterises
  the reference to the *candidate's* width, so 16dp on a 136dp frame is a 12% zoom error plus a top-left
  offset, and components matching the kit pixel-for-pixel then report ~30% differing. A component that
  genuinely draws outside its bounds — a shadow, a focus ring — asks with `@CaptureGutter`, which
  extends the capture without changing what the composable measures. That is for a real gutter only.
  **A gutter reaches the baked capture and the override-free live render, and not the live daemon render
  behind a theme or a knob** — `?themeProvider=` and `?knob.` come back cropped to the bare frame
  ([compose-ai-tools#4822](https://github.com/yschimke/compose-ai-tools/issues/4822)). Fix that there;
  padding a frame to cover it reinstates the margin this rule took out.
- **Dark-first, transparent.** A component sticker is a single dark capture on a transparent background
  (`@CatalogModes`). A component the kit draws on a display cell — scaffolds, lists, dialogs, pickers,
  swipe-to-reveal — takes `FullScreenSticker` and `@CatalogFullScreenModes` instead.
- **A display cell the kit exports *transparent* takes `TransparentScreenSticker` and
  `@CatalogTransparentScreenModes`** — the rails, page indicators, fixed clock, circular progress cell.
  Same device, clip and fan-out; no fill. Check the reference before choosing: the kit exports most
  display cells over black. **Both halves are load-bearing** — the black disc has two independent
  sources, the preview's `showBackground = true, backgroundColor = 0xFF000000` and the
  `background(colorScheme.background)` the frame paints over it, so changing either alone leaves the
  render pixel-for-pixel identical.
- **A control the kit draws across its content column takes `Modifier.kitRowWidth()`.** Wear's `Button`
  applies no `fillMaxWidth` of its own — `Card`, `Slider` and `Stepper` do — so a button given no width
  hugs its label and publishes at 120dp against a 172dp kit cell, rescaling the whole comparison.
  `fillMaxWidth()` is not the fix either: the sandbox is bounded at 227dp, so it resolves to 211dp.
  Components *supposed* to size to their content — icon buttons, the compact button, the text specimens
  — must NOT take it.
- Renders must be **deterministic**: a `TimeText` is pinned to a fixed instant, never the system clock.
- Every published comparison must invoke the actual named composable. Rebuilding a component from `Box`
  and its `*Defaults` can make a replica line up, but it cannot test the library. The shape specimens
  are the documented exception in the other direction: they draw mobile `MaterialShapes` because Wear
  publishes no shape library of its own (see README).

## Themes

The declared themes in `CatalogThemes.kt` are **not inventory** — no `@CatalogComponent`, no kit node,
no `kit-sets.json` row. A theme is a re-skin of an existing member.

- **A sticker frame installs its theme through `CatalogMaterialTheme`, never a bare
  `MaterialTheme { … }`.** A `@WearThemeCatalog` provider wraps the sticker from the outside and an
  inner theme silently shadows it: every entry in the switcher then renders identical pixels.
- **`@WearThemeCatalog`, not the mobile `@ThemeCatalog`.** The mobile one's specimen reads
  `androidx.compose.material3.MaterialTheme`, which these providers never install, so the sheet reports
  the mobile baseline instead of the theme. Enforced by `CatalogInventoryTest`.
- **A theme carries a type scale, not only a palette.** Re-point every role explicitly:
  `Typography(defaultFontFamily = …)` is a no-op on Wear, since every stock role already declares a
  family.
- **Reproduce a borrowed theme by its recipe, not its output.** The Confetti palettes run the same seed
  through the same library Confetti uses; a transcribed role table drifts the first time either side
  moves.
- **Both modules declare the SAME theme set.** `CatalogThemes.kt` and
  `remote-catalog/…/RemoteThemeCatalogs.kt` publish the same six names in the same two groups, from the
  same four seeds through the same `materialkolor` recipe — the compare page reads the two columns theme
  by theme. Add, rename or reseed in **both**, in one PR. The seeds are duplicated (different dependency
  lines, no shared constant) and each side's test pins the literals, so a one-sided edit fails the
  other.
- **What a Remote theme can carry is narrower, and that is not a licence to diverge.** A recorded
  document is re-themed by overriding named colour state (`USER:WearM3.<role>`), so the Remote side
  publishes a theme's *colours* mapped onto those 29 roles and its *faces* as data for a player lane to
  resolve — never a `Typography`. Same names, same palettes; only the mechanism differs.

## Motion

`Motion.kt` carries the recordings, and they are **outside the component inventory** on purpose — no
`@CatalogComponent`, so they answer to no kit node and change no taxonomy.

- **Do not put `@AnimatedPreview` on a component that has cells.** It rides every `@OverrideVariant`
  too, and the animated path does not apply a cell's knobs — the cells come out as byte-identical copies
  of the base recording under different names.
- **Pin the canvas.** A motion capture needs `widthDp` AND `heightDp`; an unpinned capture fails with
  "produced no GIF".
- **`@InteractionPreview` is the right tool when the motion IS the press.** It dispatches a real pointer
  at nodes resolved from the live semantics tree, and the Android backend advances the **main looper**
  per frame — the only way a platform `RippleDrawable` moves at all. A `LaunchedEffect` is still correct
  where there is no finger: a spinner, a shimmer, a switch shown resolving both ways.
- **A press is not automatically recordable — measure before claiming it is.** Two Horologist transport
  rows, same annotation, same targets: `PodcastControlButtons` opts its side buttons into
  `ButtonGroupScope.animateWidth` and gives 111 pixel-distinct frames of 178; `MediaControlButtons`
  never calls it and gives **1 of 178**.
- **A STILL of a time-driven reveal takes `@SettledPreview`, and nothing else does.** A component whose
  content arrives on a `LaunchedEffect` or a tween captures as its FIRST frame without one. The
  annotation advances the paused clock until the composition stops changing, then captures. Not for a
  **spinner** (an `InfiniteTransition` never quiesces, so it burns the budget and captures an arbitrary
  phase — those keep their `still_changing` warning;
  [compose-ai-tools#4829](https://github.com/yschimke/compose-ai-tools/issues/4829)), and not for a
  **scroll** (`@ScrollingPreview(END)` is for a component a scroll actually moves).
- **A placeholder only animates under an `AppScaffold`.** `PlaceholderState` reads its frame clock from
  the library's internal `AnimationCoordinator`, and `AppScaffold` is the one thing in Wear Compose
  composing that coordinator's looper. `AnimatedSticker` is that scaffold and nothing else.

A recording must actually move: `CatalogRenderTest` fails a GIF with fewer than six distinct frames. If
a component does not animate under this renderer, publish no recording rather than one that implies
motion nobody would see — but rule out a missing wrapper first.

## The catalog over MCP

`.mcp.json` registers the hosted catalog server (`compose-preview-catalog`,
`POST https://preview.coo.ee/mcp`) for every agent that reads project-scoped MCP config.

- **Default to this repository's own catalogs**, and pick the one matching the module you are in:
  `wear-m3-catalog` is `:catalog`, `remote-m3` is `:remote-catalog`. The endpoint is the aggregate one
  deliberately — `m3-catalog` and the app catalogs stay reachable for a cross-catalog comparison. Reach
  for a neighbour on purpose, not by leaving the `catalog` argument off.
- **No credential is committed, and none may be.** The file passes
  `X-Compose-Preview-Token: ${COMPOSE_PREVIEW_TOKEN:-}`, so a session exporting a grant token uses it and
  one that does not sends an empty header. Reading a catalog needs a short-lived grant; `initialize`,
  `ping`, `tools/list`, `request_access` and `poll_access` do not.
- **Getting a grant in-band:** call `request_access`, show the human its `approveUrl` and `userCode`,
  poll `poll_access` until it answers `approved`, then export the bearer as `COMPOSE_PREVIEW_TOKEN` and
  reconnect the server. The last step is not optional — an MCP host cannot inject a header its config
  never declared.

### The UI builder is on that same endpoint

There is no second server to register. The `ui_builder_*` tools arrive on the endpoint `.mcp.json`
already declares; pointing a second entry at `/ui-builder/mcp` gets a `404`. A sidecar on its own path
was designed and rejected — an agent holds exactly one bearer for the box, and two endpoints would mean
two origin checks, two body caps and two places to drift about what a grant means
([`CATALOG_MCP.md`](https://github.com/yschimke/compose-preview-server/blob/main/docs/design/CATALOG_MCP.md#relationship-to-ui-builder-mcp)).

| Tool | Needs |
| --- | --- |
| `ui_builder_list_catalogs`, `ui_builder_list_designs`, `ui_builder_get_design` | `ui-builder-read` |
| `ui_builder_create_design`, `ui_builder_apply` | `ui-builder-write` |
| `ui_builder_export`, `ui_builder_render_native` | `ui-builder-export` |

- **Capabilities are not scopes, and you have to ask for them.** `preview` and `live` do not carry the
  builder; pass `capabilities: ["ui-builder-read", …]` to `request_access` alongside `scope`. Every tool
  is then checked per call against the same `UiBuilderRouteCapability` mapping the browser's Design API
  uses.
- **A design pins a component catalog, and the pin is checked.** Start at `ui_builder_list_catalogs` so
  the `catalogPin` names a real revision. There is deliberately no blank-template argument: create from
  a whole `document`, or `fromDesignId` to inherit a pin that is real by construction.
- **`baseRevision` is required, but it is not a revision lock.** An apply quoting a stale base whose
  edits touch nothing that moved since is **accepted**, at a new revision, with `conflicts: []`. Quote
  the revision `ui_builder_get_design` returned — that is what makes the conflict report meaningful.
- **What the service does refuse and deduplicate.** Re-sending an `operationId` already applied answers
  `idempotentReplay: true` at the revision it first committed, so `operationId` is yours to choose and
  makes a retry safe. A *new* operation reusing an existing node id is rejected outright:
  `code: invalidCommand`, `node id is blank or already used`.
- **The tools are absent unless the box serves a builder** (`--ui-builder-dir`) — absent rather than
  listed-and-failing. `tools/list` needs no grant, so checking costs one unauthenticated call.

## Kotlin

- ktfmt Google style, 100 columns. `./gradlew ktfmtFormat`.
- Kotlin block comments **nest**, so `/*` inside a KDoc opens a nested comment and swallows the rest of
  the file.

## Git

- **`main` is protected — every change goes through a PR.** The `Protect Main` ruleset requires a pull
  request (0 approvals) with all CI checks green, and squash is the only merge method. Branch names are
  `agent/…`.
- Conventional commit subjects (`feat:`, `fix:`, `docs:`, `chore:`). The squash commit is built from the
  **PR title**, so write the PR title as the commit subject.
- **Never attribute a commit to an AI agent** — no `Co-authored-by:` trailer naming an agent, and no
  agent author/committer identity. Links to an agent session and the `_Generated by [Claude Code]_`
  footer are fine; they don't claim authorship. Enforced by one detector
  ([`.github/scripts/agent-attribution-scan.sh`](.github/scripts/agent-attribution-scan.sh)) in two
  places: the hooks in `.githooks/` (install with `scripts/install-git-hooks.sh`) and the
  [`No Agent Attribution`](.github/workflows/no-agent-attribution.yml) CI gate. **Scrub the PR
  description too, not just the commits** — GitHub builds the squash message from the PR title + body
  and credits every distinct branch commit author, server-side, after the last hook has run.
- **Reviewers, human or bot: verify before reporting an attribution finding.** It is the most-reported
  and least-real finding across these repositories, and the reports keep naming commits that do not
  exist here at all. The identity is whatever `git config user.email` held, and here that is the human.
  Check first:

      git cat-file -t <sha>          # a commit you are about to name must actually exist
      git log --format='%h %an <%ae> | %cn <%ce>' <base>..<head>
      .github/scripts/agent-attribution-scan.sh --range '<base>..<head>'

  Report only what the scanner exits 1 on, and quote its output. `Yuri Schimke <yuri@schimke.ee>` and
  the `github-actions[bot]` / `renovate[bot]` accounts are never findings. If the `Reject agent
  attribution` check is green on the head commit, there is nothing to report.
- **Point every image embed at a GitHub-hosted URL — a `preview.coo.ee` embed does not survive.** Claude
  Code on the web rewrites `![alt](url)` to `[alt](url)` on the way to the API whenever the destination
  is not a GitHub origin, silently: the API returns 201 and the tool reports success. It is an
  anti-exfiltration control rather than a bug to route around
  ([anthropics/claude-code#89540](https://github.com/anthropics/claude-code/issues/89540)), and the
  rewrite is a blind regex over the whole body — it fires inside code spans and fenced blocks, and an
  `<img src=…>` is HTML-escaped into a code span. **Kept:** `raw.githubusercontent.com`,
  `github.com/<owner>/<repo>/raw/<ref>/…`, `github.com/user-attachments/assets/…`, and the `user-images`
  / `private-user-images` / `avatars` / `objects` / `media` / `gist` `.githubusercontent.com` hosts.
  **Stripped:** everything else, `camo.githubusercontent.com` and `img.shields.io` included. Applies to
  issue bodies and comments as much as PR descriptions.

  This costs nothing, because every published render already has a twin on its delivery branch:

  | `preview.coo.ee` | `raw.githubusercontent.com/yschimke/wear-m3-catalog` |
  | --- | --- |
  | `/<system>/render/<component>__<rest>.png` | `/design-artifacts/<system>/images/<component>/<rest>.png` |
  | `/<system>/reference/<id>.png` | `/design-artifacts/<system>/references/<id>.png` |

  Split a render id on its **first** `__` and the head is the directory:
  `appcard__ideal__outlined__compact` is `images/appcard/ideal__outlined__compact.png`. A reference keeps
  its whole id. Pin to a commit SHA rather than the branch name — `design-artifacts/*` is rewritten on
  every republish. `preview.coo.ee` stays the right thing to *link* to in prose; only **embeds** need
  the GitHub origin.
- **Write `![alt](url)` in a PR body and leave the backticks alone if they appear.** A posted description
  often lands as ``![alt](`url`)``, which GitHub renders as literal text plus a stray code span. The
  backticks are injected between the agent and GitHub, not authored, and re-posting a "corrected"
  version brings them back. The [`PR Body Syntax`](.github/workflows/pr-body-syntax.yml) workflow
  rewrites the body in place, stripping only backticks that touch a link destination — **a description
  changing under you is that repair, not a reviewer.** It does not cover an image in a *review comment*,
  a destination that no longer looks like one, or proving the picture rendered. Committed render PNGs
  live in [`docs/evidence/`](docs/evidence/), linked commit-pinned.

## Dependencies

- **Renovate owns the version bumps; don't hand-bump.** `.github/renovate.json` automerges anything that
  is not a major once CI is green. Change the config instead when the policy is wrong.
- Three groups are deliberately **not** automerged: **majors**, **Compose** (Wear Compose plus the
  mobile BOM and the material3 pin), and **Horologist**. All change what the catalog renders, and the
  render is the product — a human reads the visual diff before it lands. Horologist ships as one release
  (a skew is a compile error) and only its `*-material3` artifacts are dependencies here.
- **The preview coordinates come from two repositories, on two lines.** The plugin marker and the
  pinned CI action ref are compose-ai-tools' (`composePreviewCore`) and must not skew — a skew breaks
  preview discovery outright. `preview-annotations`, `data-preview-overrides-runtime` and
  `data-remotecompose-connector` publish from compose-preview-daemon (`composePreviewDaemon`) on a
  line of their own since compose-ai-tools#5336. Two refs and two Renovate groups; pinning both to
  one ref took `main` red at configuration time when the two lines still shared a repository. Note
  that a GitHub release tag exists for every version a line publishes, so a tag can resolve as an
  ACTION ref while the plugin at that version does not exist on Central.
- **The alpha Remote line is watched, not bumped.** `remote-snapshot-probe.yml` builds
  `:remote-catalog` against the newest androidx.dev snapshot every Monday and comments on
  [#95](https://github.com/yschimke/wear-m3-catalog/issues/95) only when the picture moves. Its overlay
  is applied to the runner's checkout and thrown away; state lives on `snapshot-probe/remote-m3`. **A
  snapshot pin must never change what the BUILD resolves** — `gradle/libs.versions.toml` stays on
  released alphas, and `:catalog` stays outside the lane entirely.
- **The snapshot LANE is selected by a FILE, not a flag.** `.github/ci/remote-snapshot-pin` — one line,
  an androidx.dev build id or `latest` — repoints `:remote-catalog` and only it: `settings.gradle.kts`
  adds the repository behind a `content` filter admitting the Remote groups and nothing else, and
  `remote-catalog/build.gradle.kts` applies the `1.0.0-SNAPSHOT` substitution to that module's own
  configurations. Both read the pin file, with `-PremoteSnapshot=<id>` as a per-invocation override; an
  empty or absent pin is the released line.

  **It reads the file rather than requiring a property because a property could not reach the render.**
  The publishing workflows render through reusable workflows this repo cannot pass arguments to, and
  their one hook — `design-map-command` — runs after the render, so a `remoteSnapshot=` there reaches
  the map and not the stickers. `src/released/kotlin` and `src/snapshot/kotlin` are the lanes' source
  sets, exactly one on the path at a time, and the tests recording library behaviour (`knownDuplicate`,
  `knownBlank`) branch on `wearm3.remoteLane` because those lists are claims about a library the lane
  changes.
- **Re-discover on the released lane before regenerating any committed record.** `design-map.json`,
  `kit-cells.json` and `docs/KIT_COVERAGE.md` are projected from `build/compose-previews/previews.json`,
  and a snapshot-lane run leaves that manifest holding components `main` does not have — regenerating
  then commits a snapshot-only component into a record CI validates on the released lane. Restore it
  with `./gradlew :catalog:composePreviewDiscover :remote-catalog:composePreviewDiscover` and
  `-PremoteSnapshot=` — an EMPTY property, passed explicitly, because the pin file is on by default.
- **The published `remote-m3` SHEET and BOARD are both drawn on the SNAPSHOT lane**, at the one pinned
  build id, read by `design-artifacts.yml` (the sheet) and `design-parity.yml` (the board). The Remote
  sheet reproduces a kit whose components `remote-material3` has only just begun publishing, so a sheet
  restricted to the released line would report those kit sets as undrawn for as long as the release
  takes. **The two move together or not at all**: a board scoring a sheet built from different bytes
  makes every difference unattributable, which is why the pin is one file rather than a literal in each.
  The `wear-m3-catalog` sheet and board are NOT on the lane and must not be. **Pinned, never `latest`**
  — a floating pin would move the verdict with no commit to explain it. Bump deliberately and read the
  visual diff; the pin is in the parity job's `cache-paths` so a bump forces a re-render. The cost:
  neither is reproducible from released artifacts alone, and androidx.dev does not keep builds forever.
- **A component this catalog is WAITING FOR is tracked by SYMBOL, in `AWAITED_API`**
  (`scripts/remote-snapshot-probe.py`). Each entry names the class the library would have to publish,
  what drawing it would unlock, and a link to the upstream change. Watch the SYMBOL, never the change: a
  merged change is not a published artifact. Retire an entry the week it lands — a watch reporting
  "present" every week is the same silence-by-noise `PROBES` avoids.
- **Glance Wear is held at its release even on the snapshot lane**, behind a second opt-in
  (`-PremoteSnapshotGlance=true`). `WearWidgetPreview` gained a `boolean` parameter after alpha17, and
  this module's sources recompile against it fine. What does not is
  `ee.schimke.composeai:wear-preview-runtime`, whose `CapturingWearWidgetPreview` is a pre-compiled call
  to the old signature, so the widget-container stickers die at RENDER time with `NoSuchMethodError`
  while the build stays green. Compiling is not the check; rendering is.
- Repository settings — squash-only merges, auto-merge, and the `Protect Main` ruleset — are applied by
  `scripts/setup-repo-protection.sh`. They need an admin token, so no workflow or agent session can set
  them; re-running the script repairs drift. `DRY_RUN=1` prints without writing.

## Verifying a change

```sh
./gradlew :catalog:assembleDebug :catalog:composePreviewDiscover \
          :remote-catalog:assembleDebug :remote-catalog:composePreviewDiscover \
          test ktfmtCheck
```

`composePreviewDiscover` is the real contract: it turns the annotations into the published inventory. A
component that compiles but is not discovered vanishes from the sheet silently.
