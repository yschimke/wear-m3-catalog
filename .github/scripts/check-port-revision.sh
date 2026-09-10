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
# the version string has to move — and, for an unchanged upstream release, it has to move UP.
# Merely differing is not enough: a `portRevision` that goes backwards names a version that is very
# likely already published, which publish.yml skips exactly as silently as no bump at all.
set -euo pipefail
cd "$(dirname "$0")/../.."

base="${1:-origin/wear-compose-cmp}"

if ! git rev-parse --verify --quiet "$base" >/dev/null; then
  echo "check-port-revision: cannot resolve base ref '$base'" >&2
  exit 2
fi

# `base...HEAD` needs a merge base, and a SHALLOW clone of either side may not have one — the
# failure is a bare `fatal: no merge base` and exit 128, which reads like a broken check rather
# than a missing fetch. Say what it actually is.
if ! git merge-base "$base" HEAD >/dev/null 2>&1; then
  echo "check-port-revision: no merge base between '$base' and HEAD." >&2
  echo "  Both sides need enough history to find one — fetch the base WITHOUT --depth, and" >&2
  echo "  check out the branch with fetch-depth: 0." >&2
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

upstream_at() {
  # `<version> <portRevision>` from one ref. Read with python rather than jq, which the runner does
  # not necessarily have.
  git show "$1:upstream.json" | python3 -c '
import json, sys
u = json.load(sys.stdin)
print("%s %d" % (u["version"], int(u["portRevision"])))
'
}

version_string() { printf "%s-cmp%02d" "$1" "$2"; }

read -r before_version before_revision <<<"$(upstream_at "$base")"
read -r after_version after_revision <<<"$(upstream_at HEAD)"

before=$(version_string "$before_version" "$before_revision")
after=$(version_string "$after_version" "$after_revision")

fail_header() {
  echo
  echo "FAIL: $1"
  echo
  echo "  base: $before"
  echo "  head: $after"
  echo
  echo "Changed under a published path:"
  echo "$changed" | sed 's/^/  /'
  echo
}

if [ "$before_version" != "$after_version" ]; then
  # The upstream release moved, so the version string moved with it whatever the revision does.
  # `portRevision` resets to 1 for a new upstream (upstream.json says so), but a second change to
  # an already-published new upstream legitimately arrives at 2 — and from THIS branch's base that
  # still reads as a version change. So the reset is reported, not enforced.
  echo "OK: $before -> $after (upstream release moved)"
  exit 0
fi

# Same upstream release, so `portRevision` alone decides the version — and it has to go UP, not
# merely differ. A DECREASE is the dangerous case this catches: it names a version that is very
# likely already published, and publish.yml skips a published version silently. Two branches cut
# from the same base bumping to the same number is the other: whichever merges second changes the
# port under a version string that is already out.
if [ "$after_revision" -le "$before_revision" ]; then
  if [ "$after_revision" -eq "$before_revision" ]; then
    fail_header "this changes what a consumer resolves, but not the version they resolve."
  else
    fail_header "portRevision went BACKWARDS ($before_revision -> $after_revision)."
  fi
  echo "Set \`portRevision\` in upstream.json above $before_revision. A published version is"
  echo "immutable, so publish.yml will SKIP this change rather than fail on it — the cost of"
  echo "getting this wrong is a release that silently does not happen, which is why it is a check"
  echo "and not a convention."
  exit 1
fi

echo "OK: $before -> $after"
