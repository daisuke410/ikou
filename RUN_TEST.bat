@echo off
REM テスト環境でバッチを実行（マスク処理有効）
echo ========================================
echo テスト環境でバッチを実行します
echo マスク処理: 有効
echo ========================================
mvn spring-boot:run "-Dspring-boot.run.profiles=test"
