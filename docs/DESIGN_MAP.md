# design-map.json, and what a reference has to name

`design-map.json` is the correspondence file design-parity reads to know which kit node a rendered
component is meant to look like. It is not hand-written: [`scripts/design-map.sh`](../scripts/design-map.sh)
projects it out of the discovered preview manifest, in two steps that live in two upstream packages
because the question splits there.

1. **`@yschimke/compose-design-map`** knows what the ANNOTATIONS mean. It joins each component's
   `@CatalogComponent(reference = …)` to one of its rendered captures and emits the base map, plus
   a sidecar (`design-map-variants.json`) declaring which other previews are the same component
   with knobs turned.
2. **`@design-parity/kit-index`** knows what the KIT means. `shape=square` is a fact about a Compose
   API and `Shape=Square` is a fact about the Wear kit; it resolves those declarations against the
   committed [`figma-kit-index.json`](../figma-kit-index.json) into tagged ref/previewId pairs.

Both are pinned to an exact version, because both outputs are committed and CI fails on any
difference.

## A reference names one VARIANT, never the set

This is the rule the whole file hangs on, and it is easy to get wrong in the direction that still
looks like it works.

The kit publishes a component as a `COMPONENT_SET`: a grid whose cells are the axis vectors. The
kit's `Button` set is 50 cells over `Style / Icon / Icon size / Alignment / Disabled`. A
`reference` must name **one of those cells**, not the set frame:

```kotlin
@CatalogComponent(
  id = "Button/Filled",
  // Style=Filled, Icon=No, Icon size=n/a, Alignment=Center, Disabled=No — the cell this sticker
  // actually draws.
  reference = "figma:B24oss2tTeXAFykyeyusz0/35239:93092",
  // The set it is a cell of. Not the thing parity diffs; the handle a whole-screen import matches
  // an instance through, and the join key `kit-sets.json` and CatalogKitCoverageTest use.
  referenceSet = "figma:B24oss2tTeXAFykyeyusz0/35239:93088",
)
```

Three things break when the reference names the set instead, and none of them announce themselves:

- **Parity diffs a sticker against a grid.** The set frame's own geometry is an editor artifact —
  a 1068×928 board of 50 buttons. Comparing one 52dp button against it reports the whole board as a
  difference, which is a finding nobody can act on.
- **No variant cell can resolve.** `@OverrideVariant(kitAxis = "Disabled", kitValue = "Yes")` means
  "the sibling of my reference with `Disabled=Yes`" — the resolver varies ONE axis from a known
  vector. A set has no vector to vary from, so every cell resolves to nothing, silently.
- **The imported kit pages barely link.** The node → code join is by node id, so a set-level map
  links only the 33 set frames out of 1845 walked nodes. At variant granularity the same map links
  181, and each of them is a cell a reader can click through to the render that reproduces it.

`CatalogInventoryTest` checks that a reference exists and names this kit's file key.
`CatalogKitCoverageTest` joins on `referenceSet`, because [`kit-sets.json`](../kit-sets.json) has
one row per published set.

## …and the variant has to be the same SHAPE of artwork

Naming a cell is necessary and not sufficient. The kit's cells are not all pictures of the same
kind of thing, and a comparison only means something when the render and the reference are pictures
of the same kind. Three shapes appear in this kit, and the sizes are how you tell them apart:

| kit cell | size | what it is | what compares to it |
| --- | --- | --- | --- |
| component | `172×52`, `192×59`, … | the component alone, cropped to itself | a `Sticker` — wraps and is cropped |
| display | `192×192` (and `225×225`) | the round watch face, whole | a `FullScreenSticker` on a round device |
| long scroll | `192×354` … `192×500` | a scrolling screen unrolled past the bottom of any watch | **nothing this catalog publishes** |

The third one is a trap, because it is a perfectly good reference that no device-framed render can
be diffed against. `AlertDialog` was pointed at `58475:87041` — `Edge Option=Double Angled Button,
**Scrolling=Yes**`, 192×402 — while publishing a 454×454 capture of a round display. The comparison
squashes the reference to the render's aspect before diffing, so every element in it arrived ~2.1×
too short and the whole frame read as a difference. The finding was real and useless: it said "these
are different shapes", which was true of the mapping rather than of the component.

The `Dialog` set publishes both, and that is the fix — `Scrolling=No` cells are the same
arrangements drawn on a 192×192 display. The base moved to `58475:87077`, and because a cell
resolves by varying one axis from the base, the `edge-button` cell followed on its own from
`58475:87023` to `58475:87067`. No other referenced set has a `Scrolling=` axis, so the alert dialog
was the only one affected.

