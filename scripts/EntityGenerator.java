import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DDLファイルからJPAエンティティクラスを生成するツール
 *
 * 使用方法:
 *   javac EntityGenerator.java
 *   java EntityGenerator
 */
public class EntityGenerator {

    // 型マッピング
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    static {
        TYPE_MAPPING.put("BIGSERIAL", "Long");
        TYPE_MAPPING.put("BIGINT", "Long");
        TYPE_MAPPING.put("INTEGER", "Integer");
        TYPE_MAPPING.put("VARCHAR", "String");
        TYPE_MAPPING.put("TIMESTAMP", "LocalDateTime");
        TYPE_MAPPING.put("DATE", "LocalDate");
        TYPE_MAPPING.put("BOOLEAN", "Boolean");
        TYPE_MAPPING.put("TEXT", "String");
    }

    public static void main(String[] args) throws IOException {
        String ddlFile = "../src/main/resources/schema-new.sql";
        String outputDir = "../src/main/java/com/example/batch/entity/generated";
        String packageName = "com.example.batch.entity.generated";

        // 引数でパスを上書き可能
        if (args.length > 0) ddlFile = args[0];
        if (args.length > 1) outputDir = args[1];
        if (args.length > 2) packageName = args[2];

        EntityGenerator generator = new EntityGenerator();
        generator.generate(ddlFile, outputDir, packageName);
    }

    public void generate(String ddlFilePath, String outputDirPath, String packageName) throws IOException {
        // DDLファイル読み込み
        Path ddlPath = Paths.get(ddlFilePath);
        if (!Files.exists(ddlPath)) {
            System.err.println("DDLファイルが見つかりません: " + ddlPath.toAbsolutePath());
            System.exit(1);
        }

        String ddlContent = Files.readString(ddlPath);
        System.out.println("DDLファイルを読み込みました: " + ddlPath.toAbsolutePath());

        // テーブル情報を抽出
        List<TableInfo> tables = parseDdl(ddlContent);
        System.out.println("見つかったテーブル: " + tables.size() + "個\n");

        // 出力ディレクトリ作成
        Path outputDir = Paths.get(outputDirPath);
        Files.createDirectories(outputDir);

        // エンティティクラス生成
        for (TableInfo table : tables) {
            String className = snakeToPascal(table.name);
            String code = generateEntityCode(table, packageName);

            Path outputFile = outputDir.resolve(className + ".java");
            Files.writeString(outputFile, code);

            System.out.println("生成完了: " + outputFile.toAbsolutePath());
        }

        System.out.println("\n処理完了!");
    }

    private List<TableInfo> parseDdl(String ddlContent) {
        List<TableInfo> tables = new ArrayList<>();

        // CREATE TABLE文を抽出
        Pattern tablePattern = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)\\s*\\((.*?)\\);",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = tablePattern.matcher(ddlContent);

        while (matcher.find()) {
            String tableName = matcher.group(1);
            String columnsStr = matcher.group(2);

            System.out.println("処理中: " + tableName);

            TableInfo table = new TableInfo();
            table.name = tableName;
            table.columns = parseColumns(columnsStr);

            if (!table.columns.isEmpty()) {
                tables.add(table);
                System.out.println("  カラム数: " + table.columns.size());
            }
        }

