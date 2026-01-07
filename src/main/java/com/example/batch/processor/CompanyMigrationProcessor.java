package com.example.batch.processor;

import com.example.batch.dto.OldCompanyDto;
import com.example.batch.entity.newdb.NewCompany;
import com.example.batch.validator.CompanyValidator;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会社データを旧形式から新形式に変換するプロセッサ。
 *
 * <p>
 * このクラスは、Spring Batchの{@link ItemProcessor}を実装し、
 * TSVファイルから読み込んだ旧会社データ（{@link OldCompanyDto}）を
 * 新データベース用のエンティティ（{@link NewCompany}）に変換します。
 * </p>
 *
 * <p>
 * 主な変換処理：
 * </p>
 * <ul>
 * <li>フィールド名のマッピング（company_code → companyId など）</li>
 * <li>業種コードから業種名への変換（1 → "商社・卸売" など）</li>
 * <li>ステータス文字列からBoolean値への変換（ACTIVE → true）</li>
 * <li>移行日時の自動設定</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see OldCompanyDto
 * @see NewCompany
 */
@Component
@StepScope
public class CompanyMigrationProcessor implements ItemProcessor<OldCompanyDto, NewCompany> {

    @Value("#{jobParameters['upsertEnabled']}")
    private String upsertEnabled;

    @PersistenceContext(unitName = "new")
    private EntityManager entityManager;

    @Autowired
    private CompanyValidator validator;

    /**
     * 業種コードと業種名のマッピングテーブル。
     *
     * <p>
     * 旧システムでは業種が整数コードで管理されていましたが、
     * 新システムでは業種名（文字列）で管理するため、このマップを使用して変換します。
     * </p>
     */
    private static final Map<Integer, String> INDUSTRY_MAP = new HashMap<>();

    static {
        INDUSTRY_MAP.put(1, "商社・卸売");
        INDUSTRY_MAP.put(2, "製造業");
        INDUSTRY_MAP.put(3, "建設業");
        INDUSTRY_MAP.put(4, "情報通信業");
        INDUSTRY_MAP.put(5, "小売業");
        INDUSTRY_MAP.put(6, "運輸業");
        INDUSTRY_MAP.put(7, "食品業");
        INDUSTRY_MAP.put(8, "不動産業");
        INDUSTRY_MAP.put(9, "サービス業");
        INDUSTRY_MAP.put(10, "医療・福祉");
        INDUSTRY_MAP.put(11, "教育・出版");
    }

    /**
     * 旧会社データを新会社データに変換します。
     *
     * <p>
     * このメソッドは、各会社レコードに対して以下の処理を実行します：
     * </p>
     * <ol>
     * <li>基本情報のマッピング（コード、名前、代表者など）</li>
     * <li>業種コードの変換（1 → "商社・卸売" など、未定義の場合は "その他"）</li>
     * <li>財務・設立情報のマッピング（従業員数、資本金、設立日）</li>
     * <li>連絡先情報のマッピング（住所、電話番号、メールアドレス）</li>
     * <li>ステータスの変換（"ACTIVE" → true, "INACTIVE" → false）</li>
     * <li>移行日時の記録（現在時刻）</li>
     * </ol>
     *
     * @param oldCompany TSVファイルから読み込んだ旧会社データ
     * @return 新データベース用に変換された会社エンティティ
     * @throws Exception データ変換中にエラーが発生した場合
     */
    @Override
    public NewCompany process(OldCompanyDto oldCompany) throws Exception {
        // バリデーション実行
        validator.validate(oldCompany);

        NewCompany newCompany = null;
        String targetCompanyId = oldCompany.getCompanyCode();

        if ("true".equalsIgnoreCase(upsertEnabled) && targetCompanyId != null) {
            List<NewCompany> existingCompanies = entityManager.createQuery(
                    "SELECT n FROM NewCompany n WHERE n.companyId = :companyId", NewCompany.class)
                    .setParameter("companyId", targetCompanyId)
                    .getResultList();

            if (!existingCompanies.isEmpty()) {
                newCompany = existingCompanies.get(0);
            }
        }

        if (newCompany == null) {
            newCompany = new NewCompany();
        }

        // 基本情報のマッピング
        newCompany.setCompanyId(oldCompany.getCompanyCode());
        newCompany.setCompanyName(oldCompany.getCompanyName());
        newCompany.setRepresentative(oldCompany.getRepresentativeName());

        // 業種コードを業種名に変換（未定義の場合は "その他"）
        if (oldCompany.getIndustryType() != null) {
            newCompany.setIndustryCategory(INDUSTRY_MAP.getOrDefault(
                    oldCompany.getIndustryType(), "その他"));
        }

        // 財務・設立情報のマッピング
        newCompany.setEmployees(oldCompany.getEmployeeCount());
        newCompany.setCapitalAmount(oldCompany.getCapital());
        newCompany.setFoundationDate(oldCompany.getEstablishedDate());

        // 連絡先情報のマッピング
        newCompany.setOfficeAddress(oldCompany.getAddress());
        newCompany.setZipCode(oldCompany.getPostalCode());
        newCompany.setContactPhone(oldCompany.getPhone());
        newCompany.setContactEmail(oldCompany.getEmail());

        // ステータスの変換: "ACTIVE" → true, "INACTIVE" → false
        newCompany.setIsActive("ACTIVE".equalsIgnoreCase(oldCompany.getStatus()));

        // 移行日時の記録
        newCompany.setMigratedAt(LocalDateTime.now());

        return newCompany;
    }
}
