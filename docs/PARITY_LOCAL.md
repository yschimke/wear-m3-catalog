# Running design-parity locally

A parity question answered on CI costs a dispatch and ~10 minutes, and the board
it publishes covers the whole catalog. That is the wrong shape for *working out
an issue* — where you want one component, a measurement, and another look.

This is the same check, on your machine, for one component, with **zero Figma
calls**.

```sh
scripts/parity-local.sh FilledButton                              # :catalog
scripts/parity-local.sh --module remote-catalog OutlinedCardRemote
scripts/parity-local.sh --no-build Button IconButton              # catalog unchanged since last run
scripts/parity-local.sh --no-semantics FilledButton               # pixels only; see below
```

You get the markdown verdict on stdout — pairing, semantics and the visual diff
— plus a self-contained `report.html` per component under `.design-parity/out`
with reference, candidate and diff side by side. Name components bare; the
script resolves each against the map it just projected and stops on an unknown
or ambiguous one rather than comparing the wrong thing.

## What it runs, if you would rather run it by hand

```sh
# once per session — materialise the reference cache
git fetch origin design-parity/reference --depth 1
mkdir -p .design-parity/reference
git archive FETCH_HEAD | tar -x -C .design-parity/reference

# once per code change — the CLI at the version gradle/libs.versions.toml pins, and
# --with-semantics, or the run silently drops half its checks (see below)
compose-preview bundle pack --module :remote-catalog --with-semantics
./scripts/design-map.sh remote-catalog

# per question — seconds
npx design-parity run \
  --components "catalog/src/main/kotlin/ee/schimke/wearm3catalog/remote/CatalogPreviews.kt#OutlinedCardRemote" \
  --candidate-bundles remote-catalog/build/compose-previews/bundle.png \
  --reference-cache .design-parity/reference --reference-cache-only \
  --out .design-parity/out
```

**`--no-build` is checked, not trusted.** If any Kotlin source in the module is
newer than the bundle, the run refuses rather than comparing. A stale bundle is
this loop's worst failure precisely because it does not look like one: the run
succeeds, prints a verdict, and the verdict describes the code as it was before
your edit — which reads as *"my change did nothing"*, indistinguishable from a
change that genuinely did nothing. It cost two wrong conclusions before the
guard existed.

## The five things that waste an hour if nobody wrote them down

The script handles the first, the second and the fifth. They are written down
anyway, because the second half of this page is what you need the day you run the
CLI directly.

**The bundle has to be packed `--with-semantics`, and nothing downstream says
otherwise.** `./gradlew :<module>:composePreviewBundle` writes a bundle with no
`previews/<id>.semantics.json` in it, and design-parity does not treat that as an
error: with no candidate tree to line the spec up against it reports every token
group as *"candidate resolved no `<group>` tokens; compliance not evaluated"* and
emits no i18n, layout or contrast findings at all. Deliberately — an extraction
gap must not masquerade as a token violation — but the effect on a local verdict
is that it comes back **cleaner than the board's for code that has not changed**.
Measured on `:catalog`: 124 evaluated token checks, 42 i18n warnings and 20
layout warnings all went to zero, and `SuccessConfirmation`'s ❌ `radius.corner:
52 vs spec 200` read as a ⚠️ warn, while the 1790 visual findings stayed
byte-identical — so nothing in the diff hinted that anything was missing. Packed
`--with-semantics`, the same 50 components reproduce the board's 2344 findings
exactly. `parity-local.sh` packs that way by default and refuses a bundle without
the sidecars; `--no-semantics` is the deliberate way to accept the narrower
verdict, and it is only ever right when you are looking at pixels.

**`--candidate-bundles` wants the bundle, not the renders.** It is a PNG+zip
polyglot the pack writes to
`<module>/build/compose-previews/bundle.png`. Pointing it at `renders/` or at
`previews.json` fails with *"the file has no readable zip appended"*; pointing
it at the `compose-previews/` directory reports *"no candidate render
available"* and passes vacuously, which is the worse of the two failures — a
green verdict that compared nothing.

**`--reference-cache` wants a directory, not the branch.** `design-parity/reference`
is a git branch; materialise it with `git archive` first. It is ~84 MB.

**`--reference-cache-only` is what makes the run API-free.** With it you get
`Reference cache: 581 node(s) from 1 file(s), no API calls` and need no
`FIGMA_TOKEN`. Without it the run reaches for the Figma API. The daily import
(#129) is what keeps that cache current, so a local run is only as fresh as the
last import — check the branch date before trusting a *pass*.

**`scripts/design-map.sh remote-catalog` overwrites the committed map.**
`design-map.json` is `:catalog`'s and design-parity reads only that one path, so
projecting the Remote module clobbers it. Restore it with
`git checkout -- design-map.json design-map-variants.json` before committing
anything. AGENTS.md says this too; it is repeated here because this is the
workflow that trips it. `parity-local.sh` copies both files aside before
projecting and puts them back on exit — including on failure and on Ctrl-C, and
by copy rather than `git checkout --`, so it cannot eat an edit you were making
to them.

## What it costs

The semantics pass is a daemon render per preview, and it dominates: packing
`:catalog`'s 1035 previews measured ~14 minutes, against ~2 minutes for the pack
alone. That cost is per **code change**, not per question: the bundle covers the whole
module, so every `--no-build` run after it answers any component in seconds. The
script fetches the pinned `compose-preview` CLI into `.design-parity/cli/<version>/`
the first time (~213 MB, once per pin bump) unless a matching one is already on
`PATH`.

## What this does not replace

It is **one component at a time**, so it does not produce the board. The
published sheets on `design-parity/main` and `design-parity/remote-m3` are still
what CI writes, and still the thing to read for coverage. Use this to work out
an issue; use the board to know where the catalog stands.

`.design-parity/` is gitignored — the cache is large and both it and the report
output are reproducible from the delivery branches.
