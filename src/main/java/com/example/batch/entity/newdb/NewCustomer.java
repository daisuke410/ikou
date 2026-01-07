package com.example.batch.entity.newdb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新データベースの顧客テーブル（new_customers）を表すエンティティクラス。
 *
 * <p>旧システムから移行された顧客データを保持します。</p>
 * <p>移行履歴管理のため、以下のフィールドを持ちます：</p>
 * <ul>
 *   <li>migrated_at: 移行実行日時</li>
 *   <li>source_id: 旧システムの顧客ID（追跡用）</li>
 * </ul>
 *
 * @see com.example.batch.dto.OldCustomerDto
 * @see com.example.batch.processor.CustomerMigrationProcessor
 */
@Entity
@Table(name = "new_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCustomer {

    /** 主キー（自動採番） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 顧客ID（一意制約あり） */
    @Column(name = "customer_id", nullable = false, unique = true, length = 20)
    private String customerId;

    /** 顧客氏名 */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** メールアドレス */
    @Column(name = "email_address", length = 100)
    private String emailAddress;

    /** 電話番号 */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /** 住所 */
    @Column(name = "full_address", length = 255)
    private String fullAddress;

    /** 郵便番号 */
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /** 登録日時 */
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    /** 有効フラグ（ACTIVE→true, INACTIVE→false） */
    @Column(name = "is_active")
    private Boolean isActive;

    /** データ移行実行日時 */
    @Column(name = "migrated_at")
    private LocalDateTime migratedAt;

    /** 旧システムの顧客ID（追跡用） */
    @Column(name = "source_id")
    private Long sourceId;

    /** 性別（"男性" / "女性"） */
    @Column(name = "gender", length = 10)
    private String gender;
}