        return tables;
    }

    private List<ColumnInfo> parseColumns(String columnsStr) {
        List<ColumnInfo> columns = new ArrayList<>();

        String[] lines = columnsStr.split("\n");
        for (String line : lines) {
            line = line.trim();

            // 空行、コメント、制約行をスキップ
            if (line.isEmpty() || line.startsWith("--") ||
                line.matches("^\\s*(PRIMARY|UNIQUE|FOREIGN|CHECK|CONSTRAINT)\\s+.*")) {
                continue;
            }

            // カラム定義をパース
            Pattern colPattern = Pattern.compile(
                "^(\\w+)\\s+(\\w+)(\\(\\d+\\))?(.*?)(?:,|$)",
                Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = colPattern.matcher(line);
            if (matcher.find()) {
                String colName = matcher.group(1);
                String colType = matcher.group(2).toUpperCase();
                String sizeSpec = matcher.group(3);
                String constraints = matcher.group(4) != null ? matcher.group(4) : "";

                // 制約キーワードはスキップ
                if (colName.matches("(?i)PRIMARY|UNIQUE|FOREIGN|CHECK|CONSTRAINT")) {
                    continue;
                }

                ColumnInfo col = new ColumnInfo();
                col.name = colName;
                col.type = colType;
                col.isPrimary = constraints.matches("(?i).*PRIMARY\\s+KEY.*");
                col.isNullable = !constraints.matches("(?i).*NOT\\s+NULL.*");
                col.isUnique = constraints.matches("(?i).*UNIQUE.*");
                col.javaType = TYPE_MAPPING.getOrDefault(colType, "String");

                // VARCHAR(100) から長さを抽出
                if (colType.equals("VARCHAR") && sizeSpec != null) {
                    Pattern lengthPattern = Pattern.compile("\\((\\d+)\\)");
                    Matcher lengthMatcher = lengthPattern.matcher(sizeSpec);
                    if (lengthMatcher.find()) {
                        col.length = Integer.parseInt(lengthMatcher.group(1));
                    }
                }

                columns.add(col);
            }
        }

        return columns;
    }

    private String generateEntityCode(TableInfo table, String packageName) {
        StringBuilder code = new StringBuilder();
        String className = snakeToPascal(table.name);

        // パッケージ宣言
        code.append("package ").append(packageName).append(";\n\n");

        // インポート
        Set<String> imports = new TreeSet<>();
        imports.add("jakarta.persistence.*");
        imports.add("lombok.AllArgsConstructor");
        imports.add("lombok.Data");
        imports.add("lombok.NoArgsConstructor");

        for (ColumnInfo col : table.columns) {
            if ("LocalDateTime".equals(col.javaType)) {
                imports.add("java.time.LocalDateTime");
            } else if ("LocalDate".equals(col.javaType)) {
                imports.add("java.time.LocalDate");
            }
        }

        for (String imp : imports) {
            code.append("import ").append(imp).append(";\n");
        }

        // クラス定義
        code.append("\n@Entity\n");
        code.append("@Table(name = \"").append(table.name).append("\")\n");
        code.append("@Data\n");
        code.append("@NoArgsConstructor\n");
        code.append("@AllArgsConstructor\n");
        code.append("public class ").append(className).append(" {\n\n");

        // フィールド定義
        for (ColumnInfo col : table.columns) {
            String fieldName = snakeToCamel(col.name);

            // アノテーション
            if (col.isPrimary) {
                code.append("    @Id\n");
                if ("BIGSERIAL".equals(col.type)) {
                    code.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                }
            }

            // @Columnアノテーション
            List<String> columnAttrs = new ArrayList<>();

            if (!col.name.equals(fieldName)) {
                columnAttrs.add("name = \"" + col.name + "\"");
            }

            if (!col.isNullable && !col.isPrimary) {
                columnAttrs.add("nullable = false");
            }

            if (col.isUnique) {
                columnAttrs.add("unique = true");
            }

            if (col.length != null) {
                columnAttrs.add("length = " + col.length);
            }

            if (!columnAttrs.isEmpty()) {
                code.append("    @Column(").append(String.join(", ", columnAttrs)).append(")\n");
            }

            code.append("    private ").append(col.javaType).append(" ").append(fieldName).append(";\n\n");
        }

        code.append("}\n");

        return code.toString();
    }

    private String snakeToCamel(String snake) {
        String[] parts = snake.split("_");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            result.append(parts[i].substring(0, 1).toUpperCase())
                  .append(parts[i].substring(1).toLowerCase());
        }

        return result.toString();
    }

    private String snakeToPascal(String snake) {
        String[] parts = snake.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            result.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase());
        }

        return result.toString();
    }

    // データクラス
    static class TableInfo {
        String name;
        List<ColumnInfo> columns;
    }

    static class ColumnInfo {
        String name;
        String type;
        String javaType;
        Integer length;
        boolean isPrimary;
        boolean isNullable;
        boolean isUnique;
    }
}
