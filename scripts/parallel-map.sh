#!/usr/bin/env bash
# Check that the two sheets' `parallel` declarations still pair — see scripts/parallel-map.mjs.
#
#   scripts/parallel-map.sh
#
# Needs the discovered preview manifest of BOTH modules, because the pairing is a statement about
# both and neither manifest carries the other's components:
#
#   ./gradlew :catalog:composePreviewDiscover :remote-catalog:composePreviewDiscover
#
# There is nothing to regenerate and nothing to commit, so there is no `--check`: this is a gate,
# and running it IS the check.
set -euo pipefail

ARGS=()
for module in catalog remote-catalog; do
  manifest="$module/build/compose-previews/previews.json"
  if [ ! -f "$manifest" ]; then
    echo "::error::$manifest is missing — run ./gradlew :$module:composePreviewDiscover first" >&2
    exit 1
  fi
  ARGS+=(--previews "$module=$manifest")
done

node scripts/parallel-map.mjs "${ARGS[@]}"
