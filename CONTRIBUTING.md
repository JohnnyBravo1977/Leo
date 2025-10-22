# Contributing (V13+)
We accept changes only as patch files.

**How to apply a patch**
1. Download the `.patch`.
2. Android Studio → **VCS → Apply Patch…** → select file → **Apply**.
3. Build → **Make Project** → **Run**.
4. Commit with the provided message; push to `V13-dev`.

**Rules**
- No force-pushes to `master`.
- No edits to Gradle roots unless the patch header says so.
- Navigation structure: screen owns structure (dividers, spacing), tiles stay dumb.
