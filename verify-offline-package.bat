@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Offline Package Verification Script
REM ============================================================

echo ============================================================
echo Playwright Offline Package Verification
echo ============================================================
echo.

set OFFLINE_DIR=%CD%\dist\offline-complete
set ZIP_DIR=%CD%\dist

echo Checking offline package...
echo.

REM ============================================================
REM Check 1: Offline Directory
REM ============================================================
echo [1/5] Checking offline directory...
if exist "%OFFLINE_DIR%" (
    echo [OK] Offline directory exists: %OFFLINE_DIR%
) else (
    echo [ERROR] Offline directory not found: %OFFLINE_DIR%
    goto :end
)
echo.

REM ============================================================
REM Check 2: Required Components
REM ============================================================
echo [2/5] Checking required components...

set MISSING=0

if exist "%OFFLINE_DIR%\nodejs\node.exe" (
    echo [OK] Node.js portable found
) else (
    echo [ERROR] Node.js portable not found
    set MISSING=1
)

if exist "%OFFLINE_DIR%\node_modules" (
    echo [OK] node_modules found
) else (
    echo [ERROR] node_modules not found
    set MISSING=1
)

if exist "%OFFLINE_DIR%\.playwright" (
    echo [OK] Playwright browsers found
) else (
    echo [WARNING] Playwright browsers not found
)

if exist "%OFFLINE_DIR%\tests" (
    echo [OK] Test cases found
) else (
    echo [WARNING] Test cases not found
)

if exist "%OFFLINE_DIR%\playwright.config.ts" (
    echo [OK] Playwright config found
) else (
    echo [ERROR] Playwright config not found
    set MISSING=1
)

if exist "%OFFLINE_DIR%\scripts\run-offline.bat" (
    echo [OK] Execution scripts found
) else (
    echo [ERROR] Execution scripts not found
    set MISSING=1
)

if %MISSING%==1 (
    echo.
    echo [ERROR] Some required components are missing!
    goto :end
)
echo.

REM ============================================================
REM Check 3: Directory Size
REM ============================================================
echo [3/5] Calculating directory size...
for /f "tokens=3" %%a in ('dir /s "%OFFLINE_DIR%" ^| find "bytes"') do set SIZE=%%a
echo Total size: %SIZE% bytes
echo Estimated: ~500MB - 1GB
echo.

REM ============================================================
REM Check 4: ZIP File
REM ============================================================
echo [4/5] Checking ZIP file...
set FOUND_ZIP=0
for %%f in ("%ZIP_DIR%\playwright-offline-complete-*.zip") do (
    echo [OK] ZIP file found: %%~nxf
    echo     Size: %%~zf bytes (%%~zf bytes / 1048576 MB approximately)
    set FOUND_ZIP=1
)

if %FOUND_ZIP%==0 (
    echo [WARNING] No ZIP file found
    echo           Run create-offline-zip.bat to create ZIP file
)
echo.

REM ============================================================
REM Check 5: Test Execution Scripts
REM ============================================================
echo [5/5] Checking execution scripts...
if exist "%OFFLINE_DIR%\run-tests.bat" (
    echo [OK] run-tests.bat exists
) else (
    echo [ERROR] run-tests.bat not found
)

if exist "%OFFLINE_DIR%\run-tests-ui.bat" (
    echo [OK] run-tests-ui.bat exists
) else (
    echo [ERROR] run-tests-ui.bat not found
)

if exist "%OFFLINE_DIR%\show-report.bat" (
    echo [OK] show-report.bat exists
) else (
    echo [ERROR] show-report.bat not found
)
echo.

REM ============================================================
REM Summary
REM ============================================================
:end
echo ============================================================
echo Verification Summary
echo ============================================================
echo.
echo Package location: %OFFLINE_DIR%
echo.

if %FOUND_ZIP%==1 (
    echo ZIP file location: %ZIP_DIR%
    echo.
    echo [SUCCESS] Package is ready for offline deployment!
    echo.
    echo Next steps:
    echo   1. Transfer the ZIP file to offline environment
    echo   2. Extract the ZIP file
    echo   3. Run run-tests.bat in the extracted folder
) else (
    echo [INFO] ZIP file not created yet
    echo.
    echo To create ZIP file:
    echo   Run create-offline-zip.bat
)
echo.

REM ============================================================
REM File List
REM ============================================================
echo Package contents:
echo ----------------------------------------
dir /B "%OFFLINE_DIR%"
echo ----------------------------------------
echo.

pause
