# Repository instructions for AI agents

Read [README.md](README.md) first — it describes what this repo is and how it is laid out. This
file records the conventions that are easy to violate by accident. It is the Wear-side sibling of
[yschimke/m3-catalog](https://github.com/yschimke/m3-catalog) and inherits its rules; where they
differ, it is because Wear differs.

## Annotation-first is the rule, not a preference

The catalog inventory lives in annotations next to the composables. **Do not** add a `groups` array
to `catalog.spec.json` to add, rename or recaption a component — put it on the `@CatalogComponent` /
`@CatalogVariant`. The spec is layered over the annotations as a field-level override and exists for
cover-sheet fields only.

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
   kit node in its `reference`. This is the default and the majority.
2. **The library's door.** A Wear Compose Material 3 component the kit never published still belongs
   on a sheet whose reader is looking for *the component set*, and it enters with
   `noReference = "<why the kit has none>"`. `ButtonGroup` is the plain case: real API, no kit set.

What is NOT allowed is silence. A `@CatalogComponent` with neither fails
`CatalogInventoryTest.every component is either mapped to the kit or says why not`, and
`scripts/design-map.sh` fails the same way (it passes `--strict`) before a render is attempted —
so "I forgot to look" cannot masquerade as "the kit has nothing".

Door 2 is deliberately narrower than it sounds: it is for a **component of this library**, not for
anything a screen can be built from. A composition an app assembles (the kit's `Media-Player`) is
still out, and so is app content (its `Avatar-*`).

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

- One file per component **group**, opening with `@file:CatalogGroup(name = …, section = …)`.
- Every `@CatalogComponent` carries a `caption`. A component with no caption publishes as a bare
  picture, and a test fails the build for it.
- **Fold variants behind defaults.** A state / content axis is an `@OverrideVariant` cell (or a
  `@CatalogVariant(of = …)`) under its parent, not a new top-level component. A sheet this size is
  only browsable because the card count tracks components, not renders.
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
- **Dark-first, transparent.** A component sticker is a single dark capture on a transparent
  background (`@CatalogModes`). Full-screen components — scaffolds, lists, the edge button — want
  the round device frame and the breakpoint fan-out instead; they are not in the inventory yet, and
  adding them means adding that frame, not filling the component sticker.
- Renders must be **deterministic**: a `TimeText` is pinned to a fixed instant, never the system
  clock. An unpinned clock would make every nightly render differ from the last, which turns the
  delivery branch's history into noise.
- Every published comparison must invoke the actual named Wear Material 3 composable. Rebuilding a
  component from `Box` and its `*Defaults` can make a replica line up, but it cannot test the
  library and therefore does not belong in the comparison inventory. The shape specimens are the
  documented exception in the other direction: they draw mobile `MaterialShapes` because Wear
  publishes no shape library of its own (see README).

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
- **`@InteractionPreview` does not work here.** It is the annotation for pointer-provoked motion and
  it is implemented in the **desktop** renderer only; on Robolectric nothing writes the animated
  file and the still then fails to decode `<id>.apng: file is missing on disk` — which also costs
  the component its ordinary PNG. Use a `LaunchedEffect` state change instead, and say so.

A recording must actually move: `CatalogRenderTest` fails a GIF with fewer than six distinct frames.
If a component does not animate under this renderer — the placeholder is the live example — publish
no recording rather than one that implies motion nobody would see.

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
  footer are fine; they don't claim authorship. Enforced by the hooks in `.githooks/` (install with
  `scripts/install-git-hooks.sh`).

## Dependencies

- **Renovate owns the version bumps; don't hand-bump.** `.github/renovate.json` automerges anything
  that is not a major once CI is green. Change the config instead when the policy is wrong.
- Two groups are deliberately **not** automerged: **majors**, and **Compose** (Wear Compose plus the
  mobile Compose BOM and the material3 pin). Both change what the catalog renders, and the render is
  the product — a human reads the visual diff before it lands.
- `compose-ai-tools` is the exception in the other direction: the Gradle plugin marker, the
  annotation coordinates and the pinned CI action ref are one release and move together in a single
  PR, unscheduled and automerged. A skew between them breaks preview discovery outright.
- Repository settings — squash-only merges, auto-merge, and the `Protect Main` ruleset — are applied
  by `scripts/setup-repo-protection.sh`. They need an admin token, so no workflow (and no agent
  session) can set them; the script is the record of what they are meant to be, and re-running it
  repairs drift. `DRY_RUN=1` prints without writing.

## Verifying a change

```sh
./gradlew :catalog:assembleDebug :catalog:composePreviewDiscover test ktfmtCheck
```

`composePreviewDiscover` is the real contract: it is what turns the annotations into the published
inventory. A component that compiles but is not discovered vanishes from the sheet silently.
