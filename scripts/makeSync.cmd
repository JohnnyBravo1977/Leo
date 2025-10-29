@echo off
cd /d "%~dp0"
cd ..
powershell -ExecutionPolicy Bypass -NoProfile -File ".\scripts\makesync.ps1"
pause