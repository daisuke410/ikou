@echo off
REM ============================================================
REM オフライン配布用ZIPパッケージ作成スクリプト
REM ============================================================

echo ============================================================
echo Playwright オフライン配布パッケージ作成
echo ============================================================
echo.

REM PowerShellが使用可能か確認
where powershell >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo エラー: PowerShell が見つかりません
    pause
    exit /b 1
)

REM 1. オフライン環境のセットアップ
echo [1/3] オフライン環境をセットアップ中...
call setup-offline.bat
if %ERRORLEVEL% NEQ 0 (
    echo エラー: セットアップが失敗しました
    pause
    exit /b 1
)
echo.

REM 2. EXEファイルをコピー（存在する場合）
echo [2/3] EXEファイルをコピー中...
if exist dist\playwright-test-runner.exe (
    copy /Y dist\playwright-test-runner.exe dist\offline\
    echo    EXEファイルをコピーしました
) else (
    echo    警告: EXEファイルが見つかりません
    echo    先に build-exe.bat を実行してください
)
echo.

REM 3. ZIPファイルを作成
echo [3/3] ZIPファイルを作成中...
set ZIP_NAME=playwright-tests-offline-%date:~0,4%%date:~5,2%%date:~8,2%.zip
set ZIP_PATH=%CD%\dist\%ZIP_NAME%

REM 既存のZIPファイルを削除
if exist "%ZIP_PATH%" del "%ZIP_PATH%"

REM PowerShellでZIP作成
powershell -Command "Compress-Archive -Path 'dist\offline\*' -DestinationPath '%ZIP_PATH%' -CompressionLevel Optimal"
if %ERRORLEVEL% NEQ 0 (
    echo エラー: ZIPファイルの作成が失敗しました
    pause
    exit /b 1
)
echo.

echo ============================================================
echo ✅ 配布パッケージの作成が完了しました！
echo ============================================================
echo.
echo 生成されたファイル:
echo   - %ZIP_NAME%
echo.
echo ファイルサイズ:
for %%A in ("%ZIP_PATH%") do echo   - %%~zA bytes
echo.
echo このZIPファイルをオフライン環境に転送して解凍してください
echo.
pause
