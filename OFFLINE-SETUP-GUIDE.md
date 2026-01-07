# Playwright オフライン環境セットアップガイド

## 🎯 概要

このガイドでは、オフライン環境でPlaywrightテストを実行するための完全なセットアップ方法を説明します。

---

## 📋 前提条件

### オンライン環境（準備用）
- ✅ Node.js 18以上
- ✅ npm
- ✅ インターネット接続
- ✅ Windows 10/11

### オフライン環境（実行用）
- ✅ Node.js 18以上（インストール済み）
- ✅ Windows 10/11

---

## 🚀 セットアップ手順

### 方法1: オールインワンセットアップ（推奨）

最も簡単な方法です。すべてを自動で行います。

```batch
# 1. オンライン環境で実行
setup-all.bat
```

このスクリプトは以下を実行します：
1. ✅ npm依存関係のインストール
2. ✅ Playwrightブラウザのダウンロード
3. ✅ EXEファイルのビルド（オプション）
4. ✅ オフライン環境用パッケージの作成
5. ✅ 配布用ZIPファイルの作成

完了後、`dist/playwright-tests-offline-YYYYMMDD.zip`が生成されます。

### 方法2: ステップバイステップ

より細かく制御したい場合は、個別のスクリプトを実行します。

#### ステップ1: 依存関係のインストール
```batch
npm install
```

#### ステップ2: Playwrightブラウザのインストール
```batch
npx playwright install
```

#### ステップ3: EXEファイルのビルド（オプション）
```batch
build-exe.bat
```

#### ステップ4: オフライン環境用パッケージの作成
```batch
setup-offline.bat
```

#### ステップ5: 配布用ZIPの作成
```batch
package-for-offline.bat
```

---

## 📦 オフライン環境への転送

### ZIPファイルを使用する場合

1. **ZIPファイルの転送**
   ```
   dist/playwright-tests-offline-YYYYMMDD.zip
   ```
   をUSBメモリやネットワーク経由でオフライン環境に転送

2. **解凍**
   ```
   任意のディレクトリに解凍
   例: C:\playwright-tests\
   ```

3. **実行**
   ```batch
   cd C:\playwright-tests
   run-tests.bat
   ```

### フォルダを直接コピーする場合

1. **フォルダのコピー**
   ```
   dist/offline/
   ```
   フォルダ全体をオフライン環境にコピー

2. **実行**
   ```batch
   cd <コピー先ディレクトリ>
   run-tests.bat
   ```

---

## 🎮 オフライン環境での実行方法

### 基本的な実行

```batch
# 通常のテスト実行
run-tests.bat

# UIモード（インタラクティブ）
run-tests-ui.bat

# ヘッドモード（ブラウザを表示）
run-tests-headed.bat

# レポート表示
show-report.bat
```

### コマンドラインオプション

```batch
# 特定のテストファイルのみ実行
node test-runner.js tests\theme-selection.spec.ts

# 特定のブラウザで実行
node test-runner.js --project=chromium

# デバッグモード
node test-runner.js --debug

# 並列実行数を指定
node test-runner.js --workers=2
```

---

## 📁 パッケージ構成

```
playwright-tests-offline/
├── node_modules/              # 約200MB - 依存関係
├── .playwright/               # 約500MB - ブラウザ
│   ├── chromium-*/
│   ├── firefox-*/
│   └── webkit-*/
├── tests/                     # テストケース
│   ├── theme-selection.spec.ts
│   ├── dark-theme.spec.ts
│   ├── api-endpoints.spec.ts
│   ├── cross-theme.spec.ts
│   └── e2e.spec.ts
├── static/                    # 静的ファイル
├── playwright.config.ts       # Playwright設定
├── test-runner.js            # テストランナー
├── package.json              # パッケージ情報
├── run-tests.bat             # 実行スクリプト
├── run-tests-ui.bat          # UIモード
├── run-tests-headed.bat      # ヘッドモード
├── show-report.bat           # レポート表示
└── README-OFFLINE.md         # オフライン実行ガイド
```

**合計サイズ**: 約700MB-1GB

---

## 🔧 トラブルシューティング

### 問題1: セットアップが失敗する

**症状**: `setup-all.bat`実行時にエラー

**解決方法**:
```batch
# 1. Node.jsのバージョン確認
node --version
# v18以上であることを確認

# 2. npmキャッシュをクリア
npm cache clean --force

# 3. 再実行
setup-all.bat
```

