# 簡易版DDLからエンティティ生成スクリプト

param(
    [string]$DdlFile = "..\src\main\resources\schema-new.sql",
    [string]$OutputDir = "..\src\main\java\com\example\batch\entity\generated",
    [string]$PackageName = "com.example.batch.entity.generated"
)

$ErrorActionPreference = "Stop"

# 型マッピング
$TypeMapping = @{
    'BIGSERIAL' = 'Long'
    'BIGINT'    = 'Long'
    'INTEGER'   = 'Integer'
    'VARCHAR'   = 'String'
    'TIMESTAMP' = 'LocalDateTime'
    'DATE'      = 'LocalDate'
    'BOOLEAN'   = 'Boolean'
    'TEXT'      = 'String'
}

function ConvertTo-CamelCase {
    param([string]$SnakeCase)
    $parts = $SnakeCase -split '_'
    $result = $parts[0].ToLower()
    for ($i = 1; $i -lt $parts.Length; $i++) {
        $result += $parts[$i].Substring(0, 1).ToUpper() + $parts[$i].Substring(1).ToLower()
    }
    return $result
}

function ConvertTo-PascalCase {
    param([string]$SnakeCase)
    $parts = $SnakeCase -split '_'
    $result = ""
    foreach ($part in $parts) {
        $result += $part.Substring(0, 1).ToUpper() + $part.Substring(1).ToLower()
    }
    return $result
}

# DDLファイルを読み込み
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ddlPath = Join-Path $scriptDir $DdlFile

if (-not (Test-Path $ddlPath)) {
    Write-Error "DDLファイルが見つかりません: $ddlPath"
    exit 1
}

$ddlContent = Get-Content $ddlPath -Raw -Encoding UTF8

# CREATE TABLE文を抽出
$tablePattern = '(?si)CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(\w+)\s*\((.*?)\);'
$tables = [regex]::Matches($ddlContent, $tablePattern)

Write-Host "見つかったテーブル: $($tables.Count)個" -ForegroundColor Green

# 出力ディレクトリ作成
$outputPath = Join-Path $scriptDir $OutputDir
if (-not (Test-Path $outputPath)) {
    New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
}

foreach ($table in $tables) {
    $tableName = $table.Groups[1].Value
    $columnsStr = $table.Groups[2].Value

    Write-Host ""
    Write-Host "処理中: $tableName" -ForegroundColor Cyan

    $className = ConvertTo-PascalCase $tableName
    $columns = @()

    # カラム定義を解析
    $lines = $columnsStr -split "`n"
    foreach ($line in $lines) {
        $line = $line.Trim()

        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('--') -or
            $line -match '^\s*(PRIMARY|UNIQUE|FOREIGN|CHECK|CONSTRAINT)\s+KEY') {
            continue
        }

        if ($line -match '^(\w+)\s+(\w+)(\(\d+\))?(.*?)(?:,|$)') {
            $colName = $matches[1]
            $colType = $matches[2].ToUpper()
            $sizeSpec = $matches[3]
            $constraints = $matches[4]

            if ($colName -match '^(PRIMARY|UNIQUE|FOREIGN|CHECK|CONSTRAINT)$') {
                continue
            }

            $isPrimary = $constraints -match 'PRIMARY\s+KEY'
            $isNullable = -not ($constraints -match 'NOT\s+NULL')
            $isUnique = $constraints -match 'UNIQUE'

            $javaType = $TypeMapping[$colType]
            if (-not $javaType) {
                $javaType = 'String'
            }

            $length = $null
            if ($colType -eq 'VARCHAR' -and $sizeSpec -match '\((\d+)\)') {
                $length = $matches[1]
            }

            $columns += [PSCustomObject]@{
                Name       = $colName
                Type       = $colType
                JavaType   = $javaType
                Length     = $length
                IsPrimary  = $isPrimary
                IsNullable = $isNullable
                IsUnique   = $isUnique
            }
        }
    }

    if ($columns.Count -eq 0) {
        Write-Warning "  カラムが見つかりませんでした"
        continue
    }

    Write-Host "  カラム数: $($columns.Count)" -ForegroundColor Yellow

    # エンティティクラス生成（配列で組み立てる）
    $lines = @()
    $lines += "package $PackageName;"
    $lines += ""

    # インポート判定
    $imports = @('jakarta.persistence.*', 'lombok.AllArgsConstructor', 'lombok.Data', 'lombok.NoArgsConstructor')
    foreach ($col in $columns) {
        if ($col.JavaType -eq 'LocalDateTime' -and $imports -notcontains 'java.time.LocalDateTime') {
            $imports += 'java.time.LocalDateTime'
        }
        if ($col.JavaType -eq 'LocalDate' -and $imports -notcontains 'java.time.LocalDate') {
            $imports += 'java.time.LocalDate'
        }
    }

    foreach ($import in ($imports | Sort-Object)) {
        $lines += "import $import;"
    }

    $lines += ""
    $lines += "@Entity"
    $lines += "@Table(name = `"$tableName`")"
    $lines += "@Data"
    $lines += "@NoArgsConstructor"
    $lines += "@AllArgsConstructor"
    $lines += "public class $className {"
    $lines += ""

    # フィールド生成
    foreach ($col in $columns) {
        $fieldName = ConvertTo-CamelCase $col.Name
        $javaType = $col.JavaType

        if ($col.IsPrimary) {
            $lines += "    @Id"
            if ($col.Type -eq 'BIGSERIAL') {
                $lines += "    @GeneratedValue(strategy = GenerationType.IDENTITY)"
            }
        }

        $columnAttrs = @()
        if ($col.Name -ne $fieldName) {
            $columnAttrs += "name = `"$($col.Name)`""
        }
        if (-not $col.IsNullable -and -not $col.IsPrimary) {
            $columnAttrs += 'nullable = false'
        }
        if ($col.IsUnique) {
            $columnAttrs += 'unique = true'
        }
        if ($col.Length) {
            $columnAttrs += "length = $($col.Length)"
        }

        if ($columnAttrs.Count -gt 0) {
            $attrStr = $columnAttrs -join ', '
            $lines += "    @Column($attrStr)"
        }

        $lines += "    private $javaType $fieldName;"
        $lines += ""
    }

    $lines += "}"

    # ファイル出力
    $outputFile = Join-Path $outputPath "$className.java"
    $lines | Out-File -FilePath $outputFile -Encoding UTF8 -Force

    Write-Host "  生成完了: $outputFile" -ForegroundColor Green
}

Write-Host ""
Write-Host "処理完了!" -ForegroundColor Green
