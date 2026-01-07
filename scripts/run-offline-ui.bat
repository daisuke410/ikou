@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Playwright Offline Execution Launcher - UI Mode
REM ============================================================

setlocal

REM Get current directory
set SCRIPT_DIR=%~dp0
set ROOT_DIR=%SCRIPT_DIR%..

REM Portable Node.js path
set NODE_PORTABLE=%ROOT_DIR%\nodejs\node.exe

REM Playwright browsers path
set PLAYWRIGHT_BROWSERS_PATH=%ROOT_DIR%\.playwright

echo ============================================================
echo Playwright Test Runner - UI Mode (Offline)
echo ============================================================
echo.

REM カレントディレクトリを変更
cd /d "%ROOT_DIR%"

REM Playwrightテストを実行（UIモード）
"%NODE_PORTABLE%" "%ROOT_DIR%\node_modules\@playwright\test\cli.js" test --ui

pause
