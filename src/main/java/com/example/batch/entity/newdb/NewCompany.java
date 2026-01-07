package com.example.batch.entity.newdb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 新データベースの会社テーブル（new_companies）を表すエンティティクラス。
 *
 * <p>旧システムから移行された会社データを保持します。</p>
 * <p>業種情報は、旧システムの業種コード（1-11）から日本語の業種名に変換されて保存されます。</p>
 *
 * <p>業種コードのマッピング例：</p>
 * <ul>
 *   <li>1 → 製造業</li>
 *   <li>2 → 建設業</li>
 *   <li>3 → 小売業</li>
 *   <li>4 → サービス業</li>
 * </ul>
 *
 * @see com.example.batch.dto.OldCompanyDto
 * @see com.example.batch.processor.CompanyMigrationProcessor
 */
@Entity
@Table(name = "new_companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompany {

    /** 主キー（自動採番） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会社ID（一意制約あり） */
    @Column(name = "company_id", nullable = false, unique = true, length = 20)
    private String companyId;

    /** 会社名 */
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    /** 代表者名 */
    @Column(name = "representative", length = 100)
    private String representative;

    /** 業種区分（日本語名："製造業"、"建設業" など） */
    @Column(name = "industry_category", length = 50)
    private String industryCategory;

    /** 従業員数 */
    @Column(name = "employees")
    private Integer employees;

    /** 資本金（円） */
    @Column(name = "capital_amount")
    private Long capitalAmount;

    /** 設立日 */
    @Column(name = "foundation_date")
    private LocalDate foundationDate;

    /** 本社所在地 */
    @Column(name = "office_address", length = 300)
    private String officeAddress;

    /** 郵便番号 */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /** 連絡先電話番号 */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** 連絡先メールアドレス */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /** 有効フラグ */
    @Column(name = "is_active")
    private Boolean isActive;

    /** データ移行実行日時 */
    @Column(name = "migrated_at")
    private LocalDateTime migratedAt;
}
