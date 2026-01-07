@echo off
REM 本番環境でバッチを実行（マスク処理無効）
echo ========================================
echo 本番環境でバッチを実行します
echo マスク処理: 無効
echo ========================================
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
