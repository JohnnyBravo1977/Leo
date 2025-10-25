# 🟢 Leo (Android)

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

## 🧰 Developer Quickstart

1. Open in Android Studio (Electric Eel+)
2. Apply patch files via **VCS → Apply Patch…**
3. Build:
   ```bash
   ./gradlew :app:assembleDebug

4. Dark Mode persists via ThemeRepository (SharedPreferences).




---

🧪 Continuous Integration

Every push/PR to V13-dev runs Android CI (assembleDebug).

The badge above shows current status.

PRs receive a sticky PASS/FAIL comment with a link to the run.



---

🧭 Workflow

Patch-only on V13-dev; CI must be green before merging to main.


---

© 2025 JohnnyBravo1977 — built with brains, caffeine, and stubbornness.