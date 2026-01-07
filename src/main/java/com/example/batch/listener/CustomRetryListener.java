package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * リトライ処理を監視するリスナー。
 *
 * <p>一時的なエラー（DB接続タイムアウトなど）が発生した際の
 * リトライ処理をログに記録します。</p>
 *
 * <p>リトライ設定：</p>
 * <ul>
 *   <li>最大リトライ回数: 3回</li>
 *   <li>対象例外: DeadlockLoserDataAccessException, TransientDataAccessException</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class CustomRetryListener implements org.springframework.retry.RetryListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomRetryListener.class);

    /**
     * リトライ開始時に呼び出されます。
     *
     * @param context リトライコンテキスト
     * @param callback リトライコールバック
     * @param <T> リトライ対象の型
     * @param <E> 例外の型
     * @return 常にtrue（リトライを継続）
     */
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        logger.debug("リトライ処理開始");
        return true;
    }

    /**
     * リトライ終了時に呼び出されます。
     *
     * @param context リトライコンテキスト
     * @param callback リトライコールバック
     * @param throwable 最後に発生した例外（成功時はnull）
     * @param <T> リトライ対象の型
     * @param <E> 例外の型
     */
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            logger.warn("リトライ失敗: {} 回試行後もエラー継続", context.getRetryCount());
        } else {
            if (context.getRetryCount() > 0) {
                logger.info("リトライ成功: {} 回目で成功", context.getRetryCount());
            }
        }
    }

    /**
     * リトライ実行時に呼び出されます。
     *
     * @param context リトライコンテキスト
     * @param callback リトライコールバック
     * @param throwable 発生した例外
     * @param <T> リトライ対象の型
     * @param <E> 例外の型
     */
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        logger.warn("リトライ実行中 ({}/3): {}", context.getRetryCount(), throwable.getMessage());
    }
}
