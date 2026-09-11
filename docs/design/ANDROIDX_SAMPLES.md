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
`samples-catalog/src/main/kotlin/…/upstream/`, preserving the Apache-2.0 headers verbatim, and writes
a provenance file recording repo / SHA / path / date beside a `NOTICE`.

One thing to verify at implementation time: `android.googlesource.com` was unreachable from the
sandbox this strategy was researched in (egress policy), so the `+archive` subtree-tarball endpoint
could not be exercised. It is the cleanest option if a CI runner can reach it; the GitHub mirror at a
tag is the fallback. Either way the pin is a SHA, so the choice of transport is not load-bearing.

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

1. **Rewrite.** Strip `@Sampled` / `androidx.annotation.Sampled` and normalise the preview imports
   onto the ones this repo's stickers use.
2. **Wrap.** Generate a `@Preview` + sticker wrapper per sample into a *separate generated file*, so
   the vendored source stays byte-identical to upstream except for the recorded patches. This is
   mechanical rather than clever because AndroidX enforces that a `@Sampled` function is a zero-arg
   composable.
3. **Patch.** `samples/patches/*.patch`, applied by the importer after download, each carrying a
   one-line reason.

A sample that genuinely cannot be imported — it needs an Activity, a permission, a real
`Context`, an ambient-mode callback — goes in `samples/quarantine.json` with a reason. A test fails
when a patch stops applying cleanly **or when a quarantined sample becomes importable**, so the gap
cannot silently rot into a permanent exclusion nobody revisits. That is the answer to "make small
fixes when needed": a fix is a patch with a reason, never an untracked edit to a vendored file.

Wear should need *fewer* patches than the phone side, not more — everything is Android already, so
there is no common/Android boundary to cross. The likely quarantine cases are the samples that want
real device services rather than the ones that want a different platform.

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

**One parsing caveat, found while prototyping against the phone artifact.** A naive "KDoc block, then
the next line" scan mis-attributes about 12% of blocks — 31 of the 260 that carry a `@sample`. Two
causes: a `@sample` on a *parameter* KDoc (the next line is the parameter, not the declaration), and
`@Deprecated(…)`-annotated overloads (the next line is `message = …`). The parser needs a
balanced-paren skip over annotation entries, and must attribute a parameter-level `@sample` to its
enclosing declaration. Worth naming because the naive version looks like it works — it resolves 178
declarations and only lies about a tenth of them.

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

**3. The back-link is the one piece that needs upstream work, and `remote-m3` is why.** The component
record carries `sourceFile` / `sourceModule` / `bodyLine` for the source link and `parallel` for the
one sibling; there is no per-component "related catalog" surface. A link *from* `:catalog` *into*
`wear-m3-samples` therefore needs a small addition in compose-ai-tools, not configuration here — and
`remote-m3` cannot use a pairwise mechanism at all, having spent its `compareWith` on `:catalog`.

Recommendation: a generic `related: [{ system, componentId, label }]` on the component record, plus a
"Samples" affordance in the server's component view. A **list**, so one component can point at its
kit sibling and its samples at once, and so this repository's three-way comparison stays expressible.
A capability any catalog could want belongs upstream as a generic input — never as a forked pipeline
here.

## The CI job

A third `uses:` block against `design-artifacts-reusable.yml`. No forked pipeline; everything needed
is a generic input:

- `system: wear-m3-samples`, `spec: samples-catalog/catalog.spec.json`, `module: ':samples-catalog'`
- `cli-version: catalog` + `catalog-key: composePreviewPlugin`, as the two existing jobs do
- **no** `desktop-render` — Robolectric, like `:catalog`
- `split-per-preview: false`
- no `render-shards` to begin with: ~170 base previews is small next to what forces sharding

The `changes` / `Scope` job gains a third output (`samples`), dirtied by `samples-catalog/**` and by
the shared inputs that already dirty both others. Keep its fail-safe behaviour exactly as it is: no
resolvable change set means render everything. Publishing a fresh bundle is never wrong; skipping a
stale one is.

A weekly `samples-refresh.yml` re-runs the importer and opens a PR when the vendored tree or
`sample-map.json` moves — the cadence `figma-pages.yml` and `design-parity-import.yml` already use,
and the right shape for an input that changes on someone else's schedule rather than on a source edit
here.

## Serving on preview.coo.ee

In `compose-preview-server`, `deploy/preview.coo.ee/catalogs.json`:

- two entries, `wear-m3-samples` and `m3-samples`, each with
  `attributionRepos: ["androidx/androidx"]` — the `android/compose-samples` entries are the precedent;
- a new group (`androidx-samples`, heading `androidx/androidx samples`) rather than `design-systems`,
  so the front page keeps the reference design systems at the top, which `design-systems`'
  `priority: 100` exists to guarantee;
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
- **Render budget.** ~170 base previews before mode axes. Not a concern; it becomes one if samples
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