**When a set publishes both, take the display cell.** A long-scroll reference would need a preview
that captures its own full scroll extent rather than the display — which is a renderer capability,
not a mapping choice, and nothing here asks for it yet.

## The words are part of the mapping

A sticker and its reference have to say the same thing, and until recently none of them did. The
catalog labelled its filled button "Filled", its title card "Workout", its alert dialog "Delete this
run?"; the kit labels the same three "Primary label", "Title card title text lorem ipsum dolor sit
amet" and "Dialog title one to three lines". Every card in the sheet therefore carried a difference
that was only ever going to be reported as a difference.

`design-led` decides who moves: the kit is authoritative and cannot be written to from here
(AGENTS.md), so the catalog took the kit's copy as its **default**. It lives in one place —
[`CatalogCopy.kt`](../catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogCopy.kt) — with the
node each string is transcribed from.

Two things are worth knowing about why this mattered more than it sounds:

- **Copy moves geometry.** A component sticker is cropped to what it draws, so a button reading
  "Filled" is not a longer or shorter version of one reading "Primary label" — it is a different
  outline, and the reference then gets squashed into that frame to be compared. Matching the words
  fixed most of the shape mismatches without anything else changing.
- **It was hiding the real findings.** A wrong radius or a missing border is invisible in a diff
  already lit up by text, which is exactly the class of defect this repo exists to catch.

**The realistic copy is not gone, it is a knob.** Every string goes through `kitCopy(key, kit)`,
which is `previewOverrideString` with the kit's value as the default: the *baked* capture — the one
design-parity diffs — is always the kit's words, while the preview server's override panel retypes
any of them live. Overrides are not cells, so none of this added a render, a card, or a row here.

Where a kit cell shows its text truncated (the cards all do) the constant is the full string the kit
is truncating, so the ellipsis lands in the same place rather than the label simply being shorter.

## What is in the map today

| | |
| --- | --- |
| components mapped | 47 |
| variant cells resolved onto a kit node | 131 of 191 |
| …of which breakpoint cells, which no kit node answers yet | 44 |
| kit page nodes joined to code | 181 of 1845 |
| components with no reference, for a stated reason | 7 |

## The two kinds of miss, and which one is a bug

The projector reports them apart, and that separation is the point of reading its output at all.

**A stated absence is not a gap.** Seven components carry `noReference = "<why the kit has none>"`,
and they enter through the second door in [`AGENTS.md`](../AGENTS.md) for two distinct reasons:

- **The kit publishes no such component.** `ButtonGroup`, `TransformingLazyColumn`, `Scaffold` and
  `ArcProgressIndicator` are Wear Compose components with no kit set at all.
- **The kit publishes it, but only in a shape this catalog cannot compare against.**
  `EdgeButton/Screen` is a display and the kit's edge-button cells are all components;
  `SwipeToReveal/Card` and `SwipeToReveal/Button` are components and the kit's STR cells are all
  displays. See *Component or screen* below — the absence is about the framing, not the drawing.

