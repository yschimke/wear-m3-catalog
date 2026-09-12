# Importing the AndroidX samples as a linked catalog

A strategy, not an implementation. It answers four questions:

1. where the sample **source** comes from, given that AndroidX does not publish it;
2. which **version** to import — which, on the Wear side, turns out not to be a question at all;
3. how the `@sample` **KDoc** produces a sample → component mapping without anyone hand-writing one;
4. how the result **links** to the three catalogs that already exist — `wear-m3-catalog` and
   `remote-m3` here, `m3-catalog` in [`yschimke/m3-catalog`](https://github.com/yschimke/m3-catalog) —
   and how it is served on preview.coo.ee.

The companion copy in `yschimke/m3-catalog` covers the phone half. The two documents are deliberately
near-identical; where they differ it is called out.

## What it produces

One new system per repository, published by its own job on the existing `design-artifacts.yml`:

| System | Repository | Module | Renderer |
| --- | --- | --- | --- |
| `wear-m3-samples` | this repo | `:samples-catalog` | Robolectric, matching `:catalog` |
| `m3-samples` | `yschimke/m3-catalog` | `:samples-catalog` | CMP desktop (Skiko), matching that repo's `:catalog` |

**`remote-m3` gets no samples catalog of its own.** Remote Compose has no upstream sample corpus worth
importing; it links *into* `wear-m3-samples` instead. That case is not an afterthought — it is what
forces the linking design below to be a list rather than another pairwise handle, because `remote-m3`
has already spent its single `compareWith` on `:catalog`.

Two catalogs from one repository is well-trodden ground here: this repo already publishes
`wear-m3-catalog` and `remote-m3` from one workflow, with a `changes` job so a push that moves one
does not spend a runner re-rendering the other. A third catalog is a third job and a third output on
that job, not a new pipeline.

## What is built

This document was written as a strategy and is kept as the record of one, with decisions marked where
measurement has since changed them. The tooling landed **in the phone repo first**
([yschimke/m3-catalog#334](https://github.com/yschimke/m3-catalog/pull/334)) and this repo mirrors it:
the importer, the `@sample` reader and the pin are the same scripts with this repo's paths and
version. The `related` field they feed is merged upstream (see "Linking" below).

The `:samples-catalog` module and its generated `@Preview` wrappers are now built here too. What
this repo renders today, measured rather than projected:

| | Wear (this repo) | Phone (m3-catalog) |
| --- | --- | --- |
| Vendored files | 43 | 47 |
| Compile errors | 0 | 0 |
| Files quarantined (do not compile) | 0 | 8 |
| Patches needed | 0 | 0 |
| Samples already carrying `@Preview` upstream | 34 of 170 | 298 of 317 |
| `@Preview` wrappers generated | 115 | 0 — none needed |
| `@Sampled` functions taking arguments, refused | 20 | — |
| Samples quarantined (compile, cannot run here) | 1 | 0 |
| Patches | 3 | 0 |
| Device-bound (round watch) / wrap-content | 86 / 63 | — |
| **Published components / groups** | **149 in 79** | **240 in 102** |

The section above predicted "Wear should need *fewer* patches than the phone side": it needs zero,
and zero file quarantines against the phone's eight, because everything here is already Android and
there is no common/Android boundary to cross. It also predicted the wrapper stage would be
unnecessary; that was right for the phone and wrong here, by a factor of the two teams' annotation
habits rather than anything about the platforms. See "Transform" below.

The design-artifacts job is now built on both sides too — `wear-m3-samples` here and `m3-samples` in
the phone repo, each with the `changes`/Scope job it needed.

Not built yet, on either side: the `catalogs.json` registration on preview.coo.ee, the server's
"Samples" affordance, and the weekly `samples-refresh.yml`.

## Acquisition: vendor a pinned subtree

**The samples are not published as artifacts.** Checked against Google Maven directly — every
candidate coordinate 404s:

```
androidx/wear/compose/compose-material3-samples/maven-metadata.xml     404
androidx/compose/material3/material3-samples/maven-metadata.xml        404
androidx/compose/foundation/foundation-samples/maven-metadata.xml      404
```

So a dependency is not available, and the source has to come out of the AndroidX tree. Three ways to
get it, and only one of them is right:

- **A git submodule** — no. `platform/frameworks/support` is enormous; every clone in CI pays for it.
- **A fetch at render time** — no. The published sheet stops being reproducible and a review of a
  sample change has no diff to look at.
- **A vendored subtree, pinned to a commit SHA, committed here** — yes. It makes the render
  reproducible, the upstream bump a reviewable diff, and the licence obligation discharged in the
  obvious place.

`samples/import.json` pins the upstream repository, the **commit SHA** (never a branch), the subtree
path (`wear/compose/compose-material3/samples/…`), and the library version the module compiles
against. `scripts/import-samples.mjs` fetches that subtree into
`samples-catalog/src/main/kotlin/upstream/`, preserving the Apache-2.0 headers verbatim, and writes
a provenance file recording repo / SHA / path / date beside a `NOTICE`.

**Under the directories the samples' own `package` names** — `upstream/androidx/wear/compose/
material3/samples/…`, the ordinary Kotlin layout, derived from the manifest path by splitting it at
the module's source root. Not cosmetic: discovery resolves a preview back to its file by asking
which of the module's sources *ends with* the package-qualified path it reads off the compiled
class. Vendored flat, none did, so every sample's `sourceFile` fell back to that package path — a
string naming no file in this repository. Nothing failed; two surfaces just went quiet. The usage
panel answered `no-usage`, and the page's "source" link 404'd on GitHub. For a catalog whose entire
subject is *the code*, that is the defect that matters most and the one least likely to be caught by
a build.

**Settled, and not by the transport this section first guessed at.** Neither
`android.googlesource.com`'s `+archive` endpoint nor a codeload tarball is used: the first is
unreachable behind some egress policies and the second 403s behind a proxy, and both need a
directory listing to know what to fetch. `scripts/import-samples.mjs` in
[yschimke/m3-catalog](https://github.com/yschimke/m3-catalog/blob/main/scripts/import-samples.mjs)
— the reference implementation this repo will mirror — does a **blobless sparse clone** instead:

```
git clone --filter=blob:none --no-checkout --depth 1 <repo> <cache>
git sparse-checkout set <paths…>
git checkout <ref>
```

Only the trees, and only the blobs under the sparse paths, are transferred — **13 MB** for the two
sample subtrees, measured, against a multi-gigabyte whole-tree clone. No API token, no directory
listing, and the pinned SHA goes straight to `checkout`. The Wear subtree
(`wear/compose/compose-material3/samples/…`) is 44 files at the ref exercised.

## Which version — no question on Wear, a real one on the phone

**Here there is nothing to decide.** `:catalog` is already an Android module compiling against the
real `androidx.wear.compose:compose-material3` (`1.7.0-beta02`, pinned in `gradle/libs.versions.toml`),
whose sources jar is a flat `androidMain` tree — 136 Kotlin files carrying 170 unique `@sample`
references over 235 tag sites. The import pins to the same version `:catalog` compiles against, full
stop. No fingerprinting, no drift check, no source-set decision. Bumping `wear-compose` bumps the
samples ref in the same PR, and the compiler says immediately if they disagree.

It is worth recording *why* the sibling repo's copy of this document spends two pages on the version
question, because the natural assumption is that the problem is symmetric and it is not. That repo's
`:catalog` is deliberately **not** an Android module — it renders through Skiko so preview.coo.ee can
hold a live Compose session against it — and therefore compiles against Compose Multiplatform
(`org.jetbrains.compose.material3:material3 1.12.0-alpha03`) while the samples are written against
AndroidX (`androidx.compose.material3:material3`, on the `1.5.0-alpha2x` line). That is where the
"historic samples, or an `androidMain` source set with the latest ones?" choice actually lives.

The measured answer there, summarised because it is the kind of finding that gets re-litigated: both
artifacts publish sources jars carrying the `@sample` KDoc, so the set of sample FQNs each references
is a cheap version fingerprint, and **CMP 1.12.0-alpha03's set matches AndroidX material3
1.5.0-alpha22 exactly — 308 of 308, zero either way.** The historic version is current. The pin costs
nothing, is derivable from the artifacts rather than guessed, and is re-derivable automatically on
every bump; the `androidMain` alternative would buy ~22 mostly-renamed samples in exchange for that
repo's live-render lane, a renderer confound in the very comparison the link exists to draw, and a
second Compose line in a repository that split `:remote-catalog` out precisely to avoid one. The full
table and the reproduction commands are in that repo's copy.

**The Wear consequence of all this is just: pin to `wear-compose`, and keep `:samples-catalog` an
Android module like `:catalog`.** Same renderer on both sides of the compare page, so a side-by-side
shows an API-usage difference and not a rasteriser difference.

## Transform, and where "small fixes" live

Three mechanical stages, all idempotent and re-runnable, all producing reviewable diffs:

1. ~~**Rewrite.** Strip `@Sampled` / `androidx.annotation.Sampled` and normalise the preview
   imports.~~ **Dropped.** Stripping an annotation is an edit to every vendored file, which costs the
   byte-identity the whole import contract rests on — a re-import would then diff against upstream
   forever. `androidx.annotation.Sampled` turned out to be published in no artifact at all
   (`annotation-sampled` 404s; it is in neither the KMP nor the `-jvm` jar), so the fix is a
   four-line local shim in `samples-catalog/src/main/kotlin/androidx/annotation/Sampled.kt` declaring the
   annotation this repo's own compiler needs. Upstream's bytes are untouched.
2. **Wrap.** `scripts/samples-previews.mjs` generates a `@Preview` wrapper per sample into a
   *separate generated file* under this repo's own package, never inside `upstream/`.

   **This stage was predicted unnecessary and is load-bearing here.** 298 of the phone corpus's 317
   samples carry `@Preview` upstream, so that repo generates none. Wear carries it on **34 of 170**.
   Without wrappers this catalog would publish a fifth of the corpus. The generator refuses two
   cases rather than guessing: a `@Sampled` function taking parameters (20 of them — a helper a
   sample calls, and a wrapper for it would not compile), and a sample already annotated upstream
   (discovery would find the same composable twice).
3. **Patch.** `samples/patches/*.patch`, applied by the importer after download, each carrying a
   one-line reason. **Zero needed** at the current pin.

## Device-bound, or wrap-content

A Wear sample is one of two things, and they want opposite treatment. A `ScreenScaffold`, a
`TransformingLazyColumn`, a dialog or a pager **fills the watch screen**, so it belongs on a round
device with a black face. A `FilledIconButton` is one component that should crop to its own bounds;
pinning it to a device is the regression the plugin's own `retargetWearStickers` KDoc records, where
a sticker "became a 454x454 watch canvas with the button adrift in the corner".

**The square was wrong in two ways that compound**, which is why this was worth doing rather than
leaving at the 227dp square the retarget produces:

- it shows pixels a watch never draws — `ScaffoldSample`'s bottom row spans x=45..355 of 454, where
  a round bezel exposes only x=149..305 at that height;
- **`isRound` is load-bearing.** `androidx.wear.compose.foundation.ResourcesKt` calls
  `Configuration.isScreenRound()`, so components branch on screen shape. A square canvas can lay a
  sample out differently from the watch it ships on, which makes the published sticker a claim about
  the API the device would not honour. That is the argument that decides it; the clipping alone
  would be cosmetic.

**The split is MEASURED, not inferred.** `scripts/samples-device-bound.mjs` reads it off a render:
the retarget measures every device-less preview against the 227dp screen with both axes wrapped, so
a PNG that came out the full sandbox in both directions is a composable that filled it — the
definition rather than a proxy. A static rule over the sources was tried first and rejected on
evidence: matching layout signals in the body reproduced all 86 with **zero false negatives but five
false positives**, and each was a different kind of wrong (`PickerGroupSample` uses a `Picker` that
does not fill the screen; `ButtonWithIconAndLabelAndPlaceholders` carries `fillMaxSize` on an element
*inside* the button). Separating those means knowing which call is the root, i.e. parsing Kotlin, for
a question the renderer answers exactly.

86 of the 149 are device-bound. 60 reach the sheet through generated wrappers and get the device from
the generator; the other 26 carry upstream's own `@Preview`, so they get it from
`samples/patches/0003-round-device-for-screen-samples.patch`. One device (the 225dp round spec
`:catalog` uses for one of its five breakpoints), not five: the kit sheet renders every screen size
because it reproduces a kit cell at each, while a sample says nothing about responsive behaviour.

Verified stable afterwards rather than assumed — two forced renders, all 150 PNGs byte-identical.

`samples/quarantine.json` carries the declared, checked gaps, in **two units, because there are two
failure modes** — a distinction the first draft did not have:

- **`samples`** lists FILES that do not COMPILE. That is the importer's unit, so an entry takes the
  file's whole sample set out of the catalog. Empty here; eight entries on the phone side.
- **`previews`** lists individual SAMPLES that compile and then cannot RUN. The wrapper generator
  skips those, which keeps the rest of their file. Needed because **one failed preview fails the
  whole render job**, so a single unrenderable sample otherwise costs the catalog every picture in
  it.

Exactly one entry, and it is the one case in this import that really is unfixable rather than
misconfigured: `OneHandedGestureButtonInAmbientSample` calls `rememberAmbientModeManager()`, whose
`AmbientModeManagerImpl` constructor touches `com.google.wear.services.ambient.AmbientComponentState`
— a Wear OS **system** API that upstream itself compiles against as `compileOnly` and that ships only
in a watch's system image. There is nothing to add: `com.google.wear:wear-sdk` and
`com.google.android.wearable:wear-sdk` both 404 on Google Maven, and no `wear-sdk.jar` exists under
any installed Android SDK platform. Ambient mode is also a state no still frame can show. The other
eight samples in `OneHandedGestureSamples.kt` render, which is exactly why the per-sample unit had to
exist.

**Everything else that looked like "this sample cannot work here" was configuration, every time.**
The residue after each fix, in order: the recursive copy (a flat vendor silently dropped
`samples/icons/`, 24 errors pointing at a directory nobody had noticed was missing); vendoring `res/`
*and* setting the module `namespace` to `androidx.wear.compose.material3.samples` so `R` generates in
upstream's package, 39 errors; `androidx.activity:activity-compose` for
`LocalOnBackPressedDispatcherOwner`, 5 errors. Only after all three was there a single genuine
device-API failure left. The lesson is the ordering: exhaust the missing-configuration explanations
before believing a platform one.

## Mapping: the KDoc is the source of truth

`scripts/sample-map.mjs` reads the sources jar **of the exact artifact the module compiles against**
(`compose-material3-1.7.0-beta02-sources.jar`, 440 KB, 235 tag sites), parses the KDoc, and emits a
committed `sample-map.json` keyed by Compose API symbol:

```json
{ "api": "Button", "samples": ["…material3.samples.ButtonSample", "…material3.samples.ButtonLargeIconSample"] }
```

Reading it from the compiled-against artifact rather than from the imported tree is the point: it
makes the mapping describe the API this catalog actually renders.

Committed and regenerate-and-diff tested, the same contract `design-map.json` already has here.

**Why it is a scanner and not a regex.** A naive "KDoc block, then the next line" scan
mis-attributes about 12% of blocks — 31 of the 260 that carry a `@sample` in the phone artifact. The
confirmed cause is an **annotated declaration**: `@Deprecated(message = …, level = …)` sits between
the KDoc and the `fun`, spans lines, and contains both parentheses and a string, so "the next line"
is `message = …`. The parser tracks strings (raw `"""` included), comments and paren depth, and
skips modifiers and annotations with balanced parens.

**Correction to this document's first draft.** It also named "a `@sample` on a *parameter* KDoc" as
a second cause, citing `DatePicker`'s `locale:` line. That was wrong: the block documents the
`DatePickerState` factory and its `@sample` is at block level, so the factory is the correct owner —
the line-based prototype simply mis-landed on a `@param` line further down. Across all three
artifacts measured, **zero** blocks resolve through the parameter path. The parser handles it, and is
tested for it, as defence rather than as a fix for anything observed.

**Measured coverage** on the shipped reader: 260 of 260 blocks attributed and 319 of 319 unique
samples reached on the phone artifact, 308 of 308 on CMP, and **170 of 170 on
`androidx.wear.compose:compose-material3` 1.7.0-beta02** — this repo's own number. Nothing dropped.

### Joining a sample to a catalog component

`sample-map.json` is keyed by Compose API symbol; the catalog is keyed by `componentId`. The catalog
side already resolves the bridge: discovery records `previews[].targets[]` — the production composable
each preview renders — which is what compose-ai-tools' `figma-code-connect-target.mjs` consumes, with
the spec's `component` field as an override. So the join is
`componentId → target functionName → @sample list`.

Expect the inference to be weak here, because it walks for a *project-local* `@Composable` call and
`Button` is a library call. The durable fix is to declare it: an `api = "Button"` field on
`@CatalogComponent`, added **upstream in compose-ai-tools**, per the standing rule that a pile of
mapping config is a signal of a missing annotation. The spec's existing `component` override works as
the interim bridge and needs no upstream change to get started.

`:remote-catalog` gets the join for free wherever it already names its `:catalog` counterpart through
`parallel` — a Remote Compose sticker reaches the samples for the Wear component it reimplements by
composing the two hops, without its own `api` handles.

## Linking, in three layers

**1. Id parity — do this first, it makes the other two cheap.** A sample sticker that demonstrates a
catalog component takes the *same* `componentId` as `:catalog`'s, with each individual sample folded
in as a `@CatalogVariant`. Sample-only material takes its own ids grouped by source file. Every
cross-link is then an identity mapping, and the samples catalog is browsable in the taxonomy readers
already know — including `:remote-catalog`'s existing `parallel` handles, which are written against
those very ids.

**2. `compareWith` + `parallel` — already built, use it as-is.** `wear-m3-samples` declares
`compareWith: { system: "wear-m3-catalog", spec: "../catalog.spec.json" }` — an in-repo sibling, the
same shape `remote-catalog/catalog.spec.json` already uses — and each component carries `parallel`
naming its counterpart. `ServeParallelPairing` ranks counterparts by kit node → variant coordinates →
canonical fallback; samples publish no kit node, so pairing lands on `CANONICAL`, which is exactly the
right reading: the kit cell beside how you call it. The server already states the basis rather than
pretending the sibling drew the cell, so nothing needs to change for this to read honestly.

**3. The back-link needed upstream work — `remote-m3` is why — and it has landed.** The
component record carried `sourceFile` / `sourceModule` / `bodyLine` for the source link and
`parallel` for the one sibling, and nothing for a second relationship. `remote-m3` could not use a
pairwise mechanism at all, having spent its `compareWith` on `:catalog`, so this repository's
three-way comparison was the case that forced the shape.

`related: [{ system, componentId, label }]` now exists as a generic **list** on the component record,
carrying no parity semantics: `parallel` says two renders are pictures of one cell and should be
diffed, `related` says only that another catalog is worth looking at from here. Three pieces, all
merged, tracked by
[compose-ai-tools#5398](https://github.com/yschimke/compose-ai-tools/issues/5398):

| Piece | Where |
| --- | --- |
| The spec field, validation, and the stamp onto `catalog.json` | compose-ai-tools#5399 |
| `@CatalogComponent(related = […])` | compose-preview-daemon#62 |
| Discovery reads it; the export's inventory parses it | compose-ai-tools#5401 |

A component can declare its links in `catalog.spec.json` **today**; the annotation spelling waits on
the `composeai-preview-daemon` pin moving to a release carrying it. The entry form is
`"<system>=<componentId>=<label>"`, where an empty `<componentId>` means "the same id as mine" — so
`related = ["wear-m3-samples==Samples"]` is the ordinary spelling for the id-parity case above.

What is still missing is the **server affordance**: the "Samples" link in the component view. Until a
catalog publishes a `related` link there is nothing real for it to render, which argues for building
it after `:samples-catalog` rather than against a fixture.

## The CI job

**Built.** A third `uses:` block against `design-artifacts-reusable.yml`. No forked pipeline;
everything it needs was already a generic input:

- `system: wear-m3-samples`, `spec: samples-catalog/catalog.spec.json`, `module: ':samples-catalog'`
- `cli-version: catalog` + `catalog-key: composePreviewPlugin`, as the two existing jobs do
- `desktop-render: false` — Robolectric, like `:catalog`
- `split-per-preview: false`, `render-shards: 1`: 149 base previews, one capture each, is small
  next to what forces sharding
- `publish-live-bundle: false` — see "Serving", below; it is the one input here meant to change

The `changes` / `Scope` job gained its third output (`samples`), dirtied by `samples-catalog/**`,
`samples/**`, `sample-map.json` and `scripts/samples-*.mjs`, plus the shared inputs that already
dirty both others. Its fail-safe behaviour is unchanged: no resolvable change set means render
everything. Publishing a fresh bundle is never wrong; skipping a stale one is.

**One input is load-bearing and not obvious: `design-map-command`.** Leaving it empty means "use the
committed map as-is", and the committed map at the repo root belongs to `:catalog` — so this sheet
would publish `:catalog`'s Figma mappings under its own name, every handle naming a `catalog/src/…`
file this module does not contain. That is the shape of
[compose-ai-tools#4841](https://github.com/yschimke/compose-ai-tools/issues/4841), which is what
taught the two callers beside it to pass the input at all. So the samples job passes a command that
projects an **empty** map, and runs no Gradle to do it: with zero `@CatalogComponent` annotations in
the module, a discover-then-project round trip spends minutes deriving that same empty map.

Empty is the truthful answer rather than a placeholder. These are AndroidX's call sites, not a
reproduction of a published kit, so no sample has a node to be scored against — which is also why
the job carries no `figma_token`, no `reference-cache-branch` and no `reference-backdrop`: **the
samples sheet has no design-parity lane at all.** The reusable workflow warns that the system "will
publish 0% coverage"; 0% is correct, and a warning that says so beats a number borrowed from one of
the sheets next door.

Already wired in `ci.yml`, and independent of that job because none of it needs a render: the
`@sample` reader's tests, the importer's tests, the wrapper generator's tests, and a
regenerate-and-diff over both generated-and-committed artifacts
(`samples-previews.mjs --check`, `samples-spec.mjs --check`). The two `--check` gates are the ones
that matter day to day — the wrappers are compiled sources and the spec is what the
design-artifacts job publishes, so either drifting from the vendored tree ships a catalog that does
not match its own inventory.

A weekly `samples-refresh.yml` re-runs the importer and opens a PR when the vendored tree or
`sample-map.json` moves — the cadence `figma-pages.yml` and `design-parity-import.yml` already use,
and the right shape for an input that changes on someone else's schedule rather than on a source edit
here.

## Serving on preview.coo.ee

**Revised: registered but HIDDEN, and surfaced on component pages.** The original plan below gave the
samples their own front-page group. They are not a design system anyone browses top-down — they are
the call sites for components the other sheets already publish, so the place a reader wants them is
*on the component's own page*, the way motion previews already appear there. The front page stays the
reference design systems.

So: both entries registered with `attributionRepos: ["androidx/androidx"]` and hidden from the
catalog listing, with the component-page surface following whatever mechanism motion previews use.
That mechanism has not been read yet and this section should not guess at it; what is decided is the
shape, not the wiring.

Superseded, kept for the reasoning it carries:

- two entries, `wear-m3-samples` and `m3-samples`, each with
  `attributionRepos: ["androidx/androidx"]` — the `android/compose-samples` entries are the precedent;
- ~~a new group (`androidx-samples`, heading `androidx/androidx samples`) rather than
  `design-systems`~~, so the front page keeps the reference design systems at the top, which
  `design-systems`' `priority: 100` exists to guarantee — the hidden registration achieves the same
  end more directly;
- no `sites` entry — these want no hostname of their own.

`producers.json` needs **no change**: this repo's `design-artifacts/*` is already trusted. That is
convenient and worth a second look rather than a shrug, because on that box trust is eligibility for
server-side execution under `SERVE_ALLOW_RENDER_TRUSTED=1`, and a live bundle here would mean
executing vendored third-party Compose. **Start with `publish-live-bundle: false`** — baked stickers
only — and turn live rendering on as its own deliberate change.

## Risks

- **Licence and attribution.** Apache-2.0 headers preserved verbatim, `NOTICE` and a provenance file
  in the vendored tree, `attributionRepos` on the served catalog. Non-negotiable, and cheap.
- **Patch-set creep.** If the patch set grows past a handful, an assumption is wrong and should be
  revisited with real numbers rather than defended. The quarantine list makes that visible instead of
  gradual.
- **Version lockstep.** The samples ref and `wear-compose` move together or the module does not
  compile. That is the desired failure mode, but it does mean a Renovate bump of `wear-compose` will
  need the import re-run in the same PR rather than merging on its own.
- **Render budget.** 149 base previews before mode axes. Not a concern; it becomes one if samples
  grow variant matrices, which they should not — a sample has one right way to be drawn.

## Reproducing the evidence

Everything above is measured, not recalled.

```sh
# the Wear sample corpus this catalog would import against
curl -sO https://dl.google.com/dl/android/maven2/androidx/wear/compose/compose-material3/1.7.0-beta02/compose-material3-1.7.0-beta02-sources.jar
unzip -q compose-material3-1.7.0-beta02-sources.jar -d wear
grep -rho '@sample [A-Za-z0-9_.]*' wear | sed 's/@sample //' | sort -u | wc -l   # 170
grep -rho '@sample [A-Za-z0-9_.]*' wear | wc -l                                  # 235

# the samples artifacts that do not exist
curl -s -o /dev/null -w '%{http_code}\n' \
  https://dl.google.com/dl/android/maven2/androidx/wear/compose/compose-material3-samples/maven-metadata.xml
```

The phone-side fingerprint table and its reproduction commands are in the `yschimke/m3-catalog` copy.
