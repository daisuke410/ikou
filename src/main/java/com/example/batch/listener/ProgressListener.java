package com.example.batch.listener;

import com.example.batch.dto.ProgressMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import org.springframework.batch.item.Chunk;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * バッチ処理の進捗をリアルタイムで表示するリスナー。
 *
 * <p>このリスナーは、チャンク処理ごとに以下の情報を出力します：</p>
 * <ul>
 *   <li>読み込み件数（累計）</li>
 *   <li>書き込み件数（累計）</li>
 *   <li>進捗率（推定）</li>
 *   <li>処理速度（件/秒）</li>
 * </ul>
 *
 * @param <T> 読み込みアイテムの型
 * @param <S> 書き込みアイテムの型
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class ProgressListener<T, S> implements ChunkListener, ItemReadListener<T>, ItemWriteListener<S> {

    private static final Logger logger = LoggerFactory.getLogger(ProgressListener.class);

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private final AtomicLong readCount = new AtomicLong(0);
    private final AtomicLong writeCount = new AtomicLong(0);
    private final AtomicLong skipCount = new AtomicLong(0);
    private long startTime = 0;
    private long lastReportTime = 0;
    private Long executionId;
    private String currentStepName;
    private static final long REPORT_INTERVAL_MS = 5000; // 5秒ごとに進捗報告

    @Override
    public void beforeChunk(ChunkContext context) {
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
            lastReportTime = startTime;

            // 実行IDとステップ名を取得
            executionId = context.getStepContext().getStepExecution().getJobExecutionId();
            currentStepName = context.getStepContext().getStepName();

            logger.info("【進捗】バッチ処理開始 - ExecutionId: {}, Step: {}", executionId, currentStepName);
            sendProgressUpdate("STARTED");
        }
    }

    @Override
    public void afterChunk(ChunkContext context) {
        long currentTime = System.currentTimeMillis();

        // 5秒ごとまたはチャンク完了時に進捗を表示
        if (currentTime - lastReportTime >= REPORT_INTERVAL_MS) {
            reportProgress(currentTime);
            lastReportTime = currentTime;
        }
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        logger.error("【進捗】チャンク処理エラー発生");
    }

    @Override
    public void afterRead(T item) {
        readCount.incrementAndGet();
    }

    @Override
    public void beforeRead() {
        // 処理なし
    }

    @Override
    public void onReadError(Exception ex) {
        skipCount.incrementAndGet();
        logger.warn("【進捗】読み込みエラー: {}", ex.getMessage());
    }

    @Override
    public void beforeWrite(Chunk<? extends S> items) {
        // 処理なし
    }

    @Override
    public void afterWrite(Chunk<? extends S> items) {
        writeCount.addAndGet(items.size());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends S> items) {
        logger.warn("【進捗】書き込みエラー: {} 件", items.size());
    }

    /**
     * 進捗情報をログに出力します。
     *
     * @param currentTime 現在時刻（ミリ秒）
     */
    private void reportProgress(long currentTime) {
        long elapsedTime = currentTime - startTime;
        long currentReadCount = readCount.get();
        long currentWriteCount = writeCount.get();

        // 処理速度計算（件/秒）
        double readSpeed = elapsedTime > 0 ? (currentReadCount * 1000.0 / elapsedTime) : 0;
        double writeSpeed = elapsedTime > 0 ? (currentWriteCount * 1000.0 / elapsedTime) : 0;

        // 経過時間（秒）
        long elapsedSeconds = elapsedTime / 1000;

        logger.info("【進捗】読込: {} 件, 書込: {} 件 | 経過時間: {} 秒 | 速度: {}/{} 件/秒",
            currentReadCount,
            currentWriteCount,
            elapsedSeconds,
            String.format("%.1f", readSpeed),
            String.format("%.1f", writeSpeed)
        );

        // WebSocketで進捗を送信
        sendProgressUpdate("IN_PROGRESS");
    }

    /**
     * WebSocketで進捗更新を送信します。
     */
    private void sendProgressUpdate(String status) {
        if (messagingTemplate == null || executionId == null) {
            return;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        long currentReadCount = readCount.get();
        long currentWriteCount = writeCount.get();
        long currentSkipCount = skipCount.get();

        double readSpeed = elapsedTime > 0 ? (currentReadCount * 1000.0 / elapsedTime) : 0;
        double writeSpeed = elapsedTime > 0 ? (currentWriteCount * 1000.0 / elapsedTime) : 0;
        long elapsedSeconds = elapsedTime / 1000;

        ProgressMessage message = new ProgressMessage(
            executionId,
            currentStepName,
            status,
            currentReadCount,
            currentWriteCount,
            currentSkipCount,
            null, // 進捗率は総件数が分からないため未設定
            readSpeed,
            writeSpeed,
            elapsedSeconds,
            LocalDateTime.now(),
            String.format("読込: %d, 書込: %d, スキップ: %d", currentReadCount, currentWriteCount, currentSkipCount)
        );

        messagingTemplate.convertAndSend("/topic/progress/" + executionId, message);
    }

    /**
     * 処理完了時の最終進捗を表示します。
     */
    public void reportFinalProgress() {
        long currentTime = System.currentTimeMillis();
        reportProgress(currentTime);
    }

    /**
     * リスナーをリセットします（新しいステップ開始時）。
     */
    public void reset() {
        readCount.set(0);
        writeCount.set(0);
        skipCount.set(0);
        startTime = 0;
        lastReportTime = 0;
    }
}
