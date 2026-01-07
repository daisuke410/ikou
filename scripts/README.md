# DDLからエンティティクラスを自動生成するツール

DDLファイル（`schema-new.sql`）から自動的にJPAエンティティクラスを生成するツールです。

## 提供しているツール

### 1. Java版: `EntityGenerator.java`
### 2. PowerShell版: `Generate-EntityFromDDL.ps1`
### 3. Python版: `generate-entity-from-ddl.py`

## 使用方法

### Java版（推奨）

```bash
# コンパイル
cd scripts
javac EntityGenerator.java

# 実行
java EntityGenerator

# カスタムパスで実行
java EntityGenerator <DDLファイルパス> <出力ディレクトリ> <パッケージ名>
```

**例**:
```bash
java EntityGenerator ../src/main/resources/schema-new.sql ../src/main/java/com/example/batch/entity/generated com.example.batch.entity.generated
```

### PowerShell版

```powershell
cd scripts
.\Generate-EntityFromDDL.ps1

# パラメータ指定
.\Generate-EntityFromDDL.ps1 -DdlFile "..\src\main\resources\schema-new.sql" `
                             -OutputDir "..\src\main\java\com\example\batch\entity\generated" `
                             -PackageName "com.example.batch.entity.generated"
```

### Python版

```bash
cd scripts
python generate-entity-from-ddl.py
```

## 生成される内容

### 入力DDL例
```sql
CREATE TABLE new_customers (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(100),
    registration_date TIMESTAMP,
    is_active BOOLEAN
);
```

### 生成されるエンティティ
```java
package com.example.batch.entity.generated;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "new_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCustomers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 20)
    private String customerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "is_active")
    private Boolean isActive;

}
```

## 機能

✅ テーブル名をPascalCaseのクラス名に変換（`new_customers` → `NewCustomers`）
✅ カラム名をcamelCaseのフィールド名に変換（`customer_id` → `customerId`）
✅ PostgreSQL型をJava型に自動マッピング
✅ 主キー・UNIQUE・NOT NULL制約を自動認識
✅ `@Column`アノテーションの自動生成
✅ VARCHAR長さの自動抽出
✅ Lombokアノテーション自動付与
✅ 必要なインポート文の自動追加

## 型マッピング

| PostgreSQL型 | Java型 |
|--------------|--------|
| BIGSERIAL | Long |
| BIGINT | Long |
| INTEGER | Integer |
| VARCHAR | String |
| TIMESTAMP | LocalDateTime |
| DATE | LocalDate |
| BOOLEAN | Boolean |
| TEXT | String |

## 出力先

デフォルト出力先:
```
src/main/java/com/example/batch/entity/generated/
```

生成されるファイル:
- `NewCustomers.java`
- `NewCompanies.java`

## トラブルシューティング

### Javaでコンパイルエラーが出る
- Java 17以上がインストールされているか確認
- `javac -version` でバージョン確認

### PowerShellで実行ポリシーエラーが出る
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 生成されたファイルが空
- DDLファイルのパスが正しいか確認
- DDLファイルの形式が正しいか確認

## 手動での利用例

DDLを変更した後、エンティティクラスを再生成する場合：

```bash
# 1. schema-new.sqlを編集
# 2. ツールを実行
cd scripts
java EntityGenerator

# 3. 生成されたファイルを確認
ls ../src/main/java/com/example/batch/entity/generated/
```

## ライセンス

このプロジェクトはサンプルツールです。自由にカスタマイズしてください。
