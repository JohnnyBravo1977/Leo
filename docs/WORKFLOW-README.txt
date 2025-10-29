## LEO WORKFLOW-README (Post-SyncPack v2 System)
Baseline: V13-dev, 2025-10-29
Purpose: Maintain a clean, reversible, error-proof patching and sync workflow between human and AI development phases.

---
🧭 Golden Rules
1. Never patch blind. Always provide a LEO_TARGETS_BUNDLE_*.zip before any modification.
2. AI must build ready-to-apply .patch files — tested for IDE import compatibility.
3. Every patch must be atomic, reversible, and explicitly scoped (no side edits).
4. No lies. If something won’t work, it must be stated clearly.
5. Baseline drift is prevented through .gitattributes and consistent EOL handling.
6. New system replaces all SyncPack Lite / Meta methods.

---
⚙️ Preparation (One-Time Setup)
Run once at repo root to normalize endings and stop CRLF corruption:

# .gitattributes (repo root)
* text=auto eol=lf
*.bat text eol=crlf
*.cmd text eol=crlf
*.ps1 text eol=crlf
*.png binary
*.jpg binary

Then execute:
git rm --cached -r .
git reset --hard
git config core.autocrlf false

Commit this .gitattributes file before any further patches.

---
🧩 Patch Workflow Protocol v2.0
Objective: Generate small, targeted bundles that allow ChatGPT to produce precise, ready-to-apply patches.

1️⃣ Prepare TargetFiles.txt
Located in /scripts/. Each line = a relative path from repo root, e.g.:
app/src/main/java/com/example/leo/ui/chat/ChatScreen.kt
app/src/main/java/com/example/leo/ui/settings/SettingsScreen.kt
docs/WORKFLOW-README.txt

These files represent the contract set — all code involved in a single feature or fix.

2️⃣ Generate Bundle
Run:
./scripts/makeTargetSet.ps1

This script:
- Gathers listed files
- Adds minimal _meta/ context (branch, commit, status)
- Zips to:
  ~/Desktop/SyncStage/LEO_TARGETS_BUNDLE_<timestamp>.zip
You then send that ZIP to ChatGPT.

---
🧠 AI Phase
1. ChatGPT unpacks your bundle.
2. It analyzes all file paths and cross-references dependencies.
3. It builds a single unified .patch file, IDE-ready (no manual copy/paste).
4. You apply via Android Studio: VCS → Apply Patch from File…
5. If any hunk fails, stop immediately. Don’t rebuild manually; request a corrected patch.

---
🔁 Post-Patch Verification
After applying a patch:
git diff --stat
Confirm only the intended files changed. Then commit:
git add .
git commit -m "Apply LEO Patch <desc>"
git push

---
🧯 Drift Prevention
When switching devices or sessions:
git fetch --all --prune
git pull --rebase
This keeps your local files synced with the canonical baseline.

---
📦 End-of-Cycle Maintenance
Every few major edits, create a lightweight sync archive:
tar -czf LEO_SYNC_LITE_<date>_<branch>.tar.gz app/ docs/ scripts/
This acts as a clean restore point — never used for patching, only for safety backups.

---
✅ Summary
- Old SyncPack, Meta, and Diffstat methods are deprecated.
- New workflow = TargetFiles + makeTargetSet.ps1 → LEO_TARGETS_BUNDLE → AI Patch.
- All line-ending, EOL, and “unknown chunk” corruption errors are permanently resolved via .gitattributes.
- Patches are small, readable, and completely IDE-safe.
- Every ChatGPT session from this point forward should ask: “Send me a LEO_TARGETS_BUNDLE containing the relevant files.”

---
🧪 SmokeTest Checklist (for each patch)
1. Patch applies cleanly in Android Studio (no chunk errors).
2. App builds successfully (Run → Build Project).
3. All edited screens open normally.
4. No console errors appear on startup.
5. Commit and push after passing checks.
