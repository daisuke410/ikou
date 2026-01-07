# GitLab と Eclipse Che の統合ガイド

このドキュメントでは、GitLabとEclipse Cheを統合した開発環境での作業方法について説明します。

## 概要

社内環境では、GitLabリポジトリから直接Eclipse Cheワークスペースを起動できるように統合されています。これにより、ブラウザだけで完全な開発環境にアクセスできます。

## 統合のメリット

1. **環境構築不要**: ローカルPCにJavaやMavenをインストールする必要がありません
2. **一貫性**: devfile.ymlで定義された統一された開発環境
3. **迅速な開始**: GitLabから数クリックで開発環境が起動
4. **コラボレーション**: チーム全員が同じ環境で作業
5. **リソース管理**: 社内k8sクラスターでリソースを効率的に利用

## GitLabからEclipse Cheを起動する方法

### 方法1: GitLab UIから起動

1. **GitLabプロジェクトページにアクセス**
   ```
   https://gitlab.company.com/your-group/ikou
   ```

2. **「Web IDE」または「Eclipse Che」ボタンをクリック**
   - プロジェクトのトップページまたはファイルブラウザに表示されます
   - ボタン名は管理者の設定により異なる場合があります

3. **自動的にワークスペースが作成・起動**
   - プロジェクトのクローン
   - devfile.ymlの検出と適用
   - コンテナの起動（Java 21環境とPostgreSQL）
   - 依存関係のダウンロード

4. **開発開始**
   - IDEが起動したら、すぐにコーディングを開始できます

### 方法2: URLから直接起動

GitLabリポジトリURLを使用してEclipse Cheを起動：

#### 基本形式
```
https://your-che-server.company.com/#https://gitlab.company.com/your-group/ikou
```

#### 特定のブランチで起動
```
https://your-che-server.company.com/#https://gitlab.company.com/your-group/ikou?branch=feature/new-feature
```

#### 特定のコミットで起動
```
https://your-che-server.company.com/#https://gitlab.company.com/your-group/ikou?commit=a1b2c3d4
```

#### マージリクエストのブランチで起動
```
https://your-che-server.company.com/#https://gitlab.company.com/your-group/ikou?branch=mr/123-feature-name
```

### 方法3: Eclipse Cheダッシュボードから

1. Eclipse Cheダッシュボードにアクセス
   ```
   https://your-che-server.company.com
   ```

2. 「Create Workspace」をクリック

3. GitLabリポジトリURLを入力
   ```
   https://gitlab.company.com/your-group/ikou
   ```

4. 「Create & Open」をクリック

## 認証とアクセス

### GitLab OAuth連携（推奨）

管理者がGitLabとEclipse CheのOAuth連携を設定済みの場合：

1. 初回アクセス時にGitLabの認証画面が表示されます
2. GitLabにログイン（またはすでにログイン済み）
3. Eclipse Cheへのアクセスを承認
4. 以降は自動的に認証されます

### 個人アクセストークン方式

OAuth連携が設定されていない場合：

1. **GitLabで個人アクセストークンを作成**
   - GitLab > Settings > Access Tokens
   - Token name: `Eclipse Che`
   - Scopes: `api`, `read_repository`, `write_repository`, `read_user`
   - Create personal access token
   - トークンをコピー（一度しか表示されません）

2. **Eclipse Cheに設定**
   - Eclipse Che > User Settings > Git Providers
   - 「Add Provider」をクリック
   - Provider Type: GitLab
   - Provider URL: `https://gitlab.company.com`
   - Personal Access Token: 先ほどコピーしたトークンを貼り付け
   - Save

### SSH鍵方式

1. **Eclipse Cheワークスペース内でSSH鍵を生成**
   ```bash
   ssh-keygen -t ed25519 -C "your.email@company.com"
   cat ~/.ssh/id_ed25519.pub
   ```

2. **GitLabに公開鍵を追加**
   - GitLab > Settings > SSH Keys
   - 公開鍵を貼り付けて保存

3. **GitのリモートURLをSSH形式に変更**
   ```bash
   git remote set-url origin git@gitlab.company.com:your-group/ikou.git
   ```

## 開発ワークフロー

### 1. ワークスペースの起動

