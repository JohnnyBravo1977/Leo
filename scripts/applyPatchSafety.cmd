@echo off
cd /d "%~dp0..\"
powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\applyPatchSafety.ps1"
pause
