package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * バッチジョブの処理統計をCSVレポートとして出力するリスナー。
 *
 * <p>このリスナーは、ジョブ実行完了時に以下の統計情報をCSV形式で出力します：</p>
 * <ul>
 *   <li>ジョブ実行ID、ジョブ名、ステータス</li>
 *   <li>開始時刻、終了時刻、処理時間</li>
 *   <li>ステップごとの読み込み件数、書き込み件数、スキップ件数</li>
 *   <li>ステップごとのコミット回数、ロールバック回数</li>
 *   <li>処理速度（件/秒）</li>
 *   <li>エラー情報（発生した場合）</li>
 * </ul>
 *
 * <p>CSVファイルの出力先は、application.ymlの{@code batch.report.output-dir}で設定します。</p>
 * <p>ファイル名形式: {@code batch-stats-yyyyMMdd-HHmmss.csv}</p>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class StatisticsReportListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsReportListener.class);

    /** レポート出力ディレクトリ */
    @Value("${batch.report.output-dir:./reports}")
    private String reportOutputDir;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("【統計レポート】ジョブ開始 - ID: {}, ジョブ名: {}",
                jobExecution.getJobId(), jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            generateCsvReport(jobExecution);
            logger.info("【統計レポート】CSVレポート生成完了");
        } catch (IOException e) {
            logger.error("【統計レポート】CSVレポート生成失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * CSVレポートを生成します。
     *
     * @param jobExecution ジョブ実行情報
     * @throws IOException ファイル出力エラー
     */
    private void generateCsvReport(JobExecution jobExecution) throws IOException {
        // 出力ディレクトリの作成
        Path reportDir = Paths.get(reportOutputDir);
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir);
        }

        // ファイル名生成（batch-stats-yyyyMMdd-HHmmss.csv）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path reportFile = reportDir.resolve("batch-stats-" + timestamp + ".csv");

        // CSVファイルの書き込み
        try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
            // ヘッダー行
            writer.write("セクション,項目,値");
            writer.newLine();

            // ジョブ情報
            writer.write(String.format("ジョブ情報,ジョブID,%d", jobExecution.getJobId()));
            writer.newLine();
            writer.write(String.format("ジョブ情報,ジョブ名,%s", jobExecution.getJobInstance().getJobName()));
            writer.newLine();
            writer.write(String.format("ジョブ情報,ステータス,%s", jobExecution.getStatus()));
            writer.newLine();
            writer.write(String.format("ジョブ情報,終了コード,%s",
                    jobExecution.getExitStatus().getExitCode()));
            writer.newLine();

            // 実行時間情報
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            writer.write(String.format("実行時間,開始時刻,%s",
                    jobExecution.getStartTime() != null ?
                            jobExecution.getStartTime().format(formatter) : "N/A"));
            writer.newLine();
            writer.write(String.format("実行時間,終了時刻,%s",
                    jobExecution.getEndTime() != null ?
                            jobExecution.getEndTime().format(formatter) : "N/A"));
            writer.newLine();

            long durationMillis = jobExecution.getEndTime() != null && jobExecution.getStartTime() != null ?
                    java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis() : 0;
            long durationSeconds = durationMillis / 1000;
            writer.write(String.format("実行時間,処理時間(秒),%d", durationSeconds));
            writer.newLine();
            writer.write(String.format("実行時間,処理時間(ミリ秒),%d", durationMillis));
            writer.newLine();

            // ステップ情報
            Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
            int stepNumber = 1;
            long totalReadCount = 0;
            long totalWriteCount = 0;
            long totalSkipCount = 0;

            for (StepExecution stepExecution : stepExecutions) {
                String stepPrefix = String.format("ステップ%d(%s)", stepNumber, stepExecution.getStepName());

                writer.write(String.format("%s,ステータス,%s", stepPrefix, stepExecution.getStatus()));
                writer.newLine();
                writer.write(String.format("%s,読み込み件数,%d", stepPrefix, stepExecution.getReadCount()));
                writer.newLine();
                writer.write(String.format("%s,書き込み件数,%d", stepPrefix, stepExecution.getWriteCount()));
                writer.newLine();
                writer.write(String.format("%s,読み込みスキップ件数,%d", stepPrefix, stepExecution.getReadSkipCount()));
                writer.newLine();
                writer.write(String.format("%s,処理スキップ件数,%d", stepPrefix, stepExecution.getProcessSkipCount()));
                writer.newLine();
                writer.write(String.format("%s,書き込みスキップ件数,%d", stepPrefix, stepExecution.getWriteSkipCount()));
                writer.newLine();

                long stepSkipCount = stepExecution.getReadSkipCount() +
                        stepExecution.getProcessSkipCount() +
                        stepExecution.getWriteSkipCount();
                writer.write(String.format("%s,スキップ合計,%d", stepPrefix, stepSkipCount));
                writer.newLine();

                writer.write(String.format("%s,コミット回数,%d", stepPrefix, stepExecution.getCommitCount()));
                writer.newLine();
                writer.write(String.format("%s,ロールバック回数,%d", stepPrefix, stepExecution.getRollbackCount()));
                writer.newLine();

                // ステップごとの処理時間と速度
                long stepDuration = stepExecution.getEndTime() != null && stepExecution.getStartTime() != null ?
                        java.time.Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis() : 0;
                long stepDurationSeconds = stepDuration / 1000;
                double processingSpeed = stepDurationSeconds > 0 ?
                        (double) stepExecution.getWriteCount() / stepDurationSeconds : 0;

                writer.write(String.format("%s,処理時間(秒),%d", stepPrefix, stepDurationSeconds));
                writer.newLine();
                writer.write(String.format("%s,処理速度(件/秒),%.2f", stepPrefix, processingSpeed));
                writer.newLine();

                // エラー情報
                if (!stepExecution.getFailureExceptions().isEmpty()) {
                    String errorMessage = stepExecution.getFailureExceptions().get(0).getMessage();
                    writer.write(String.format("%s,エラー内容,\"%s\"", stepPrefix,
                            errorMessage != null ? errorMessage.replace("\"", "\"\"") : "N/A"));
                    writer.newLine();
                }

                totalReadCount += stepExecution.getReadCount();
                totalWriteCount += stepExecution.getWriteCount();
                totalSkipCount += stepSkipCount;
                stepNumber++;
            }

            // サマリー情報
            writer.write(String.format("サマリー,総読み込み件数,%d", totalReadCount));
            writer.newLine();
            writer.write(String.format("サマリー,総書き込み件数,%d", totalWriteCount));
            writer.newLine();
            writer.write(String.format("サマリー,総スキップ件数,%d", totalSkipCount));
            writer.newLine();

            double overallSpeed = durationSeconds > 0 ?
                    (double) totalWriteCount / durationSeconds : 0;
            writer.write(String.format("サマリー,全体処理速度(件/秒),%.2f", overallSpeed));
            writer.newLine();

            // 成功率
            if (totalReadCount > 0) {
                double successRate = ((double) (totalReadCount - totalSkipCount) / totalReadCount) * 100;
                writer.write(String.format("サマリー,成功率(%%),%.2f", successRate));
                writer.newLine();
            }
        }

        logger.info("【統計レポート】レポートファイル: {}", reportFile.toAbsolutePath());
    }
}
