package com.example.batch.config;

import com.example.batch.dto.OldCompanyDto;
import com.example.batch.dto.OldCustomerDto;
import com.example.batch.entity.newdb.NewCompany;
import com.example.batch.entity.newdb.NewCustomer;
import com.example.batch.processor.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * プロセッサの選択を行う設定クラス。
 *
 * <p>
 * マスク機能の有効/無効に応じて、適切なプロセッサを提供します：
 * </p>
 * <ul>
 * <li>マスク無効（本番環境）: 変換のみのプロセッサ</li>
 * <li>マスク有効（テスト環境）: 変換+マスクの複合プロセッサ</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Configuration
public class ProcessorConfiguration {

    @Autowired(required = false)
    private CompositeCustomerProcessor compositeCustomerProcessor;

    @Autowired(required = false)
    private CompositeCompanyProcessor compositeCompanyProcessor;

    @Autowired
    private CustomerMigrationProcessor customerMigrationProcessor;

    @Autowired
    private CompanyMigrationProcessor companyMigrationProcessor;

    /**
     * 顧客データ処理用のプロセッサを提供します。
     *
     * <p>
     * マスク機能が有効な場合は複合プロセッサ、無効な場合は変換のみのプロセッサを返します。
     * </p>
     *
     * @return 顧客データ処理用のItemProcessor
     */
    @Bean(name = "effectiveCustomerProcessor")
    @StepScope
    public ItemProcessor<OldCustomerDto, NewCustomer> effectiveCustomerProcessor() {
        if (compositeCustomerProcessor != null) {
            return compositeCustomerProcessor;
        }
        return customerMigrationProcessor;
    }

    /**
     * 会社データ処理用のプロセッサを提供します。
     *
     * <p>
     * マスク機能が有効な場合は複合プロセッサ、無効な場合は変換のみのプロセッサを返します。
     * </p>
     *
     * @return 会社データ処理用のItemProcessor
     */
    @Bean(name = "effectiveCompanyProcessor")
    @StepScope
    public ItemProcessor<OldCompanyDto, NewCompany> effectiveCompanyProcessor() {
        if (compositeCompanyProcessor != null) {
            return compositeCompanyProcessor;
        }
        return companyMigrationProcessor;
    }
}
