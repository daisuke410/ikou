@echo off
chcp 65001 >nul 2>&1
REM ============================================================
REM Complete Offline Package Build Script
REM Creates a package with portable Node.js for offline execution
REM ============================================================

echo ============================================================
echo Playwright Complete Offline Package Build
echo ============================================================
echo.
echo This script creates a complete offline package including:
echo   - Portable Node.js
echo   - npm/npx
echo   - All node_modules
echo   - Playwright browsers
echo   - Test cases
echo   - Execution scripts
echo.
echo ============================================================
echo.

REM 変数設定
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
xcopy /E /I /Y /Q "dist\nodejs\%NODE_PACKAGE%" "%OFFLINE_DIR%\nodejs" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to copy Node.js
    pause
    exit /b 1
)
echo    [OK] Node.js copied
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
xcopy /E /I /Y /Q node_modules "%OFFLINE_DIR%\node_modules" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to copy node_modules
    pause
    exit /b 1
)
echo    [OK] node_modules copied
echo.

REM ============================================================
REM Step 5: Copy Playwright Browsers
REM ============================================================
echo [5/6] Copying Playwright browsers...
echo    This may take several minutes (progress will be shown)...
echo.

set PLAYWRIGHT_BROWSERS_PATH=%USERPROFILE%\AppData\Local\ms-playwright
if exist "%PLAYWRIGHT_BROWSERS_PATH%" (
    REM Show progress by not using /Q flag
    xcopy /E /I /Y "%PLAYWRIGHT_BROWSERS_PATH%" "%OFFLINE_DIR%\.playwright"
    if %ERRORLEVEL% NEQ 0 (
        echo    [ERROR] Failed to copy browsers
        pause
        exit /b 1
    )
    echo.
    echo    [OK] Browsers copied
) else (
    echo    [WARNING] Playwright browsers not found
    echo    Please run 'npx playwright install' first
    echo.
    echo    Continue anyway? (Y/N)
    choice /C YN /N
    if errorlevel 2 exit /b 1
)
echo.

REM ============================================================
REM Step 6: Copy Test Cases and Configuration Files
REM ============================================================
echo [6/6] Copying test cases and configuration files...

REM 設定ファイル
copy /Y playwright.config.ts "%OFFLINE_DIR%\"
copy /Y package.json "%OFFLINE_DIR%\"
copy /Y package-lock.json "%OFFLINE_DIR%\" 2>nul

REM テストケース
if exist tests xcopy /E /I /Y tests "%OFFLINE_DIR%\tests"

REM 静的ファイル
if exist src\main\resources\static xcopy /E /I /Y src\main\resources\static "%OFFLINE_DIR%\static"

REM 実行スクリプト
if not exist "%OFFLINE_DIR%\scripts" mkdir "%OFFLINE_DIR%\scripts"
copy /Y scripts\run-offline.bat "%OFFLINE_DIR%\scripts\"
copy /Y scripts\run-offline-ui.bat "%OFFLINE_DIR%\scripts\"
copy /Y scripts\show-report-offline.bat "%OFFLINE_DIR%\scripts\"

REM ルート実行スクリプト作成
(
echo @echo off
echo REM Playwright テスト実行 - オフライン
echo call scripts\run-offline.bat %%*
) > "%OFFLINE_DIR%\run-tests.bat"

(
echo @echo off
echo REM Playwright テスト実行 - UIモード
echo call scripts\run-offline-ui.bat
) > "%OFFLINE_DIR%\run-tests-ui.bat"

(
echo @echo off
echo REM Playwright レポート表示
echo call scripts\show-report-offline.bat
) > "%OFFLINE_DIR%\show-report.bat"

REM README作成
(
echo # Playwright オフライン実行パッケージ
echo.
echo このパッケージには、インターネット接続なしでPlaywrightテストを実行するために必要なすべてが含まれています。
echo.
echo ## 📦 パッケージ内容
echo.
echo - Node.js v%NODE_VERSION% ^(ポータブル版^)
echo - すべての依存関係 ^(node_modules^)
echo - Playwrightブラウザ ^(Chromium, Firefox, WebKit^)
echo - テストケース
echo - 実行スクリプト
echo.
echo ## 🚀 使用方法
echo.
echo ### 1. テストの実行
echo.
echo ```batch
echo # すべてのテストを実行
echo run-tests.bat
echo.
echo # UIモードで実行
echo run-tests-ui.bat
echo.
echo # レポートを表示
echo show-report.bat
echo ```
echo.
echo ### 2. 特定のテストを実行
echo.
echo ```batch
echo # 特定のファイルのみ
echo run-tests.bat tests\theme-selection.spec.ts
echo.
echo # 特定のブラウザで実行
echo run-tests.bat --project=chromium
echo run-tests.bat --project=firefox
echo run-tests.bat --project=webkit
echo.
echo # ヘッドモードで実行
echo run-tests.bat --headed
echo ```
echo.
echo ## 📋 システム要件
echo.
echo - Windows 10/11 ^(64-bit^)
echo - 管理者権限は不要
echo - インターネット接続は不要
echo.
echo ## 🔧 トラブルシューティング
echo.
echo ### テストが失敗する場合
echo.
echo 1. Spring Bootアプリケーションが起動しているか確認
echo 2. ポート8080が使用可能か確認
echo 3. playwright.config.tsの設定を確認
echo.
echo ### ブラウザが起動しない場合
echo.
echo .playwrightディレクトリが正しくコピーされているか確認してください。
echo.
echo ## 📞 サポート
echo.
echo 問題が発生した場合は、以下の情報を含めてお問い合わせください:
echo.
echo - エラーメッセージ
echo - 実行したコマンド
echo - playwright-report/の内容
echo.
echo ---
echo 最終更新: %date%
echo バージョン: 1.0.0
) > "%OFFLINE_DIR%\README.md"

echo    [OK] Files copied
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
