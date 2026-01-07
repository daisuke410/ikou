package com.example.batch.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * バッチジョブを実行・監視するためのRESTコントローラー。
 *
 * <p>
 * このコントローラーは以下のエンドポイントを提供します：
 * </p>
 * <ul>
 * <li>POST /api/batch/start - バッチジョブを開始</li>
 * <li>POST /api/batch/stop/{executionId} - バッチジョブを停止</li>
 * <li>GET /api/batch/status/{executionId} - ジョブ実行状態を取得</li>
 * <li>GET /api/batch/latest - 最新のジョブ実行状態を取得</li>
 * <li>GET /api/batch/history - ジョブ実行履歴を取得</li>
 * <li>POST /api/batch/validate - データ件数を事前チェック</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/batch")
@CrossOrigin(origins = "*")
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job dataMigrationJob;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobRepository jobRepository;

    @Autowired(required = false)
    private JobOperator jobOperator;

    // 実行中のジョブを追跡
    private static final Map<Long, JobExecution> runningJobs = new ConcurrentHashMap<>();

    /**
     * バッチジョブを開始します。
     *
     * @param params リクエストパラメータ（maskingEnabled等）
     * @return ジョブ実行情報
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startJob(@RequestBody(required = false) Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 並行実行制御: 実行中のジョブがあるかチェック
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("dataMigrationJob");
            if (!runningExecutions.isEmpty()) {
                JobExecution runningExecution = runningExecutions.iterator().next();
                String runningInfo = String.format("ID: %d, Status: %s, StartTime: %s",
                        runningExecution.getId(), runningExecution.getStatus(), runningExecution.getStartTime());

                System.out.println("Conflict: Running job found - " + runningInfo);

                response.put("success", false);
                response.put("message", "既にバッチジョブが実行中です。\n詳細: " + runningInfo);
                response.put("runningExecutionId", runningExecution.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // ジョブパラメータの構築（実行ごとに一意にするため現在時刻を使用）
            JobParametersBuilder builder = new JobParametersBuilder();
            builder.addLong("timestamp", System.currentTimeMillis());

            // リクエストパラメータをジョブパラメータに追加
            if (params != null) {
                params.forEach((key, value) -> builder.addString(key, value));
            }

            JobParameters jobParameters = builder.toJobParameters();

            // ジョブを非同期で実行
            JobExecution execution = jobLauncher.run(dataMigrationJob, jobParameters);

            // 実行中のジョブとして登録
            runningJobs.put(execution.getId(), execution);

            response.put("success", true);
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus().name());
            response.put("message", "バッチジョブを開始しました");
            response.put("startTime", execution.getStartTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "バッチジョブの開始に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 実行中のバッチジョブを停止します。
     *
     * @param executionId ジョブ実行ID
     * @return 停止結果
     */
    @PostMapping("/stop/{executionId}")
    public ResponseEntity<Map<String, Object>> stopJob(@PathVariable Long executionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobExecution execution = jobExplorer.getJobExecution(executionId);

            if (execution == null) {
                response.put("success", false);
                response.put("message", "指定されたジョブ実行が見つかりません");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!execution.isRunning()) {
                response.put("success", false);
                response.put("message", "ジョブは実行中ではありません");
                response.put("status", execution.getStatus().name());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // ジョブを停止
            execution.setStatus(BatchStatus.STOPPING);
            jobRepository.update(execution);

            // 実行中のジョブリストから削除
            runningJobs.remove(executionId);

            response.put("success", true);
            response.put("message", "ジョブの停止を要求しました");
            response.put("executionId", executionId);
            response.put("status", "STOPPING");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ジョブの停止に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 指定されたジョブ実行IDの状態を取得します。
     *
     * @param executionId ジョブ実行ID
     * @return ジョブ実行状態
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long executionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            JobExecution execution = jobExplorer.getJobExecution(executionId);

            if (execution == null) {
                response.put("success", false);
                response.put("message", "指定されたジョブ実行が見つかりません");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus().name());
            response.put("startTime", execution.getStartTime());
            response.put("endTime", execution.getEndTime());
            response.put("exitStatus", execution.getExitStatus().getExitCode());

            // ステップ情報を追加
            long totalReadCount = 0;
            long totalWriteCount = 0;
            long totalSkipCount = 0;

            execution.getStepExecutions().forEach(step -> {
                response.put("step_" + step.getStepName() + "_status", step.getStatus().name());
                response.put("step_" + step.getStepName() + "_readCount", step.getReadCount());
                response.put("step_" + step.getStepName() + "_writeCount", step.getWriteCount());
            });

            // 合計を計算
            for (var step : execution.getStepExecutions()) {
                totalReadCount += step.getReadCount();
                totalWriteCount += step.getWriteCount();
                totalSkipCount += step.getReadSkipCount() + step.getProcessSkipCount() + step.getWriteSkipCount();
            }

            response.put("totalReadCount", totalReadCount);
            response.put("totalWriteCount", totalWriteCount);
            response.put("totalSkipCount", totalSkipCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ジョブ実行状態の取得に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 最新のジョブ実行状態を取得します。
     *
     * @return 最新のジョブ実行状態
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestJobStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            Set<JobExecution> executions = jobExplorer.findRunningJobExecutions("dataMigrationJob");

            // 実行中のジョブがある場合
            if (!executions.isEmpty()) {
                JobExecution execution = executions.iterator().next();
                response.put("success", true);
                response.put("executionId", execution.getId());
                response.put("status", execution.getStatus().name());
                response.put("message", "ジョブ実行中");
                return ResponseEntity.ok(response);
            }

            // 最新の完了済みジョブを取得
            var jobInstances = jobExplorer.findJobInstancesByJobName("dataMigrationJob", 0, 1);
            if (!jobInstances.isEmpty()) {
                var jobInstance = jobInstances.get(0);
                var jobExecutions = jobExplorer.getJobExecutions(jobInstance);
                if (!jobExecutions.isEmpty()) {
                    JobExecution execution = jobExecutions.get(0);
                    response.put("success", true);
                    response.put("executionId", execution.getId());
                    response.put("status", execution.getStatus().name());
                    response.put("message", "最新のジョブ実行状態");
                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            response.put("message", "ジョブ実行履歴が見つかりません");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ジョブ実行状態の取得に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ジョブ実行履歴を取得します。
     *
     * @param page ページ番号（0始まり）
     * @param size ページサイズ
     * @return ジョブ実行履歴のリスト
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getJobHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName("dataMigrationJob", page, size);
            List<Map<String, Object>> historyList = new ArrayList<>();

            for (JobInstance instance : jobInstances) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                for (JobExecution execution : executions) {
                    Map<String, Object> historyItem = new HashMap<>();
                    historyItem.put("executionId", execution.getId());
                    historyItem.put("instanceId", instance.getId());
                    historyItem.put("status", execution.getStatus().name());
                    historyItem.put("exitStatus", execution.getExitStatus().getExitCode());
                    historyItem.put("startTime", execution.getStartTime());
                    historyItem.put("endTime", execution.getEndTime());

                    // 処理時間を計算（ミリ秒）
                    Long duration = null;
                    if (execution.getEndTime() != null && execution.getStartTime() != null) {
                        duration = java.time.Duration.between(
                                execution.getStartTime().toInstant(java.time.ZoneOffset.UTC),
                                execution.getEndTime().toInstant(java.time.ZoneOffset.UTC)).toMillis();
                    }
                    historyItem.put("duration", duration);

                    // ステップ情報を集計
                    long totalRead = 0;
                    long totalWrite = 0;
                    long totalSkip = 0;

                    for (StepExecution step : execution.getStepExecutions()) {
                        totalRead += step.getReadCount();
                        totalWrite += step.getWriteCount();
                        totalSkip += step.getReadSkipCount() + step.getProcessSkipCount() + step.getWriteSkipCount();
                    }

                    historyItem.put("totalReadCount", totalRead);
                    historyItem.put("totalWriteCount", totalWrite);
                    historyItem.put("totalSkipCount", totalSkip);

                    historyList.add(historyItem);
                }
            }

            response.put("success", true);
            response.put("history", historyList);
            response.put("page", page);
            response.put("size", size);
            response.put("totalElements", historyList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "ジョブ実行履歴の取得に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * データ件数を事前にチェックします（ドライラン）。
     *
     * @return データ件数情報
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateData() {
        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: 実際にはTSVファイルを読み込んで件数をカウント
            // ここでは簡易実装として固定値を返す
            response.put("success", true);
            response.put("message", "データ検証を実行しました");
            response.put("customerCount", "検証機能は実装中です");
            response.put("companyCount", "検証機能は実装中です");
            response.put("estimatedDuration", "不明");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "データ検証に失敗しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
