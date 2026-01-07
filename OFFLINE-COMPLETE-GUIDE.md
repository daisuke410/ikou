# Playwright 完全オフライン実行ガイド

## 📦 概要

このガイドでは、**インターネット接続が全くない環境**でもPlaywrightテストを実行できる完全なオフラインパッケージの作成と使用方法について説明します。

Node.jsポータブル版を含むため、**オフライン環境にNode.jsがインストールされていなくても実行可能**です。

---

## 🎯 2つのアプローチ

### アプローチ1: 完全オフラインパッケージ（推奨）

**特徴:**
- Node.jsポータブル版を含む
- オフライン環境にNode.jsのインストール不要
- 完全に自己完結型
- ファイルサイズが大きい（約500MB～1GB）

**適用ケース:**
- Node.jsがインストールできない環境
- セキュリティ制限が厳しい環境
- 管理者権限がない環境

### アプローチ2: 軽量オフラインパッケージ

**特徴:**
- Node.jsはオフライン環境に既にインストール済みと想定
- ファイルサイズが小さい（約300MB～500MB）
- 既存の`setup-offline.bat`を使用

**適用ケース:**
- Node.jsが既にインストールされている環境
- ファイル転送サイズを小さくしたい場合

---

## 🚀 アプローチ1: 完全オフラインパッケージの作成手順

### ステップ1: オンライン環境での準備

#### 1.1 依存関係のインストール

```batch
# Node.jsとnpmがインストール済みであることを確認
node --version
npm --version

# プロジェクトの依存関係をインストール
npm install

# Playwrightブラウザをインストール
npx playwright install
```

#### 1.2 Node.jsポータブル版のダウンロード

```batch
# Node.jsポータブル版をダウンロード
download-nodejs.bat
```

このスクリプトは以下を実行します:
- Node.js v18.20.5 (Windows x64) をダウンロード
- `dist\nodejs\` に展開
- 既にダウンロード済みの場合はスキップ

#### 1.3 完全オフラインパッケージのビルド

```batch
# 完全なオフラインパッケージを作成
build-offline-complete.bat
```

このスクリプトは以下を実行します:
1. Node.jsポータブル版をコピー
2. すべての`node_modules`をコピー
3. Playwrightブラウザをコピー
4. テストケースと設定ファイルをコピー
5. 実行スクリプトを作成
6. `dist\offline-complete\`にパッケージを作成

#### 1.4 ZIPファイルの作成（オプション）

```batch
# ZIPファイルを作成
create-offline-zip.bat
```

これにより、`dist\playwright-offline-complete-YYYYMMDD.zip`が作成されます。

### ステップ2: オフライン環境への転送

#### 方法A: ZIPファイルで転送（推奨）

```batch
# オンライン環境
1. dist\playwright-offline-complete-YYYYMMDD.zip をUSBメモリなどにコピー

# オフライン環境
2. ZIPファイルを任意の場所にコピー（例: C:\playwright-tests）
3. ZIPファイルを解凍
```

#### 方法B: ディレクトリごとコピー

```batch
# オンライン環境
1. dist\offline-complete\ フォルダをUSBメモリなどにコピー

# オフライン環境
2. フォルダを任意の場所にコピー（例: C:\playwright-tests）
```

### ステップ3: オフライン環境での実行

#### 3.1 基本的な実行

```batch
# ディレクトリに移動
cd C:\playwright-tests

# すべてのテストを実行
run-tests.bat

# UIモードで実行
run-tests-ui.bat

# レポートを表示
show-report.bat
```

#### 3.2 詳細なオプション

```batch
# 特定のテストファイルのみ実行
run-tests.bat tests\theme-selection.spec.ts

# 特定のブラウザで実行
run-tests.bat --project=chromium
run-tests.bat --project=firefox
run-tests.bat --project=webkit

# ヘッドモードで実行（ブラウザを表示）
run-tests.bat --headed

# デバッグモード
run-tests.bat --debug

# 並列実行数を指定
run-tests.bat --workers=4
```

---

## 🚀 アプローチ2: 軽量オフラインパッケージの作成手順

### ステップ1: オンライン環境での準備

```batch
# 依存関係をインストール
npm install

# Playwrightブラウザをインストール
npx playwright install

# オフラインパッケージを作成
setup-offline.bat

# ZIPファイルを作成（オプション）
package-for-offline.bat
```

### ステップ2: オフライン環境での実行

```batch
# 前提条件: Node.js 18以上がインストール済み
node --version

# ディレクトリに移動
cd C:\playwright-tests