```
GitLab UI → 「Eclipse Che」ボタン → ワークスペース起動 (約1-2分)
```

### 2. ブランチの作成と作業

```bash
# 新しいブランチを作成
git checkout -b feature/my-new-feature

# コードを編集...

# 変更をステージング
git add .

# コミット
git commit -m "Add new feature"

# GitLabにプッシュ
git push origin feature/my-new-feature
```

### 3. マージリクエストの作成

Eclipse Cheから直接マージリクエストを作成することはできませんが、以下の方法があります：

**方法A: GitLab UIから作成**
1. ワークスペースからGitLabのプロジェクトページを開く
2. 「Create merge request」ボタンをクリック
3. マージリクエストの詳細を入力して作成

**方法B: CLIから作成（GitLab CLIがインストールされている場合）**
```bash
# GitLab CLIを使用してマージリクエストを作成
glab mr create --title "My feature" --description "Description of changes"
```

### 4. レビューと修正

1. チームメンバーがGitLabでレビュー
2. 修正が必要な場合、同じワークスペースで修正
3. 追加のコミットをプッシュ
4. マージリクエストに自動的に反映

### 5. マージとデプロイ

1. マージリクエストが承認されたら、GitLabでマージ
2. GitLab CI/CDパイプラインが自動実行
3. テストが成功したら自動または手動でデプロイ

## GitLab CI/CDとの連携

### devfile.ymlとの一貫性

`.gitlab-ci.yml.example`ファイルには、devfile.ymlと同じビルド環境を使用するCI/CD設定のサンプルが含まれています。

**開発環境（Eclipse Che）**
```yaml
# devfile.yml
- id: maven-build
  exec:
    commandLine: mvn clean package -DskipTests
```

**CI/CD環境（GitLab CI）**
```yaml
# .gitlab-ci.yml
build:
  script:
    - mvn clean package -DskipTests
```

同じコマンドを使用することで、「ローカルでは動くがCIで失敗する」という問題を防げます。

### パイプラインのセットアップ

1. **サンプルファイルをリネーム**
   ```bash
   mv .gitlab-ci.yml.example .gitlab-ci.yml
   ```

2. **必要に応じてカスタマイズ**
   - デプロイ先の設定
   - 通知設定
   - スケジュール設定

3. **コミットしてプッシュ**
   ```bash
   git add .gitlab-ci.yml
   git commit -m "Add GitLab CI/CD configuration"
   git push origin main
   ```

4. **GitLabでパイプラインを確認**
   - GitLab > CI/CD > Pipelines
   - 自動的にパイプラインが実行されます

### CI/CDで使用されるステージ

| ステージ | 内容 | 実行タイミング |
|---------|------|---------------|
| build | アプリケーションのコンパイル | 全てのコミット |
| test | ユニットテストとコード品質チェック | 全てのコミット |
| package | JARファイルの作成 | main/developブランチ |
| deploy | 環境へのデプロイ | mainブランチ、タグ |

## Eclipse Cheでの作業のベストプラクティス

### 1. ワークスペースの管理

- **使用後は停止**: リソースを節約するため、作業終了後はワークスペースを停止
- **定期的な再作成**: devfile.ymlを更新した場合は、ワークスペースを再作成
- **複数ワークスペース**: ブランチごとに別のワークスペースを作成可能

### 2. データの永続化

- **コミットを頻繁に**: ワークスペースが削除されても、コミット済みのコードは安全
- **ローカルファイル注意**: `/projects`以外のファイルはワークスペース削除時に失われます
- **設定ファイル**: IDEの設定はworkspace設定として保存されます

### 3. パフォーマンスの最適化

- **Mavenキャッシュ**: 自動的にキャッシュされ、ビルド時間を短縮
- **バックグラウンド処理**: ビルド中も他のタスクを実行可能
- **リソース制限**: devfile.ymlで適切なリソース制限を設定

## トラブルシューティング

### ワークスペースが起動しない

**原因1: リソース不足**
```
解決策: 管理者に連絡して、k8sクラスターのリソースを確認
```

**原因2: devfile.ymlの構文エラー**
```
解決策: devfile.ymlの構文を確認、YAMLバリデーターを使用
```

