<#
.SYNOPSIS
Create a zip containing only the files listed in TargetFiles.txt (with full paths).
Use this when preparing a “Target-Set” for patching.
#>

Param(
    [string]$OutName = ("TARGETSET_" + (Get-Date -Format "yyyyMMdd_HHmm") + ".zip")
)

$root = (Resolve-Path "..").Path
$manifest = Join-Path $PSScriptRoot "TargetFiles.txt"
$outFile = Join-Path $PSScriptRoot $OutName

if (!(Test-Path $manifest)) {
    Write-Host "No TargetFiles.txt found. Create it in /scripts/ listing one relative path per line."
    exit 1
}

$files = Get-Content $manifest | Where-Object { $_ -and ($_ -notmatch '^\s*#') }
if ($files.Count -eq 0) {
    Write-Host "TargetFiles.txt is empty."
    exit 1
}

Write-Host "Creating $OutName …"
if (Test-Path $outFile) { Remove-Item $outFile -Force }

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($outFile, 'Create')

foreach ($f in $files) {
    $path = Join-Path $root $f
    if (Test-Path $path) {
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $path, $f)
        Write-Host "  added $f"
    } else {
        Write-Host "  MISSING $f"
    }
}

$zip.Dispose()
Write-Host "`nDone: $outFile"