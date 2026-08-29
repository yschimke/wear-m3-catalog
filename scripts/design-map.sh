#!/usr/bin/env bash
# Regenerate design-map.json (and its variant sidecar) from the @CatalogComponent annotations.
#
#   scripts/design-map.sh [--check]
#   scripts/design-map.sh --out-dir DIR [MODULE]
#
# Two steps, in two upstream packages, because the question splits there:
#
#   1. `@yschimke/compose-design-map` knows what the ANNOTATIONS mean — it defines
#      @CatalogComponent / @CatalogVariant / @OverrideVariant and writes them into previews.json.
#      It emits base references, plus a sidecar declaring which other previews are the same
#      component with knobs turned.
#   2. `@design-parity/kit-index` knows what the KIT means — `shape=square` is a fact about a
#      Compose API and `Shape=Square` is a fact about the Wear kit. It resolves those declarations
#      against the committed figma-kit-index.json into tagged ref/previewId pairs.
#
# Neither step is this repo's business to implement. What stays here is this wrapper, the committed
# outputs, and the kit handle on each annotation.
#
# BOTH ARE PINNED to an exact version, because both outputs are COMMITTED and CI fails on any
# difference: float them and the next release upstream turns this repo red for a change nobody here
# made. Bumping is a commit that regenerates the map in the same diff.
#
# WHY IT STAGES. Step 1's map is an INTERMEDIATE — base references with the variants still
# unresolved — so a run that wrote it directly and then failed in step 2 would leave the repo
# holding a map that looks complete while comparing fewer nodes than it claims.
set -euo pipefail

CHECK=""
[ "${1:-}" = "--check" ] && { CHECK=1; shift; }

# `--out-dir DIR` projects into DIR and leaves the working tree alone.
#
# The committed map belongs to `:catalog` (see WHICH catalog, below), so anything that wants a
# SECOND module's map alongside it — `scripts/kit-cells.sh` reads both, to count how much of each
# kit set each sheet draws — cannot go through the root path without clobbering the one that is
# committed. This is that door: same two pinned upstream steps, same inputs, a destination that is
# not the repo. It never reconciles against the working tree, so `--check` means nothing with it.
OUT_DIR=""
if [ "${1:-}" = "--out-dir" ]; then
  OUT_DIR="${2:?--out-dir needs a directory}"
  shift 2
fi
if [ -n "$OUT_DIR" ] && [ -n "$CHECK" ]; then
  echo "error: --check reconciles the committed map; --out-dir writes somewhere else. Pick one." >&2
  exit 2
fi

# WHICH catalog. This repo publishes two, and each projects its own map:
#
#   scripts/design-map.sh [--check]                 -> :catalog        (the kit rendition)
#   scripts/design-map.sh remote-catalog            -> :remote-catalog (the Remote Compose one)
#
# The output path is NOT a parameter, because design-parity does not treat it as one: the action
# reads `<repoRoot>/design-map.json` (packages/action/src/config.ts), and the reusable workflow
# hashes and figma-scans that same path. One map per checkout is the contract.
#
# That is workable because the two parity runs are separate JOBS with separate workspaces: each
# regenerates the root map for its own module before comparing, and neither sees the other's. What
# it means locally is that projecting the Remote map overwrites the committed one, which belongs to
# `:catalog` — so say so, loudly, rather than letting someone commit the wrong map.
MODULE_DIR="${1:-catalog}"
if [ "$MODULE_DIR" != "catalog" ] && [ -z "$OUT_DIR" ]; then
  echo "note: projecting $MODULE_DIR into ./design-map.json, which is where design-parity reads it." >&2
  echo "note: that file is COMMITTED for :catalog — restore it with 'git checkout -- design-map.json'" >&2
  echo "      before committing anything else." >&2
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

# --strict, WITH the opt-in that makes it usable here.
#
# `--strict` fails the run on any component that reaches no design reference — which is what a
# catalog that reproduces a kit wants, since an unmapped sticker is one nothing can ever check.
# On its own it also fails on an absence somebody already looked at and WROTE DOWN, and this
# catalog's membership has two doors (AGENTS.md): a component reproducing a published kit set names
# its node, and a component of either library here that the kit never published as a set enters with
# `noReference = "<why>"`.
# `ButtonGroup`, `TransformingLazyColumn`, `Scaffold` and `ArcProgressIndicator` are through door 2,
# so plain `--strict` reddened this repo on four components that are exactly as intended. The
# Horologist components added since (the media parts, the sign-in surfaces, the fast-scrolling list)
# go through the same door for the same reason, so the count is sixteen now rather than four.
#
# `--allow-stated-absence` narrows the gate to what it is actually for: still fatal on a missing
# reference and on captures that pair with none, permissive about a stated one. They are still
# reported, under a heading that keeps them apart from a gap.
#
# NO `--base-breakpoint`. The full-screen stickers now render at each of the five screen sizes the
# kit recognises, and exactly one of those captures carries the design reference — the rest fold
# under it as `<dp>dp` cells. The projector picks the NARROWEST as that base, which is 192dp, and
# 192 is where the kit draws every one of its screen cells. Passing the flag would say the same
# thing in more words; it exists for a kit that draws somewhere else.
#
# Gated BEFORE anything is written, so a failed run leaves the committed map intact rather than
# replacing it with one CI would report as merely stale.
npx --yes @yschimke/compose-design-map@1.25.0 \
  --previews "$MODULE_DIR/build/compose-previews/previews.json" \
  --out "$WORK/design-map.json" \
  --variants "$WORK/design-map-variants.json" \
  --strict \
  --allow-stated-absence

# Step 2 needs the checked-in kit index, which is built by `.github/workflows/figma-refs.yml` and
# therefore needs a FIGMA_TOKEN. Until that secret exists on this repository the index is absent and
# the base references — one exact kit node per component — stand on their own; what is missing is
# only the per-cell resolution of a variant axis onto the kit's own variant values. Skipping loudly
# rather than failing keeps the map regenerable (and CI's staleness check honest) in the meantime.
if [ -f figma-kit-index.json ]; then
  npx --yes @design-parity/kit-index@0.1.53 resolve \
    --map "$WORK/design-map.json" \
    --variants "$WORK/design-map-variants.json" \
    --index figma-kit-index.json \
    --out "$WORK/design-map.json"
else
  echo "note: figma-kit-index.json is absent — variant cells stay unresolved against the kit." >&2
fi

# A component that declares no variant axis writes no sidecar; an empty file would assert only that
# it has nothing to say. Reconcile the absence too, so a catalog that loses its last axis does not
# keep a stale one committed.
if [ -n "$OUT_DIR" ]; then
  mkdir -p "$OUT_DIR"
  for f in design-map.json design-map-variants.json; do
    rm -f "$OUT_DIR/$f"
    if [ -f "$WORK/$f" ]; then cp "$WORK/$f" "$OUT_DIR/$f"; fi
  done
  exit 0
fi

for f in design-map.json design-map-variants.json; do
  if [ -n "$CHECK" ]; then
    if [ -f "$WORK/$f" ]; then
      diff -q "$WORK/$f" "$f" >/dev/null 2>&1 || {
        echo "::error::$f is out of date — regenerate with scripts/design-map.sh"
        exit 1
      }
    elif [ -f "$f" ]; then
      echo "::error::$f is stale and should be removed — regenerate with scripts/design-map.sh"
      exit 1
    fi
  elif [ -f "$WORK/$f" ]; then
    cp "$WORK/$f" "$f"
  else
    rm -f "$f"
  fi
done

[ -n "$CHECK" ] && echo "✓ design-map.json and its sidecar match the annotations."
exit 0
