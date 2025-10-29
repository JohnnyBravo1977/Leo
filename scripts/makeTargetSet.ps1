# scripts\makeTargetSet.ps1
# Build three deliverables into Desktop\SyncStage:
#  1) LEO_TARGETS_BUNDLE_<ts>.zip        -> _targets/ + _meta/  (legacy structure)
#  2) LEO_TARGETS_BUNDLE_FULL_<ts>.zip   -> app/... + _meta/... (flat, stream-friendly; preferred to send)
#  3) LEO_SYNC_LITE_<ts>.zip             -> repo snapshot (excludes .git/.gradle/build/.idea/etc.)

$ErrorActionPreference = 'Stop'

# --- Paths ---
$RepoRoot  = (Get-Location).Path
$StageRoot = Join-Path $env:USERPROFILE 'Desktop\SyncStage'   # fixed location
$Targets   = Join-Path $StageRoot '_targets'
$Meta      = Join-Path $StageRoot '_meta'
$LiteStg   = Join-Path $StageRoot '_sync_lite'
$ListFile  = Join-Path $RepoRoot 'scripts\TargetFiles.txt'

# Ensure stage root
if (!(Test-Path $StageRoot)) { New-Item -ItemType Directory -Path $StageRoot | Out-Null }

# Clean staging folders
foreach ($p in @($Targets,$Meta,$LiteStg)) {
  if (Test-Path $p) { Remove-Item $p -Recurse -Force }
  New-Item -ItemType Directory -Path $p | Out-Null
}

# --- Read target list ---
if (!(Test-Path $ListFile)) { throw "Missing scripts\TargetFiles.txt" }
$wanted = Get-Content $ListFile | Where-Object { $_ -and $_.Trim() -ne '' -and -not $_.Trim().StartsWith('#') }

# --- Copy targets preserving paths under _targets ---
foreach ($rel in $wanted) {
  $src = Join-Path $RepoRoot $rel
  if (!(Test-Path $src)) { throw "Target not found: $rel" }
  $dst = Join-Path $Targets $rel
  $dstDir = Split-Path $dst -Parent
  if (!(Test-Path $dstDir)) { New-Item -ItemType Directory -Path $dstDir -Force | Out-Null }
  Copy-Item $src $dst -Force
}

# --- Verify copied targets look sane (no placeholders/empty files) ---
$errors = @()
foreach ($rel in $wanted) {
  $dst = Join-Path $Targets $rel
  if (!(Test-Path $dst)) { $errors += "Missing after copy: $rel"; continue }
  $len = (Get-Item $dst).Length
  $text = Get-Content $dst -Raw -ErrorAction SilentlyContinue
  if ($len -lt 80) { $errors += "Suspiciously small file ($len bytes): $rel" }
  if ($text -match '^\s*\.\.\.\s*$') { $errors += "Placeholder content ('...') in: $rel" }
}
if ($errors.Count -gt 0) {
  $errors | ForEach-Object { Write-Host "[TargetSet verify] $_" -ForegroundColor Red }
  throw "TargetSet verification failed. Fix scripts\\TargetFiles.txt and rerun."
}