**原因3: コンテナイメージへのアクセス権限がない**
```
解決策: 社内レジストリのイメージを使用するようdevfile.ymlを修正
```

### GitLabにプッシュできない

**原因1: 認証情報が設定されていない**
```
解決策: 個人アクセストークンまたはSSH鍵を設定
```

**原因2: ブランチ保護ルール**
```
解決策: 別のブランチ名を使用、または管理者に権限を確認
```

**原因3: ネットワーク問題**
```
解決策: プロキシ設定を確認、ネットワーク管理者に連絡
```

### PostgreSQLに接続できない

**原因1: PostgreSQLコンテナが起動していない**
```bash
# 確認方法
curl http://localhost:5432

# 解決策
# ワークスペースを再起動
```

**原因2: 接続情報が間違っている**
```
解決策: application.propertiesで以下を確認
spring.datasource.url=jdbc:postgresql://localhost:5432/migration_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## セキュリティのベストプラクティス

### 1. 認証情報の管理

- **コミットしない**: パスワードやトークンをコードに含めない
- **環境変数使用**: 機密情報は環境変数で管理
- **GitLab CI/CD Variables**: パイプラインで必要な認証情報はGitLabの変数機能を使用

### 2. アクセス制御

- **最小権限の原則**: 必要最小限の権限でトークンを作成
- **定期的な更新**: 個人アクセストークンを定期的に更新
- **共有しない**: 認証情報を他の開発者と共有しない

### 3. コードの保護

- **ブランチ保護**: mainブランチへの直接プッシュを禁止
- **マージリクエスト必須**: コードレビューを経てからマージ
- **自動テスト**: CI/CDでテストを自動実行

## よくある質問

### Q: Eclipse Cheワークスペースはどのくらい保持されますか？

A: 社内k8sクラスターの設定によりますが、通常：
- アクティブなワークスペース: 無期限
- 非アクティブ（例: 8時間アクセスなし）: 自動停止（データは保持）
- 長期間未使用（例: 30日）: 削除される可能性あり

### Q: ローカル開発とEclipse Cheのどちらを使うべきですか？

A: 状況に応じて使い分けてください：

**Eclipse Cheが適している場合:**
- 新しいプロジェクトや機能の調査
- 一時的な作業
- 環境構築が不要な場合
- 複数のブランチを同時に作業

**ローカル開発が適している場合:**
- 長期的な開発作業
- オフラインでの作業が必要
- IDE固有の機能が必要
- ネットワーク遅延が気になる

### Q: Eclipse Cheで使用するIDEは何ですか？

A: Eclipse Che 7以降は、Theia IDEまたはVS Code（ブラウザ版）を使用します。
UIはVS Codeと非常に似ており、多くの拡張機能が利用可能です。

### Q: devfile.ymlを変更したら、既存のワークスペースはどうなりますか？

A: 既存のワークスペースには自動的に反映されません。以下の手順が必要です：

1. 現在のワークスペースで作業中の変更をコミット・プッシュ
2. ワークスペースを停止
3. ワークスペースを削除
4. GitLabから新しいワークスペースを作成

### Q: GitLab CI/CDとEclipse Cheで異なる結果になります

A: 考えられる原因：

1. **環境の違い**: devfile.ymlと.gitlab-ci.ymlで異なるイメージを使用
2. **依存関係の違い**: キャッシュの状態が異なる
3. **環境変数の違い**: 環境変数の設定を確認

解決策: 両方で同じDockerイメージと設定を使用してください。

## 関連ドキュメント

- [K8S-DEVFILE-SETUP.md](./K8S-DEVFILE-SETUP.md) - Kubernetes環境でのDevfileセットアップ
- [devfile.yml](./devfile.yml) - プロジェクトのDevfile設定
- [.gitlab-ci.yml.example](./.gitlab-ci.yml.example) - GitLab CI/CD設定サンプル
- [README.md](./README.md) - プロジェクトの概要

## サポート

問題が発生した場合：

1. このドキュメントのトラブルシューティングセクションを確認
2. K8S-DEVFILE-SETUP.mdのFAQを確認
3. 社内のDevOpsチームに連絡
4. GitLabプロジェクトのIssueセクションで報告
