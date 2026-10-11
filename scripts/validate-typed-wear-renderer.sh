#!/usr/bin/env bash
# The typed Wear pilot: compiled entry points, their Kotlin adapter definitions, a parallel
# `wear-m3-typed-pilot` pair, its JVM parity tests against the production adapters, the compiled
# source fixtures and the packaged browser runtime. No production input or delivery is rewritten.
#
#   scripts/validate-typed-wear-renderer.sh <compose-ai-tools checkout> <compose-ui-builder checkout>
set -euo pipefail

root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
tools=$(cd "${1:-$root/../compose-ai-tools}" && pwd)
if [[ $# -gt 0 ]]; then shift; fi
builder=$(cd "${1:-$root/../compose-ui-builder}" && pwd)
if [[ $# -gt 0 ]]; then shift; fi

runner=("$root/scripts/agent-gradle.sh")
if [[ ${CI:-false} == true ]]; then runner=("$root/gradlew"); fi
"${runner[@]}" -p "$root" \
  -PtypedWearCatalog=true -PtypedAdapterCatalog=true -PlocalBuilds=tools \
  "-PcomposeAiToolsDir=$tools" "-PlocalBuild.tools=$tools" "-PcomposeUiBuilderDir=$builder" \
  :catalog-typed-adapters:verifyTypedCatalog :catalog-typed-adapters:rendererArchive "$@"

printf 'Pilot pair and readiness: %s\n' "$root/catalog-typed-adapters/build/catalog"
printf 'Parity evidence: %s\n' "$root/catalog-typed-adapters/build/render-evidence"
