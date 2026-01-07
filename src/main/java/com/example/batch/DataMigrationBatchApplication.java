package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * データ移行バッチアプリケーションのメインクラス。
 *
 * <p>このアプリケーションは、Spring Batchを使用して以下の処理を実行します：</p>
 * <ul>
 *   <li>TSVファイルからの顧客データ読み込み</li>
 *   <li>TSVファイルからの会社データ読み込み</li>
 *   <li>データ変換処理（フィールドマッピング、型変換）</li>
 *   <li>新データベースへのデータ書き込み</li>
 * </ul>
 *
 * <p>バッチジョブの実行方法：</p>
 * <ul>
 *   <li>Web UI: http://localhost:8080</li>
 *   <li>REST API: POST /api/batch/start</li>
 *   <li>Swing GUI: java -cp target/classes com.example.batch.gui.BatchLauncherGUI</li>
 * </ul>
 *
 * <p>注意: spring.batch.job.enabled=falseに設定されているため、
 * 起動時に自動実行はされません。GUI/REST APIから手動で実行してください。</p>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see com.example.batch.config.BatchConfiguration
 * @see com.example.batch.controller.BatchController
 */
@SpringBootApplication
public class DataMigrationBatchApplication {

    /**
     * アプリケーションのエントリーポイント。
     *
     * <p>Spring Bootアプリケーションを起動し、Webサーバーとして動作します。
     * バッチジョブはGUI/REST APIから手動で実行可能です。</p>
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(DataMigrationBatchApplication.class, args);
    }
}
