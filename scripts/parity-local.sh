#!/usr/bin/env bash
# Compare ONE component against the Figma kit, locally, with no Figma API calls.
#
#   scripts/parity-local.sh OutlinedCard
#   scripts/parity-local.sh --module remote-catalog OutlinedCardRemote
#   scripts/parity-local.sh --no-build Button IconButton
#   scripts/parity-local.sh --no-semantics Button        # pixels only, no CLI download
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
#   * The bundle `composePreviewBundle` packs carries NO semantics, and a run against it does not
#     LOOK short of anything: design-parity reports each token group as "candidate resolved no
#     <group> tokens; compliance not evaluated" and emits no i18n, layout or contrast findings at
#     all, so the verdict comes back CLEANER than the board's for a catalog that has not changed.
#     Measured on `:catalog` against the published board: 124 evaluated token checks, 42 i18n and
#     20 layout warnings all went to zero, and one ❌ fail (`SuccessConfirmation`, `radius.corner:
#     52 vs spec 200`) read as a ⚠️ warn — with the pixels byte-identical, so nothing had moved.
#     CI packs through `compose-preview bundle pack --with-semantics`; so does this, by default.
set -euo pipefail

cd "$(dirname "$0")/.."

MODULE="catalog"
BUILD=1
REFRESH=0
SEMANTICS=1
COMPONENTS=()

while [ $# -gt 0 ]; do
  case "$1" in
    --module) MODULE="${2:?--module needs a module directory}"; shift 2 ;;
    --no-build) BUILD=0; shift ;;      # you have not touched the catalog since the last run
    --refresh-cache) REFRESH=1; shift ;;
    --no-semantics) SEMANTICS=0; shift ;;  # pixels only; the checks below go unevaluated
    -h|--help) sed -n '2,31p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    -*) echo "unknown flag: $1" >&2; exit 2 ;;
    *) COMPONENTS+=("$1"); shift ;;
  esac
done

