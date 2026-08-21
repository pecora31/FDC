@echo off
title Tactical Tablet UI Live Preview & Auto-Watcher
echo ===============================================================================
echo   TACTICAL TABLET UI LIVE RENDERER ^& AUTO-RELOAD WATCHER (60 FPS)
echo   [!] Tự động xuất PNG ra UI/preview.png và cập nhật cửa sổ mỗi khi bạn bấm Lưu
echo ===============================================================================
cd /d "%~dp0\.."
java "UI/LiveRenderer.java"
pause
