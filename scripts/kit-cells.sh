#!/usr/bin/env bash
# Regenerate kit-cells.json — how much of each published kit set each sheet draws.
#
#   scripts/kit-cells.sh [--check]
#
# Needs the discovered preview manifest of BOTH modules, because both sheets reproduce the same
# kit and the record answers for both:
#
#   ./gradlew :catalog:composePreviewDiscover :remote-catalog:composePreviewDiscover
#
# The numerator is each module's RESOLVED design map, projected here through the same pinned
# upstream steps `scripts/design-map.sh` uses — into a temp dir, so the map committed for
# `:catalog` is left exactly as it is. Why the resolved map rather than the annotations:
# scripts/kit-cells.mjs, top.
set -euo pipefail

CHECK=""
[ "${1:-}" = "--check" ] && { CHECK=1; shift; }

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

MODULES="catalog remote-catalog"
ARGS=()
for module in $MODULES; do
  manifest="$module/build/compose-previews/previews.json"
  if [ ! -f "$manifest" ]; then
    echo "::error::$manifest is missing — run ./gradlew :$module:composePreviewDiscover first" >&2
    exit 1
  fi
  scripts/design-map.sh --out-dir "$WORK/$module" "$module"
  ARGS+=(--map "$module=$WORK/$module/design-map.json")
done

if [ -n "$CHECK" ]; then
  node scripts/kit-cells.mjs "${ARGS[@]}" --check
else
  node scripts/kit-cells.mjs "${ARGS[@]}"
fi
