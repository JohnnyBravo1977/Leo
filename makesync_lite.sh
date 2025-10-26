#!/usr/bin/env bash
set -euo pipefail

git am --abort 2>/dev/null || true
rm -rf .git/rebase-apply .git/rebase-merge 2>/dev/null || true
git fetch --all --prune

BASE=$(git merge-base HEAD origin/main || echo HEAD)
TS=$(date +%Y%m%d_%H%M)
OUT="syncpacks/LEO_SYNC_LITE_${TS}.patch"

mkdir -p syncpacks

git diff --full-index "$BASE"..HEAD -- \
  ':(exclude)*.png' ':(exclude)*.jpg' ':(exclude)*.jpeg' ':(exclude)*.webp' \
  ':(exclude)*.gif' ':(exclude)*.svg' \
  ':(exclude)*.aab' ':(exclude)*.apk' \
  ':(exclude).gradle/**' ':(exclude)build/**' ':(exclude).idea/**' \
  ':(exclude)build.gradle.kts' \
  ':(exclude)gradle.properties' \
  ':(exclude)gradlew' \
  ':(exclude)gradlew.bat' \
  ':(exclude)settings.gradle.kts' \
  ':(exclude)VERSION' \
  ':(exclude)local.properties' \
  ':(exclude)LEO-CHARTER.md' \
  ':(exclude)PROJECT-OUTLINE.md' \
  ':(exclude)WORKFLOW-README.md' \
  ':(exclude)CONTRIBUTING.md' \
  ':(exclude)CHANGELOG.MD' \
  ':(exclude)docs/**' \
  > "$OUT"

echo "✅ SyncPack Lite created: $OUT"
ls -lh "$OUT"