#!/usr/bin/env bash
# CI: prove the committed generated sources are exactly what the pipeline produces.
#
# `modules/*/src/commonMain` is generated and committed — committed so the build needs no Python
# and so each AndroidX release arrives as a reviewable diff. That only holds if the two never
# drift, which is what this checks: regenerate from the vendored `upstream/`, and fail if the
# working tree moved.
set -euo pipefail
cd "$(dirname "$0")/.."

python3 tools/transform.py

# Only the GENERATED paths, named exactly. Everything else under `modules/` and `docs/` is
# hand-written — the port's own source sets, the pipeline guide — and listing their parents made an
# ordinary work-in-progress edit look like generator drift.
generated=(docs/ANDROID_SURFACE.md)
for module in modules/*/src/commonMain; do generated+=("$module"); done

if ! git diff --quiet -- "${generated[@]}"; then
  echo
  echo "FAIL: the committed generated sources differ from what tools/transform.py produces."
  echo "Run scripts/regenerate.sh and commit the result."
  echo
  git --no-pager diff --stat -- "${generated[@]}"
  exit 1
fi

echo "OK: generated sources match."