This is what `scripts/design-map.sh` runs `--strict --allow-stated-absence` for. Plain `--strict`
gates on every kind of absence including a stated one, so it failed this repo on all seven; the
opt-in narrows it back to what it is actually for — still fatal on a missing reference and on
captures that pair with none, permissive about one somebody already looked at and wrote down. It
landed upstream in `@yschimke/compose-design-map` v1.19.0
([compose-ai-tools#4250](https://github.com/yschimke/compose-ai-tools/pull/4250)); before it, the
choice here was plain `--strict` (red on intended components) or no gate at all.

**An unresolved variant cell is usually the kit's matrix, not a mistake.** 60 of 191 do not resolve.
Forty-four are breakpoint cells, which no kit node answers because only `Picker` publishes a second
size (see *Every screen size the kit recognises*). The other sixteen fall into two shapes:

- *The kit has no such axis.* `LevelIndicator`'s `low` / `full` turn a Compose float; the kit's
  `Level-Indicator-RSB` varies `Size`, not level. `CircularProgressIndicator`'s `indeterminate` has
  no kit counterpart at all. Nothing to name, so these cells carry no `kitAxis`.
- *The kit couples two axes and the cell can only name one.* The `Button` set has
  `Icon size=n/a` exactly when `Icon=No`, so moving `Icon=No → Yes` is inherently a two-axis move
  and no sibling exists one step away. `AlertDialog`'s `Edge Option=None` only ever appears with
  `Bottom=No`; `DatePicker`'s `Year first` only with `Limit=Past only`/`Future only`;
  `IconToggleButton`'s `Selected=Off` only with `Style=Tonal`. `@OverrideVariant` carries one
  `kitAxis`/`kitValue` pair, so these are reported rather than guessed at — pairing the wrong cell
  would diff a whole palette and call it a finding.

## Every screen size the kit recognises

A full-screen sticker publishes one capture per screen size, and the sizes are the **kit's own**.
It enumerates them in its private `.WatchPuck` set on the Meta Components page:

| kit name | dp | how it is rendered here |
| --- | --- | --- |
| `xSml 192 (Legacy)` | 192 | `id:wearos_small_round` — **the base** |
| `Sml 204` | 204 | `spec:width=204dp,…,dpi=320,isRound=true` |
| `Med 216` | 216 | `spec:width=216dp,…` |
| `Lrg 225 (breakpoint)` | 225 | `spec:width=225dp,…` |
| `xLrg 240` | 240 | `id:wearos_xl_round` |

Two things about that table are worth writing down, because both are easy to get wrong.

**`id:wearos_large_round` is deliberately absent.** It is 227dp, which is not a size the kit draws —
it sits between `Lrg 225` and `xLrg 240`. Rendering every full-screen sticker there is what put a
scale difference under every full-screen comparison: a 52dp button was 27% of the kit's frame and
23% of ours, and no amount of matching content was going to close that. Wear tooling publishes named
device ids for only two of the kit's five sizes, so the middle three are `spec:` strings at the same
2.0 density — which the renderer handles, whatever the note in the upstream Wear sample says.

**192 is the base, and the kit's own table calls it legacy.** That is not a contradiction to resolve
in the catalog's favour: whatever the puck table says, the kit *draws* every one of its screen cells
at 192×192, so the base capture has to be the 192 one or the base comparison is not like-for-like.
The projector picks the narrowest by default, which is that, so `scripts/design-map.sh` passes no
`--base-breakpoint`.

The other four **fold under the base as `<dp>dp` cells** rather than becoming four more components —
a size is an argument to the same screen, not a different screen (AGENTS.md). That is a projector
capability rather than an annotation: a multipreview's per-device captures used to read as colour
modes, so a full-screen component dropped out of the map the moment it gained a second size. Fixed
in `@yschimke/compose-design-map` v1.20.0
([compose-ai-tools#4262](https://github.com/yschimke/compose-ai-tools/pull/4262)).

Only the `Picker` set publishes a second size in the kit (`Larger Screen (BP)=Yes` at 225), so the
rest of the breakpoint cells are renders with no kit counterpart — reported as such rather than
pretended to have matched. Resolving Picker's 225 cell onto that node needs a way to spell a kit
axis on a breakpoint, which no annotation carries yet.

## What still differs, and which kind of thing it is

With the copy aligned, what is left in the comparison is structural — which is the point. Recorded
here so the next reader can tell a known divergence from a new one.

**Findings against Wear Compose.** The library draws these; the catalog cannot change them without
misrepresenting it, and `design-led` says to write them down rather than work around them.

- `DatePicker` / `TimePicker` label their focused column from the library (`Day`, `Hour`); the kit
  draws the placeholder `Label`. Not a slot the caller supplies.
- `TimePicker` at midnight renders `12:00 AM` under `HoursMinutesAmPm12H`; the kit's cell reads
  `00 : 00 AM`. Both pinned to the same instant.
- `Stepper`'s increase/decrease glyphs are the Wear defaults (chevrons); the kit's cell draws
  app-supplied volume icons. `Stepper` takes them as slots, so the kit is showing an app's choice.
- `Picker`'s kit cell has a header and a confirm button around the wheel. The Compose `Picker` is
  the wheel alone — the surrounding screen is the caller's.

**Gaps in the icon set, not in the mapping.** The catalog depends on `material-icons-core`, which is
about forty glyphs. The kit's plus, cross and chevrons are all in it and are now used; its
circled-exclamation dialog icon and its headphones glyph are not, so those slots stay empty rather
than being filled with an approximation that would read as a *different* difference.

**Open, and deliberately not fixed here** — each moves a published preview URL, which AGENTS.md says
to do on purpose rather than in passing:


## Component or screen: the mapped sticker is the component

Two components were framed opposite to their kit cells, in opposite directions, and the rule that
settles both is the same one: **the mapped sticker is the component-shaped one, and a screen stays
unmapped.**

**`EdgeButton` gained a component.** All 64 cells of the kit's `Edge-Button` set are the button
alone, cropped to itself — 192dp wide by 49/59/73/99 for its four sizes — and there is no
display-shaped cell anywhere in it. This catalog published only a screen: a twelve-row list with the
button revealed at the bottom, because the scaffold reveals it from scroll state. A screenful of
list rows was being diffed against a bare button.

So there are two entries now. `EdgeButton` is the component, cropped at the kit's own 192dp width,
and it carries the reference and the whole `Style / Type / Size / Disabled` matrix — all seven cells
resolve onto distinct kit nodes. `EdgeButton/Screen` is the button where it actually lives, and it
is **unmapped**: the kit publishes nothing of that shape, which is a fact about the kit rather than
a gap here. It keeps no cells of its own — the matrix belongs to the mapped component, and
repeating it would publish the same seven knobs again, at five breakpoints each, against nothing
that can check them.

**`SwipeToReveal` lost its references**, for the mirror reason. All eight cells across `STR-card`
and `STR-button` are 192×192 **displays** — the watch face with the component mid-swipe, running off
the left edge. Not one is the component cropped to itself. These stickers are the opposite: the
whole component, wrap-and-cropped. Pointed at those cells they were diffing a 392×136 crop against a
round screen, with the comparison squashing one into the other first — the same category error the
alert dialog had against a long-scroll cell. Both now carry `noReference`, and their `kit-sets.json`
rows are stated exclusions. What would change that is a display-shaped STR sticker, which this
catalog does not have a frame for.

### What `EdgeButton` still differs on, and why it is left visible

The component sticker draws **131×70dp** where the kit's cell is **192×59**. Both halves are worth
knowing apart:

- **The width is the scaffold's doing.** `EdgeButton` measures to the width it is offered but draws
  to its content: given 192dp it lays out 192dp and paints a 131dp pill inside it. Neither
  `Modifier.width(192.dp)` on the button nor `fillMaxWidth()` changes that — both were tried and
  rendered identically. Its span across the display comes from `ScreenScaffold`, and in isolation
  there is nothing doing the stretching. The 192dp box is still there, because it pins the *crop*:
  without it the sticker crops to the renderer's 227dp measuring bound, a size the kit does not
  draw.
- **The 11dp of height is not.** That is `EdgeButtonSize.Medium` against the kit's `Size=Default`,
  and nothing about the framing explains it. It is a finding.

## Rebuilding the kit index without a Figma token

`figma-kit-index.json` is normally built by [`figma-refs.yml`](../.github/workflows/figma-refs.yml),
which holds the `FIGMA_TOKEN` and walks the REST API. Its output is authoritative and supersedes
anything below.

But the index is an INPUT to the map, so it going stale is silent: a variant the kit gained reads as
"no counterpart in the kit" rather than "nobody looked since". So it can also be rebuilt from
committed data alone:

```sh
node scripts/kit-inventory-from-pages.mjs --out figma-inventory.json
npx @design-parity/kit-index@0.1.53 build --file B24oss2tTeXAFykyeyusz0 \
  --map design-map.json --inventory figma-inventory.json --out figma-kit-index.json
```

[`scripts/kit-inventory-from-pages.mjs`](../scripts/kit-inventory-from-pages.mjs) re-shapes
`design/pages/pages.json` — the kit page walk `figma-pages.yml` already committed, listing every
component, set and instance it found with node ids and layer names — into the inventory
`kit-index build` reads. The index itself is still built by the real generator; only the walk is
substituted, and it is a walk of the same file made with the same credential.

What it cannot recover is component **properties** and configured **instance** vectors: the page
import records a node's id, name and type and nothing else. `kit-index build` treats both as
optional enrichment, so a token-less index is a strict subset — same sets, same variants, same ids,
no property tables. Axis-shaped variant cells resolve; property-shaped ones stay unresolved, which
is the honest answer rather than a guessed one.

## Refreshing the page join without a Figma token

The node → code join is a pure function of two committed files, but it used to be computed only
while fetching — so picking up a map change meant re-importing 22 pages and ~41 MB of SVG behind a
token nobody has locally. That made the join the stalest thing on the page, and it is the part that
changes every time a component is added or a reference is repointed.

```sh
node scripts/import-figma-pages.mjs --relink
```

recomputes it in place from `design-map.json`, touching no network and leaving the SVGs and the node
walk alone. It shares the import's own `linkNode` decorator, so the two cannot drift. A kit that has
actually moved still needs the real import.
