package com.example.batch.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * ドライラン用のItemWriter。
 *
 * <p>実際にはデータベースに書き込まず、ログ出力のみを行います。</p>
 *
 * @param <T> 書き込むアイテムの型
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
public class DryRunItemWriter<T> implements ItemWriter<T> {

    private static final Logger logger = LoggerFactory.getLogger(DryRunItemWriter.class);

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        logger.info("【ドライラン】{} 件のデータを書き込み（シミュレーション）", chunk.size());

        if (logger.isDebugEnabled()) {
            chunk.getItems().forEach(item ->
                logger.debug("【ドライラン】書き込みデータ: {}", item)
            );
        }
    }
}
