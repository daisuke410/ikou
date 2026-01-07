# Gitpod セットアップガイド

このプロジェクトはGitpodでJava 21を使用したSpring Bootアプリケーションとして実行できます。

## 前提条件

- Gitpodアカウント（https://gitpod.io でサインアップ）
- GitHubリポジトリへのアクセス

## Gitpodでの起動

### 方法1: URLから直接起動

リポジトリのURLの前に `gitpod.io/#` を追加してアクセス：

```
https://gitpod.io/#https://github.com/your-username/ikou
```

### 方法2: Gitpodボタンから起動

GitHubリポジトリページから「Open in Gitpod」ボタンをクリック

## 設定ファイル

このプロジェクトには以下の設定ファイルが含まれています：

### `.devfile.yaml`
- Devfile v2.2.0形式の設定
- Java 21 (Eclipse Temurin) コンテナ
- PostgreSQL 15データベース
- 利用可能なコマンド：
  - `maven-build`: アプリケーションのビルド
  - `maven-test`: テストの実行
  - `spring-boot-run`: アプリケーションの起動
  - `spring-boot-debug`: デバッグモードでの起動

### `.gitpod.yml`
- Gitpod固有の設定
- ポート設定（8080: Spring Boot、5432: PostgreSQL）
- タスク設定（PostgreSQLの自動起動とSpring Bootアプリケーションの起動）

### `.gitpod.Dockerfile`
- カスタムDockerイメージ
- Java 21のインストール
- Maven 3.9.6のインストール
- PostgreSQLクライアントツールのインストール

## 利用可能なポート

| ポート | 用途 | 公開設定 |
|--------|------|----------|
| 8080 | Spring Boot Application | Public |
| 5005 | Java Debug Port | Internal |
| 5432 | PostgreSQL Database | Internal |

## 開発ワークフロー

### 1. ワークスペースの起動

Gitpodワークスペースが起動すると、自動的に以下が実行されます：

1. Maven依存関係のダウンロード
2. PostgreSQLデータベースの起動
3. Spring Bootアプリケーションの起動

### 2. アプリケーションのビルド

```bash
mvn clean package
```

または、Devfileコマンドを使用：

```bash
# Gitpodコマンドパレット（Ctrl+Shift+P）から
# "Tasks: Run Task" → "maven-build"
```

### 3. アプリケーションの実行

```bash
mvn spring-boot:run
```

### 4. デバッグ

デバッグモードで起動：

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

その後、VSCodeのデバッガをポート5005にアタッチします。

### 5. テストの実行

```bash
mvn test
```

## データベース接続情報

Gitpod環境でのデータベース接続設定：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/migration_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## トラブルシューティング

### PostgreSQLが起動しない場合

```bash
sudo service postgresql start
sudo -u postgres psql -c "CREATE USER postgres WITH PASSWORD 'postgres' SUPERUSER;"
sudo -u postgres createdb migration_db
```

### Java バージョンの確認

```bash
java -version
# 出力: openjdk version "21.0.1"
```

### Maven バージョンの確認

```bash
mvn -version
# 出力: Apache Maven 3.9.6
```

## VS Code 拡張機能

以下の拡張機能が自動的にインストールされます：

- Java Extension Pack
- Spring Boot Extension Pack
- Spring Boot Dashboard
- Maven for Java

## 環境変数

以下の環境変数が設定されています：

- `JAVA_HOME`: Java 21のインストールパス
- `MAVEN_OPTS`: `-Xmx1024m`（Mavenのメモリ設定）

## リソース制限

- メモリ: 2Gi（リクエスト: 1Gi）
- CPU: 2000m（リクエスト: 1000m）

## 注意事項

- ワークスペースは一定時間非アクティブ状態が続くと自動的に停止します
- データベースのデータは永続化されないため、ワークスペース再起動時は初期状態に戻ります
- 重要なデータは必ずGitリポジトリにコミットしてください

## サポート

問題が発生した場合は、プロジェクトのIssueセクションで報告してください。