### 問題2: ZIPファイルが大きすぎる

**症状**: ZIPファイルが1GB以上

**解決方法**:
```batch
# 不要なブラウザを削除してサイズを削減
# playwright.config.ts を編集して使用するブラウザを制限

projects: [
  {
    name: 'chromium',  # Chromiumのみ使用
    use: { ...devices['Desktop Chrome'] },
  },
  # firefox と webkit をコメントアウト
],
```

### 問題3: オフライン環境でブラウザが見つからない

**症状**: `Executable doesn't exist`エラー

**解決方法**:
```batch
# 環境変数を設定
set PLAYWRIGHT_BROWSERS_PATH=%CD%\.playwright

# または、run-tests.batを使用（自動設定）
```

### 問題4: Node.jsがオフライン環境にない

**解決方法**:
1. Node.js 18のインストーラをダウンロード
   - https://nodejs.org/
2. インストーラをオフライン環境に転送
3. インストール実行

---

## 💡 最適化のヒント

### 1. パッケージサイズの削減

```batch
# 使用しないブラウザを削除
rd /s /q dist\offline\.playwright\firefox-*
rd /s /q dist\offline\.playwright\webkit-*
```

### 2. 高速化

```batch
# 並列実行数を増やす
node test-runner.js --workers=4
```

### 3. 特定のテストのみ実行

```batch
# テーマ選択テストのみ
node test-runner.js tests\theme-selection.spec.ts

# E2Eテストを除外
node test-runner.js --grep-invert "E2E"
```

---

## 📊 ファイルサイズの内訳

| コンポーネント       | サイズ | 説明             |
| -------------------- | ------ | ---------------- |
| node_modules         | ~200MB | Node.js依存関係  |
| .playwright/chromium | ~150MB | Chromiumブラウザ |
| .playwright/firefox  | ~100MB | Firefoxブラウザ  |
| .playwright/webkit   | ~50MB  | WebKitブラウザ   |
| tests                | ~50KB  | テストケース     |
| その他               | ~10MB  | 設定ファイルなど |

**合計**: 約500MB-700MB（圧縮前）

---

## 🔄 アップデート手順

新しいバージョンのテストを配布する場合：

### オンライン環境で

```batch
# 1. 最新のコードを取得
git pull

# 2. 依存関係を更新
npm install

# 3. 新しいパッケージを作成
setup-all.bat
```

### オフライン環境で

```batch
# 1. 古いバージョンをバックアップ
move playwright-tests playwright-tests-backup

# 2. 新しいZIPを解凍
# 3. テストを実行
```

---

## 📝 チェックリスト

### オンライン環境（準備）

- [ ] Node.js 18以上がインストールされている
- [ ] インターネット接続が利用可能
- [ ] `setup-all.bat`を実行
- [ ] ZIPファイルが生成された
- [ ] ZIPファイルのサイズを確認（500MB-1GB程度）

### オフライン環境（実行）

- [ ] Node.js 18以上がインストールされている
- [ ] ZIPファイルを転送・解凍
- [ ] `run-tests.bat`が実行できる
- [ ] テストが正常に実行される
- [ ] レポートが生成される

---

## 🎓 よくある質問

### Q1: EXEファイルは必須ですか？

**A**: いいえ、オプションです。Node.jsがインストールされていれば`test-runner.js`で十分です。

### Q2: 複数のオフライン環境で使用できますか？

**A**: はい、ZIPファイルを複数の環境に配布できます。

### Q3: テストケースを追加できますか？

**A**: はい、`tests/`ディレクトリに新しい`.spec.ts`ファイルを追加してください。

### Q4: Spring Bootアプリケーションも含まれますか？

**A**: いいえ、テストのみです。Spring Bootアプリケーションは別途起動してください。

---

## 📞 サポート

問題が発生した場合は、以下の情報を含めてお問い合わせください：

1. エラーメッセージの全文
2. 実行したコマンド
3. 環境情報
   - OS: Windows 10/11
   - Node.jsバージョン: `node --version`
   - npmバージョン: `npm --version`
4. テストレポート（`playwright-report/`）

---

**最終更新**: 2026-01-04  
**バージョン**: 1.0.0  
**対応環境**: Windows 10/11, Node.js 18+
