package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 旧システムの顧客データを表すDTO（Data Transfer Object）。
 *
 * <p>このクラスは、TSVファイル（old_customers.tsv）から読み込んだ
 * 顧客データを一時的に保持するために使用されます。</p>
 *
 * <p>TSVファイルの形式：</p>
 * <pre>
 * customer_code	customer_name	email	phone	address	postal_code	created_at	status	gender_code
 * CUST001	山田太郎	yamada.taro@example.com	090-1234-5678	東京都渋谷区1-2-3	150-0001	2025-12-26 10:00:00	ACTIVE	1
 * </pre>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see com.example.batch.processor.CustomerMigrationProcessor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OldCustomerDto {

    /** 顧客コード（例: CUST001） */
    private String customerCode;

    /** 顧客名（例: 山田太郎） */
    private String customerName;

    /** メールアドレス（例: yamada.taro@example.com） */
    private String email;

    /** 電話番号（例: 090-1234-5678） */
    private String phone;

    /** 住所（例: 東京都渋谷区1-2-3） */
    private String address;

    /** 郵便番号（例: 150-0001） */
    private String postalCode;

    /** 登録日時（例: 2025-12-26 10:00:00） */
    private LocalDateTime createdAt;

    /** ステータス（ACTIVE または INACTIVE） */
    private String status;

    /** 性別コード（1=男性, 2=女性） */
    private Integer genderCode;
}
