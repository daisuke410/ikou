# Spring Batch データ移行アプリケーション

## 概要

このアプリケーションは、Spring Batchを使用してTSVファイルから新データベースへ顧客データと会社データを移行するバッチアプリケーションです。

**重要**: 旧データは別システム会社が管理するSQL Serverから抽出したTSVファイルとして提供されます。このアプリケーションはTSVファイルを読み込み、データ変換して新データベースへ移行します。

## 主な機能

### データ移行機能
- **TSVファイル読込**: 別システムから抽出された顧客・会社データ（TSV形式）を読み込み
- **複数テーブル対応**: 顧客テーブルと会社テーブルの両方を移行
- **データ変換**: 移行時にフィールド名とデータ形式を変換
- **バッチ処理**: 100件ずつのチャンク処理で効率的にデータを移行（設定変更可能）
- **エラーハンドリング**: スキップ・リトライ機能によるエラー耐性
- **バリデーション**: メール、電話番号、郵便番号の形式チェック
- **データマスク処理**: テスト環境用の機密データマスク機能（本番では無効化可能）

### GUI機能
- **Web UI**: ブラウザベースの美しいGUI（http://localhost:8080）
  - バッチ開始ボタン
  - データマスク設定チェックボックス
  - リアルタイム進捗表示
  - ログ出力
- **Swing GUI**: デスクトップアプリケーション
  - ネイティブなルック&フィール
  - リアルタイムステータス更新
  - プログレスバー表示
- **REST API**: プログラムから実行可能なAPI
  - POST /api/batch/start
  - GET /api/batch/status/{executionId}
  - GET /api/batch/latest

### 監視・運用機能
- **進捗監視**: リアルタイム進捗表示（5秒ごと）
- **統計レポート**: 処理完了時にCSVレポート自動生成
- **Spring Boot Actuator**: メトリクス監視とヘルスチェック機能

## プロジェクト構成

```
src/
├── main/
│   ├── java/com/example/batch/
│   │   ├── DataMigrationBatchApplication.java  # アプリケーション起動クラス
│   │   ├── config/
│   │   │   ├── BatchConfiguration.java         # バッチジョブ設定（TSV読込含む）
│   │   │   └── DatabaseConfiguration.java      # 新データベース設定
│   │   ├── dto/
│   │   │   ├── OldCustomerDto.java            # 旧顧客データDTO
│   │   │   └── OldCompanyDto.java             # 旧会社データDTO
│   │   ├── entity/
│   │   │   └── newdb/
│   │   │       ├── NewCustomer.java           # 新顧客エンティティ
│   │   │       └── NewCompany.java            # 新会社エンティティ
│   │   ├── processor/
│   │   │   ├── CustomerMigrationProcessor.java # 顧客データ変換処理
│   │   │   └── CompanyMigrationProcessor.java  # 会社データ変換処理
│   │   └── listener/
│   │       └── JobCompletionNotificationListener.java # ジョブ監視
│   └── resources/
│       ├── application.yml                     # アプリケーション設定
│       ├── schema-new.sql                      # 新DBスキーマ
│       └── data/
│           ├── old_customers.tsv              # 旧顧客データ（TSV）
│           └── old_companies.tsv              # 旧会社データ（TSV）
└── test/
```

## データ構造

### 入力データ1（顧客TSVファイル）
**ファイルパス**: `src/main/resources/data/old_customers.tsv`

TSVファイルのフォーマット（タブ区切り）:
```
customer_code	customer_name	email	phone	address	postal_code	created_at	status	gender_code
CUST001	山田太郎	yamada.taro@example.com	090-1234-5678	東京都渋谷区1-2-3	150-0001	2025-12-26 10:00:00	ACTIVE	1
```

