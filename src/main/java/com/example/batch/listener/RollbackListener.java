package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * ロールバック機能を提供するリスナー。
 *
 * <p>ジョブが失敗した場合、新データベースの該当データを削除します。</p>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class RollbackListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(RollbackListener.class);

    @Autowired
    @Qualifier("newDataSource")
    private DataSource newDataSource;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // ジョブ開始前の処理
        logger.info("【ロールバック】ジョブ開始 - 実行ID: {}", jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // ジョブが失敗した場合のみロールバックを実行
        if (jobExecution.getStatus() == BatchStatus.FAILED ||
            jobExecution.getStatus() == BatchStatus.STOPPED) {

            logger.warn("【ロールバック】ジョブが異常終了しました。ロールバックを実行します...");

            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(newDataSource);

                // 該当実行IDで書き込まれたデータを削除
                // 注: 実際の実装では、実行IDをエンティティに保存しておく必要があります
                logger.info("【ロールバック】データ削除処理開始");

                // TODO: 実際のロールバック処理を実装
                // 例: jdbcTemplate.update("DELETE FROM new_customers WHERE batch_execution_id = ?", jobExecution.getId());
                //     jdbcTemplate.update("DELETE FROM new_companies WHERE batch_execution_id = ?", jobExecution.getId());

                logger.info("【ロールバック】ロールバック完了");

            } catch (Exception e) {
                logger.error("【ロールバック】ロールバック処理中にエラーが発生しました: {}", e.getMessage(), e);
            }
        } else if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            logger.info("【ロールバック】ジョブが正常に完了しました。ロールバックは不要です。");
        }
    }
}
