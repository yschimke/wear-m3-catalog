#!/usr/bin/env bash
# Compare ONE component against the Figma kit, locally, with no Figma API calls.
#
#   scripts/parity-local.sh OutlinedCard
#   scripts/parity-local.sh --module remote-catalog OutlinedCardRemote
#   scripts/parity-local.sh --no-build Button IconButton
#
# The same comparison CI publishes as a board, asked one component at a time. A parity question on
# CI costs a workflow dispatch and ~10 minutes and answers for the whole catalog; this answers for
# the component you are actually working out an issue on, in seconds once the bundle is built.
# The recipe, and what each step is for, is in docs/PARITY_LOCAL.md.
#
# WHY THIS EXISTS AS A SCRIPT rather than four lines in a doc: two of the four steps are traps that
# fail QUIETLY.
#
#   * `--candidate-bundles` wants the PNG+zip bundle, not the render directory. Pointed at
#     `build/compose-previews/`, design-parity reports "no candidate render available" and PASSES —
#     a green verdict that compared nothing. This script always passes `bundle.png`.
#   * `scripts/design-map.sh <module>` overwrites the COMMITTED map, which is `:catalog`'s
#     (AGENTS.md, "One design map per checkout"). This script saves both map files before
#     projecting and restores them on exit, including on failure and on Ctrl-C — by copy rather than
#     `git checkout --`, so it cannot eat an edit you were making to them.
set -euo pipefail

cd "$(dirname "$0")/.."

MODULE="catalog"
BUILD=1
REFRESH=0
COMPONENTS=()

while [ $# -gt 0 ]; do
  case "$1" in
    --module) MODULE="${2:?--module needs a module directory}"; shift 2 ;;
    --no-build) BUILD=0; shift ;;      # you have not touched the catalog since the last run
    --refresh-cache) REFRESH=1; shift ;;
    -h|--help) sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    -*) echo "unknown flag: $1" >&2; exit 2 ;;
    *) COMPONENTS+=("$1"); shift ;;
  esac
done