| カラム名 | 説明 | 例 |
|---------|------|-----|
| customer_code | 顧客コード | CUST001 |
| customer_name | 顧客名 | 山田太郎 |
| email | メールアドレス | yamada.taro@example.com |
| phone | 電話番号 | 090-1234-5678 |
| address | 住所 | 東京都渋谷区1-2-3 |
| postal_code | 郵便番号 | 150-0001 |
| created_at | 登録日時 | 2025-12-26 10:00:00 |
| status | ステータス | ACTIVE/INACTIVE |
| gender_code | 性別コード | 1=男性, 2=女性 |

### 入力データ2（会社TSVファイル）
**ファイルパス**: `src/main/resources/data/old_companies.tsv`

TSVファイルのフォーマット（タブ区切り）:
```
company_code	company_name	representative_name	industry_type	employee_count	capital	established_date	address	postal_code	phone	email	status
COMP001	株式会社サンプル商事	山田太郎	1	150	50000000	2010-04-01	東京都千代田区丸の内1-1-1	100-0001	03-1234-5678	info@sample-corp.co.jp	ACTIVE
```

| カラム名 | 説明 | 例 |
|---------|------|-----|
| company_code | 会社コード | COMP001 |
| company_name | 会社名 | 株式会社サンプル商事 |
| representative_name | 代表者名 | 山田太郎 |
| industry_type | 業種コード | 1=商社・卸売, 2=製造業, 3=建設業... |
| employee_count | 従業員数 | 150 |
| capital | 資本金 | 50000000 |
| established_date | 設立日 | 2010-04-01 |
| address | 住所 | 東京都千代田区丸の内1-1-1 |
| postal_code | 郵便番号 | 100-0001 |
| phone | 電話番号 | 03-1234-5678 |
| email | メールアドレス | info@sample-corp.co.jp |
| status | ステータス | ACTIVE/INACTIVE |

**業種コード一覧**:
- 1: 商社・卸売
- 2: 製造業
- 3: 建設業
- 4: 情報通信業
- 5: 小売業
- 6: 運輸業
- 7: 食品業
- 8: 不動産業
- 9: サービス業
- 10: 医療・福祉
- 11: 教育・出版

### 出力データ1（新顧客テーブル）
**テーブル名**: `new_customers`

| カラム名 | 説明 | 変換元 |
|---------|------|--------|
| id | 顧客ID（自動採番） | - |
| customer_id | 顧客ID | customer_code |
| full_name | 氏名 | customer_name |
| email_address | メールアドレス | email |
| phone_number | 電話番号 | phone |
| full_address | 住所 | address |
| zip_code | 郵便番号 | postal_code |
| registration_date | 登録日 | created_at |
| is_active | 有効フラグ | status（ACTIVE→true） |
| gender | 性別 | gender_code（1→"男性", 2→"女性"） |
| migrated_at | 移行日時 | 現在時刻 |
| source_id | 移行元ID（未使用） | - |

### 出力データ2（新会社テーブル）
**テーブル名**: `new_companies`

| カラム名 | 説明 | 変換元 |
|---------|------|--------|
| id | 会社ID（自動採番） | - |
| company_id | 会社ID | company_code |
| company_name | 会社名 | company_name |
| representative | 代表者 | representative_name |
| industry_category | 業種カテゴリ | industry_type（コード→文字列変換） |
| employees | 従業員数 | employee_count |
| capital_amount | 資本金 | capital |
| foundation_date | 設立日 | established_date |
| office_address | 事業所住所 | address |
| zip_code | 郵便番号 | postal_code |
| contact_phone | 連絡先電話番号 | phone |
| contact_email | 連絡先メール | email |
| is_active | 有効フラグ | status（ACTIVE→true） |
| migrated_at | 移行日時 | 現在時刻 |

## 必要な環境

- Java 17以上
- Maven 3.6以上
- PostgreSQL 13以上（新データベース用）

## データベース準備

PostgreSQLで新データベースを作成してください：

```sql
CREATE DATABASE newdb;
```

データベース接続情報は `DatabaseConfiguration.java` で設定されています：
- URL: `jdbc:postgresql://localhost:5434/newdb`
- ユーザー名: `postgres`
- パスワード: `password`

## TSVファイルの配置

