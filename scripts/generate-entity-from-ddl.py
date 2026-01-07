#!/usr/bin/env python3
"""
DDLファイルからJPAエンティティクラスを生成するスクリプト
"""
import re
from pathlib import Path

# 型マッピング
TYPE_MAPPING = {
    'BIGSERIAL': 'Long',
    'BIGINT': 'Long',
    'INTEGER': 'Integer',
    'VARCHAR': 'String',
    'TIMESTAMP': 'LocalDateTime',
    'DATE': 'LocalDate',
    'BOOLEAN': 'Boolean',
    'TEXT': 'String',
}

def parse_ddl(ddl_content):
    """DDLをパースしてテーブル情報を抽出"""
    tables = []

    # CREATE TABLE文を抽出
    table_pattern = r'CREATE TABLE\s+(\w+)\s*\((.*?)\);'
    matches = re.finditer(table_pattern, ddl_content, re.DOTALL | re.IGNORECASE)

    for match in matches:
        table_name = match.group(1)
        columns_str = match.group(2)

        columns = []
        for line in columns_str.split('\n'):
            line = line.strip()
            if not line or line.startswith('--'):
                continue

            # カラム定義をパース
            col_match = re.match(r'(\w+)\s+(\w+)(\(\d+\))?(.*)?,?', line, re.IGNORECASE)
            if col_match:
                col_name = col_match.group(1)
                col_type = col_match.group(2).upper()
                constraints = col_match.group(4) or ''

                # PRIMARY KEY, UNIQUEなどの制約行はスキップ
                if col_name.upper() in ['PRIMARY', 'UNIQUE', 'FOREIGN', 'CHECK', 'CONSTRAINT']:
                    continue

                is_primary = 'PRIMARY KEY' in constraints.upper()
                is_nullable = 'NOT NULL' not in constraints.upper()
                is_unique = 'UNIQUE' in constraints.upper()

                java_type = TYPE_MAPPING.get(col_type, 'String')

                columns.append({
                    'name': col_name,
                    'type': col_type,
                    'javaType': java_type,
                    'isPrimary': is_primary,
                    'isNullable': is_nullable,
                    'isUnique': is_unique,
                })

        if columns:
            tables.append({
                'name': table_name,
                'columns': columns
            })

    return tables

def snake_to_camel(snake_str):
    """スネークケースをキャメルケースに変換"""
    components = snake_str.split('_')
    return components[0] + ''.join(x.title() for x in components[1:])

def snake_to_pascal(snake_str):
    """スネークケースをパスカルケースに変換"""
    return ''.join(x.title() for x in snake_str.split('_'))

def generate_entity(table):
    """エンティティクラスを生成"""
    class_name = snake_to_pascal(table['name'])

    imports = set(['jakarta.persistence.*', 'lombok.AllArgsConstructor',
                   'lombok.Data', 'lombok.NoArgsConstructor'])

    # 必要なインポートを追加
    for col in table['columns']:
        if col['javaType'] == 'LocalDateTime':
            imports.add('java.time.LocalDateTime')
        elif col['javaType'] == 'LocalDate':
            imports.add('java.time.LocalDate')

    # エンティティクラス生成
    code = f"""package com.example.batch.entity.newdb;

"""

    # インポート
    for imp in sorted(imports):
        code += f"import {imp};\n"

    code += f"""
@Entity
@Table(name = "{table['name']}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class {class_name} {{

"""

    # フィールド生成
    for col in table['columns']:
        field_name = snake_to_camel(col['name'])
        java_type = col['javaType']

        # アノテーション
        if col['isPrimary']:
            code += "    @Id\n"
            if col['type'] == 'BIGSERIAL':
                code += "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"

        # カラムアノテーション
        annotations = []
        if col['name'] != snake_to_camel(col['name']):
            annotations.append(f'name = "{col["name"]}"')
        if not col['isNullable'] and not col['isPrimary']:
            annotations.append('nullable = false')
        if col['isUnique']:
            annotations.append('unique = true')

        if annotations or col['type'].startswith('VARCHAR'):
            code += f"    @Column("
            if annotations:
                code += ', '.join(annotations)
            if col['type'].startswith('VARCHAR'):
                if annotations:
                    code += ', '
                # VARCHAR(100) から長さを抽出
                length_match = re.search(r'VARCHAR\((\d+)\)', col['type'])
                if length_match:
                    code += f"length = {length_match.group(1)}"
            code += ")\n"

        code += f"    private {java_type} {field_name};\n\n"

    code += "}\n"

    return class_name, code

def main():
    """メイン処理"""
    # DDLファイルを読み込み
    ddl_file = Path(__file__).parent.parent / 'src' / 'main' / 'resources' / 'schema-new.sql'

    if not ddl_file.exists():
        print(f"Error: {ddl_file} が見つかりません")
        return

    ddl_content = ddl_file.read_text(encoding='utf-8')

    # テーブル情報を抽出
    tables = parse_ddl(ddl_content)

    # エンティティ生成
    output_dir = Path(__file__).parent.parent / 'src' / 'main' / 'java' / 'com' / 'example' / 'batch' / 'entity' / 'generated'
    output_dir.mkdir(parents=True, exist_ok=True)

    for table in tables:
        class_name, code = generate_entity(table)
        output_file = output_dir / f"{class_name}.java"
        output_file.write_text(code, encoding='utf-8')
        print(f"Generated: {output_file}")

if __name__ == '__main__':
    main()