# 環境変数を設定
set PLAYWRIGHT_BROWSERS_PATH=%CD%\.playwright

# テストを実行
npx playwright test
```

---

## 📁 完全オフラインパッケージの構造

```
playwright-offline-complete/
├── nodejs/                      # Node.jsポータブル版
│   ├── node.exe                 # Node.js実行ファイル
│   ├── npm.cmd                  # npmコマンド
│   ├── npx.cmd                  # npxコマンド
│   └── node_modules/            # Node.js組み込みモジュール
├── node_modules/                # プロジェクトの依存関係
│   ├── @playwright/             # Playwrightパッケージ
│   ├── playwright/              # Playwrightコア
│   └── ...                      # その他の依存関係
├── .playwright/                 # Playwrightブラウザ
│   ├── chromium-*/              # Chromiumブラウザ
│   ├── firefox-*/               # Firefoxブラウザ
│   └── webkit-*/                # WebKitブラウザ
├── tests/                       # テストケース
│   ├── theme-selection.spec.ts
│   ├── dark-theme.spec.ts
│   ├── api-endpoints.spec.ts
│   ├── cross-theme.spec.ts
│   └── e2e.spec.ts
├── scripts/                     # 内部スクリプト
│   ├── run-offline.bat
│   ├── run-offline-ui.bat
│   └── show-report-offline.bat
├── playwright.config.ts         # Playwright設定
├── package.json                 # パッケージ情報
├── run-tests.bat                # テスト実行（メイン）
├── run-tests-ui.bat             # UIモード実行
├── show-report.bat              # レポート表示
└── README.md                    # 使用方法
```

---

## 🔧 スクリプト詳細

### オンライン環境用スクリプト

| スクリプト名 | 説明 |
|------------|------|
| `download-nodejs.bat` | Node.jsポータブル版をダウンロード |
| `build-offline-complete.bat` | 完全オフラインパッケージをビルド |
| `create-offline-zip.bat` | オフラインパッケージをZIP化 |
| `setup-offline.bat` | 軽量オフラインパッケージを作成 |
| `package-for-offline.bat` | 軽量版をZIP化 |

### オフライン環境用スクリプト（完全版）

| スクリプト名 | 説明 |
|------------|------|
| `run-tests.bat` | テストを実行（ポータブルNode.js使用） |
| `run-tests-ui.bat` | UIモードで実行 |
| `show-report.bat` | HTMLレポートを表示 |
| `scripts/run-offline.bat` | 内部実行スクリプト（詳細版） |

---

## 💡 使用例

### 例1: 基本的なテスト実行

```batch
# オフライン環境
cd C:\playwright-tests
run-tests.bat
```

### 例2: 特定のテーマのみテスト

```batch
run-tests.bat tests\dark-theme.spec.ts
```

### 例3: Chromiumのみでテスト

```batch
run-tests.bat --project=chromium
```

### 例4: UIモードでインタラクティブにテスト

```batch
run-tests-ui.bat
```

### 例5: ヘッドモードでブラウザを見ながらテスト

```batch
run-tests.bat --headed
```

### 例6: テスト結果のレポートを表示

```batch
# テスト実行後
show-report.bat
```

---

## 🔍 トラブルシューティング

### 問題1: Node.jsが見つからない

**エラーメッセージ:**
```
❌ エラー: Node.jsポータブル版が見つかりません
```

**解決方法:**
1. パッケージが正しく解凍されているか確認
2. `nodejs\node.exe`が存在するか確認
3. 必要に応じて`build-offline-complete.bat`を再実行

### 問題2: Playwrightブラウザが見つからない

**エラーメッセージ:**
```
⚠️ 警告: Playwrightブラウザが見つかりません
```

**解決方法:**
1. `.playwright`ディレクトリが存在するか確認
2. オンライン環境で`npx playwright install`を実行
3. `build-offline-complete.bat`を再実行

### 問題3: テストが失敗する

**確認事項:**
1. Spring Bootアプリケーションが起動しているか確認
   ```batch
   # 別のコマンドプロンプトで
   curl http://localhost:8080/actuator/health
   ```
2. ポート8080が使用可能か確認
3. `playwright.config.ts`の`baseURL`設定を確認

### 問題4: 権限エラー

**解決方法:**
1. 管理者権限でコマンドプロンプトを起動
2. ディレクトリのアクセス権限を確認
3. アンチウイルスソフトを一時的に無効化（必要な場合）

### 問題5: スクリプトが実行できない

**解決方法:**
1. 実行ポリシーを確認（PowerShellの場合）
2. `.bat`ファイルが正しく作成されているか確認
3. 改行コードがCRLFになっているか確認

---

## 📊 パッケージサイズの目安

| コンポーネント | サイズ |
|--------------|--------|
| Node.jsポータブル版 | 約50MB |
| node_modules | 約200MB～300MB |
| Playwrightブラウザ | 約300MB～500MB |
| テストケース | 1MB以下 |
| **合計** | **約550MB～850MB** |

ZIPファイル圧縮後: 約300MB～500MB

---

## 🔄 更新とメンテナンス

### オフラインパッケージの更新

1. オンライン環境で変更を加える
2. `build-offline-complete.bat`を再実行
3. 新しいZIPファイルをオフライン環境に転送

### Node.jsバージョンの変更

`download-nodejs.bat`の以下の行を編集:

```batch
set NODE_VERSION=18.20.5  # 変更したいバージョン
```

### Playwrightバージョンの更新

```batch
# package.jsonを編集
# "@playwright/test": "^1.XX.X" に変更

