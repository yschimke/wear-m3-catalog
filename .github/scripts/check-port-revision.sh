#!/usr/bin/env bash
# A change to what a consumer resolves must change the version it resolves.
#
# `publish.yml` treats a published version as immutable: it reads `<version>-cmp<portRevision>` from
# upstream.json, checks the `wear-compose-cmp-maven` branch, and SKIPS every publishing step when
# that version is already out. That is the right design — republishing would silently move what a
# consumer already resolved — but it has an invisible failure mode. A pull request that changes the
# port without bumping `portRevision` merges green, and the publish job then skips it. Nothing goes
# red; the change simply never reaches anyone.
#
# It has happened. PR #398 (the DatePicker/TimePicker port) and PR #400 (the java.time signature)
# both landed on the trunk without a bump, and for about a day the published cmp02 — built from
# #394 — was what consumers got. Publish run 8 records it exactly: "Already published" succeeded and
# "Build the repository tree", "Publish to GitHub Packages" and "Push the Maven tree" were skipped.
#
# So this is the other half of that gate: if the diff touches anything that ends up in an artifact,
# the version string has to move.
set -euo pipefail
cd "$(dirname "$0")/../.."

base="${1:-origin/wear-compose-cmp}"

if ! git rev-parse --verify --quiet "$base" >/dev/null; then
  echo "check-port-revision: cannot resolve base ref '$base'" >&2
  exit 2
fi

# What lands in a published artifact. Test source sets are deliberately absent: a test is compiled
# into no publication, so changing one changes nothing a consumer resolves.
changed=$(git diff --name-only "$base...HEAD" -- \
  'modules/**' \
  'build.gradle.kts' \
  'settings.gradle.kts' \
  'gradle/libs.versions.toml' \
  ':(exclude)modules/*/src/jvmTest/**' \
  ':(exclude)modules/*/src/commonTest/**' \
  ':(exclude)modules/*/src/wasmJsTest/**')

if [ -z "$changed" ]; then
  echo "OK: nothing that reaches a published artifact changed."
  exit 0
fi

version_at() {
  # `<version>-cmp<portRevision>`, the same string build.gradle.kts computes. Read with python
  # rather than jq, which the runner does not necessarily have.
  git show "$1:upstream.json" | python3 -c '
import json, sys
u = json.load(sys.stdin)
print("%s-cmp%02d" % (u["version"], int(u["portRevision"])))
'
}

before=$(version_at "$base")
after=$(version_at HEAD)

if [ "$before" = "$after" ]; then
  echo
  echo "FAIL: this changes what a consumer resolves, but not the version they resolve."
  echo
  echo "  published version, unchanged: $after"
  echo
  echo "Changed under a published path:"
  echo "$changed" | sed 's/^/  /'
  echo
  echo "Bump \`portRevision\` in upstream.json. A published version is immutable, so publish.yml"
  echo "will SKIP this change rather than fail on it — the cost of getting this wrong is a release"
  echo "that silently does not happen, which is why it is a check and not a convention."
  exit 1
fi

echo "OK: $before -> $after"
