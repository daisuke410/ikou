package com.example.batch.processor;

import com.example.batch.dto.OldCustomerDto;
import com.example.batch.entity.newdb.NewCustomer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 顧客データの変換とマスク処理を組み合わせた複合プロセッサ。
 *
 * <p>このプロセッサは以下の処理を順番に実行します：</p>
 * <ol>
 *   <li>データ変換処理（CustomerMigrationProcessor）</li>
 *   <li>マスク処理（DataMaskingProcessor）※マスク機能が有効な場合のみ</li>
 * </ol>
 *
 * <p>{@code batch.masking.enabled=true}の場合、このBeanが使用されます。</p>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component("compositeCustomerProcessor")
@ConditionalOnProperty(name = "batch.masking.enabled", havingValue = "true")
public class CompositeCustomerProcessor implements ItemProcessor<OldCustomerDto, NewCustomer> {

    @Autowired
    private CustomerMigrationProcessor migrationProcessor;

    @Autowired
    private DataMaskingProcessor<NewCustomer> maskingProcessor;

    @Override
    public NewCustomer process(OldCustomerDto item) throws Exception {
        // 1. データ変換
        NewCustomer customer = migrationProcessor.process(item);
        if (customer == null) {
            return null;
        }

        // 2. マスク処理
        return maskingProcessor.process(customer);
    }
}