1. 別システムからSQL Serverで抽出したデータをTSV形式で用意
2. 以下のファイルを配置：
   - `src/main/resources/data/old_customers.tsv` - 顧客データ
   - `src/main/resources/data/old_companies.tsv` - 会社データ
3. フォーマットは上記のデータ構造を参照
4. ヘッダー行を含める（1行目はスキップされます）

## ビルド方法

```bash
mvn clean package
```

## 実行方法

**重要**: このアプリケーションはGUI/REST APIから手動でバッチを実行する方式です。
Spring Boot起動時に自動実行はされません（`spring.batch.job.enabled=false`）。

### 方法1: Web UI（最も簡単、推奨）

**1. Spring Bootアプリケーションを起動:**
```bash
mvn spring-boot:run
```

**2. ブラウザでアクセス:**
```
http://localhost:8080
```

**3. GUI画面で「バッチ開始」ボタンをクリック**
- データマスクが必要な場合はチェックボックスをON
- リアルタイムで進捗を確認可能
- ステータス、読込/書込件数が自動更新

### 方法2: Swing GUIアプリケーション

**1. Spring Bootアプリケーションを起動:**
```bash
mvn spring-boot:run
```

**2. 別のターミナルでSwing GUIを起動:**
```bash
RUN_GUI.bat
```

または

```bash
java -cp target/classes com.example.batch.gui.BatchLauncherGUI
```

**3. GUI画面で「バッチ開始」ボタンをクリック**

### 方法3: REST APIで実行

**1. Spring Bootアプリケーションを起動:**
```bash
mvn spring-boot:run
```

**2. curlまたはPostmanでバッチを開始:**

**バッチ開始（データマスクなし）:**
```bash
curl -X POST http://localhost:8080/api/batch/start \
  -H "Content-Type: application/json" \
  -d '{}'
```

**バッチ開始（データマスクあり）:**
```bash
curl -X POST http://localhost:8080/api/batch/start \
  -H "Content-Type: application/json" \
  -d '{"maskingEnabled":"true"}'
```

**ステータス確認:**
```bash
# 実行IDを指定
curl http://localhost:8080/api/batch/status/1

# 最新のジョブ実行状態を取得
curl http://localhost:8080/api/batch/latest
```

### 方法4: コマンドラインから直接実行（GUIなし）

GUIを使わず、バッチを即座に実行したい場合は`spring.batch.job.enabled=true`に変更して起動します。

**テスト環境で実行（データマスク処理有効）:**
```bash
RUN_TEST.bat
```

**本番環境で実行（データマスク処理無効）:**
```bash
RUN_PROD.bat
```

**application.ymlを編集してenabled=trueに変更:**
```yaml
spring:
  batch:
    job:
      enabled: true  # trueに変更
```

その後、通常通り起動:
```bash
mvn spring-boot:run
```

## テストデータ

- `src/main/resources/data/old_customers.tsv` - 20件の顧客データ
- `src/main/resources/data/old_companies.tsv` - 15件の会社データ

これらは別システムから抽出されたデータのサンプルです。

## データ移行の流れ

```
┌─────────────────────────┐
│ 別システム（SQL Server） │
│                         │
│ ← 顧客・会社データ抽出   │
└───────────┬─────────────┘
            │
            │ TSVエクスポート
            ↓
┌─────────────────────────┐
│ old_customers.tsv       │
│ old_companies.tsv       │
│ （タブ区切りテキスト）   │
└───────────┬─────────────┘
            │
            │ Spring Batch
            │ FlatFileItemReader
            ↓
┌─────────────────────────┐
│ Step 1: 顧客移行        │
│ OldCustomerDto          │
│ → CustomerMigration     │
│   Processor             │
│ → NewCustomer           │
└───────────┬─────────────┘
            │
            ↓
┌─────────────────────────┐
│ Step 2: 会社移行        │
│ OldCompanyDto           │
│ → CompanyMigration      │
│   Processor             │
│ → NewCompany            │
└───────────┬─────────────┘
            │
            │ JpaItemWriter
            │ データベース書き込み
            ↓
┌─────────────────────────┐
│ PostgreSQL（newdb）      │
│ - new_customers テーブル │
│ - new_companies テーブル │
└─────────────────────────┘
```

