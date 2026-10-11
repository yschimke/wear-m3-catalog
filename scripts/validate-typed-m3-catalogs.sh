#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
runner=("$root/scripts/agent-gradle.sh")
if [[ ${CI:-false} == true ]]; then runner=("$root/gradlew"); fi
# Fresh compilation/discovery owns membership and callable signatures; Kotlin owns policy.
"${runner[@]}" -p "$root"  :catalog:composePreviewDiscover :samples-catalog:composePreviewDiscover "$@"
"${runner[@]}" -p "$root/catalog-typed-generator" \
  generateCatalogs test ktfmtCheck "$@"
