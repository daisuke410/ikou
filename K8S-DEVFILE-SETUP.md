# Kubernetes環境でのDevfileセットアップガイド

このプロジェクトは、Kubernetes上で動作する開発環境（Eclipse Che、OpenShift Dev Spaces、CodeReady Workspacesなど）でJava 21を使用したSpring Bootアプリケーションとして実行できます。

## 前提条件

- Kubernetes クラスター（社内k8sサーバ）
- Eclipse Che、OpenShift Dev Spaces、またはCodeReady Workspacesがインストールされている
- Devfile v2.2.0をサポートする開発環境
- 必要なコンテナイメージへのアクセス権限

## 使用するコンテナイメージ

デフォルトでは以下の公開イメージを使用します：

- **Java開発環境**: `eclipse-temurin:21-jdk`
- **データベース**: `postgres:15-alpine`

### 社内コンテナレジストリを使用する場合

`devfile.yml` の `image:` セクションを社内レジストリのURLに変更してください：

```yaml
components:
  - name: tools
    container:
      image: your-registry.company.com/eclipse-temurin:21-jdk

  - name: postgres
    container:
      image: your-registry.company.com/postgres:15-alpine
```

## 設定ファイル

### `devfile.yml`

Devfile v2.2.0形式の設定ファイルで、以下を定義しています：

#### コンポーネント

1. **tools** - Java 21開発環境
   - メモリ: 2Gi (リクエスト: 1Gi)
   - CPU: 2000m (リクエスト: 1000m)
   - Java 21 (Eclipse Temurin)
   - Maven (含まれている)

2. **postgres** - PostgreSQL 15データベース
   - メモリ: 512Mi (リクエスト: 256Mi)
   - CPU: 500m (リクエスト: 200m)
   - データベース名: `migration_db`
   - ユーザー名: `postgres`
   - パスワード: `postgres`

#### エンドポイント

| エンドポイント | ポート | 公開 | 用途 |
|---------------|--------|------|------|
| spring-boot | 8080 | Public | Spring Bootアプリケーション |
| debug | 5005 | Internal | Javaリモートデバッグ |
| postgres | 5432 | Internal | PostgreSQLデータベース |

#### 利用可能なコマンド

| コマンドID | 説明 | グループ |
|-----------|------|---------|
| `init-workspace` | ワークスペースの初期化と依存関係のダウンロード | build |
| `maven-build` | アプリケーションのビルド（テストスキップ） | build (default) |
| `maven-build-with-tests` | 完全ビルド（テスト含む） | build |
| `maven-test` | テストの実行 | test (default) |
| `spring-boot-run` | アプリケーションの起動 | run (default) |
| `spring-boot-debug` | デバッグモードでの起動 | debug (default) |
| `run-jar` | JARファイルから起動 | run |
| `maven-clean` | クリーン | - |
| `maven-update-dependencies` | 依存関係の更新 | - |
| `check-postgres` | PostgreSQL接続確認 | - |
| `show-logs` | ログの表示 | - |

## ワークスペースの作成と起動

### Eclipse Cheの場合

1. Eclipse Cheのダッシュボードにアクセス
2. 「Workspaces」→「Create Workspace」
3. GitリポジトリのURLを入力
4. devfile.ymlが自動検出されます
5. 「Create & Open」をクリック

### OpenShift Dev Spacesの場合

1. Dev Spacesのダッシュボードにアクセス
2. 「Create Workspace」
3. GitリポジトリのURLを入力、またはdevfile.ymlをアップロード
4. ワークスペースが自動的に作成・起動されます

### URLから直接起動

```
https://your-che-server.company.com/#https://github.com/your-org/ikou
```

## 開発ワークフロー

### 1. ワークスペースの起動

ワークスペースが起動すると、`postStart` イベントにより自動的に以下が実行されます：

- Javaバージョンの確認
- Mavenバージョンの確認
- Maven依存関係のダウンロード

### 2. PostgreSQLの接続確認

```bash
# コマンドパレットから "check-postgres" を実行
# または以下のコマンドを実行
pg_isready -h localhost -p 5432 -U postgres
```

### 3. アプリケーションのビルド

```bash
# コマンドパレットから "maven-build" を実行
# または
mvn clean package -DskipTests
```

### 4. アプリケーションの実行

```bash
# コマンドパレットから "spring-boot-run" を実行
# または
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

アプリケーションは `http://localhost:8080` で利用可能になります。

### 5. デバッグ

```bash
# コマンドパレットから "spring-boot-debug" を実行
```

その後、IDEのデバッガを `localhost:5005` にアタッチします。

### 6. テストの実行

```bash
# コマンドパレットから "maven-test" を実行
# または
mvn test
```

## データベース接続情報

ワークスペース内でのデータベース接続設定：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/migration_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

## 環境変数

以下の環境変数が設定されています：

