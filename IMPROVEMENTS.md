# アプリケーション改善内容

このドキュメントでは、データ移行バッチアプリケーションに追加された改善点を説明します。

## 実装済み改善（優先度順）

### 1. エラーハンドリングの強化

#### Skip機能
- **目的**: 不正なデータがあってもバッチ処理を継続
- **実装内容**:
  - `SkipListener.java`: スキップされたアイテムをログ出力
  - 読み込み、処理、書き込みの各段階でのスキップ対応
  - スキップ上限: デフォルト10件（`application.yml`で変更可能）

```java
// BatchConfiguration.java
.faultTolerant()
.skip(Exception.class)
.skipLimit(skipLimit)
.listener(skipListener)
```

#### Retry機能
- **目的**: 一時的なエラー（DB接続タイムアウトなど）のリトライ
- **実装内容**:
  - `RetryListener.java`: リトライ処理のログ記録
  - 最大3回までのリトライ（設定可能）

### 2. 設定の外部化

#### application.yml
データベース接続情報やバッチ設定をすべて外部化しました。

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5434/newdb}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:10}

batch:
  chunk-size: ${BATCH_CHUNK_SIZE:100}
  skip-limit: ${BATCH_SKIP_LIMIT:10}
  input:
    customer-file: ${CUSTOMER_FILE:classpath:data/old_customers.tsv}
    company-file: ${COMPANY_FILE:classpath:data/old_companies.tsv}
```

#### 環境変数での上書き

本番環境では環境変数で設定を上書きできます：

```bash
# Windows
set DB_URL=jdbc:postgresql://prod-server:5432/production_db
set DB_USERNAME=prod_user
set DB_PASSWORD=secure_password
set BATCH_CHUNK_SIZE=500
set BATCH_SKIP_LIMIT=50

# Linux/Mac
export DB_URL=jdbc:postgresql://prod-server:5432/production_db
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export BATCH_CHUNK_SIZE=500
export BATCH_SKIP_LIMIT=50
```

### 3. バリデーション機能の追加

#### CustomerValidator
顧客データに対して以下のチェックを実行：
- 必須項目チェック（顧客コード、顧客名）
- メールアドレス形式チェック
- 電話番号形式チェック（日本形式）
- 郵便番号形式チェック（XXX-XXXX形式）
- 性別コード範囲チェック（1または2）

#### CompanyValidator
会社データに対して以下のチェックを実行：
- 必須項目チェック（会社コード、会社名）
- メールアドレス形式チェック
- 電話番号形式チェック（日本形式）
- 郵便番号形式チェック（XXX-XXXX形式）
- 業種コード範囲チェック（1-11）
- 従業員数・資本金の妥当性チェック（正の数）

#### バリデーションエラーの処理
バリデーションエラーが発生した場合：
1. `ValidationException`がスローされる
2. `SkipListener`がエラーをログ出力
3. スキップ上限に達するまで処理を継続
4. スキップされたデータは後で手動確認可能（ログに記録）

## パフォーマンス最適化

### バッチINSERT最適化
Hibernateのバッチサイズを設定：
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
```

### チャンクサイズの調整
デフォルト100件ですが、データ量に応じて調整可能：
```yaml
batch:
  chunk-size: 500  # 大量データの場合は増やす
```

## 運用時の推奨事項

### 1. ログファイルの確認
バッチ実行後、以下を確認：
- スキップされたレコードの件数
- リトライが発生したかどうか
- バリデーションエラーの内容

### 2. エラーレコードの手動修正
スキップされたレコードは、以下の手順で修正：
1. ログからエラー内容を確認
2. TSVファイルの該当行を修正
3. 修正したレコードのみ再実行

### 3. パフォーマンスチューニング
大量データの場合：
- `BATCH_CHUNK_SIZE`を500-1000に増加
- `DB_POOL_SIZE`を20-50に増加
- `hibernate.jdbc.batch_size`を100に増加

## 今後の改善候補

以下の改善も検討可能です：

### 4. 監視・運用機能
- Spring Boot Actuatorの追加
- ヘルスチェックエンドポイント
- メトリクス収集

### 5. 再実行可能性
- UPSERT処理の実装（重複時は更新）
- 中断からの再開機能

### 6. テストの追加
- 単体テスト（Processorロジック）
- 統合テスト（Step動作確認）
- E2Eテスト（Job全体）

### 7. 並列処理
- パーティショニング（データ分割並列処理）
- マルチスレッドステップ
- 顧客と会社の同時並列処理

### 8. セキュリティ強化
- パスワードの暗号化（Jasypt）
- 機密データのマスキング

### 9. ログ出力の改善
- 構造化ロギング（JSON形式）
- MDCによる処理追跡
- ログレベルの細分化

### 10. 通知機能
- メール通知（成功/失敗時）
- Slack通知
- 処理統計レポートの生成
