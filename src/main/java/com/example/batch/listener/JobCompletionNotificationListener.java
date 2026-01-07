package com.example.batch.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * バッチジョブの実行前後に処理を行うリスナークラス。
 *
 * <p>ジョブの開始・終了時に以下の情報をコンソールに出力します：</p>
 * <ul>
 *   <li>ジョブ名</li>
 *   <li>開始時刻・終了時刻</li>
 *   <li>処理ステータス（COMPLETED / FAILED）</li>
 *   <li>読込件数・書込件数（全ステップの合計）</li>
 *   <li>処理時間（ミリ秒）</li>
 *   <li>エラー情報（失敗時）</li>
 * </ul>
 *
 * @see org.springframework.batch.core.JobExecutionListener
 */
@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    /**
     * ジョブ開始前に実行される処理。
     *
     * <p>ジョブ名と開始時刻をコンソールに出力します。</p>
     *
     * @param jobExecution ジョブ実行情報
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("===========================================");
        System.out.println("データ移行バッチ開始");
        System.out.println("ジョブ名: " + jobExecution.getJobInstance().getJobName());
        System.out.println("開始時刻: " + LocalDateTime.now());
        System.out.println("===========================================");
    }

    /**
     * ジョブ終了後に実行される処理。
     *
     * <p>ジョブのステータスに応じて以下の情報を出力します：</p>
     * <ul>
     *   <li>成功時（COMPLETED）: 読込件数、書込件数、処理時間</li>
     *   <li>失敗時（FAILED）: エラー情報とスタックトレース</li>
     * </ul>
     *
     * @param jobExecution ジョブ実行情報
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            // 処理時間を計算
            LocalDateTime startTime = jobExecution.getStartTime();
            LocalDateTime endTime = jobExecution.getEndTime();
            Duration duration = Duration.between(startTime, endTime);

            System.out.println("===========================================");
            System.out.println("データ移行バッチ完了");
            System.out.println("ステータス: " + jobExecution.getStatus());

            // 全ステップの読込件数を合計
            System.out.println("読込件数: " + jobExecution.getStepExecutions().stream()
                    .mapToLong(stepExecution -> stepExecution.getReadCount())
                    .sum());

            // 全ステップの書込件数を合計
            System.out.println("書込件数: " + jobExecution.getStepExecutions().stream()
                    .mapToLong(stepExecution -> stepExecution.getWriteCount())
                    .sum());

            System.out.println("処理時間: " + duration.toMillis() + " ms");
            System.out.println("===========================================");
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            // エラー情報を出力
            System.err.println("===========================================");
            System.err.println("データ移行バッチ失敗");
            System.err.println("エラー情報:");
            jobExecution.getAllFailureExceptions().forEach(throwable -> {
                System.err.println(throwable.getMessage());
                throwable.printStackTrace();
            });
            System.err.println("===========================================");
        }
    }
}
