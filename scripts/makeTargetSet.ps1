# scripts\makeTargetSet.ps1
# Build three deliverables into Desktop\SyncStage:
#  1) LEO_TARGETS_BUNDLE_<ts>.zip        -> _targets/ + _meta/
#  2) LEO_TARGETS_BUNDLE_FULL_<ts>.zip   -> flat: app/... + _meta/...
#  3) LEO_SYNC_LITE_<ts>.zip             -> trimmed repo snapshot (no .git/.gradle/build/.idea/etc.)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# --- Paths & names ---
$RepoRoot  = (Get-Location).Path
$StageRoot = Join-Path $env:USERPROFILE 'Desktop\SyncStage'
$Targets   = Join-Path $StageRoot '_targets'
$Meta      = Join-Path $StageRoot '_meta'
$LiteStg   = Join-Path $StageRoot '_sync_lite'
$ListFile  = Join-Path $RepoRoot 'scripts\TargetFiles.txt'

if (-not (Test-Path $StageRoot)) { New-Item -ItemType Directory -Path $StageRoot | Out-Null }

foreach ($p in @($Targets,$Meta,$LiteStg)) {
  if (Test-Path $p) { Remove-Item $p -Recurse -Force }
  New-Item -ItemType Directory -Path $p | Out-Null
}

# --- Read target list ---
if (-not (Test-Path $ListFile)) { throw "Missing scripts\TargetFiles.txt" }
$wanted = Get-Content $ListFile | Where-Object {
  $_ -and $_.Trim() -ne '' -and -not $_.Trim().StartsWith('#')
}

# --- Copy targets (preserve layout under _targets) ---
foreach ($rel in $wanted) {
  $src = Join-Path $RepoRoot $rel
  if (-not (Test-Path $src)) { throw ("Target not found: {0}" -f $rel) }
  $dst = Join-Path $Targets $rel
  $dstDir = Split-Path $dst -Parent
  if (-not (Test-Path $dstDir)) { New-Item -ItemType Directory -Path $dstDir -Force | Out-Null }
  Copy-Item $src $dst -Force
}

# --- Verify (no placeholders/truncation) ---
$errors = New-Object System.Collections.Generic.List[string]
foreach ($rel in $wanted) {
  $dst = Join-Path $Targets $rel
  if (-not (Test-Path $dst)) { $errors.Add(("Missing after copy: {0}" -f $rel)); continue }
  $fi  = Get-Item $dst
  $len = $fi.Length
  $text = try { Get-Content $dst -Raw -ErrorAction Stop } catch { "" }
  if ($len -lt 80) { $errors.Add(("Suspiciously small file ({0} bytes): {1}" -f $len, $rel)) }
  if ($text -match '^\s*\.\.\.\s*$') { $errors.Add(("Placeholder content ('...') in: {0}" -f $rel)) }
}
if ($errors.Count -gt 0) {
  $errors | ForEach-Object { Write-Host "[TargetSet verify] $_" -ForegroundColor Red }
  throw "TargetSet verification failed. Fix scripts\TargetFiles.txt and rerun."
}

