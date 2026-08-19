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
| components mapped | 49 |
| references, base + resolved variant | 182 |
| variant cells resolved onto a kit node | 133 of 149 |
| kit page nodes joined to code | 181 of 1845 |
| components with no reference, for a stated reason | 4 |

## The two kinds of miss, and which one is a bug

The projector reports them apart, and that separation is the point of reading its output at all.

**A stated absence is not a gap.** `ButtonGroup`, `TransformingLazyColumn`, `Scaffold` and
`ArcProgressIndicator` carry `noReference = "<why the kit has none>"` — they enter through the
second door in [`AGENTS.md`](../AGENTS.md), being Wear Compose components the kit never published.
This is also why `scripts/design-map.sh` does **not** pass `--strict`: that flag gates on every kind
of absence including a stated one, so it fails this repo on four components that are exactly as
intended. What `--strict` was here to catch — silence, a component with neither a reference nor a
reason — `CatalogInventoryTest` fails the build for. The posture that would let it run gated
(strict about silence, permissive about a stated absence) belongs upstream, and is proposed there as
`--allow-stated-absence`.

**An unresolved variant cell is usually the kit's matrix, not a mistake.** 16 of 149 do not resolve,
and they fall into two shapes:

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

- **The breakpoint.** Every kit display cell is **192dp** — the small round watch — and one set
  (`Picker`) additionally publishes **225dp** cells behind `Larger Screen (BP)=Yes`. Full-screen
  stickers here render on `id:wearos_large_round`, which is **227dp**. So a 52dp button is 27% of
  the kit's frame and 23% of ours, and every full-screen comparison carries that scale difference
  underneath whatever else it finds. Fixing it means either rendering full-screen stickers at small
  round or repointing `Picker` at its `BP=Yes` cells; the first changes ~30 preview ids from
  `__largeround`.
- **`EdgeButton` is framed the other way round.** Its kit cells are **192×59** — the button alone,
  a *component* shape — while this catalog publishes it as a whole screen, because the scaffold
  reveals the button from scroll state and a bare capture would freeze it collapsed
  (`EdgeButtonScreen`). So a screenful of list rows is being diffed against a bare button.
- **`SwipeToReveal` is framed the other way round too, in the other direction.** Its kit cells are
  **192×192 displays** showing the component sliding off the edge of the watch; this catalog
  publishes cropped stickers of the whole component. The copy and the action glyph now match; the
  frame does not.

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
