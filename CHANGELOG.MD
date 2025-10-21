# 🧾 Little Genius (Leo App) – Changelog

A living record of Leo’s evolution from concept to consciousness.  
Each version builds directly on the previous **Stable Baseline** snapshot.

---

## 🧱 Version 12 – Active Stable (2025-10-20)
**Baseline:** `Leo-Main-Stable-V12.zip`  
**Status:** 🟢 Active Build

### Highlights
- **Dark / Light Theme Toggle** — functional in Settings.
- **Centered title bar** now displays “Little Genius”.
- **Stable ChatStore merge** (ChatRecord integrated).
- **Swipe-to-delete** with undo + snackbar feedback.
- **Offline message retry logic** with pending/failed/sent state icons.
- **Timestamp formatting** added (12-hour local time).
- **Persistent local data** storage via `DataStore` and JSON serialization.
- **Settings and Chat navigation** working (bottom bar and top button).
- **Gradle performance tuning** in `gradle.properties`.
- **Kotlin 2.0 / Compose Material 3** fully integrated and stable.

---

## 🧱 Version 11 – Pre-Stable Integration
- Merged data layer into single `ChatStore.kt`.
- Added atomic JSON writes and backup logic.
- Added message delivery `SyncStatus` enum (Pending / Sent / Failed).
- Implemented snackbar for “Chat Cleared” events.
- Introduced retry button for failed sends.

---

## 🧱 Version 10 – Persistent Chat Prototype
- Introduced offline chat storage via `ChatStore`.
- Added timestamps and simulated network failure logic.
- Early retry-on-failure experiment (manual trigger).
- UI polish: rounded message bubbles, elevation, colors.

---

## 🧱 Version 9 – Core Retry and Status Flow
- Added `SyncStatus` tracking for each message.
- Snackbar feedback integrated for user events.
- Chat messages show “sending…” / “failed” states.
- Improved coroutine safety for background tasks.

---

## 🧱 Version 8 – Offline Send Simulation
- Introduced random network-failure simulation.
- Added coroutine-based delay for pseudo-response.
- Persisted sent/failed status via DataStore.

---

## 🧱 Version 7 – Performance & Stability Baseline
- Build time optimizations (`org.gradle.configuration-cache=true`).
- Compiler memory tuning and parallelization.
- All previous UI and logic tested stable.

---

## 🧱 Version 6 – Structural Cleanup
- Folder restructuring for clarity (`data/`, `ui/`, `theme/`).
- Removed legacy unused imports and duplicate files.
- Added full import ordering standardization.

---

## 🧱 Version 5 – UI Core Established
- ChatScreen established with persistent layout.
- Bubble UI polish + rounded corners.
- AppScaffold navigation created with bottom bar.

---

## 🧱 Version 4 – Foundation
- Introduced modular navigation structure.
- Added placeholder `SettingsScreen`.
- Material 3 baseline styling introduced.

---

## 🧱 Version 3 – Early Compose Build
- Functional message input and send button.
- Simple echo response system.
- Introduced timestamp prototype.

---

## 🧱 Version 2 – Early Android Scaffold
- Integrated basic navigation host.
- Initial file structure setup (`MainActivity`, `AppScaffold`).

---

## 🧱 Version 1 – Prototype Shell
- “Hello, I’m Leo!” demo screen.
- Minimal Compose layout with static text.

---

### 🚀 Next Targets
- **V13:** Swipe color polish, Settings refinement, new nav behavior.
- **V14:** Mood system + voice engine integration.
- **V15:** Cloud sync / AI personality logic.

---

> _“Leo’s not just learning how to talk — he’s learning how to listen.”_