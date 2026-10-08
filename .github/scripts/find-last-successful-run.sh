#!/bin/bash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

# Finds the run ID of the last successful run of a given workflow on a given branch
# by walking backwards through the commit history (up to MAX_COMMITS) and querying
# the GitHub API per commit SHA. This avoids the GitHub API bug where
# --status success --limit 1 can return stale run IDs.
#
# Usage:
#   find-last-successful-run.sh --repo <owner/repo> --workflow <workflow-name> --branch <branch>
#
# Outputs the run ID to stdout and exits 0 on success, exits 1 if not found.
#
# Required environment variable:
#   GH_TOKEN - a GitHub token with actions:read permission on the target repo

set -e

MAX_COMMITS=20

REPO=""
WORKFLOW=""
BRANCH=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)     REPO="$2";     shift 2 ;;
    --workflow) WORKFLOW="$2"; shift 2 ;;
    --branch)   BRANCH="$2";   shift 2 ;;
    *) echo "Unknown argument: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$REPO" || -z "$WORKFLOW" || -z "$BRANCH" ]]; then
  echo "Error: --repo, --workflow, and --branch are all required" >&2
  exit 1
fi

echo "Searching for last successful run of '$WORKFLOW' on '$REPO' branch '$BRANCH' (max $MAX_COMMITS commits)" >&2

COMMITS=$(gh api "repos/$REPO/commits?sha=$BRANCH&per_page=$MAX_COMMITS" --jq '.[].sha')

if [[ -z "$COMMITS" ]]; then
  echo "Error: could not retrieve commit history for $REPO@$BRANCH" >&2
  exit 1
fi

while IFS= read -r SHA; do
  RUN_ID=$(gh run list \
    --repo "$REPO" \
    --workflow "$WORKFLOW" \
    --commit "$SHA" \
    --status success \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId // empty')

  if [[ -n "$RUN_ID" ]]; then
    echo "Found successful run $RUN_ID for commit $SHA" >&2
    echo "$RUN_ID"
    exit 0
  fi
done <<< "$COMMITS"

echo "Error: no successful run of '$WORKFLOW' found in the last $MAX_COMMITS commits on '$REPO' branch '$BRANCH'" >&2
exit 1
