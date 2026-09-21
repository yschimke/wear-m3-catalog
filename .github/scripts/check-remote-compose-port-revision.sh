#!/usr/bin/env bash
set -euo pipefail

base=${1:-HEAD^1}
metadata=vendor/remote-compose-upstream.json

published=$(git diff --name-only "$base" HEAD -- \
  'vendor/remote-core/**' \
  'vendor/remote-write-core/**' \
  'vendor/remote-creation-compose/**' \
  'vendor/remote-foundation/**' \
  'vendor/remote-material3/**' \
  | grep -Ev '(^|/)(src/[^/]*Test|README\.md$)' || true)

if [[ -z "$published" ]]; then
  exit 0
fi

head_revision=$(python3 -c 'import json; print(json.load(open("vendor/remote-compose-upstream.json"))["portRevision"])')
if git cat-file -e "$base:$metadata" 2>/dev/null; then
  base_revision=$(git show "$base:$metadata" | python3 -c 'import json,sys; print(json.load(sys.stdin)["portRevision"])')
else
  base_revision=0
fi

if (( head_revision <= base_revision )); then
  echo "Published Remote Compose sources changed without increasing portRevision." >&2
  echo "Base: $base_revision; head: $head_revision" >&2
  echo "$published" >&2
  exit 1
fi
