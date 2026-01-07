@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Portable Node.js Download Script
REM ============================================================

echo ============================================================
echo Portable Node.js Download
echo ============================================================
echo.

REM Node.js version settings
set NODE_VERSION=18.20.5
set NODE_ARCH=win-x64
set NODE_PACKAGE=node-v%NODE_VERSION%-%NODE_ARCH%
set NODE_URL=https://nodejs.org/dist/v%NODE_VERSION%/%NODE_PACKAGE%.zip
set DOWNLOAD_DIR=%CD%\dist\nodejs

echo Node.js version: %NODE_VERSION%
echo Architecture: %NODE_ARCH%
echo Download URL: %NODE_URL%
echo.

REM Create directories
if not exist dist mkdir dist
if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

REM Check if already downloaded
if exist "%DOWNLOAD_DIR%\%NODE_PACKAGE%" (
    echo [OK] Portable Node.js already downloaded
    echo    Path: %DOWNLOAD_DIR%\%NODE_PACKAGE%
    goto :cleanup
)

REM Download with PowerShell
echo [1/2] Downloading Node.js...
echo    This may take several minutes...
echo.

powershell -Command "& {$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '%NODE_URL%' -OutFile '%DOWNLOAD_DIR%\%NODE_PACKAGE%.zip'}"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Download failed
    echo.
    echo To download manually:
    echo    1. Visit %NODE_URL%
    echo    2. Save the ZIP file to %DOWNLOAD_DIR%
    echo    3. Re-run this script
    pause
    exit /b 1
)
echo [OK] Download complete
echo.

REM Extract ZIP file
echo [2/2] Extracting ZIP file...
powershell -Command "Expand-Archive -Path '%DOWNLOAD_DIR%\%NODE_PACKAGE%.zip' -DestinationPath '%DOWNLOAD_DIR%' -Force"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Extraction failed
    pause
    exit /b 1
)
echo [OK] Extraction complete
echo.

REM Delete ZIP file (optional)
del "%DOWNLOAD_DIR%\%NODE_PACKAGE%.zip"

:cleanup
echo ============================================================
echo [SUCCESS] Portable Node.js ready!
echo ============================================================
echo.
echo Installation path:
echo   %DOWNLOAD_DIR%\%NODE_PACKAGE%
echo.
echo Node.js version:
"%DOWNLOAD_DIR%\%NODE_PACKAGE%\node.exe" --version
echo.
echo Next step:
echo   Run build-offline-complete.bat to create offline package
echo.
pause
