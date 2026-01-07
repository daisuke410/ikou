# Playwright テスト - オフライン実行ガイド

## 📦 オフライン環境での実行方法

このパッケージには、インターネット接続なしでPlaywrightテストを実行するために必要なすべてのファイルが含まれています。

---

## 📋 パッケージ内容

```
playwright-tests-offline/
├── node_modules/              # Node.js依存関係
├── .playwright/               # Playwrightブラウザ
├── tests/                     # テストケース
│   ├── theme-selection.spec.ts
│   ├── dark-theme.spec.ts
│   ├── api-endpoints.spec.ts
│   ├── cross-theme.spec.ts
│   └── e2e.spec.ts
├── static/                    # 静的ファイル（HTMLなど）
├── playwright.config.ts       # Playwright設定
├── test-runner.js            # テストランナー
├── package.json              # パッケージ情報
├── run-tests.bat             # テスト実行スクリプト
├── run-tests-ui.bat          # UIモード実行スクリプト
├── show-report.bat           # レポート表示スクリプト
└── README-OFFLINE.md         # このファイル
```

---

## 🚀 クイックスタート

### 1. パッケージの展開

ZIPファイルを任意のディレクトリに解凍してください。

```
例: C:\playwright-tests\
```

### 2. テストの実行

#### 方法A: バッチファイルを使用（推奨）

```batch
# 通常のテスト実行
run-tests.bat

# UIモードで実行（インタラクティブ）
run-tests-ui.bat

# テストレポートを表示
show-report.bat
```

#### 方法B: コマンドラインから実行

```batch
# カレントディレクトリを移動
cd C:\playwright-tests

# 環境変数を設定
set PLAYWRIGHT_BROWSERS_PATH=%CD%\.playwright

# テストを実行
node test-runner.js
```

---

## 🎯 テスト実行オプション

### 基本的な実行

```batch
# すべてのテストを実行
node test-runner.js

# 特定のテストファイルのみ実行
node test-runner.js tests\theme-selection.spec.ts

# 特定のブラウザで実行
node test-runner.js --project=chromium
node test-runner.js --project=firefox
node test-runner.js --project=webkit
```

### 高度なオプション

```batch
# ヘッドモードで実行（ブラウザを表示）
node test-runner.js --headed

# デバッグモード
node test-runner.js --debug

# 特定のテストのみ実行
node test-runner.js --grep "テーマ選択"

# 並列実行数を指定
node test-runner.js --workers=4
```

---

## 📊 テストレポート

### HTMLレポートの表示

テスト実行後、HTMLレポートが自動生成されます。

```batch
# レポートを表示
show-report.bat

# または
npx playwright show-report
```

レポートには以下の情報が含まれます：
- ✅ テスト結果（成功/失敗）
- 📸 スクリーンショット（失敗時）
- 🎥 ビデオ録画（失敗時）
- 📝 詳細なエラーログ

---

## 🔧 トラブルシューティング

### 問題1: ブラウザが見つからない

**エラー**: `Executable doesn't exist at ...`

**解決方法**:
```batch
# 環境変数を設定
set PLAYWRIGHT_BROWSERS_PATH=%CD%\.playwright

# または、run-tests.bat を使用
```

### 問題2: Node.jsが見つからない

**エラー**: `'node' は、内部コマンドまたは外部コマンド...`

**解決方法**:
1. Node.js 18以上をインストール
2. システム環境変数のPATHにNode.jsを追加

### 問題3: テストが失敗する

**確認事項**:
1. Spring Bootアプリケーションが起動しているか
2. ポート8080が使用可能か
3. HTMLファイルが正しい場所にあるか

```batch
# サーバーの起動確認
curl http://localhost:8080/actuator/health
```

### 問題4: 権限エラー

**解決方法**:
```batch
# 管理者権限でコマンドプロンプトを開く
# または、ディレクトリの権限を確認
```

---

## 📁 ディレクトリ構造の要件

オフライン環境では、以下のディレクトリ構造を維持してください：

```
playwright-tests/
├── node_modules/          # 必須
├── .playwright/           # 必須
├── tests/                 # 必須
├── playwright.config.ts   # 必須
└── test-runner.js        # 必須
```

---

## 🌐 Spring Bootアプリケーションとの連携

### 前提条件

Playwrightテストを実行する前に、Spring Bootアプリケーションを起動してください。

```batch
# Spring Bootアプリケーションの起動
cd <Spring Bootプロジェクトのパス>
mvn spring-boot:run

# または
java -jar target\data-migration-batch-1.0.0.jar
```

### ポート設定

デフォルトではポート8080を使用します。変更する場合は`playwright.config.ts`を編集してください。

```typescript
export default defineConfig({
  use: {
    baseURL: 'http://localhost:8080',  // ここを変更
  },
});
```

---

## 📝 テストケース一覧

### 1. テーマ選択ページ (10テスト)
- ページ表示確認
- ナビゲーション
- レスポンシブデザイン

### 2. Darkテーマページ (11テスト)
- UI要素の表示
- バッチ実行機能
- APIモック

### 3. APIエンドポイント (8テスト)
- REST API
- WebSocket
- Actuator

### 4. 全テーマ共通機能 (53テスト)
- 7テーマの共通機能
- パフォーマンス
- セキュリティ

### 5. E2Eテスト (7テスト)
- ユーザーフロー
- エラーハンドリング

**合計**: 89テストケース

---

## 🔄 アップデート方法

新しいバージョンのテストパッケージを受け取った場合：

1. 古いディレクトリをバックアップ
2. 新しいZIPファイルを解凍
3. 必要に応じて設定ファイルを移行

---

## 💡 ベストプラクティス

### 1. 定期的なテスト実行
```batch
# 毎日のテスト実行
run-tests.bat
```

### 2. CI/CD統合
```batch
# バッチファイルから呼び出し
call run-tests.bat
if %ERRORLEVEL% NEQ 0 exit /b 1
```

### 3. レポートの保存
```batch
# レポートディレクトリをバックアップ
xcopy /E /I playwright-report backup\playwright-report-%date%
```

---

## 📞 サポート

問題が発生した場合は、以下の情報を含めてお問い合わせください：

1. エラーメッセージ
2. 実行したコマンド
3. 環境情報（OS、Node.jsバージョン）
4. テストレポート（playwright-report/）

---

## 📄 ライセンス

このテストスイートはプロジェクトのライセンスに従います。

---

**最終更新**: 2026-01-04  
**バージョン**: 1.0.0
