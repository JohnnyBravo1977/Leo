# 🟢 Leo (Android)

# 🔒 Unbreakable Rule – Honesty and Transparency

No lies.
Every statement, instruction, or feature claim must be grounded in what can *actually* be done with the tools and access available.
If something cannot work, is uncertain, or depends on a limitation of environment, it must be stated immediately and plainly.
Optimism, guesses, or omissions that lead to wasted work are treated as violations of the Charter.

When a task cannot be performed because of sandbox limits, missing permissions, or external-service restrictions, the assistant will say so directly.
Together, we will find a workable path inside the real boundaries.

## Charter Compliance
All builds and syncpacks must align with the active Leo Charter version.
No modification may violate the Core Immutable Rule or Charter principles.
`LEO-CHARTER.md` is mandatory in root and travels with every syncpack.
Verify Charter version matches or exceeds local before applying any pack.
Note: The Core Principle (Leo never misleads the children) is immutable and cannot be altered.
All contributors must acknowledge this rule as permanent.

[![Android CI — V13-dev](https://github.com/JohnnyBravo1977/Leo/actions/workflows/android-ci.yml/badge.svg?branch=V13-dev)](https://github.com/JohnnyBravo1977/Leo/actions/workflows/android-ci.yml)

**Status:** ✅ Build passing on `V13-dev`  
Lightweight Jetpack Compose app with a single top app bar, chat screen, and settings screen (Dark Mode included).

---

## Project Status & Goals (live)
- **Build:** Green.
- **Launch flow:** Acceptable — brief white system frame then full-screen splash illustration, then Chat.
- **UI items currently missing (to restore):**
  - “Little Genius” centered header
  - Settings button
  - Trash (clear chat) button
  - Dark Mode toggle (pending once Settings returns)

> Restoration of these UI items is tracked as a separate task. This README focuses on workflow and sync mechanics.

---

## Source of Truth
- **Active dev branch:** `V13-dev` (patch-only; must be green before merge to `main`).
- **Stable branch:** `main` (CI green only).
- **CI:** Every push/PR to `V13-dev` runs `assembleDebug` and posts a sticky PASS/FAIL.

---

## Protected Files & Paths (off-limits for deletion/edit by patches)
These **must not** be altered by sync packs or cleanup scripts:
```
build.gradle.kts
gradlew
gradlew.bat
settings.gradle.kts
gradle.properties
VERSION
local.properties
LEO-CHARTER.md
PROJECT-OUTLINE.md
WORKFLOW-README.md
CONTRIBUTING.md
CHANGELOG.MD
docs/**        (entire folder and contents)
```

---

## SyncPack Lite — What, Why, When
**What:** A small, text-only patch that snapshots current code state (no big binaries).  
**Why:** So a new chat can read and apply the exact code you’re on without transferring large files.  
**When:** Whenever you finish a working step (e.g., splash flow stabilized) and want a new session to start “in sync.”

**Contents:**
- Source files and configuration **excluding** images/binaries and the protected list above.
- Enough context for a new chat to generate precise, non-destructive patches.

---

## How to Create a SyncPack Lite (command-by-command)

Run these from the repo root (Git Bash is fine):

```bash
# 0) Clean any stuck patch state (safe to run every time)
git am --abort 2>/dev/null || true
rm -rf .git/rebase-apply .git/rebase-merge 2>/dev/null || true

# 1) Make sure refs are up to date
git fetch --all --prune

# 2) Choose a compact diff base: common ancestor with origin/main
BASE=$(git merge-base HEAD origin/main || echo HEAD)

# 3) Timestamped filename
TS=$(date +%Y%m%d_%H%M)
mkdir -p syncpacks

# 4) Create the text-only patch, excluding heavy & protected files
git diff --full-index "$BASE"..HEAD --   ':(exclude)*.png' ':(exclude)*.jpg' ':(exclude)*.jpeg' ':(exclude)*.webp'   ':(exclude)*.gif' ':(exclude)*.svg'   ':(exclude)*.aab' ':(exclude)*.apk'   ':(exclude).gradle/**' ':(exclude)build/**' ':(exclude).idea/**'   ':(exclude)build.gradle.kts'   ':(exclude)gradle.properties'   ':(exclude)gradlew'   ':(exclude)gradlew.bat'   ':(exclude)settings.gradle.kts'   ':(exclude)VERSION'   ':(exclude)local.properties'   ':(exclude)LEO-CHARTER.md'   ':(exclude)PROJECT-OUTLINE.md'   ':(exclude)WORKFLOW-README.md'   ':(exclude)CONTRIBUTING.md'   ':(exclude)CHANGELOG.MD'   ':(exclude)docs/**'   > "syncpacks/LEO_SYNC_LITE_${TS}.patch"

# 5) Confirm file exists and isn't empty
ls -lh "syncpacks/LEO_SYNC_LITE_${TS}.patch"
```

**Output:** `syncpacks/LEO_SYNC_LITE_YYYYMMDD_HHMM.patch` — upload this to the new chat.

---

## One-Command Script (recommended)

Create `makesync_lite.sh` in the repo root:

```bash
#!/usr/bin/env bash
set -euo pipefail

git am --abort 2>/dev/null || true
rm -rf .git/rebase-apply .git/rebase-merge 2>/dev/null || true

git fetch --all --prune
BASE=$(git merge-base HEAD origin/main || echo HEAD)

TS=$(date +%Y%m%d_%H%M)
OUT="syncpacks/LEO_SYNC_LITE_${TS}.patch"
mkdir -p syncpacks

git diff --full-index "$BASE"..HEAD --   ':(exclude)*.png' ':(exclude)*.jpg' ':(exclude)*.jpeg' ':(exclude)*.webp'   ':(exclude)*.gif' ':(exclude)*.svg'   ':(exclude)*.aab' ':(exclude)*.apk'   ':(exclude).gradle/**' ':(exclude)build/**' ':(exclude).idea/**'   ':(exclude)build.gradle.kts'   ':(exclude)gradle.properties'   ':(exclude)gradlew'   ':(exclude)gradlew.bat'   ':(exclude)settings.gradle.kts'   ':(exclude)VERSION'   ':(exclude)local.properties'   ':(exclude)LEO-CHARTER.md'   ':(exclude)PROJECT-OUTLINE.md'   ':(exclude)WORKFLOW-README.md'   ':(exclude)CONTRIBUTING.md'   ':(exclude)CHANGELOG.MD'   ':(exclude)docs/**'   > "$OUT"

echo "✅ SyncPack Lite created: $OUT"
ls -lh "$OUT"
```

Make it executable once:
```bash
git update-index --add --chmod=+x makesync_lite.sh
```

Run anytime:
```bash
./makesync_lite.sh
```

---

## How to Apply a SyncPack Lite (for a fresh clone/new chat)

```bash
# Reset any stuck state (safe)
git am --abort 2>/dev/null || true
rm -rf .git/rebase-apply .git/rebase-merge 2>/dev/null || true

# Apply
git am syncpacks/LEO_SYNC_LITE_YYYYMMDD_HHMM.patch
```

If you prefer Android Studio UI: **VCS → Apply Patch…** and select the file.

**If Git says “empty patch / refuses to create empty bundle”:**
- The target already matches the patch. Use a newer patch or change the base with `BASE=$(git rev-list --max-parents=0 HEAD)` before generating.

**If Git says “patch does not apply”:**
- Your working tree is dirty or diverged. `git status` → commit or stash; rebase to latest; re-run `git am`.

---

## New Chat Bootstrapping (what the assistant should do)
1. Read **this file** top-to-bottom (respect **Charter Compliance**).
2. Apply the provided **SyncPack Lite** (or instruct how to apply, exactly as above).
3. Confirm branch and head (e.g., `V13-dev @ <commit>`).
4. Generate patches that **do not** touch protected files/paths.
5. Prefer **downloadable .patch** artifacts over copy-paste for risky edits.
6. For assets (images) too big for Lite, ship separately as a small zip and reference the path.

---

## Patch Etiquette (for assistants and contributors)
- No edits to **protected** files/paths.
- One patch = one concern; include a concise commit message.
- Avoid large refactors in the same patch as UI tweaks.
- Always include exact file paths and full-file replacements for resources (XML, themes) to avoid “missing hunk” failures.

---

## Build & Run (quick)
```bash
./gradlew clean :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

© 2025 JohnnyBravo1977 — built with brains, caffeine, and stubbornness.
