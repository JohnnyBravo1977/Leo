───────────────────────────────────────────────
LEO WORKFLOW README — PATCH PROTOCOL v3.0
───────────────────────────────────────────────
Updated: 2025-10-29
Applies from: V13-dev-E2E-PASS onward

───────────────────────────────────────────────
PURPOSE
───────────────────────────────────────────────
This document defines the only acceptable method for building,
delivering, and applying patches to the Leo project. It replaces
all prior SyncPack-only steps.

The purpose is to ensure:
• Every patch is fully reproducible
• No guessing or code drift
• Full internal verification before delivery
• Downloadable, apply-ready patches ONLY

───────────────────────────────────────────────
PROTOCOL LAYERS
───────────────────────────────────────────────
1.  TARGETBUNDLE CREATION (Your Side)
───────────────────────────────────────────────
Use makeTargetSet.ps1 to bundle only the current working files.
Before running it:
    • Verify that targetfiles.txt contains the correct files.
    • Run deploy_check.ps1 (new script) to ensure every file exists.
    • The bundle must include:
        - All files involved in the feature or fix
        - _meta/version.txt containing tag + SHA checksum

Output format:
    LEO_TARGETS_BUNDLE_<timestamp>.zip

───────────────────────────────────────────────
2.  PRE-FLIGHT CHECK (My Side)
───────────────────────────────────────────────
When you upload the bundle:
    • I confirm checksum and file count.
    • I extract it into a clean temp repo.
    • I commit the snapshot as BASE.
    • I normalize all line endings (LF).
    • I rebuild the requested feature using those paths only.
    • I run:
        git apply --check <patch>
      ✅ Must pass before delivery.
    • I export ONE verified patch:
        LEO_PATCH_<feature>_<branch>.patch
      — No copy-paste, no text blobs.

───────────────────────────────────────────────
3.  DELIVERY
───────────────────────────────────────────────
    • You receive a downloadable .patch file only.
    • I include an APPLY_README inline with:
        - exact apply command
        - rollback instructions (git revert or inverse patch)

───────────────────────────────────────────────
4.  APPLY + VERIFY (Your Side)
───────────────────────────────────────────────
In Android Studio:
    VCS → Apply Patch… → Select patch → Apply
Then:
    git status   → should show modified files
    Build        → confirm success

If errors appear:
    • Create a new TargetBundle with the affected files only.
    • I issue a micro-patch to resolve the missing imports or paths.

───────────────────────────────────────────────
5.  VERSION TAGGING
───────────────────────────────────────────────
Every successful patch application creates a new baseline:
    TAG: V13-dev-E2E-PASS-#increment
Example:
    V13-dev-E2E-PASS-002

Each tag represents a verified working state.

───────────────────────────────────────────────
6.  DRIFT PREVENTION
───────────────────────────────────────────────
If I ever produce non-verified text or skip testing,
type the command:
/reset_procedure
→ I immediately revert to strict Preflight Mode and
stop producing unverified content.

───────────────────────────────────────────────
7.  ZERO-TOLERANCE RULES
───────────────────────────────────────────────
• No unverified patches.
• No partial edits or inline code unless requested for reference.
• No CRLF endings.
• No speculative file guessing.
• No new file names unless provided in TargetBundle.

Violation of these halts patch creation automatically.

───────────────────────────────────────────────
8.  NEXT STEPS
───────────────────────────────────────────────
Once this file is placed in your repo root as
    /WORKFLOW-README.txt
you can delete old makesync.ps1 references.
All future operations will follow this document.
───────────────────────────────────────────────
END OF DOCUMENT
───────────────────────────────────────────────
