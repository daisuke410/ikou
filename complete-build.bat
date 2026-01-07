@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Complete the offline build (Step 6 only)
REM Use this if the main build script stopped at step 5
REM ============================================================

echo ============================================================
echo Completing Offline Package Build - Step 6
echo ============================================================
echo.

set OFFLINE_DIR=%CD%\dist\offline-complete

REM Check if offline directory exists
if not exist "%OFFLINE_DIR%" (
    echo [ERROR] Offline directory not found: %OFFLINE_DIR%
    pause
    exit /b 1
)

echo Copying test cases and configuration files...
echo.

REM Configuration files
echo Copying configuration files...
copy /Y playwright.config.offline.ts "%OFFLINE_DIR%\playwright.config.ts" >nul 2>&1
copy /Y package.json "%OFFLINE_DIR%\" >nul 2>&1
copy /Y package-lock.json "%OFFLINE_DIR%\" >nul 2>&1

REM Test cases
echo Copying test cases...
if exist tests (
    xcopy /E /I /Y /Q tests "%OFFLINE_DIR%\tests" >nul 2>&1
)

REM Static files
if exist src\main\resources\static (
    xcopy /E /I /Y /Q src\main\resources\static "%OFFLINE_DIR%\static" >nul 2>&1
)

REM Execution scripts
echo Creating execution scripts...
if not exist "%OFFLINE_DIR%\scripts" mkdir "%OFFLINE_DIR%\scripts"
copy /Y scripts\run-offline.bat "%OFFLINE_DIR%\scripts\" >nul 2>&1
copy /Y scripts\run-offline-ui.bat "%OFFLINE_DIR%\scripts\" >nul 2>&1
copy /Y scripts\show-report-offline.bat "%OFFLINE_DIR%\scripts\" >nul 2>&1
copy /Y scripts\OFFLINE-README.txt "%OFFLINE_DIR%\" >nul 2>&1

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
echo Creating README...
(
echo # Playwright Offline Execution Package
echo.
echo ## Quick Start
echo.
echo 1. Run all tests:
echo    run-tests.bat
echo.
echo 2. Run with UI mode:
echo    run-tests-ui.bat
echo.
echo 3. Show test report:
echo    show-report.bat
echo.
echo ## Requirements
echo.
echo - Windows 10/11 ^(64-bit^)
echo - No internet connection required
echo - No Node.js installation required
echo.
echo ## Package Contents
echo.
echo - nodejs/           : Portable Node.js v18.20.5
echo - node_modules/     : All dependencies
echo - .playwright/      : Browser binaries
echo - tests/            : Test cases
echo - scripts/          : Execution scripts
echo.
echo For detailed instructions, see OFFLINE-COMPLETE-GUIDE.md
) > "%OFFLINE_DIR%\README.md"

echo.
echo ============================================================
echo [SUCCESS] Build completed!
echo ============================================================
echo.
echo Package location: %OFFLINE_DIR%
echo.
echo Package contents:
dir /B "%OFFLINE_DIR%"
echo.
echo Next steps:
echo   1. Run create-offline-zip.bat to create ZIP file
echo   2. Or copy the dist\offline-complete folder directly
echo.
pause
