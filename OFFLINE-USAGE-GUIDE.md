# オフライン環境での使用ガイド

## 📦 前提条件

オンライン環境で以下を完了していること:
- ✅ `build-offline-complete.bat` を実行済み
- ✅ `create-offline-zip.bat` を実行済み
- ✅ ZIPファイルが作成済み（`dist\playwright-offline-complete-YYYYMMDD.zip`）

---

## 🚀 オフライン環境での使用手順

### ステップ1: ZIPファイルの転送

1. **オンライン環境で**、作成されたZIPファイルを確認
   ```
   場所: dist\playwright-offline-complete-YYYYMMDD.zip
   サイズ: 約300MB～500MB
   ```

2. **USBメモリまたはネットワーク経由**でオフライン環境に転送

3. **オフライン環境**の任意の場所にコピー
   ```
   推奨: C:\playwright-tests\
   ```

### ステップ2: ZIPファイルの解凍

1. ZIPファイルを右クリック → **すべて展開**

2. または、PowerShellで解凍:
   ```powershell
   Expand-Archive -Path "C:\playwright-offline-complete-20260104.zip" -DestinationPath "C:\playwright-tests"
   ```

3. 解凍後のフォルダ構造を確認:
   ```
   C:\playwright-tests\
   ├── nodejs\              (Node.jsポータブル版)
   ├── node_modules\        (依存関係)
   ├── .playwright\         (ブラウザ)
   ├── tests\               (テストケース)
   ├── scripts\             (内部スクリプト)
   ├── playwright.config.ts
   ├── package.json
   ├── run-tests.bat        ← メイン実行スクリプト
   ├── run-tests-ui.bat     ← UIモード実行
   ├── show-report.bat      ← レポート表示
   └── README.md
   ```

### ステップ3: テストの実行

#### 方法A: すべてのテストを実行

1. 解凍したフォルダを開く（例: `C:\playwright-tests\`）

2. **`run-tests.bat`** をダブルクリック

3. コマンドプロンプトが開き、テストが実行されます:
   ```
   ============================================================
   Playwright Test Runner - Offline Execution
   ============================================================

   Node.js version:
   v18.20.5

   Working directory: C:\playwright-tests
   Browsers path: C:\playwright-tests\.playwright

   ============================================================

   Running tests...
   ```

4. テスト完了後、結果が表示されます

#### 方法B: UIモードで実行

1. **`run-tests-ui.bat`** をダブルクリック

2. Playwright UIが起動し、インタラクティブにテストを実行できます

3. テストを選択して実行、デバッグ、トレース表示が可能

#### 方法C: コマンドラインから実行

コマンドプロンプトを開いて:

```batch
# ディレクトリに移動
cd C:\playwright-tests

# すべてのテストを実行
run-tests.bat

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

### ステップ4: テストレポートの表示

1. テスト実行後、**`show-report.bat`** をダブルクリック

2. ブラウザが開き、HTMLレポートが表示されます

3. レポートには以下が含まれます:
   - ✅ テスト結果（成功/失敗）
   - 📸 スクリーンショット（失敗時）
   - 🎥 ビデオ録画（失敗時）
   - 📝 詳細なエラーログ

---

## 🔧 Spring Bootアプリケーションとの連携

### 前提条件

Playwrightテストは、Spring Bootアプリケーションが起動している必要があります。

### Spring Bootアプリケーションの起動

#### 方法A: Mavenで起動

```batch
# Spring Bootプロジェクトのディレクトリに移動
cd D:\your-spring-boot-project

# アプリケーションを起動
mvn spring-boot:run
```

#### 方法B: JARファイルで起動

```batch
# JARファイルがある場所に移動
cd D:\your-spring-boot-project\target

# JARファイルを実行
java -jar data-migration-batch-1.0.0.jar
```

#### 方法C: IDEで起動

1. IntelliJ IDEA または Eclipse で Spring Boot プロジェクトを開く
2. メインクラス（`@SpringBootApplication`アノテーション付き）を実行

### アプリケーション起動の確認

```batch
# ブラウザで以下にアクセス
http://localhost:8080

# またはコマンドラインで確認
curl http://localhost:8080/actuator/health
```

### ポート設定の変更

デフォルトではポート8080を使用します。変更する場合:

1. `playwright.config.ts` を編集:
   ```typescript
   export default defineConfig({
     use: {
       baseURL: 'http://localhost:8080',  // ここを変更
     },
   });
   ```

2. Spring Bootアプリケーションのポートも変更:
   ```properties
   # application.properties
   server.port=8080  # ここを変更
   ```

---

## 📊 実行例

### 例1: 基本的なテスト実行

```batch
C:\playwright-tests> run-tests.bat

Running 89 tests...

  ✓ tests\theme-selection.spec.ts (10/10)
  ✓ tests\dark-theme.spec.ts (11/11)
  ✓ tests\api-endpoints.spec.ts (8/8)
  ✓ tests\cross-theme.spec.ts (53/53)
  ✓ tests\e2e.spec.ts (7/7)

89 passed (2.5m)
```

### 例2: 特定のテーマのみテスト

```batch
C:\playwright-tests> run-tests.bat tests\dark-theme.spec.ts

Running 11 tests...

  ✓ Dark Theme UI elements visible
  ✓ Dark Theme batch execution
  ...

11 passed (15s)
```

### 例3: Chromiumのみでテスト

```batch
C:\playwright-tests> run-tests.bat --project=chromium

Running 89 tests on chromium...

89 passed (1.8m)
```

---

## 🔍 トラブルシューティング

### 問題1: Node.jsが見つからない

