@echo off
REM ============================================================
REM Playwright Test Runner - EXE ビルドスクリプト
REM ============================================================

echo ============================================================
echo Playwright Test Runner - EXE ビルド
echo ============================================================
echo.

REM 1. 依存関係のインストール
echo [1/4] 依存関係をインストール中...
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo エラー: npm install が失敗しました
    pause
    exit /b 1
)
echo.

REM 2. pkgのインストール（グローバル）
echo [2/4] pkg をインストール中...
call npm install -g pkg
if %ERRORLEVEL% NEQ 0 (
    echo エラー: pkg のインストールが失敗しました
    pause
    exit /b 1
)
echo.

REM 3. distディレクトリの作成
echo [3/4] ビルドディレクトリを準備中...
if not exist dist mkdir dist
echo.

REM 4. EXEファイルのビルド
echo [4/4] EXEファイルをビルド中...
call pkg . --targets node18-win-x64 --output dist/playwright-test-runner.exe
if %ERRORLEVEL% NEQ 0 (
    echo エラー: EXEのビルドが失敗しました
    pause
    exit /b 1
)
echo.

echo ============================================================
echo ✅ ビルドが完了しました！
echo ============================================================
echo.
echo 生成されたファイル:
echo   - dist\playwright-test-runner.exe
echo.
echo 次のステップ:
echo   1. setup-offline.bat を実行してオフライン環境用パッケージを作成
echo   2. または、package-for-offline.bat を実行して配布用ZIPを作成
echo.
pause
