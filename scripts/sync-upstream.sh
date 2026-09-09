#!/usr/bin/env bash
# Pull the AndroidX release pinned in `upstream.json` and regenerate everything from it.
#
# The whole update path for a new AndroidX release is:
#
#   1. edit `version` in upstream.json           (and reset portRevision to 1)
#   2. scripts/sync-upstream.sh
#   3. ./gradlew assemble
#   4. read the diff — upstream/ shows what AndroidX changed, modules/ what it means here
#
# A patch that no longer applies stops step 2 and names the file. That is the design: upstream
# moved under a hand-written port, and a human has to decide what it now means.
set -euo pipefail
cd "$(dirname "$0")/.."

python3 tools/sync.py
python3 tools/transform.py