[ ${#COMPONENTS[@]} -gt 0 ] || { echo "usage: scripts/parity-local.sh [--module <dir>] [--no-build] [--no-semantics] <Component>..." >&2; exit 2; }

WORK=".design-parity"
CACHE="$WORK/reference"
# Seconds `bundle pack` may spend rendering. The same default CI's reusable workflow carries, for
# the same reason: the semantics pass is a daemon render per preview, so it is the long pole on a
# full module rather than an idle wait.
RENDER_TIMEOUT="${RENDER_TIMEOUT:-1800}"

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

# THE CLI, AT THE VERSION THE CATALOG PINS — the same resolution CI's install action performs
# (`compose-preview-version: catalog`, `catalog-key: composePreviewPlugin`). Not "whatever is on
# PATH": the bundle format is the plugin's, so a CLI on a different version packs sidecars the
# renderer on the other end reads differently, and neither half announces the skew. A `compose-
# preview` already on PATH is used when it reports the pinned version, and otherwise ignored.
#
# The tarball is ~213 MB and lands in gitignored `.design-parity/cli/<version>/`, keyed by version
# so a pin bump fetches the new one and leaves the old. Same bargain as the reference cache above:
# fetched once, kept, and no manual install step between a checkout and a verdict.
compose_preview_cli() {
  local pin dir
  pin=$(sed -n 's/^composePreviewPlugin = "\(.*\)"/\1/p' gradle/libs.versions.toml | head -1)
  [ -n "$pin" ] || {
    echo "no composePreviewPlugin pin in gradle/libs.versions.toml — cannot pick a CLI." >&2
    exit 1
  }
  if command -v compose-preview >/dev/null 2>&1 &&
     compose-preview --version 2>/dev/null | grep -qw "$pin"; then
    command -v compose-preview
    return
  fi
  dir="$WORK/cli/$pin"
  if [ ! -x "$dir/bin/compose-preview" ]; then
    echo "==> downloading compose-preview $pin (~213 MB, once per pin)" >&2
    rm -rf "$dir" "$dir.tar.gz"
    mkdir -p "$dir"
    curl -fL --retry 3 --retry-connrefused --connect-timeout 20 -o "$dir.tar.gz" \
      "https://github.com/yschimke/compose-ai-tools/releases/download/v$pin/compose-preview-$pin.tar.gz"
    tar -xzf "$dir.tar.gz" -C "$dir" --strip-components=1
    rm -f "$dir.tar.gz"
  fi
  echo "$dir/bin/compose-preview"
}

# How many previews in [BUNDLE] carry a semantics tree. Zero is the whole failure this script's
# `--with-semantics` exists to prevent, and it is silent downstream — hence a count rather than a
# boolean, so the message can say what it found.
bundle_semantics_count() {
  unzip -l "$BUNDLE" 2>/dev/null | grep -c 'semantics\.json' || true
}

# The line every "your verdict is missing checks" message opens with, in one place because both
# the pack and the `--no-build` guard reach it.
semantics_missing() {
  echo "  $BUNDLE carries no previews/<id>.semantics.json, so design-parity has no candidate" >&2
  echo "  tree to evaluate: every token group reports 'compliance not evaluated' and the i18n," >&2
  echo "  layout and contrast checks produce nothing. The verdict then reads CLEANER than the" >&2
  echo "  board's on unchanged code, which is the one failure a local loop must not have." >&2
}

BUNDLE="$MODULE/build/compose-previews/bundle.png"
RENDERS="$MODULE/build/compose-previews/renders"
# Written only by a successful build in THIS script, and what the staleness
# guard measures against. See the two comments that use it.
STAMP="$MODULE/build/compose-previews/.parity-local-rendered"

if [ "$BUILD" = 1 ]; then
  echo "==> rendering :$MODULE"
  # `bundle pack` owns the whole render: it discovers, renders, packs the same
  # `<module>/build/compose-previews/bundle.png` the Gradle task writes, and then injects each
  # preview's semantics tree from a short-lived daemon render. One command, and the same one CI
  # packs its candidate with — which is the point, since a local verdict that cannot be lined up
  # against the board's is a second opinion nobody can act on.
  if [ "$SEMANTICS" = 1 ]; then
    CLI="$(compose_preview_cli)"
    "$CLI" bundle pack --module ":$MODULE" --with-semantics --timeout "$RENDER_TIMEOUT"
  else
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
    # until it lands, this is the fix that does not need a plugin release. `bundle pack`
    # above needs none of this: it sequences its own render.
    ./gradlew ":$MODULE:composePreviewDiscover" ":$MODULE:composePreviewRenderAll"
    ./gradlew ":$MODULE:composePreviewBundle"
  fi

  # A bundle whose renders are missing is not a broken build — design-parity
  # accepts it and fails SOFT, "none of the N listed preview(s) carry an image;
  # the pack rendered nothing", after which the run prints no verdict at all.
  # Catch it here, where the cause is one line away.
  if [ ! -d "$RENDERS" ] || [ -z "$(ls -A "$RENDERS" 2>/dev/null)" ]; then
    echo "the render pass produced no pixels in $RENDERS." >&2
    echo "  A bundle packed from an empty renders/ compares nothing and still exits 0." >&2
    exit 1
  fi
  # `--with-semantics` is BEST-EFFORT inside the CLI: a daemon that fails to open, an unsupported
  # backend or a render error warns to stderr and leaves the already-written bundle untouched
  # rather than failing the pack. That is exactly the bundle this flag exists to avoid producing,
  # and nothing downstream will say so — so assert the artifact, the way the renders/ check above
  # asserts the pixels.
  if [ "$SEMANTICS" = 1 ] && [ "$(bundle_semantics_count)" = 0 ]; then
    echo "the pack carried no semantics, despite --with-semantics." >&2
    semantics_missing
    echo "  Re-read the pack output above for the daemon's warning; --no-semantics accepts the" >&2
    echo "  pixels-only verdict deliberately." >&2
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

# Fresh is not the same as complete. `--no-build` reuses whatever bundle is on disk, and a bundle
# left by an earlier `--no-semantics` run (or by `./gradlew :<module>:composePreviewBundle` run by
# hand) is current for the pixels and empty of everything else. Same class of failure as the
# staleness above — a verdict that is quietly narrower than it looks — so it is refused the same
# way rather than warned about.
if [ "$SEMANTICS" = 1 ] && [ "$(bundle_semantics_count)" = 0 ]; then
  echo "the bundle on disk was packed without semantics." >&2
  semantics_missing
  echo "  Drop --no-build to repack, or pass --no-semantics to accept that verdict." >&2
  exit 1
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
