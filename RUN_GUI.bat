@echo off
REM Swing GUIアプリケーションを起動

echo ========================================
echo Swing GUIアプリケーションを起動します
echo ========================================
echo.
echo 事前準備:
echo 1. 別のターミナルでSpring Bootアプリケーションを起動してください
echo    mvn spring-boot:run
echo 2. アプリケーションが起動したら、このGUIを使用できます
echo.
pause

echo GUIを起動しています...
java -cp target/classes com.example.batch.gui.BatchLauncherGUI
