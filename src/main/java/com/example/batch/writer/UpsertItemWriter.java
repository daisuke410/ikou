package com.example.batch.writer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * Upsert（Insert or Update）をサポートするItemWriter。
 *
 * <p>
 * 既存のデータが存在する場合は更新、存在しない場合は挿入を行います。
 * </p>
 * <p>
 * これにより、バッチジョブを複数回実行しても安全に処理できます。
 * </p>
 *
 * @param <T> エンティティの型
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
public class UpsertItemWriter<T> implements ItemWriter<T> {

    private static final Logger logger = LoggerFactory.getLogger(UpsertItemWriter.class);

    private final EntityManager entityManager;

    public UpsertItemWriter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        for (T item : chunk.getItems()) {
            if (!entityManager.contains(item)) {
                if (item instanceof com.example.batch.entity.newdb.NewCustomer ||
                        item instanceof com.example.batch.entity.newdb.NewCompany) {
                    // Ensure entity is merged into current persistence context
                    entityManager.merge(item);
                } else {
                    entityManager.persist(item);
                }
            }
        }
        logger.debug("【Upsert】{} 件のデータを処理しました", chunk.size());
    }
}
