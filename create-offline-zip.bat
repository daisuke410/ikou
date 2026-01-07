@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Offline Package ZIP Creation Script
REM ============================================================

echo ============================================================
echo Creating Offline Package ZIP File
echo ============================================================
echo.

set OFFLINE_DIR=%CD%\dist\offline-complete
set ZIP_NAME=playwright-offline-complete-%date:~0,4%%date:~5,2%%date:~8,2%.zip
set ZIP_PATH=%CD%\dist\%ZIP_NAME%

REM Check offline package exists
if not exist "%OFFLINE_DIR%" (
    echo [ERROR] Offline package not found
    echo.
    echo Please run build-offline-complete.bat first
    pause
    exit /b 1
)

REM Check PowerShell
where powershell >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] PowerShell not found
    pause
    exit /b 1
)

echo Offline package: %OFFLINE_DIR%
echo ZIP file name: %ZIP_NAME%
echo.
echo Creating ZIP file...
echo This may take several minutes...
echo.

REM Delete existing ZIP file
if exist "%ZIP_PATH%" del "%ZIP_PATH%"

REM Create ZIP with PowerShell
powershell -Command "Compress-Archive -Path '%OFFLINE_DIR%\*' -DestinationPath '%ZIP_PATH%' -CompressionLevel Optimal"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to create ZIP file
    pause
    exit /b 1
)

echo.
echo ============================================================
echo [SUCCESS] ZIP file created successfully!
echo ============================================================
echo.
echo File name: %ZIP_NAME%
echo File path: %ZIP_PATH%
echo.
echo File size:
for %%A in ("%ZIP_PATH%") do echo   %%~zA bytes
echo.
echo Transfer this ZIP file to offline environment and extract
echo.
pause