# 依存関係を再インストール
npm install

# ブラウザを再インストール
npx playwright install

# パッケージを再ビルド
build-offline-complete.bat
```

---

## 🎯 ベストプラクティス

### 1. 定期的なパッケージ更新

```batch
# 月1回程度の頻度で
1. npm update
2. npx playwright install
3. build-offline-complete.bat
4. オフライン環境に転送
```

### 2. バージョン管理

```batch
# ZIPファイル名に日付を含める
playwright-offline-complete-20260104.zip
```

### 3. バックアップ

```batch
# 古いバージョンを保持
dist\
├── playwright-offline-complete-20260104.zip  # 最新
└── playwright-offline-complete-20251201.zip  # バックアップ
```

### 4. CI/CD統合

```batch
# バッチファイルから呼び出し
call run-tests.bat
if %ERRORLEVEL% NEQ 0 (
    echo テストが失敗しました
    exit /b 1
)
```

---

## 📞 サポート

### 問題報告時に含める情報

1. **エラーメッセージ:** 完全なエラー出力
2. **実行コマンド:** 実行したコマンド
3. **環境情報:**
   ```batch
   # Node.jsバージョン
   nodejs\node.exe --version

   # Playwrightバージョン
   type package.json | find "@playwright/test"

   # Windowsバージョン
   ver
   ```
4. **ディレクトリ構造:**
   ```batch
   dir /B
   ```
5. **テストレポート:** `playwright-report\`の内容

---

## 📄 FAQ

### Q1: オフライン環境でブラウザを更新できますか？

A: いいえ。ブラウザの更新はオンライン環境で`npx playwright install`を実行し、パッケージを再ビルドする必要があります。

### Q2: 複数のマシンで使用できますか？

A: はい。パッケージを各マシンにコピーすれば使用可能です。

### Q3: Linuxでも動作しますか？

A: 現在のスクリプトはWindows用です。Linux版を作成する場合は、`download-nodejs.bat`でLinux版Node.jsをダウンロードし、`.sh`スクリプトを作成してください。

### Q4: ディスク容量はどれくらい必要ですか？

A: 最低1GB、余裕を持って2GB以上を推奨します。

### Q5: Node.jsバージョンを変更できますか？

A: はい。`download-nodejs.bat`の`NODE_VERSION`変数を変更してください。

---

## 📝 チェックリスト

### オンライン環境での準備

- [ ] Node.jsとnpmがインストール済み
- [ ] `npm install`を実行
- [ ] `npx playwright install`を実行
- [ ] `download-nodejs.bat`を実行
- [ ] `build-offline-complete.bat`を実行
- [ ] `create-offline-zip.bat`を実行（オプション）

### オフライン環境での確認

- [ ] ZIPファイルまたはディレクトリをコピー済み
- [ ] `nodejs\node.exe`が存在
- [ ] `node_modules`が存在
- [ ] `.playwright`が存在
- [ ] `run-tests.bat`を実行可能
- [ ] Spring Bootアプリケーションが起動中

---

## 🎉 まとめ

このガイドに従うことで、**完全にインターネット接続がない環境**でもPlaywrightテストを実行できるようになります。

**重要なポイント:**
1. ✅ Node.jsポータブル版を含むため、オフライン環境にNode.jsのインストールが不要
2. ✅ すべての依存関係とブラウザを含む自己完結型パッケージ
3. ✅ シンプルなバッチファイルで簡単に実行可能
4. ✅ 管理者権限不要で実行可能

---

**最終更新:** 2026-01-04
**バージョン:** 1.0.0
**対応プラットフォーム:** Windows 10/11 (64-bit)