**エラーメッセージ:**
```
[ERROR] Portable Node.js not found
Expected path: C:\playwright-tests\nodejs\node.exe
```

**解決方法:**
1. ZIPファイルが正しく解凍されているか確認
2. `nodejs\node.exe` が存在するか確認
3. 必要に応じて再度解凍

### 問題2: Playwrightブラウザが見つからない

**エラーメッセージ:**
```
[WARNING] Playwright browsers not found
Expected path: C:\playwright-tests\.playwright
```

**解決方法:**
1. `.playwright` ディレクトリが存在するか確認
2. オンライン環境で `npx playwright install` を実行してから、パッケージを再作成
3. ZIPファイルを再転送

### 問題3: Spring Bootアプリケーションに接続できない

**エラーメッセージ:**
```
Error: connect ECONNREFUSED 127.0.0.1:8080
```

**解決方法:**
1. Spring Bootアプリケーションが起動しているか確認:
   ```batch
   curl http://localhost:8080/actuator/health
   ```

2. ポート8080が使用中か確認:
   ```batch
   netstat -ano | findstr :8080
   ```

3. ファイアウォールの設定を確認

### 問題4: テストが失敗する

**確認事項:**

1. **Spring Bootアプリケーションの状態**
   - アプリケーションが正常に起動しているか
   - エラーログを確認

2. **HTMLファイルの配置**
   - `src\main\resources\static` にHTMLファイルが存在するか
   - ファイル名が正しいか

3. **データベース接続**
   - データベースが起動しているか
   - 接続情報が正しいか

4. **テストレポートを確認**
   ```batch
   show-report.bat
   ```

### 問題5: 権限エラー

**エラーメッセージ:**
```
Access denied
```

**解決方法:**
1. **管理者権限でコマンドプロンプトを起動**
   - スタートメニュー → cmd → 右クリック → 管理者として実行

2. **ディレクトリのアクセス権限を確認**
   - フォルダを右クリック → プロパティ → セキュリティ

3. **アンチウイルスソフトを一時的に無効化**（必要な場合）

---

## 💡 ベストプラクティス

### 1. 定期的なテスト実行

```batch
# 毎日のテスト実行スケジュール
# Windowsタスクスケジューラで設定可能

# 実行コマンド
C:\playwright-tests\run-tests.bat
```

### 2. CI/CD統合

```batch
# バッチファイルから呼び出し
call C:\playwright-tests\run-tests.bat
if %ERRORLEVEL% NEQ 0 (
    echo テストが失敗しました
    exit /b 1
)
```

### 3. レポートの保存

```batch
# レポートディレクトリをバックアップ
xcopy /E /I C:\playwright-tests\playwright-report C:\backup\report-%date%
```

### 4. ログの確認

テスト実行時のログは以下に保存されます:
```
playwright-report\
test-results\
```

---

## 📝 よくある質問（FAQ）

### Q1: Node.jsのインストールは必要ですか？

**A:** いいえ、不要です。パッケージにNode.jsポータブル版が含まれています。

### Q2: インターネット接続は必要ですか？

**A:** いいえ、完全にオフラインで動作します。

### Q3: 複数のマシンで使用できますか？

**A:** はい。ZIPファイルを各マシンにコピーして解凍すれば使用できます。

### Q4: ブラウザを更新できますか？

**A:** オフライン環境では更新できません。オンライン環境で `npx playwright install` を実行し、パッケージを再作成してください。

### Q5: テストケースを追加できますか？

**A:** `tests\` フォルダに `.spec.ts` ファイルを追加すれば、自動的に認識されます。

### Q6: Windows以外のOSで動作しますか？

**A:** 現在のパッケージはWindows専用です。Linux/Mac版を作成する場合は、対応するNode.jsポータブル版を使用してください。

---

## 📞 サポート

問題が発生した場合は、以下の情報を収集してください:

1. **エラーメッセージ**（完全な出力）
2. **実行したコマンド**
3. **環境情報**:
   ```batch
   # Node.jsバージョン
   nodejs\node.exe --version

   # Windowsバージョン
   ver
   ```
4. **ディレクトリ構造**:
   ```batch
   dir /B
   ```
5. **テストレポート**（`playwright-report\` の内容）

---

## 📄 チェックリスト

### オフライン環境での初回セットアップ

- [ ] ZIPファイルをオフライン環境に転送済み
- [ ] ZIPファイルを解凍済み
- [ ] `nodejs\node.exe` が存在することを確認
- [ ] `node_modules\` が存在することを確認
- [ ] `.playwright\` が存在することを確認
- [ ] `run-tests.bat` が存在することを確認
- [ ] Spring Bootアプリケーションが起動済み
- [ ] `run-tests.bat` を実行してテスト成功を確認

### 通常のテスト実行

- [ ] Spring Bootアプリケーションが起動しているか確認
- [ ] ポート8080が使用可能か確認
- [ ] `run-tests.bat` を実行
- [ ] テスト結果を確認
- [ ] 必要に応じて `show-report.bat` でレポートを確認

---

## 🎉 まとめ

### オフライン環境での使用は簡単です:

1. **ZIPファイルを転送** → USBメモリなどで
2. **解凍** → 任意の場所に
3. **実行** → `run-tests.bat` をダブルクリック

### 必要なもの:

- ✅ Windows 10/11 (64-bit)
- ✅ Spring Bootアプリケーション（起動済み）
- ✅ オフラインパッケージ（ZIPファイル）

### 不要なもの:

- ❌ Node.jsのインストール
- ❌ インターネット接続
- ❌ 管理者権限（通常は不要）

---

**最終更新:** 2026-01-04
**バージョン:** 1.0.0
**対応プラットフォーム:** Windows 10/11 (64-bit)
