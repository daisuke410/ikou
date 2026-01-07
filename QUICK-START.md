# Playwright オフライン実行 - クイックスタート

## 📦 オンライン環境での準備（3ステップ）

### 1. 依存関係のインストール

```batch
npm install
npx playwright install
```

### 2. オフラインパッケージのビルド

```batch
download-nodejs.bat
build-offline-complete.bat
create-offline-zip.bat
```

### 3. ZIPファイルの転送

`dist\playwright-offline-complete-YYYYMMDD.zip` をUSBメモリなどでオフライン環境に転送

---

## 🚀 オフライン環境での実行（2ステップ）

### 1. ZIPファイルの解凍

任意の場所に解凍（例: `C:\playwright-tests\`）

### 2. テストの実行

```batch
# すべてのテストを実行
run-tests.bat

# UIモードで実行
run-tests-ui.bat

# レポートを表示
show-report.bat
```

---

## 💡 その他の実行オプション

```batch
# 特定のテストのみ実行
run-tests.bat tests\theme-selection.spec.ts

# 特定のブラウザで実行
run-tests.bat --project=chromium

# ヘッドモードで実行（ブラウザを表示）
run-tests.bat --headed
```

---

## ⚠️ 注意事項

- オフライン環境にNode.jsのインストールは不要
- パッケージサイズ: 約500MB～1GB（ZIP圧縮後: 300～500MB）
- Spring Bootアプリケーション（ポート8080）が起動している必要があります

---

## 📖 詳細なガイド

完全な使用方法は `OFFLINE-COMPLETE-GUIDE.md` を参照してください。

---

**作成日:** 2026-01-04
