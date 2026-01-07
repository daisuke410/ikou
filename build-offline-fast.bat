@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Fast Offline Package Build Script (with robocopy)
REM ============================================================

echo ============================================================
echo Playwright Complete Offline Package Build (Fast Version)
echo ============================================================
echo.

REM Variables
set OFFLINE_DIR=%CD%\dist\offline-complete
set NODE_VERSION=18.20.5
set NODE_ARCH=win-x64
set NODE_PACKAGE=node-v%NODE_VERSION%-%NODE_ARCH%

REM ============================================================
REM Step 1: Download Portable Node.js
REM ============================================================
echo [1/6] Downloading portable Node.js...
echo.

if not exist "dist\nodejs\%NODE_PACKAGE%\node.exe" (
    call download-nodejs.bat
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Failed to download Node.js
        pause
        exit /b 1
    )
) else (
    echo [OK] Portable Node.js already exists
)
echo.

REM ============================================================
REM Step 2: Prepare Directories
REM ============================================================
echo [2/6] Preparing directories...
if not exist dist mkdir dist
if exist "%OFFLINE_DIR%" (
    echo    Removing existing directory...
    rmdir /S /Q "%OFFLINE_DIR%"
)
mkdir "%OFFLINE_DIR%"
echo    [OK] Directory prepared
echo.

REM ============================================================
REM Step 3: Copy Portable Node.js
REM ============================================================
echo [3/6] Copying portable Node.js...
robocopy "dist\nodejs\%NODE_PACKAGE%" "%OFFLINE_DIR%\nodejs" /E /NFL /NDL /NJH /NJS /nc /ns /np
if %ERRORLEVEL% GEQ 8 (
    echo [ERROR] Failed to copy Node.js
    pause
    exit /b 1
)
echo [OK] Node.js copied
echo.

REM ============================================================
REM Step 4: Copy node_modules
REM ============================================================
echo [4/6] Copying node_modules...
echo    This may take several minutes...
if not exist node_modules (
    echo [ERROR] node_modules not found
    echo    Please run 'npm install' first
    pause
    exit /b 1
)
robocopy node_modules "%OFFLINE_DIR%\node_modules" /E /NFL /NDL /NJH /NJS /nc /ns /np
if %ERRORLEVEL% GEQ 8 (
    echo [ERROR] Failed to copy node_modules
    pause
    exit /b 1
)
echo [OK] node_modules copied
echo.

REM ============================================================
REM Step 5: Copy Playwright Browsers
REM ============================================================
echo [5/6] Copying Playwright browsers...
echo    This may take several minutes...
echo.

set PLAYWRIGHT_BROWSERS_PATH=%USERPROFILE%\AppData\Local\ms-playwright
if exist "%PLAYWRIGHT_BROWSERS_PATH%" (
    echo Starting browser copy with progress...
    robocopy "%PLAYWRIGHT_BROWSERS_PATH%" "%OFFLINE_DIR%\.playwright" /E /NP
    if %ERRORLEVEL% GEQ 8 (
        echo [ERROR] Failed to copy browsers
        pause
        exit /b 1
    )
    echo.
    echo [OK] Browsers copied
) else (
    echo [WARNING] Playwright browsers not found
    echo Please run 'npx playwright install' first
    echo.
    echo Continue anyway? (Y/N)
    choice /C YN /N
    if errorlevel 2 exit /b 1
)
echo.

REM ============================================================
REM Step 6: Copy Test Cases and Configuration Files
REM ============================================================
echo [6/6] Copying test cases and configuration files...

REM Configuration files
copy /Y playwright.config.ts "%OFFLINE_DIR%\" >nul
copy /Y package.json "%OFFLINE_DIR%\" >nul
copy /Y package-lock.json "%OFFLINE_DIR%\" 2>nul

REM Test cases
if exist tests robocopy tests "%OFFLINE_DIR%\tests" /E /NFL /NDL /NJH /NJS >nul

REM Static files
if exist src\main\resources\static robocopy src\main\resources\static "%OFFLINE_DIR%\static" /E /NFL /NDL /NJH /NJS >nul

REM Execution scripts
if not exist "%OFFLINE_DIR%\scripts" mkdir "%OFFLINE_DIR%\scripts"
copy /Y scripts\run-offline.bat "%OFFLINE_DIR%\scripts\" >nul
copy /Y scripts\run-offline-ui.bat "%OFFLINE_DIR%\scripts\" >nul
copy /Y scripts\show-report-offline.bat "%OFFLINE_DIR%\scripts\" >nul

REM Root execution scripts
(
echo @echo off
echo REM Playwright Test - Offline
echo call scripts\run-offline.bat %%*
) > "%OFFLINE_DIR%\run-tests.bat"

(
echo @echo off
echo REM Playwright Test - UI Mode
echo call scripts\run-offline-ui.bat
) > "%OFFLINE_DIR%\run-tests-ui.bat"

(
echo @echo off
echo REM Playwright Report Viewer
echo call scripts\show-report-offline.bat
) > "%OFFLINE_DIR%\show-report.bat"

REM README
(
echo # Playwright Offline Execution Package
echo.
echo This package contains everything needed to run Playwright tests offline.
echo.
echo ## Quick Start
echo.
echo 1. Run tests: run-tests.bat
echo 2. UI mode: run-tests-ui.bat
echo 3. Show report: show-report.bat
echo.
echo See OFFLINE-COMPLETE-GUIDE.md for detailed instructions.
) > "%OFFLINE_DIR%\README.md"

echo [OK] Files copied
echo.

REM ============================================================
REM Completion Message
REM ============================================================
echo ============================================================
echo [SUCCESS] Complete offline package created successfully!
echo ============================================================
echo.
echo Package location: %OFFLINE_DIR%
echo.
echo Package contents:
dir /B "%OFFLINE_DIR%"
echo.
echo Next steps:
echo   1. Copy this directory to offline environment
echo   2. Run run-tests.bat in offline environment
echo.
echo Or, to create a ZIP file:
echo   Run create-offline-zip.bat
echo.
pause
