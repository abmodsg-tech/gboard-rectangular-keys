#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${GITHUB_ACTOR:-}" || -z "${GITHUB_TOKEN:-}" ]]; then
  echo "Set GITHUB_ACTOR and GITHUB_TOKEN (a GitHub token with read:packages) first."
  exit 1
fi

./gradlew clean buildAndroid
printf '\nBuilt bundles:\n'
find patches/build/libs -maxdepth 1 -type f -name '*.mpp' -print
