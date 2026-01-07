package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 旧システムの会社データを表すDTO（Data Transfer Object）。
 *
 * <p>このクラスは、TSVファイル（old_companies.tsv）から読み込んだ
 * 会社データを一時的に保持するために使用されます。</p>
 *
 * <p>TSVファイルの形式：</p>
 * <pre>
 * company_code	company_name	representative_name	industry_type	employee_count	capital	established_date	address	postal_code	phone	email	status
 * COMP001	株式会社サンプル商事	山田太郎	1	150	50000000	2010-04-01	東京都千代田区丸の内1-1-1	100-0001	03-1234-5678	info@sample-corp.co.jp	ACTIVE
 * </pre>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see com.example.batch.processor.CompanyMigrationProcessor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OldCompanyDto {

    /** 会社コード（例: COMP001） */
    private String companyCode;

    /** 会社名（例: 株式会社サンプル商事） */
    private String companyName;

    /** 代表者名（例: 山田太郎） */
    private String representativeName;

    /** 業種コード（1=商社・卸売, 2=製造業, 3=建設業, ...） */
    private Integer industryType;

    /** 従業員数（例: 150） */
    private Integer employeeCount;

    /** 資本金（単位: 円、例: 50000000） */
    private Long capital;

    /** 設立日（例: 2010-04-01） */
    private LocalDate establishedDate;

    /** 住所（例: 東京都千代田区丸の内1-1-1） */
    private String address;

    /** 郵便番号（例: 100-0001） */
    private String postalCode;

    /** 電話番号（例: 03-1234-5678） */
    private String phone;

    /** メールアドレス（例: info@sample-corp.co.jp） */
    private String email;

    /** ステータス（ACTIVE または INACTIVE） */
    private String status;
}
