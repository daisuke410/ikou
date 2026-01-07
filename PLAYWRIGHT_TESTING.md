# Playwright テストガイド

Spring Batch Data Migration Applicationの自動テストドキュメント

## 📋 目次

- [セットアップ](#セットアップ)
- [テスト実行](#テスト実行)
- [テストケース概要](#テストケース概要)
- [トラブルシューティング](#トラブルシューティング)

## 🚀 セットアップ

### 1. Node.jsのインストール

Node.js 18以上が必要です。

```bash
node --version
```

### 2. 依存関係のインストール

```bash
npm install
```

### 3. Playwrightブラウザのインストール

```bash
npx playwright install
```

すべてのブラウザ（Chromium, Firefox, WebKit）がインストールされます。

## 🧪 テスト実行

### 基本的なテスト実行

```bash
# すべてのテストを実行
npm test

# 特定のブラウザでテスト
npx playwright test --project=chromium

# ヘッドモードで実行（ブラウザを表示）
npm run test:headed

# UIモードで実行（インタラクティブ）
npm run test:ui

# デバッグモード
npm run test:debug
```

### 特定のテストファイルを実行

```bash
# テーマ選択ページのテストのみ
npx playwright test theme-selection.spec.ts

# Darkテーマのテストのみ
npx playwright test dark-theme.spec.ts

# APIテストのみ
npx playwright test api-endpoints.spec.ts

# E2Eテストのみ
npx playwright test e2e.spec.ts
```

### テストレポートの表示

```bash
# HTMLレポートを開く
npm run test:report
```

## 📝 テストケース概要

### 1. theme-selection.spec.ts
**テーマ選択ページのテスト**

- ページタイトルとヘッダーの表示確認
- 7つのテーマカードの表示確認
- カードのホバー効果
- テーマ選択時のナビゲーション
- レスポンシブデザイン（モバイル対応）
- バッジとフッターの表示

**テスト数**: 10個

### 2. dark-theme.spec.ts
**Darkテーマページのテスト**

- ページ読み込み確認
- UI要素の表示（ボタン、チェックボックス、ステータスパネル）
- ログエリアとプログレスバーの存在確認
- レスポンシブデザイン（デスクトップ/タブレット/モバイル）
- ダークテーマスタイルの適用確認
- バッチ実行機能（APIモック使用）

**テスト数**: 11個

### 3. api-endpoints.spec.ts
**REST APIエンドポイントのテスト**

- バッチ開始API（正常系、Upsert有効）
- バッチステータスAPI
- バッチ履歴API
- WebSocket接続テスト
- Spring Boot Actuatorエンドポイント
  - ヘルスチェック
  - メトリクス
  - Prometheusメトリクス

**テスト数**: 8個

### 4. cross-theme.spec.ts
**全テーマ共通機能のテスト**

各テーマ（7種類）に対して以下をテスト：
- ページ読み込み
- JavaScriptエラーチェック
- 基本UI要素の存在確認
- キーボードナビゲーション
- 画像とリソースの読み込み
- CSSの適用確認

追加テスト：
- パフォーマンステスト（読み込み時間、メモリリーク）
- セキュリティテスト（XSS対策、HTTPS）

**テスト数**: 49個（7テーマ × 7テスト + 4追加テスト）

### 5. e2e.spec.ts
**エンドツーエンドテスト**

- 完全なバッチ実行フロー
- テーマ切り替えフロー
- エラーハンドリング（API障害時）
- 複数回実行フロー
- 初回ユーザー体験フロー
- モバイルユーザー体験フロー
- キーボードのみでの操作フロー

**テスト数**: 7個

### 合計テストケース数
**約85個のテストケース**

## 🔧 テスト設定

### playwright.config.ts

主要な設定：
- **ベースURL**: `http://localhost:8080`
- **タイムアウト**: 60秒
- **リトライ**: CI環境で2回
- **レポート**: HTML、リスト、JSON形式
- **ブラウザ**: Chromium, Firefox, WebKit, Mobile Chrome, Mobile Safari

### 自動サーバー起動

テスト実行時に自動的にSpring Bootアプリケーションが起動します：

```javascript
webServer: {
  command: 'mvn spring-boot:run',
  url: 'http://localhost:8080',
  timeout: 120000,
}
```

## 🐛 トラブルシューティング

### サーバーが起動しない

手動でサーバーを起動してからテストを実行：

```bash
# 別のターミナルでサーバーを起動
mvn spring-boot:run

# テストを実行（サーバー起動なし）
npx playwright test --config=playwright.config.ts
```

### ポート8080が使用中

`playwright.config.ts`の`baseURL`を変更：

```javascript
baseURL: 'http://localhost:8081',
```

### ブラウザのインストールエラー

```bash
# すべてのブラウザを再インストール
npx playwright install --force

# 特定のブラウザのみインストール
npx playwright install chromium
```

### テストがタイムアウトする

`playwright.config.ts`でタイムアウトを延長：

```javascript
timeout: 120000, // 120秒
```

### APIテストが失敗する

Spring Bootアプリケーションが起動していることを確認：

```bash
curl http://localhost:8080/actuator/health
```

### データベース接続エラー

PostgreSQLが起動していることを確認：

```bash
# PostgreSQLの状態確認（Windows）
Get-Service postgresql*

# PostgreSQLの起動
Start-Service postgresql-x64-14
```

## 📊 テストレポート

テスト実行後、以下のレポートが生成されます：

- **HTMLレポート**: `playwright-report/index.html`
- **JSONレポート**: `test-results.json`
- **スクリーンショット**: `test-results/` (失敗時のみ)
- **ビデオ**: `test-results/` (失敗時のみ)

## 🎯 ベストプラクティス

### 1. テストの独立性
各テストは独立して実行可能で、他のテストに依存しません。

### 2. APIモック
外部APIへの依存を減らすため、必要に応じてモックを使用します。

### 3. 待機戦略
適切な待機戦略を使用：
- `waitForLoadState('networkidle')`: ネットワークアイドル待機
- `waitForSelector()`: 要素の出現待機
- `waitForTimeout()`: 固定時間待機（最小限に）

### 4. エラーハンドリング
APIが起動していない場合でもテストが失敗しないよう、適切なエラーハンドリングを実装。

## 🔍 デバッグ

### Playwrightインスペクター

```bash
npx playwright test --debug
```

ステップバイステップでテストを実行し、要素をインスペクトできます。

### トレースビューアー

```bash
npx playwright show-trace test-results/trace.zip
```

失敗したテストのトレースを視覚的に確認できます。

## 📚 参考資料

- [Playwright公式ドキュメント](https://playwright.dev/)
- [Playwrightベストプラクティス](https://playwright.dev/docs/best-practices)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

## 🤝 貢献

テストケースの追加や改善は歓迎します。新しいテストを追加する際は：

1. 適切なテストファイルに追加（または新規作成）
2. テストケースに明確な説明を追加
3. このREADMEを更新

## 📄 ライセンス

このテストスイートはプロジェクトのライセンスに従います。
