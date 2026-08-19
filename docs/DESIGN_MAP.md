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

This is what `scripts/design-map.sh` runs `--strict --allow-stated-absence` for. Plain `--strict`
gates on every kind of absence including a stated one, so it failed this repo on those four; the
opt-in narrows it back to what it is actually for — still fatal on a missing reference and on
captures that pair with none, permissive about one somebody already looked at and wrote down. It
landed upstream in `@yschimke/compose-design-map` v1.19.0
([compose-ai-tools#4250](https://github.com/yschimke/compose-ai-tools/pull/4250)); before it, the
choice here was plain `--strict` (red on four intended components) or no gate at all.

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
