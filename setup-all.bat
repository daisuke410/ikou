@echo off
REM ============================================================
REM Playwright オフライン環境 - オールインワンセットアップ
REM ============================================================

echo ============================================================
echo Playwright オフライン環境 - 完全セットアップ
echo ============================================================
echo.
echo このスクリプトは以下を実行します:
echo   1. 依存関係のインストール
echo   2. Playwrightブラウザのインストール
echo   3. EXEファイルのビルド
echo   4. オフライン環境用パッケージの作成
echo   5. 配布用ZIPファイルの作成
echo.
echo 処理には10-20分程度かかる場合があります。
echo.
pause

REM ステップ1: 依存関係のインストール
echo.
echo ============================================================
echo [1/5] 依存関係をインストール中...
echo ============================================================
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo エラー: npm install が失敗しました
    pause
    exit /b 1
)

REM ステップ2: Playwrightブラウザのインストール
echo.
echo ============================================================
echo [2/5] Playwright ブラウザをインストール中...
echo ============================================================
call npx playwright install
if %ERRORLEVEL% NEQ 0 (
    echo エラー: Playwright ブラウザのインストールが失敗しました
    pause
    exit /b 1
)

REM ステップ3: pkgのインストール
echo.
echo ============================================================
echo [3/5] pkg をインストール中...
echo ============================================================
call npm install -g pkg
if %ERRORLEVEL% NEQ 0 (
    echo 警告: pkg のグローバルインストールが失敗しました
    echo ローカルインストールを試みます...
    call npm install pkg --save-dev
)

REM ステップ4: EXEファイルのビルド
echo.
echo ============================================================
echo [4/5] EXE ファイルをビルド中...
echo ============================================================
if not exist dist mkdir dist
call npx pkg . --targets node18-win-x64 --output dist/playwright-test-runner.exe
if %ERRORLEVEL% NEQ 0 (
    echo 警告: EXE のビルドが失敗しました
    echo オフライン環境ではNode.jsランナーを使用してください
)

REM ステップ5: オフライン環境用パッケージの作成
echo.
echo ============================================================
echo [5/5] オフライン環境用パッケージを作成中...
echo ============================================================

REM distディレクトリの準備
if not exist dist\offline mkdir dist\offline
if not exist dist\offline\node_modules mkdir dist\offline\node_modules

REM node_modulesをコピー
echo    node_modules をコピー中...
xcopy /E /I /Y /Q node_modules dist\offline\node_modules > nul

REM Playwrightブラウザをコピー
echo    Playwright ブラウザをコピー中...
set PLAYWRIGHT_BROWSERS_PATH=%USERPROFILE%\AppData\Local\ms-playwright
if exist "%PLAYWRIGHT_BROWSERS_PATH%" (
    if not exist dist\offline\.playwright mkdir dist\offline\.playwright
    xcopy /E /I /Y /Q "%PLAYWRIGHT_BROWSERS_PATH%" dist\offline\.playwright > nul
)

REM 設定ファイルとテストをコピー
echo    設定ファイルとテストをコピー中...
copy /Y playwright.config.ts dist\offline\ > nul
copy /Y test-runner.js dist\offline\ > nul
copy /Y package.json dist\offline\ > nul
copy /Y README-OFFLINE.md dist\offline\ > nul
xcopy /E /I /Y tests dist\offline\tests > nul
if exist src\main\resources\static xcopy /E /I /Y src\main\resources\static dist\offline\static > nul

REM EXEファイルをコピー（存在する場合）
if exist dist\playwright-test-runner.exe (
    copy /Y dist\playwright-test-runner.exe dist\offline\ > nul
)

REM 実行用バッチファイルを作成
(
echo @echo off
echo REM Playwright Test Runner - オフライン実行
echo set PLAYWRIGHT_BROWSERS_PATH=%%CD%%\.playwright
echo node test-runner.js %%*
echo pause
) > dist\offline\run-tests.bat

(
echo @echo off
echo REM Playwright Test Runner - UIモード
echo set PLAYWRIGHT_BROWSERS_PATH=%%CD%%\.playwright
echo node test-runner.js --ui
echo pause
) > dist\offline\run-tests-ui.bat

(
echo @echo off
echo REM Playwright Test Runner - レポート表示
echo npx playwright show-report
echo pause
) > dist\offline\show-report.bat

(
echo @echo off
echo REM Playwright Test Runner - ヘッドモード
echo set PLAYWRIGHT_BROWSERS_PATH=%%CD%%\.playwright
echo node test-runner.js --headed
echo pause
) > dist\offline\run-tests-headed.bat

REM ZIPファイルを作成
echo    ZIP ファイルを作成中...
set ZIP_NAME=playwright-tests-offline-%date:~0,4%%date:~5,2%%date:~8,2%.zip
set ZIP_PATH=%CD%\dist\%ZIP_NAME%

if exist "%ZIP_PATH%" del "%ZIP_PATH%"
powershell -Command "Compress-Archive -Path 'dist\offline\*' -DestinationPath '%ZIP_PATH%' -CompressionLevel Optimal" 2>nul

echo.
echo ============================================================
echo ✅ すべてのセットアップが完了しました！
echo ============================================================
echo.
echo 生成されたファイル:
echo   📁 dist\offline\                  (オフライン環境用フォルダ)
if exist dist\playwright-test-runner.exe echo   📦 dist\playwright-test-runner.exe  (スタンドアロンEXE)
if exist "%ZIP_PATH%" (
    echo   📦 %ZIP_NAME%
    for %%A in ("%ZIP_PATH%") do echo      サイズ: %%~zA bytes
)
echo.
echo 次のステップ:
echo   1. dist\offline フォルダをオフライン環境にコピー
echo   2. または、ZIPファイルを転送して解凍
echo   3. オフライン環境で run-tests.bat を実行
echo.
echo テスト実行方法:
echo   - 通常実行:     run-tests.bat
echo   - UIモード:     run-tests-ui.bat
echo   - ヘッドモード: run-tests-headed.bat
echo   - レポート表示: show-report.bat
echo.
pause
