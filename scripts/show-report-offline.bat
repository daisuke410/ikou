@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Playwright Report Viewer - Offline Execution
REM ============================================================

setlocal

REM Get current directory
set SCRIPT_DIR=%~dp0
set ROOT_DIR=%SCRIPT_DIR%..

REM Portable Node.js path
set NODE_PORTABLE=%ROOT_DIR%\nodejs\node.exe

echo ============================================================
echo Playwright Test Report Viewer (Offline)
echo ============================================================
echo.

REM カレントディレクトリを変更
cd /d "%ROOT_DIR%"

REM Playwrightレポートを表示
"%NODE_PORTABLE%" "%ROOT_DIR%\node_modules\@playwright\test\cli.js" show-report

pause
