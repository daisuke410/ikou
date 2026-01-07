package com.example.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

/**
 * データ処理中にスキップされたアイテムを記録するリスナー。
 *
 * <p>このリスナーは、以下の状況でスキップされたデータを記録します：</p>
 * <ul>
 *   <li>読み込みエラー（ファイル形式不正など）</li>
 *   <li>処理エラー（データ変換失敗など）</li>
 *   <li>書き込みエラー（DB制約違反など）</li>
 * </ul>
 *
 * <p>本番運用時は、スキップされたデータを別ファイルに出力して
 * 後で手動で確認・修正できるようにすることを推奨します。</p>
 *
 * @param <T> 入力アイテムの型
 * @param <S> 出力アイテムの型
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class CustomSkipListener<T, S> implements org.springframework.batch.core.SkipListener<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(CustomSkipListener.class);

    /**
     * データ読み込み時にスキップされた場合に呼び出されます。
     *
     * @param t 読み込みエラーの原因となった例外
     */
    @Override
    public void onSkipInRead(Throwable t) {
        logger.warn("【読み込みスキップ】エラー: {}", t.getMessage());
        logger.debug("スタックトレース: ", t);
    }

    /**
     * データ処理時にスキップされた場合に呼び出されます。
     *
     * @param item スキップされたアイテム
     * @param t 処理エラーの原因となった例外
     */
    @Override
    public void onSkipInProcess(T item, Throwable t) {
        logger.warn("【処理スキップ】アイテム: {}, エラー: {}", item, t.getMessage());
        logger.debug("スタックトレース: ", t);
        // TODO: 本番環境では、スキップされたデータをエラーファイルに出力
    }

    /**
     * データ書き込み時にスキップされた場合に呼び出されます。
     *
     * @param item スキップされたアイテム
     * @param t 書き込みエラーの原因となった例外
     */
    @Override
    public void onSkipInWrite(S item, Throwable t) {
        logger.warn("【書き込みスキップ】アイテム: {}, エラー: {}", item, t.getMessage());
        logger.debug("スタックトレース: ", t);
        // TODO: 本番環境では、スキップされたデータをエラーファイルに出力
    }
}
