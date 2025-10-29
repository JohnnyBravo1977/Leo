# makesync.ps1  (ASCII-safe, no backticks)
# Create a compact SyncPack Lite (source + _meta) into /syncpacks

Set-Location "$PSScriptRoot\.."  # go to project root

# Info
$branch = (git rev-parse --abbrev-ref HEAD 2>$null)
if (-not $branch) { $branch = "unknown" }
$timestamp = Get-Date -Format "yyyyMMdd_HHmm"
$packName = "LEO_SYNC_LITE_${timestamp}_${branch}.tar.gz"

# Output folder
$syncDir = Join-Path (Get-Location) "syncpacks"
if (-not (Test-Path $syncDir)) { New-Item -ItemType Directory -Path $syncDir | Out-Null }
$packPath = Join-Path $syncDir $packName

# Sanity checks
if (-not (Test-Path ".\app\src")) { throw "Run from project root: app/src not found" }
if (-not (Test-Path ".\gradlew")) { throw "gradlew not found (wrong directory?)" }

# Build includelist (only add what exists)
$paths = @(
  "app/src",
  "app/build.gradle.kts",
  "app/build.gradle",
  "build.gradle.kts",
  "build.gradle",
  "settings.gradle.kts",
  "settings.gradle",
  "gradle",
  "gradlew",
  "gradlew.bat",
  "_meta",
  "WORKFLOW-README_20251026_2213.txt",
  "README.md",
  "LEO-CHARTER.md"
) | Where-Object { Test-Path $_ }

if ($paths.Count -eq 0) { throw "Nothing to pack (include list empty)" }

# Run tar (no line continuations, ASCII only)
& tar -czf $packPath $paths

Write-Host ""
Write-Host "OK  SyncPack Lite created:"
Write-Host "    $packPath"
Write-Host "Verify:  tar -tvf `"$packPath`" | more"