# --- Meta helpers ---
function W($name,$text) {
  $p = Join-Path $Meta $name
  $d = Split-Path $p -Parent
  if (-not (Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
  ($text | Out-String) | Out-File -FilePath $p -Encoding UTF8
}
$tsDisp = (Get-Date).ToString('yyyy-MM-dd HH:mm:ss K')
$tsTag  = (Get-Date).ToString('yyyyMMdd_HHmmss')

# Quiet git (no CRLF/pager noise)
$git = 'git -c core.autocrlf=false -c core.safecrlf=false -c pager.diff=false -c pager.status=false'
function G($args) { try { (Invoke-Expression "$git $args 2>`$null") | Out-String } catch { "" } }

$revshort   = (G 'rev-parse --short=12 HEAD').Trim()
$revfull    = (G 'rev-parse HEAD').Trim()
$branch     = (G 'rev-parse --abbrev-ref HEAD').Trim()
$nearestTag = (G 'describe --tags --abbrev=0').Trim()
$status     = G 'status --porcelain=v1'
$diffstat   = G 'diff --stat'

W 'timestamp.txt'    $tsDisp
W 'branch.txt'       $branch
W 'nearest_tag.txt'  $nearestTag
W 'commit.txt'       $revfull
W 'git.txt'          $status
W 'diffstat.txt'     $diffstat
W 'inventory.txt'    ($wanted -join "`r`n")

# --- Trees (maps) ---
function Write-Tree($paths, $name) { ($paths | Sort-Object) -join "`r`n" | Out-File -FilePath (Join-Path $Meta $name) -Encoding UTF8 }
$all = Get-ChildItem -LiteralPath $RepoRoot -Recurse -File | ForEach-Object {
  $_.FullName -replace [regex]::Escape($RepoRoot + '\'), ''
}
$src = $all | Where-Object { $_ -match '(^app\\src\\main\\java\\.*\.kt$)|(^app\\src\\main\\AndroidManifest\.xml$)|(^app\\src\\main\\res\\.*\.xml$)' }
$res = $all | Where-Object { $_ -match '^app\\src\\main\\res\\' }
Write-Tree $all 'TREE_all.txt'
Write-Tree $src 'TREE_src.txt'
Write-Tree $res 'TREE_res.txt'

# --- SHA256 for every staged target file ---
$shaLines = New-Object System.Collections.Generic.List[string]
Get-ChildItem -LiteralPath $Targets -Recurse -File | ForEach-Object {
  $rel = $_.FullName -replace [regex]::Escape($Targets + '\'), ''
  $sha = (Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName).Hash
  $shaLines.Add(("{0}  {1}" -f $sha, $rel))
}
$shaLines | Out-File -FilePath (Join-Path $Meta 'SHA256_TARGETS.txt') -Encoding UTF8

# --- Zip: TARGETS bundle (legacy) ---
$targetsZip = Join-Path $StageRoot ("LEO_TARGETS_BUNDLE_{0}.zip" -f $tsTag)
if (Test-Path $targetsZip) { Remove-Item $targetsZip -Force }
Compress-Archive -CompressionLevel Optimal -Path @($Targets,$Meta) -DestinationPath $targetsZip -Force

# --- Zip: TARGETS FULL (flat entries) ---
$targetsFullZip = Join-Path $StageRoot ("LEO_TARGETS_BUNDLE_FULL_{0}.zip" -f $tsTag)
if (Test-Path $targetsFullZip) { Remove-Item $targetsFullZip -Force }
Compress-Archive -CompressionLevel Optimal -Path @("$Targets\*", "$Meta\*") -DestinationPath $targetsFullZip -Force

# --- SHA256 + size guard ---
function SizeMB($path) { [Math]::Round((Get-Item $path).Length / 1MB, 2) }
function HashZip($path) { (Get-FileHash -Algorithm SHA256 -LiteralPath $path).Hash }
$shaTargets     = HashZip $targetsZip
$shaTargetsFull = HashZip $targetsFullZip
W 'SHA256_ZIPS.txt' ("{0}  {1}`r`n{2}  {3}" -f $shaTargets, (Split-Path -Leaf $targetsZip), $shaTargetsFull, (Split-Path -Leaf $targetsFullZip))

$capMB = 25
$sz1 = SizeMB $targetsZip
$sz2 = SizeMB $targetsFullZip
if (($sz1 -gt $capMB) -or ($sz2 -gt $capMB)) {
  Write-Host ""
  Write-Host ("⚠ Bundle exceeds {0} MB email cap:" -f $capMB) -ForegroundColor Yellow
  if ($sz1 -gt $capMB) { Write-Host ("  - {0} : {1} MB" -f (Split-Path -Leaf $targetsZip), $sz1) -ForegroundColor Yellow }
  if ($sz2 -gt $capMB) { Write-Host ("  - {0} : {1} MB" -f (Split-Path -Leaf $targetsFullZip), $sz2) -ForegroundColor Yellow }
  Write-Host "Tip: reduce entries in scripts\TargetFiles.txt or split across two runs." -ForegroundColor Yellow
}

# --- Build SyncPack-Lite (mirror minus heavy dirs) ---
$excludeDirs  = @('.git','.gradle','build','.idea','.kotlin','out','.dart_tool','.vscode','node_modules','.fleet','.gitlab')
$excludeFiles = @('*.iml','*.class','*.log','*.bak')
$XD = @(); foreach ($d in $excludeDirs)  { $XD += @('/XD', (Join-Path $RepoRoot $d)) }
$XF = @(); foreach ($f in $excludeFiles) { $XF += @('/XF', $f) }
$null = robocopy $RepoRoot $LiteStg /MIR /NFL /NDL /NJH /NJS /NP /R:1 /W:1 @XD @XF

# --- Zip: SYNC_LITE ---
$syncZip = Join-Path $StageRoot ("LEO_SYNC_LITE_{0}.zip" -f $tsTag)
if (Test-Path $syncZip) { Remove-Item $syncZip -Force }
Compress-Archive -CompressionLevel Optimal -Path $LiteStg -DestinationPath $syncZip -Force

# --- Embed reset marker for next chat session (simple, harmless text file) ---
$resetNote = @"
# RESET PROCEDURE MARKER
timestamp: $tsDisp
branch: $branch
commit: $revshort
instruction: /reset_procedure
laser_focus: true
note: Hand this bundle to Chat and run /reset_procedure before any work.
"@
W 'RESET_PROCEDURE.txt' $resetNote

# --- Friendly summary ---
Write-Host ""
Write-Host "✅ DONE" -ForegroundColor Green
Write-Host ("Targets bundle       : {0}  ({1} MB)" -f $targetsZip, (SizeMB $targetsZip))             -ForegroundColor Green
Write-Host ("Targets FULL bundle  : {0}  ({1} MB)" -f $targetsFullZip, (SizeMB $targetsFullZip))   -ForegroundColor Green
Write-Host ("SyncPack-Lite        : {0}"             -f $syncZip)                                   -ForegroundColor Green
Write-Host ("SHA256 targets zip   : {0}"             -f $shaTargets)                                -ForegroundColor DarkGreen
Write-Host ("SHA256 targets FULL  : {0}"             -f $shaTargetsFull)                           -ForegroundColor DarkGreen
