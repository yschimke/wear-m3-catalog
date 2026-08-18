#!/usr/bin/env bash
#
# Applies the repository settings that the checked-in config cannot: squash-only
# merges, GitHub auto-merge, and the `Protect Main` branch ruleset that Renovate's
# automerge depends on.
#
# Why a script and not a workflow: these are repository-administration writes, so
# they need a token with `administration: write` on the repo. That is a scope no
# CI job here carries and no agent session is given, which means the settings
# would otherwise drift with nothing recording what they are meant to be. Running
# this file IS the record — it is idempotent, so re-running it after a manual
# change in the GitHub UI puts the repo back to what this file says.
#
# Usage:
#   scripts/setup-repo-protection.sh            # apply to yschimke/wear-m3-catalog
#   REPO=owner/name scripts/setup-repo-protection.sh
#   DRY_RUN=1 scripts/setup-repo-protection.sh  # print what would change, write nothing
#
# Auth: uses `gh` if it is authenticated, otherwise GITHUB_TOKEN. Either way the
# credential needs admin rights on the repo — a default Actions token does not.

set -euo pipefail

REPO="${REPO:-yschimke/wear-m3-catalog}"
DRY_RUN="${DRY_RUN:-}"
RULESET_NAME="Protect Main"

# Every check that runs on a pull request, by the name GitHub reports it under
# (the job's `name:`, not its key). A job missing from this list can fail without
# blocking a merge — which for an automerged Renovate PR means it lands red.
#
# `Render previews` is the one worth naming: it is the only check that proves the
# catalog still renders, and compose-ai-tools is both the most frequently
# automerged group and the one whose failure mode is preview discovery breaking
# outright. The 90-minute timeout on that job describes a cold full render; the
# last 15 runs came in at 1-5 min, because the action renders against a warm
# baseline and only redraws what changed. So it is cheap to require, and there is
# no automerge-latency argument for leaving it out.
REQUIRED_CHECKS=(
  "Build + discover previews"
  "Unit tests"
  "ktfmtCheck"
  "Validate catalog spec"
  "Render previews (baseline or PR comment, auto-selected)"
)

# GitHub Actions' own app id. Pinning each check to it stops an unrelated
# integration satisfying a required context by reporting the same name.
ACTIONS_APP_ID=15368

api() {
  local method="$1" path="$2"
  shift 2
  if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
    gh api --method "$method" "$path" "$@"
  else
    : "${GITHUB_TOKEN:?set GITHUB_TOKEN (needs administration:write on $REPO) or authenticate gh}"
    local args=(-sS --fail-with-body -X "$method"
      -H "Authorization: Bearer $GITHUB_TOKEN"
      -H "Accept: application/vnd.github+json"
      -H "Content-Type: application/json")
    if [[ "${1:-}" == "--input" ]]; then
      args+=(--data-binary "@${2}")
    fi
    curl "${args[@]}" "https://api.github.com/${path#/}"
  fi
}

say() { printf '\n== %s\n' "$1"; }

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

# ---------------------------------------------------------------------------
# 1. Merge strategy: squash only.
#
# The delivery branches (design-artifacts/*) and release tooling read main's
# history as one commit per change. A merge commit puts a branch's intermediate
# states on main — for this repo that means half-regenerated design-map.json and
# render manifests appearing as real commits on the branch other repos pin to.
#
# allow_auto_merge is what Renovate's `platformAutomerge` actually uses: without
# it, an automerge PR sits until a human presses the button.
# ---------------------------------------------------------------------------
cat >"$tmp/repo.json" <<'JSON'
{
  "allow_squash_merge": true,
  "allow_merge_commit": false,
  "allow_rebase_merge": true,
  "allow_auto_merge": true,
  "delete_branch_on_merge": true,
  "squash_merge_commit_title": "PR_TITLE",
  "squash_merge_commit_message": "BLANK"
}
JSON

say "Merge settings for $REPO"
if [[ -n "$DRY_RUN" ]]; then
  cat "$tmp/repo.json"
else
  api PATCH "/repos/$REPO" --input "$tmp/repo.json" |
    (command -v jq >/dev/null 2>&1 &&
      jq '{allow_squash_merge,allow_merge_commit,allow_auto_merge,delete_branch_on_merge,squash_merge_commit_title}' ||
      cat)
fi

# ---------------------------------------------------------------------------
# 2. The `Protect Main` ruleset.
#
# required_approving_review_count is 0 on purpose: this is a single-maintainer
# repo, and requiring an approval would mean every Renovate PR waits for a human
# to click approve, which is the thing automerge exists to avoid. The gate that
# actually protects main is the required status checks — automerge cannot outrun
# them, because GitHub holds the merge until every one reports success.
# ---------------------------------------------------------------------------
{
  printf '{"name":"%s","target":"branch","enforcement":"active",' "$RULESET_NAME"
  printf '"conditions":{"ref_name":{"include":["~DEFAULT_BRANCH"],"exclude":[]}},'
  printf '"bypass_actors":[],"rules":[{"type":"deletion"},{"type":"non_fast_forward"},'
  printf '{"type":"pull_request","parameters":{"required_approving_review_count":0,'
  printf '"dismiss_stale_reviews_on_push":false,"require_code_owner_review":false,'
  printf '"require_last_push_approval":false,"required_review_thread_resolution":false,'
  printf '"allowed_merge_methods":["squash"]}},'
  printf '{"type":"required_status_checks","parameters":{'
  printf '"strict_required_status_checks_policy":false,"do_not_enforce_on_create":false,'
  printf '"required_status_checks":['
  sep=""
  for check in "${REQUIRED_CHECKS[@]}"; do
    printf '%s{"context":"%s","integration_id":%d}' "$sep" "$check" "$ACTIONS_APP_ID"
    sep=","
  done
  printf ']}}]}'
} >"$tmp/ruleset.json"

say "Ruleset '$RULESET_NAME' for $REPO"
if [[ -n "$DRY_RUN" ]]; then
  (command -v jq >/dev/null 2>&1 && jq . "$tmp/ruleset.json" || cat "$tmp/ruleset.json")
  exit 0
fi

existing="$(api GET "/repos/$REPO/rulesets" | jq -r --arg n "$RULESET_NAME" '.[] | select(.name == $n) | .id' | head -1)"

if [[ -n "$existing" ]]; then
  echo "updating ruleset $existing"
  api PUT "/repos/$REPO/rulesets/$existing" --input "$tmp/ruleset.json" | jq '{id, name, enforcement}'
else
  echo "creating ruleset"
  api POST "/repos/$REPO/rulesets" --input "$tmp/ruleset.json" | jq '{id, name, enforcement}'
fi

say "Done. Verify at https://github.com/$REPO/settings/rules"
