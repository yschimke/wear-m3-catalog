#!/usr/bin/env bash
# Re-run the transform over the vendored sources in `upstream/`.
#
# Does NOT touch the network — that is `sync-upstream.sh`. Run this after editing
# `transform-rules.json` or anything in `patches/`, and after `sync-upstream.sh` has pulled a new
# AndroidX release down.
set -euo pipefail
cd "$(dirname "$0")/.."

python3 tools/transform.py "$@"

echo
echo "Generated sources are under modules/*/src/commonMain/kotlin, and are committed."
echo "Compile them with: ./gradlew assemble"
