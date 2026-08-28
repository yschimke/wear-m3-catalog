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

# once per code change
./gradlew :remote-catalog:composePreviewDiscover :remote-catalog:composePreviewBundle
./scripts/design-map.sh remote-catalog

# per question — seconds
npx design-parity run \
  --components "catalog/src/main/kotlin/ee/schimke/wearm3catalog/remote/CatalogPreviews.kt#OutlinedCardRemote" \
  --candidate-bundles remote-catalog/build/compose-previews/bundle.png \
  --reference-cache .design-parity/reference --reference-cache-only \
  --out .design-parity/out
```

## The four things that waste an hour if nobody wrote them down

The script handles the first and the fourth. They are written down anyway,
because the second half of this page is what you need the day you run the CLI
directly.

**`--candidate-bundles` wants the bundle, not the renders.** It is a PNG+zip
polyglot that `composePreviewBundle` writes to
`build/compose-previews/bundle.png`. Pointing it at `renders/` or at
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

## What this does not replace

It is **one component at a time**, so it does not produce the board. The
published sheets on `design-parity/main` and `design-parity/remote-m3` are still
what CI writes, and still the thing to read for coverage. Use this to work out
an issue; use the board to know where the catalog stands.

`.design-parity/` is gitignored — the cache is large and both it and the report
output are reproducible from the delivery branches.
