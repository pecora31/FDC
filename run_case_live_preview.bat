@echo off
title Tactical Tablet Case Real-Time Live Preview (case.png)
echo ===============================================================================
echo   TACTICAL TABLET CASE.PNG REAL-TIME LIVE PREVIEW & AUTO-WATCHER
echo   [!] Tu dong cap nhat cua so Desktop va xuat file case.png moi khi luu code
echo   [!] Bam phim [R] hoac [F5] tren cua so de ep tai lai ngay lap tuc
echo ===============================================================================
cd /d "%~dp0\ArtilleryTacticalTablet"
.\gradlew.bat caseLive --continuous
pause
