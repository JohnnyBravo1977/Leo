param(
  [Parameter(Mandatory=$true)][string]$ZipPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path $ZipPath)) { throw "Zip not found: $ZipPath" }
$TMP = New-Item -ItemType Directory -Force -Path (Join-Path $env:TEMP ("leoverify_" + [guid]::NewGuid().ToString())) | % FullName

Expand-Archive -Path $ZipPath -DestinationPath $TMP

$meta = Join-Path $TMP '_meta'
$targets = Join-Path $TMP '_targets'
if (-not (Test-Path $meta) -or -not (Test-Path $targets)) { throw "Zip missing _meta or _targets" }

$chkFile = Join-Path $meta 'checksums.sha256'
$invFile = Join-Path $meta 'inventory.txt'
$tree    = Join-Path $meta 'tree.txt'
$facts   = Join-Path $meta 'facts.txt'
$missing = @()

foreach ($line in Get-Content $invFile) {
  $parts = $line -split "`t",2
  $rel = $parts[0]
  $full = Join-Path $targets $rel
  if (-not (Test-Path $full)) { $missing += $rel }
}

if ($missing.Count -gt 0) {
  Write-Error "Missing files:`n$($missing -join "`n")"
  exit 1
}

# checksum verify
$bad = @()
foreach ($line in Get-Content $chkFile) {
  if ($line.Trim() -eq '') { continue }
  $sha = $line.Substring(0,64)
  $rel = $line.Substring(66)
  $full = Join-Path $targets $rel
  $calc = (Get-FileHash -Algorithm SHA256 -Path $full).Hash.ToLower()
  if ($sha -ne $calc) { $bad += $rel }
}

if ($bad.Count -gt 0) {
  Write-Error "Checksum mismatches:`n$($bad -join "`n")"
  exit 1
}

Write-Host "Bundle OK:"
Write-Host "  $(Get-Content $facts | Out-String)"
