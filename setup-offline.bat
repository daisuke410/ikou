@echo off
REM ============================================================
REM オフライン環境用セットアップスクリプト
REM ============================================================

echo ============================================================
echo Playwright オフライン環境セットアップ
echo ============================================================
echo.

REM 現在のディレクトリを保存
set CURRENT_DIR=%CD%

REM 1. distディレクトリの作成
echo [1/5] ディレクトリを準備中...
if not exist dist mkdir dist
if not exist dist\offline mkdir dist\offline
if not exist dist\offline\node_modules mkdir dist\offline\node_modules
echo.

REM 2. node_modulesをコピー
echo [2/5] node_modules をコピー中...
echo    これには数分かかる場合があります...
xcopy /E /I /Y node_modules dist\offline\node_modules
if %ERRORLEVEL% NEQ 0 (
    echo エラー: node_modules のコピーが失敗しました
    pause
    exit /b 1
)
echo.

REM 3. Playwrightブラウザをコピー
echo [3/5] Playwright ブラウザをコピー中...
set PLAYWRIGHT_BROWSERS_PATH=%USERPROFILE%\AppData\Local\ms-playwright
if exist "%PLAYWRIGHT_BROWSERS_PATH%" (
    if not exist dist\offline\.playwright mkdir dist\offline\.playwright
    xcopy /E /I /Y "%PLAYWRIGHT_BROWSERS_PATH%" dist\offline\.playwright
    echo    ブラウザのコピーが完了しました
) else (
    echo    警告: Playwrightブラウザが見つかりません
    echo    先に 'npx playwright install' を実行してください
)
echo.

REM 4. 必要なファイルをコピー
echo [4/5] 設定ファイルをコピー中...
copy /Y playwright.config.ts dist\offline\
copy /Y test-runner.js dist\offline\
copy /Y package.json dist\offline\
xcopy /E /I /Y tests dist\offline\tests
if exist src\main\resources\static xcopy /E /I /Y src\main\resources\static dist\offline\static
echo.

REM 5. 実行用バッチファイルを作成
echo [5/5] 実行スクリプトを作成中...

(
echo @echo off
echo REM Playwright Test Runner - オフライン実行
echo.
echo REM Playwrightブラウザのパスを設定
echo set PLAYWRIGHT_BROWSERS_PATH=%%CD%%\.playwright
echo.
echo REM テストを実行
echo node test-runner.js %%*
echo.
echo pause
) > dist\offline\run-tests.bat

(
echo @echo off
echo REM Playwright Test Runner - UIモード
echo.
echo set PLAYWRIGHT_BROWSERS_PATH=%%CD%%\.playwright
echo.
echo node test-runner.js --ui
echo.
echo pause
) > dist\offline\run-tests-ui.bat

(
echo @echo off
echo REM Playwright Test Runner - レポート表示
echo.
echo npx playwright show-report
echo.
echo pause
) > dist\offline\show-report.bat

echo.
echo ============================================================
echo ✅ オフライン環境用パッケージの準備が完了しました！
echo ============================================================
echo.
echo 生成されたファイル:
echo   - dist\offline\node_modules\     (依存関係)
echo   - dist\offline\.playwright\      (ブラウザ)
echo   - dist\offline\tests\            (テストケース)
echo   - dist\offline\run-tests.bat     (テスト実行)
echo   - dist\offline\run-tests-ui.bat  (UIモード)
echo   - dist\offline\show-report.bat   (レポート表示)
echo.
echo 次のステップ:
echo   1. dist\offline フォルダをオフライン環境にコピー
echo   2. オフライン環境で run-tests.bat を実行
echo.
echo または、package-for-offline.bat を実行してZIPファイルを作成できます
echo.
pause
