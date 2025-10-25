#!/usr/bin/env bash
set -euo pipefail

# repo root (run from anywhere)
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
APP="$ROOT/app/src/main"
VALUES="$APP/res/values"
VALUES31="$APP/res/values-v31"
DRAWABLE="$APP/res/drawable"

backup_once () {
  local f="$1"
  [[ -f "$f" ]] || return 0
  if [[ ! -f "${f}.bak" ]]; then
    cp -p "$f" "${f}.bak"
    echo "  • backup -> ${f}.bak"
  fi
}

echo "== Splash doctor (no patches, idempotent) =="
echo "[1/4] Create drawable splash background…"
mkdir -p "$DRAWABLE"
backup_once "$DRAWABLE/splash_blank.xml"
cat > "$DRAWABLE/splash_blank.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <!-- Full-bleed gradient that matches the app background -->
    <gradient
        android:type="linear"
        android:angle="270"
        android:startColor="#7FD2FF"   <!-- top: aqua/blue -->
        android:endColor="#8BEF2C"     <!-- bottom: mint/green -->
        android:centerColor="#80BFE7"
        android:useLevel="false" />
    <corners android:radius="0dp"/>
</shape>
XML
echo "  • wrote $DRAWABLE/splash_blank.xml"

echo "[2/4] Ensure values/styles.xml (base theme)…"
mkdir -p "$VALUES"
backup_once "$VALUES/styles.xml"
cat > "$VALUES/styles.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App theme: base for all API levels -->
    <style name="Theme.LittleGenius" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Keep action bar/content out of the way; Compose draws its own -->
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <!-- This drives the full-bleed gradient while Compose starts -->
        <item name="android:windowBackground">@drawable/splash_blank</item>
    </style>
</resources>
XML
echo "  • wrote $VALUES/styles.xml"

echo "[3/4] Ensure values-v31/styles.xml (Android 12+ splash attrs only)…"
mkdir -p "$VALUES31"
backup_once "$VALUES31/styles.xml"
cat > "$VALUES31/styles.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Same theme name; adds Android 12 splash attributes -->
    <style name="Theme.LittleGenius" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Use full artwork as splash icon (optional). Leave transparent if you only want the background. -->
        <item name="android:windowSplashScreenAnimatedIcon">@android:color/transparent</item>
        <!-- Prevent any white flash -->
        <item name="android:windowSplashScreenBackground">@android:color/transparent</item>
        <!-- We also keep status/nav bars transparent -->
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
    </style>
</resources>
XML
echo "  • wrote $VALUES31/styles.xml"

echo "[4/4] Verify AndroidManifest uses Theme.LittleGenius…"
MANIFEST="$APP/AndroidManifest.xml"
backup_once "$MANIFEST"
if grep -q 'android:theme=' "$MANIFEST"; then
  # replace theme on the launcher activity line only
  awk '
    /<activity/ && seen==0 {
      gsub(/android:theme="[^"]*"/, "android:theme=\"@style/Theme.LittleGenius\"")
      seen=1
    } { print }
  ' "$MANIFEST" > "$MANIFEST.__tmp__" && mv "$MANIFEST.__tmp__" "$MANIFEST"
else
  # add theme attribute to first activity if missing
  awk '
    !added && /<activity/ {
      sub(/>/, " android:theme=\"@style/Theme.LittleGenius\">")
      added=1
    } { print }
  ' "$MANIFEST" > "$MANIFEST.__tmp__" && mv "$MANIFEST.__tmp__" "$MANIFEST"
fi
echo "  • manifest ensured to reference @style/Theme.LittleGenius"

echo "Done. Backups were created next to any changed file (*.bak). Build now."