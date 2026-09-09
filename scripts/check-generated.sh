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

if ! git diff --quiet -- modules docs; then
  echo
  echo "FAIL: the committed generated sources differ from what tools/transform.py produces."
  echo "Run scripts/regenerate.sh and commit the result."
  echo
  git --no-pager diff --stat -- modules docs
  exit 1
fi

echo "OK: generated sources match."