## バッチ処理の実行順序

1. **Step 1: customerMigrationStep** - 顧客データ移行
   - `old_customers.tsv` を読込
   - 顧客データを変換
   - `new_customers` テーブルへ書込

2. **Step 2: companyMigrationStep** - 会社データ移行
   - `old_companies.tsv` を読込
   - 会社データを変換（業種コード→業種名）
   - `new_companies` テーブルへ書込

## 設定のカスタマイズ

### 環境別プロファイル

アプリケーションは3つのプロファイルをサポートしています：

#### 1. デフォルト設定 (`application.yml`)
- データベース: `jdbc:postgresql://localhost:5434/newdb`
- チャンクサイズ: 100件
- スキップ上限: 10件
- **データマスク: 無効**

#### 2. テスト環境 (`application-test.yml`)
- データベース: `jdbc:postgresql://localhost:5434/testdb`
- **データマスク: 有効**
- 詳細ログ出力

#### 3. 本番環境 (`application-prod.yml`)
- データベース: 環境変数から取得（`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`）
- チャンクサイズ: 500件（高速化）
- スキップ上限: 100件
- **データマスク: 無効**
- INFOレベルログ

### データマスク機能

テスト環境でのみ有効化される機密データ保護機能です。

**マスク対象:**
- メールアドレス: `test@example.com` → `te***@example.com`
- 電話番号: `03-1234-5678` → `03-***-5678`
- 住所: `東京都渋谷区1-2-3` → `東京都渋谷区***`
- 郵便番号: `123-4567` → `123-****`

**有効化方法:**
```bash
# プロファイルで制御
RUN_TEST.bat

# または環境変数で制御
set BATCH_MASKING_ENABLED=true
mvn spring-boot:run
```

### 環境変数での設定変更

| 環境変数 | デフォルト値 | 説明 |
|---------|-------------|------|
| `DB_URL` | `jdbc:postgresql://localhost:5434/newdb` | データベースURL |
| `DB_USERNAME` | `postgres` | データベースユーザー名 |
| `DB_PASSWORD` | `password` | データベースパスワード |
| `DB_POOL_SIZE` | `10` | コネクションプール最大サイズ |
| `BATCH_CHUNK_SIZE` | `100` | チャンクサイズ |
| `BATCH_SKIP_LIMIT` | `10` | スキップ上限 |
| `BATCH_MASKING_ENABLED` | `false` | データマスク有効化 |
| `CUSTOMER_FILE` | `classpath:data/old_customers.tsv` | 顧客TSVファイルパス |
| `COMPANY_FILE` | `classpath:data/old_companies.tsv` | 会社TSVファイルパス |
| `BATCH_REPORT_DIR` | `./reports` | 統計レポート出力先 |

**使用例:**
```bash
set DB_URL=jdbc:postgresql://production-db:5432/newdb
set BATCH_CHUNK_SIZE=500
set CUSTOMER_FILE=file:C:/data/customers.tsv
java -jar target/data-migration-batch-1.0.0.jar
```

### TSVファイルパス変更

**クラスパス内のファイルを指定:**
```bash
set CUSTOMER_FILE=classpath:data/old_customers.tsv
```

**外部ファイルを指定:**
```bash
set CUSTOMER_FILE=file:C:/data/old_customers.tsv
set COMPANY_FILE=file:C:/data/old_companies.tsv
```

## 監視・レポート機能

### 1. リアルタイム進捗表示

バッチ実行中、5秒ごとにコンソールへ進捗情報を表示：
```
【進捗】読込: 500 件, 書込: 500 件 | 経過時間: 25 秒 | 速度: 20.0/20.0 件/秒
```

### 2. CSV統計レポート

