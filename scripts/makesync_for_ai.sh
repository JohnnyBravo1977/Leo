#!/usr/bin/env bash
set -euo pipefail

# --- Config -----------------------------------------------------
OUTDIR="syncpacks"
TS="$(date +%Y%m%d_%H%M)"
PACK="LEO_SYNC_LITE_${TS}.tar.gz"

# Name the temporary meta + staging folders (cleaned at end)
METADIR=".syncmeta_${TS}"
STAGE=".syncstage_${TS}"

# What to EXCLUDE from the pack (build outputs, caches, big IDE files, etc.)
EXCLUDES=(
  "./.git"
  "./.gradle"
  "./**/build"
  "./**/.idea"
  "./**/*.iml"
  "./local.properties"
  "./${OUTDIR}"
)

# --- Preflight --------------------------------------------------
mkdir -p "$OUTDIR" "$METADIR" "$STAGE"

# Try to capture branch/remote nicely even inside detached HEAD
BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo 'HEAD')"
REMOTE_URL="$(git remote get-url origin 2>/dev/null || echo 'NO_REMOTE')"
HEAD_SHA="$(git rev-parse --verify HEAD 2>/dev/null || echo 'NO_HEAD')"
SYNC_TAG="$(git tag -l 'SYNCPOINT' | head -n1 || true)"

# --- Meta capture ----------------------------------------------
{
  echo "# --- GIT ---"
  echo "branch: ${BRANCH}"
  echo "remote: ${REMOTE_URL}"
  echo "head:   ${HEAD_SHA}"
  echo "syncpoint_tag: ${SYNC_TAG}"
  echo
  echo "## status (porcelain v2)"
  git status --porcelain=v2 || true
  echo
  echo "## last 15 commits (oneline)"
  git log --oneline -n 15 || true
  echo
  echo "## tracked files"
  git ls-files || true
} > "${METADIR}/git.txt"

# diff against SYNCPOINT (if present)
if git rev-parse -q --verify "refs/tags/SYNCPOINT" >/dev/null 2>&1 ; then
  git diff --stat SYNCPOINT..HEAD > "${METADIR}/diffstat.txt" || true
  git diff SYNCPOINT..HEAD > "${METADIR}/diff.patch" || true
else
  printf "No SYNCPOINT tag found.\n" > "${METADIR}/diffstat.txt"
fi

# project facts
{
  echo "# --- PROJECT FACTS ---"
  echo "generated: ${TS}"
  echo
  echo "## root build.gradle(.kts)"
  ls -1 build.gradle* 2>/dev/null || true
  echo
  echo "## settings.gradle(.kts)"
  ls -1 settings.gradle* 2>/dev/null || true
  echo
  echo "## compile/target SDK (best effort grep)"
  grep -Rin "compileSdk" app/build.gradle* 2>/dev/null || true
  grep -Rin "targetSdk"  app/build.gradle* 2>/dev/null || true
  echo
  echo "## Manifest package + permissions (best effort grep)"
  grep -Rin 'package="' app/src/main/AndroidManifest.xml 2>/dev/null || true
  grep -Rin '<uses-permission' app/src/main/AndroidManifest.xml 2>/dev/null || true
} > "${METADIR}/facts.txt"

# file inventory + hashes (kt/xml only; safe + compact)
INV="${METADIR}/inventory.txt"
echo "# path | git-hash-object (blob sha1) | size" > "$INV"
while IFS= read -r f ; do
  sz="$(wc -c < "$f" | tr -d ' ')"
  sha="$(git hash-object --no-filters "$f" 2>/dev/null || echo 'NA')"
  echo "${f}|${sha}|${sz}" >> "$INV"
done < <(find app/src -type f \( -name "*.kt" -o -name "*.xml" \) | sort)

# --- Build file list for pack ----------------------------------
LISTFILE=".syncfiles_${TS}.txt"
# Start with everything, then prune excludes
find . -type f \
  ! -path "./${STAGE}/*" \
  ! -path "./${METADIR}/*" \
  | sed 's#^\./##' > "$LISTFILE"

# Apply excludes
for ex in "${EXCLUDES[@]}"; do
  # turn "./dir" into grep -v '^dir(/|$)'
  pat="$(echo "${ex#./}" | sed 's#\.#\\.#g')"
  tmp="${LISTFILE}.tmp"
  grep -Ev "^${pat}(/|$)" "$LISTFILE" > "$tmp" || true
  mv "$tmp" "$LISTFILE"
done

# We want to INCLUDE useful scripts (including this one)
# If the earlier exclusion knocked them out, add back scripts/*.sh
if [ -d scripts ]; then
  find scripts -maxdepth 1 -type f -name "*.sh" >> "$LISTFILE"
fi

# stage sources
while IFS= read -r p ; do
  dstdir="${STAGE}/$(dirname "$p")"
  mkdir -p "$dstdir"
  cp -p "$p" "$dstdir/" 2>/dev/null || true
done < "$LISTFILE"

# add meta under _meta/
mkdir -p "${STAGE}/_meta"
cp -a "${METADIR}/." "${STAGE}/_meta/"

# --- Create tarball --------------------------------------------
tar -C "$STAGE" -czf "${OUTDIR}/${PACK}" .

# --- Cleanup ----------------------------------------------------
rm -rf "$STAGE" "$METADIR" "$LISTFILE"

echo "✅ SyncPack Lite for AI created:"
echo "   ${OUTDIR}/${PACK}"
