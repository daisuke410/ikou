@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Playwright Offline Execution Launcher
REM ============================================================

setlocal

REM カレントディレクトリを取得
set SCRIPT_DIR=%~dp0
set ROOT_DIR=%SCRIPT_DIR%..

REM Node.jsポータブル版のパス
set NODE_PORTABLE=%ROOT_DIR%\nodejs\node.exe
set NPM_PORTABLE=%ROOT_DIR%\nodejs\npm.cmd

REM Playwrightブラウザのパス
set PLAYWRIGHT_BROWSERS_PATH=%ROOT_DIR%\.playwright

REM Check Node.js exists
if not exist "%NODE_PORTABLE%" (
    echo ============================================================
    echo [ERROR] Portable Node.js not found
    echo ============================================================
    echo.
    echo Expected path: %NODE_PORTABLE%
    echo.
    echo Please verify the package was extracted correctly.
    echo.
    pause
    exit /b 1
)

REM Check node_modules exists
if not exist "%ROOT_DIR%\node_modules" (
    echo ============================================================
    echo [ERROR] node_modules not found
    echo ============================================================
    echo.
    echo Expected path: %ROOT_DIR%\node_modules
    echo.
    echo Please verify the package was extracted correctly.
    echo.
    pause
    exit /b 1
)

REM Check Playwright browsers exist
if not exist "%PLAYWRIGHT_BROWSERS_PATH%" (
    echo ============================================================
    echo [WARNING] Playwright browsers not found
    echo ============================================================
    echo.
    echo Expected path: %PLAYWRIGHT_BROWSERS_PATH%
    echo.
    echo Tests may fail.
    echo.
)

echo ============================================================
echo Playwright Test Runner - Offline Execution
echo ============================================================
echo.
echo Node.js version:
"%NODE_PORTABLE%" --version
echo.
echo Working directory: %ROOT_DIR%
echo Browsers path: %PLAYWRIGHT_BROWSERS_PATH%
echo.
echo ============================================================
echo.

REM カレントディレクトリを変更
cd /d "%ROOT_DIR%"

REM Playwrightテストを実行
"%NODE_PORTABLE%" "%ROOT_DIR%\node_modules\@playwright\test\cli.js" test %*

set EXIT_CODE=%ERRORLEVEL%

echo.
echo ============================================================
if %EXIT_CODE% EQU 0 (
    echo [SUCCESS] Tests completed successfully
) else (
    echo [INFO] Tests finished ^(exit code: %EXIT_CODE%^)
)
echo ============================================================
echo.

pause
exit /b %EXIT_CODE%
