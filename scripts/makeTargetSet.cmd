@echo off
cd /d "%~dp0..\"
powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\makeTargetSet.ps1"
pause