[ ${#COMPONENTS[@]} -gt 0 ] || { echo "usage: scripts/parity-local.sh [--module <dir>] [--no-build] <Component>..." >&2; exit 2; }

WORK=".design-parity"
CACHE="$WORK/reference"

# The reference cache is the whole reason this run needs no FIGMA_TOKEN: `design-parity/reference`
# is a long-lived branch of kit PNGs the daily import (#129) refreshes, and `--reference-cache-only`
# below makes the CLI read it instead of reaching for the API. ~84 MB, so fetch it once and keep it;
# `.design-parity/` is gitignored.
#
# It is only as fresh as the last import, which matters for a PASS: report the date rather than let
# a stale cache quietly agree with you.
if [ ! -d "$CACHE" ] || [ "$REFRESH" = 1 ]; then
  echo "==> materialising the reference cache from design-parity/reference"
  git fetch origin design-parity/reference --depth 1
  rm -rf "$CACHE"
  mkdir -p "$CACHE"
  git archive FETCH_HEAD | tar -x -C "$CACHE"
fi
echo "==> reference cache: $(git log -1 --format=%cd --date=short FETCH_HEAD 2>/dev/null || echo "unknown date") (refresh with --refresh-cache)"

BUNDLE="$MODULE/build/compose-previews/bundle.png"
RENDERS="$MODULE/build/compose-previews/renders"
# Written only by a successful build in THIS script, and what the staleness
# guard measures against. See the two comments that use it.
STAMP="$MODULE/build/compose-previews/.parity-local-rendered"

if [ "$BUILD" = 1 ]; then
  echo "==> rendering :$MODULE"
  # THE BUNDLE TASK DOES NOT RENDER, AND DOES NOT DEPEND ON ANYTHING THAT DOES.
  # `./gradlew :catalog:composePreviewBundle --dry-run` lists exactly two tasks,
  # `composePreviewDiscover` and itself: the pixels come from the separate
  # `composePreviewRender*` family, and nothing in the graph connects them. So a
  # bundle built without an explicit render packs whatever happens to be left in
  # `renders/` from some earlier run — which is not a hypothetical. This script
  # asked for discover+bundle only, and spent a session serving 05:12 pixels for
  # a 09:23 edit, reading exactly like "my change did nothing".
  #
  # Two invocations, not one. Asked for together, Gradle's validation rejects the
  # build outright — "Declare an explicit dependency on
  # ':catalog:composePreviewRenderLottie' from ':catalog:composePreviewBundle'" —
  # because bundle consumes that task's output without declaring it. Split, each
  # build's graph is internally consistent and the ordering here supplies what
  # the plugin does not. The missing edge belongs upstream in compose-ai-tools;
  # until it lands, this is the fix that does not need a plugin release.
  ./gradlew ":$MODULE:composePreviewDiscover" ":$MODULE:composePreviewRenderAll"
  ./gradlew ":$MODULE:composePreviewBundle"

  # A bundle whose renders are missing is not a broken build — design-parity
  # accepts it and fails SOFT, "none of the N listed preview(s) carry an image;
  # the pack rendered nothing", after which the run prints no verdict at all.
  # Catch it here, where the cause is one line away.
  if [ ! -d "$RENDERS" ] || [ -z "$(ls -A "$RENDERS" 2>/dev/null)" ]; then
    echo "the render pass produced no pixels in $RENDERS." >&2
    echo "  A bundle packed from an empty renders/ compares nothing and still exits 0." >&2
    exit 1
  fi
  # Stamp it, because Gradle is content-addressed and this check is not. A task
  # that decides it is UP-TO-DATE writes nothing, so a source file whose mtime
  # moved without its content changing — a touch, an edit reverted, a branch
  # switched back — would leave every render looking older than a source they in
  # fact describe, and the guard below would then refuse every `--no-build` run
  # with no way to satisfy it. After a successful build the renders DO match the
  # sources, whether or not Gradle had to do anything, so say so.
  touch "$STAMP"
fi

[ -f "$BUNDLE" ] || { echo "no bundle at $BUNDLE — drop --no-build" >&2; exit 1; }

# A STALE BUNDLE IS THE WORST FAILURE THIS SCRIPT HAS, because it does not look
# like one: the run succeeds, prints a verdict, and the verdict describes the
# code as it was before your edit. The reading is "my change did nothing" —
# which is indistinguishable from a change that genuinely did nothing, and it
# cost two wrong conclusions in the session that wrote this guard.
#
# So `--no-build` is checked rather than trusted. Any Kotlin source in the
# module newer than the bundle means the bundle cannot describe it.
#
# This refuses rather than rebuilding: `--no-build` is a claim about what you
# have already done, and a script that silently did the opposite of the flag
# would be its own surprise. Fatal, not a warning — a warning above a verdict
# is a warning nobody reads.
if [ "$BUILD" = 0 ]; then
  # Measured against the PIXELS, not the pack. The bundle can arrive whole from
  # Gradle's build cache, so its mtime says when it was packed rather than when
  # what is inside it was drawn — which is how a bundle rebuilt at 09:25 came to
  # carry renders from 05:12 and pass this check.
  #
  # The newest of the stamp and the newest render, so a render pass run by hand
  # outside this script still satisfies the guard.
  newest=$(ls -t "$STAMP" "$RENDERS"/*.png 2>/dev/null | head -1 || true)
  stale=""
  if [ -z "$newest" ]; then
    stale="(nothing rendered yet)"
  else
    stale=$(find "$MODULE/src" -name '*.kt' -newer "$newest" -print -quit 2>/dev/null || true)
  fi
  if [ -n "$stale" ]; then
    echo "stale renders: $stale is newer than anything in $RENDERS." >&2
    echo "  The comparison would describe the code as it was BEFORE that edit," >&2
    echo "  and read as 'my change did nothing'. Drop --no-build." >&2
    exit 1
  fi
fi

# Save the committed map BEFORE projecting, restore it however this exits. See the header.
SAVED="$(mktemp -d)"
trap 'for f in design-map.json design-map-variants.json; do
        if [ -f "$SAVED/$f" ]; then cp "$SAVED/$f" "$f"; else rm -f "$f"; fi
      done
      rm -rf "$SAVED"' EXIT
for f in design-map.json design-map-variants.json; do
  [ -f "$f" ] && cp "$f" "$SAVED/$f"
done

echo "==> projecting the design map for :$MODULE"
if [ "$MODULE" = "catalog" ]; then scripts/design-map.sh; else scripts/design-map.sh "$MODULE"; fi

# design-parity identifies a component by its full `<source path>#<Name>`, which nobody wants to
# type. Resolve a bare name against the map just projected — the same file the run itself reads, so
# a name that resolves here is a name the run can compare. An ambiguous or absent name stops the run
# rather than silently comparing the wrong thing (or, worse, nothing).
ARGS=()
for name in "${COMPONENTS[@]}"; do
  case "$name" in
    */*"#"*) ARGS+=(--components "$name"); continue ;;   # already fully qualified
  esac
  # `|| true` is load-bearing: under `set -e` a no-match grep inside a command substitution kills
  # the script, and an unknown component name would exit 1 with no message at all.
  matches=$(grep -o "\"[^\"]*#$name\"" design-map.json | tr -d '"' | sort -u || true)
  count=$(printf '%s' "$matches" | grep -c . || true)
  if [ "$count" = 0 ]; then
    echo "no component '#$name' in :$MODULE's map." >&2
    if [ "$MODULE" = "catalog" ]; then
      echo "  This repo publishes two catalogs — try --module remote-catalog." >&2
    else
      echo "  This repo publishes two catalogs — try without --module, for :catalog." >&2
    fi
    exit 1
  elif [ "$count" != 1 ]; then
    echo "'$name' is ambiguous in :$MODULE — pass one of these in full:" >&2
    printf '  %s\n' $matches >&2
    exit 1
  fi
  ARGS+=(--components "$matches")
done

# NOT pinned, deliberately — the opposite call from scripts/design-map.sh, for the opposite reason.
# That script's outputs are committed and CI fails on any difference, so a float turns the repo red
# for a change nobody here made. This one writes nothing tracked: what it must agree with is the CI
# board, which the reusable workflow runs from the current release. Pinning would make the local
# answer drift from the published one, which is the only thing that would make it useless.
echo "==> comparing"
npx --yes "design-parity@${DESIGN_PARITY_VERSION:-latest}" run \
  "${ARGS[@]}" \
  --candidate-bundles "$BUNDLE" \
  --reference-cache "$CACHE" --reference-cache-only \
  --out "$WORK/out"

echo
echo "reports: $WORK/out — report.html per component, reference / candidate / diff side by side."
