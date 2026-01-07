package com.example.batch.processor;

import com.example.batch.dto.OldCompanyDto;
import com.example.batch.entity.newdb.NewCompany;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 会社データの変換とマスク処理を組み合わせた複合プロセッサ。
 *
 * <p>このプロセッサは以下の処理を順番に実行します：</p>
 * <ol>
 *   <li>データ変換処理（CompanyMigrationProcessor）</li>
 *   <li>マスク処理（DataMaskingProcessor）※マスク機能が有効な場合のみ</li>
 * </ol>
 *
 * <p>{@code batch.masking.enabled=true}の場合、このBeanが使用されます。</p>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component("compositeCompanyProcessor")
@ConditionalOnProperty(name = "batch.masking.enabled", havingValue = "true")
public class CompositeCompanyProcessor implements ItemProcessor<OldCompanyDto, NewCompany> {

    @Autowired
    private CompanyMigrationProcessor migrationProcessor;

    @Autowired
    private DataMaskingProcessor<NewCompany> maskingProcessor;

    @Override
    public NewCompany process(OldCompanyDto item) throws Exception {
        // 1. データ変換
        NewCompany company = migrationProcessor.process(item);
        if (company == null) {
            return null;
        }

        // 2. マスク処理
        return maskingProcessor.process(company);
    }
}