| 変数名 | 値 | 説明 |
|--------|-----|------|
| `JAVA_HOME` | `/opt/java/openjdk` | Java インストールパス |
| `MAVEN_OPTS` | `-Xmx1024m -XX:+UseContainerSupport` | Mavenメモリ設定 |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring Bootプロファイル |
| `POSTGRES_USER` | `postgres` | PostgreSQLユーザー名 |
| `POSTGRES_PASSWORD` | `postgres` | PostgreSQLパスワード |
| `POSTGRES_DB` | `migration_db` | PostgreSQLデータベース名 |

## ボリュームとデータ永続化

以下のボリュームが定義されています：

- **m2** (3Gi): Mavenローカルリポジトリ（依存関係キャッシュ）
- **postgres-storage** (1Gi): PostgreSQLデータ永続化

ワークスペースを停止しても、これらのボリュームのデータは保持されます。

## トラブルシューティング

### PostgreSQLに接続できない

```bash
# PostgreSQLコンテナの状態を確認
kubectl get pods -n your-namespace

# PostgreSQLのログを確認
kubectl logs <postgres-pod-name> -n your-namespace
```

### Java バージョンの確認

```bash
java -version
# 出力例: openjdk version "21.0.1" 2023-10-17
```

### Maven バージョンの確認

```bash
mvn -version
# 出力例: Apache Maven 3.9.x
```

### メモリ不足エラー

`devfile.yml` のメモリ設定を調整してください：

```yaml
components:
  - name: tools
    container:
      memoryLimit: 3Gi  # 2Gi から増加
      memoryRequest: 2Gi  # 1Gi から増加
```

### ビルドが遅い

Mavenの依存関係がキャッシュされているか確認：

```bash
# 依存関係を強制的に再ダウンロード
mvn dependency:resolve -U
```

## リソース要件

### 最小要件

- CPU: 2.5コア (tools: 2コア, postgres: 0.5コア)
- メモリ: 2.5Gi (tools: 2Gi, postgres: 0.5Gi)
- ストレージ: 4Gi (m2: 3Gi, postgres: 1Gi)

### 推奨要件

- CPU: 3コア以上
- メモリ: 3Gi以上
- ストレージ: 5Gi以上

## セキュリティに関する注意事項

### 本番環境への適用

このdevfile.ymlは開発環境用です。本番環境では以下を変更してください：

1. **データベース認証情報**: ハードコードされたパスワードを使用しない
   - Kubernetes Secretsを使用
   - 環境変数として注入

2. **イメージのバージョン**: 具体的なバージョンタグを指定
   ```yaml
   image: postgres:15.3-alpine  # 'latest' や '15-alpine' ではなく
   ```

3. **リソース制限**: 適切な制限を設定

### シークレットの使用例

```yaml
components:
  - name: postgres
    container:
      image: postgres:15-alpine
      env:
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
```

## カスタマイズ

### Spring Bootプロファイルの変更

```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: your-profile  # dev, test, prod など
```

### Javaメモリ設定の調整

```yaml
env:
  - name: MAVEN_OPTS
    value: -Xmx2048m -XX:+UseContainerSupport  # メモリを増やす
```

### PostgreSQLバージョンの変更

```yaml
- name: postgres
  container:
    image: postgres:16-alpine  # バージョンを変更
```

## サポートとドキュメント

- [Devfile v2.2.0 仕様](https://devfile.io/docs/2.2.0/)
- [Eclipse Che ドキュメント](https://www.eclipse.org/che/docs/)
- [OpenShift Dev Spaces](https://developers.redhat.com/products/openshift-dev-spaces)
- [Spring Boot ドキュメント](https://spring.io/projects/spring-boot)

## よくある質問

### Q: ワークスペースはどのくらいの期間保持されますか？

A: 社内k8sクラスターの設定によります。通常、非アクティブ期間（例: 8時間）を過ぎると自動停止しますが、データは保持されます。

### Q: 複数のワークスペースを同時に実行できますか？

A: はい、リソースが許す限り可能です。ただし、各ワークスペースは独立したリソースを消費します。

### Q: 社内プロキシを使用している場合は？

A: `devfile.yml` に環境変数を追加してください：

```yaml
env:
  - name: HTTP_PROXY
    value: http://proxy.company.com:8080
  - name: HTTPS_PROXY
    value: http://proxy.company.com:8080
  - name: NO_PROXY
    value: localhost,127.0.0.1,.company.com
```

### Q: カスタムMaven設定を使用したい

A: `settings.xml` をプロジェクトルートに配置し、以下のコマンドを使用：

```bash
mvn -s settings.xml clean package
```

または、devfile.ymlのコマンドを修正：

```yaml
- id: maven-build
  exec:
    commandLine: mvn -s settings.xml clean package -DskipTests
```

## 問い合わせ

問題が発生した場合は、社内のDevOpsチームまたはプロジェクトのIssueセクションで報告してください。