ジョブ完了時、`./reports/`ディレクトリに統計レポートを自動生成：
- ファイル名: `batch-stats-yyyyMMdd-HHmmss.csv`
- 内容:
  - ジョブ情報（ID、ステータス、実行時間）
  - ステップ別統計（読込/書込/スキップ件数、処理速度）
  - エラー情報
  - サマリー（総件数、成功率）

### 3. Spring Boot Actuator

以下のエンドポイントで監視情報を取得可能：

| エンドポイント | URL | 説明 |
|--------------|-----|------|
| ヘルスチェック | `http://localhost:8080/actuator/health` | アプリケーション稼働状況 |
| メトリクス | `http://localhost:8080/actuator/metrics` | 各種メトリクス情報 |
| バッチ情報 | `http://localhost:8080/actuator/batch` | バッチジョブ実行履歴 |
| Prometheus | `http://localhost:8080/actuator/prometheus` | Prometheus形式メトリクス |

### 4. ログ出力

ジョブ実行時には以下の情報が出力されます：
- ジョブ開始時刻
- ジョブ名
- 読込件数（顧客+会社）
- 書込件数（顧客+会社）
- 処理時間
- スキップ件数（エラー発生時）
- エラー詳細情報（失敗時）

## エラーハンドリング

### スキップ機能

バリデーションエラーやデータ変換エラーが発生した場合、該当レコードをスキップして処理を継続します。

**スキップ上限:** デフォルト10件（`BATCH_SKIP_LIMIT`で変更可能）

**スキップされるケース:**
- メールアドレス形式が不正
- 電話番号形式が不正
- 郵便番号形式が不正
- 必須フィールドが空
- データ変換エラー

**スキップ時のログ出力例:**
```
【読み込みスキップ】エラー: バリデーションエラー - メールアドレスが不正です
【処理スキップ】アイテム: OldCustomerDto(...), エラー: 性別コードが不正です
```

### リトライ機能

一時的なエラー（データベース接続エラーなど）の場合、最大3回まで自動リトライします。

## トラブルシューティング

### TSVファイルが見つからない
```
Error: class path resource [data/old_customers.tsv] cannot be opened
```
**解決方法:**
- ファイルパスが正しいか確認
- ファイルが `src/main/resources/data/` に配置されているか確認
- 外部ファイルの場合は `file:` プレフィックスを使用

### 日付フォーマットエラー
```
DateTimeParseException: Text '2025/12/26 10:00:00' could not be parsed
```
**解決方法:**
- 顧客TSVの `created_at` カラムが `yyyy-MM-dd HH:mm:ss` 形式か確認
- 会社TSVの `established_date` カラムが `yyyy-MM-dd` 形式か確認

### バリデーションエラー
```
【処理スキップ】アイテム: ..., エラー: 顧客データバリデーションエラー...
```
**解決方法:**
- メールアドレス: `user@example.com` 形式か確認
- 電話番号: `03-1234-5678` または `0312345678` 形式か確認
- 郵便番号: `123-4567` 形式か確認
- 性別コード: `1`（男性）または `2`（女性）か確認
- 業種コード: `1`～`11` の整数値か確認

### データベース接続エラー
```
Connection refused: localhost:5434
```
**解決方法:**
- PostgreSQLが起動しているか確認
- ポート番号が正しいか確認（デフォルト: 5434）
- データベース名が存在するか確認（`newdb` または `testdb`）

### スキップ上限超過エラー
```
SkipLimitExceededException: Skip limit of 10 exceeded
```
**解決方法:**
- TSVファイルのデータ品質を確認
- スキップ上限を増やす: `set BATCH_SKIP_LIMIT=100`
- バリデーションエラーの原因を修正

### データマスクが動作しない
**確認事項:**
- テストプロファイルで実行しているか確認: `RUN_TEST.bat`
- または環境変数を設定: `set BATCH_MASKING_ENABLED=true`
- ログに `【マスク処理】` が出力されているか確認

## ライセンス

このプロジェクトはサンプルアプリケーションです。
