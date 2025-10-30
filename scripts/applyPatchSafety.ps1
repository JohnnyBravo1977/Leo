param(
  [Parameter(Mandatory=$true)][string]$PatchPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ROOT = (Resolve-Path "$PSScriptRoot/..").Path
Push-Location $ROOT

# Dry-run check
git apply --check --whitespace=nowarn $PatchPath

# Apply with 3-way as fallback
try {
  git am --abort 2>$null | Out-Null
} catch {}

git am --keep-cr --signoff <$PatchPath

# Build smoke
& ./gradlew assembleDebug -q
if ($LASTEXITCODE -ne 0) {
  Write-Error "Build failed. Auto-reverting patch."
  git am --abort 2>$null | Out-Null
  git reset --hard HEAD~1
  exit 1
}

Write-Host "Patch applied and build passed."
Pop-Location
