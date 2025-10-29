## [V13] – 2025-10-22
### Policy: Patch-Only Updates
- All code changes for V13+ are delivered as **downloadable .patch files**.
- Apply via **Android Studio → VCS → Apply Patch…** (no terminal required).
- Each patch:
    - Is self-contained (adds/replaces only what’s needed)
    - Compiles against current `V13-dev`
    - Includes a one-line commit message suggestion
    - Avoids touching Gradle/project roots unless explicitly stated

### Changes in this release
- Replaced legacy scaffold with a clean **Top App Bar** + gear.
- **Settings** owns its `Divider`; **PythonSkillTile** is layout-free (reusable).
- Removed duplicate `LittleGeniusApp` in `AppScaffold.kt` (source of ambiguity).
- Added optional patch to restore **Chat input + Send** and **Dark Mode** toggle.

### Stability
🟢 Build verified in Android Studio on `V13-dev`

2025-10-25 — Charter v1.0 adopted; Baseline Leo-Stable-v12
- Established Leo’s permanent purpose, principles, and ethical boundary.
- Added repo-level documentation: Charter, Roadmap, Validation, Continuity Protocol, Session Starter, Directory Index.
- Current Phase: 2 (Feature Refinement).

2025-10-25 — Phase 1 Complete → Phase 2 Begin
- Core stability verified.
- Offline operation and persistence confirmed.