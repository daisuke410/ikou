package com.example.batch.processor;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.batch.dto.OldCustomerDto;
import com.example.batch.entity.newdb.NewCustomer;
import com.example.batch.validator.CustomerValidator;

/**
 * 顧客データを旧形式から新形式に変換するプロセッサ。
 *
 * <p>
 * このクラスは、Spring Batchの{@link ItemProcessor}を実装し、
 * TSVファイルから読み込んだ旧顧客データ（{@link OldCustomerDto}）を
 * 新データベース用のエンティティ（{@link NewCustomer}）に変換します。
 * </p>
 *
 * <p>
 * 主な変換処理：
 * </p>
 * <ul>
 * <li>フィールド名のマッピング（customer_code → customerId など）</li>
 * <li>ステータス文字列からBoolean値への変換（ACTIVE → true）</li>
 * <li>性別コードから性別名への変換（1 → "男性", 2 → "女性"）</li>
 * <li>移行日時の自動設定</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see OldCustomerDto
 * @see NewCustomer
 */
@Component
@StepScope
public class CustomerMigrationProcessor implements ItemProcessor<OldCustomerDto, NewCustomer> {

    @Value("#{jobParameters['upsertEnabled']}")
    private String upsertEnabled;

    @PersistenceContext(unitName = "new")
    private EntityManager entityManager;

    @Autowired
    private CustomerValidator validator;

    /**
     * 旧顧客データを新顧客データに変換します。
     *
     * <p>
     * 変換前にバリデーションを実行します。
     * </p>
     *
     * <p>
     * このメソッドは、各顧客レコードに対して以下の処理を実行します：
     * </p>
     * <ol>
     * <li>基本情報のマッピング（コード、名前、連絡先など）</li>
     * <li>ステータスの変換（"ACTIVE" → true, "INACTIVE" → false）</li>
     * <li>性別コードの変換（1 → "男性", 2 → "女性"）</li>
     * <li>移行日時の記録（現在時刻）</li>
     * </ol>
     *
     * @param oldCustomer TSVファイルから読み込んだ旧顧客データ
     * @return 新データベース用に変換された顧客エンティティ
     * @throws Exception データ変換中にエラーが発生した場合
     */
    @Override
    public NewCustomer process(OldCustomerDto oldCustomer) throws Exception {
        // バリデーション実行
        validator.validate(oldCustomer);

        NewCustomer newCustomer = null;
        String targetCustomerId = oldCustomer.getCustomerCode();

        // 既存データの存在確認（Upsert対応）
        if ("true".equalsIgnoreCase(upsertEnabled) && targetCustomerId != null) {
            List<NewCustomer> existingCustomers = entityManager.createQuery(
                    "SELECT n FROM NewCustomer n WHERE n.customerId = :customerId", NewCustomer.class)
                    .setParameter("customerId", targetCustomerId)
                    .getResultList();

            if (!existingCustomers.isEmpty()) {
                newCustomer = existingCustomers.get(0);
            }
        }

        if (newCustomer == null) {
            newCustomer = new NewCustomer();
        }

        // 基本情報のマッピング
        newCustomer.setCustomerId(oldCustomer.getCustomerCode());
        newCustomer.setFullName(oldCustomer.getCustomerName());
        newCustomer.setEmailAddress(oldCustomer.getEmail());
        newCustomer.setPhoneNumber(oldCustomer.getPhone());
        newCustomer.setFullAddress(oldCustomer.getAddress());
        newCustomer.setZipCode(oldCustomer.getPostalCode());
        newCustomer.setRegistrationDate(oldCustomer.getCreatedAt());

        // ステータスの変換: "ACTIVE" → true, "INACTIVE" → false
        newCustomer.setIsActive("ACTIVE".equalsIgnoreCase(oldCustomer.getStatus()));

        // 移行日時の記録
        newCustomer.setMigratedAt(LocalDateTime.now());

        // 性別コードの変換: 1 → "男性", 2 → "女性"
        if (oldCustomer.getGenderCode() != null) {
            switch (oldCustomer.getGenderCode()) {
                case 1:
                    newCustomer.setGender("男性");
                    break;
                case 2:
                    newCustomer.setGender("女性");
                    break;
                default:
                    // 未定義のコードはnullとして扱う
                    newCustomer.setGender(null);
                    break;
            }
        }

        return newCustomer;
    }
}