# --- Write _meta ---
function W($name,$text) {
  $p = Join-Path $Meta $name
  $d = Split-Path $p -Parent
  if (!(Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
  ($text | Out-String) | Out-File -FilePath $p -Encoding UTF8
}

$ts      = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss K')
$ts_tag  = (Get-Date).ToString('yyyyMMdd_HHmmss')

# Quiet git (avoid CRLF noise/pagers)
$git = 'git -c core.autocrlf=false -c core.safecrlf=false -c pager.diff=false -c pager.status=false'
function G($args) { try { (Invoke-Expression "$git $args 2>`$null") | Out-String } catch { "" } }

$revshort   = G 'rev-parse --short=12 HEAD'
$revfull    = G 'rev-parse HEAD'
$branch     = G 'rev-parse --abbrev-ref HEAD'
$nearestTag = G 'describe --tags --abbrev=0'
$status     = G 'status --porcelain=v1'
$diffstat   = G 'diff --stat'

W 'timestamp.txt'   $ts
W 'branch.txt'      ($branch.Trim())
W 'nearest_tag.txt' ($nearestTag.Trim())
W 'commit.txt'      ($revfull.Trim())
W 'git.txt'         $status
W 'diffstat.txt'    $diffstat
W 'inventory.txt'   ($wanted -join "`r`n")

# --- Tree manifests (full map without shipping whole repo) ---
function Write-Tree($paths, $name) {
  ($paths | Sort-Object) -join "`r`n" | Out-File -FilePath (Join-Path $Meta $name) -Encoding UTF8
}
$all = Get-ChildItem -LiteralPath $RepoRoot -Recurse -File |
       ForEach-Object { $_.FullName -replace [regex]::Escape($RepoRoot + '\'), '' }
$src = $all | Where-Object {
  $_ -match '(^app\\src\\main\\java\\.*\.kt$)|(^app\\src\\main\\AndroidManifest\.xml$)|(^app\\src\\main\\res\\.*\.xml$)'
}
$res = $all | Where-Object { $_ -match '^app\\src\\main\\res\\' }
Write-Tree $all 'TREE_all.txt'
Write-Tree $src 'TREE_src.txt'
Write-Tree $res 'TREE_res.txt'

# --- Per-file SHA256 for _targets (verifies no truncation/encoding issues) ---
$shaLines = @()
$targetFiles = Get-ChildItem -LiteralPath $Targets -Recurse -File
foreach ($f in $targetFiles) {
  $rel = $f.FullName -replace [regex]::Escape($Targets + '\'), ''
  $sha = (Get-FileHash -Algorithm SHA256 -LiteralPath $f.FullName).Hash
  $shaLines += "{0}  {1}" -f $sha, $rel
}
$shaLines | Out-File -FilePath (Join-Path $Meta 'SHA256_TARGETS.txt') -Encoding UTF8

# --- Make TARGETS bundle (legacy: _targets + _meta) ---
$targetsZip = Join-Path $StageRoot ("LEO_TARGETS_BUNDLE_{0}.zip" -f $ts_tag)
if (Test-Path $targetsZip) { Remove-Item $targetsZip -Force }
Compress-Archive -CompressionLevel Optimal -Path @($Targets,$Meta) -DestinationPath $targetsZip -Force

# --- Make TARGETS FULL bundle (flat entries: app/... and _meta/...) ---
$targetsFullZip = Join-Path $StageRoot ("LEO_TARGETS_BUNDLE_FULL_{0}.zip" -f $ts_tag)
if (Test-Path $targetsFullZip) { Remove-Item $targetsFullZip -Force }
Compress-Archive -CompressionLevel Optimal -Path @("$Targets\*", "$Meta\*") -DestinationPath $targetsFullZip -Force

# --- SHA256 for zips + size guard (25 MB email cap) ---
function SizeMB($path) { [Math]::Round((Get-Item $path).Length / 1MB, 2) }
function HashZip($path) { (Get-FileHash -Algorithm SHA256 -LiteralPath $path).Hash }

$shaTargets     = HashZip $targetsZip
$shaTargetsFull = HashZip $targetsFullZip

W 'SHA256_ZIPS.txt' @"
$shaTargets  $(Split-Path -Leaf $targetsZip)
$shaTargetsFull  $(Split-Path -Leaf $targetsFullZip)
"@

$capMB = 25
$sz1 = SizeMB $targetsZip
$sz2 = SizeMB $targetsFullZip

if ($sz1 -gt $capMB -or $sz2 -gt $capMB) {
  Write-Host ""
  Write-Host "⚠ Bundle exceeds ${capMB} MB email cap:" -ForegroundColor Yellow
  if ($sz1 -gt $capMB) { Write-Host ("  - {0} : {1} MB" -f (Split-Path -Leaf $targetsZip), $sz1) -ForegroundColor Yellow }
  if ($sz2 -gt $capMB) { Write-Host ("  - {0} : {1} MB" -f (Split-Path -Leaf $targetsFullZip), $sz2) -ForegroundColor Yellow }
  Write-Host "Tip: reduce entries in scripts\\TargetFiles.txt or split across two runs." -ForegroundColor Yellow
}

# --- Build SyncPack-Lite (mirror repo minus heavy dirs) ---
$excludeDirs  = @('.git','.gradle','build','.idea','.kotlin','out','.dart_tool','.vscode','node_modules','.fleet','.gitlab')
$excludeFiles = @('*.iml','*.class','*.log','*.bak')

$xd = @(); foreach ($d in $excludeDirs)  { $xd += @('/XD', (Join-Path $RepoRoot $d)) }
$xf = @(); foreach ($f in $excludeFiles) { $xf += @('/XF', $f) }

$null = robocopy $RepoRoot $LiteStg /MIR /NFL /NDL /NJH /NJS /NP /R:1 /W:1 @xd @xf

# --- Make SYNC_LITE zip ---
$syncZip = Join-Path $StageRoot ("LEO_SYNC_LITE_{0}.zip" -f $ts_tag)
if (Test-Path $syncZip) { Remove-Item $syncZip -Force }
Compress-Archive -CompressionLevel Optimal -Path $LiteStg -DestinationPath $syncZip -Force

Write-Host ""
Write-Host "✅ DONE" -ForegroundColor Green
Write-Host ("Targets bundle       : {0}  ({1} MB)" -f $targetsZip, (SizeMB $targetsZip))       -ForegroundColor Green
Write-Host ("Targets FULL bundle  : {0}  ({1} MB)" -f $targetsFullZip, (SizeMB $targetsFullZip)) -ForegroundColor Green
Write-Host ("SyncPack-Lite        : {0}"             -f $syncZip)                                 -ForegroundColor Green
Write-Host ("SHA256 targets zip   : {0}"             -f $shaTargets)                              -ForegroundColor DarkGreen
Write-Host ("SHA256 targets FULL  : {0}"             -f $shaTargetsFull)                         -ForegroundColor DarkGreen