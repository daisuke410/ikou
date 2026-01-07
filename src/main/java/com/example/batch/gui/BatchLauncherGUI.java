package com.example.batch.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Spring Batchジョブを実行・監視するためのSwing GUIアプリケーション。
 *
 * <p>
 * このGUIは以下の機能を提供します：
 * </p>
 * <ul>
 * <li>バッチジョブの開始ボタン</li>
 * <li>データマスク機能の有効/無効切り替え</li>
 * <li>リアルタイムステータス表示</li>
 * <li>処理件数の表示</li>
 * <li>ログ出力エリア</li>
 * </ul>
 *
 * <p>
 * 使用方法：
 * </p>
 * 
 * <pre>
 * 1. Spring Bootアプリケーションを起動（mvn spring-boot:run）
 * 2. このGUIアプリケーションを起動（java -cp target/classes com.example.batch.gui.BatchLauncherGUI）
 * </pre>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
public class BatchLauncherGUI extends JFrame {

    private static final String API_BASE_URL = "http://localhost:8080/api/batch";

    private JButton startButton;
    private JCheckBox maskingCheckBox;
    private JCheckBox upsertCheckBox;
    private JLabel statusLabel;
    private JLabel executionIdLabel;
    private JLabel readCountLabel;
    private JLabel writeCountLabel;
    private JLabel skipCountLabel;
    private JTextArea logArea;
    private JProgressBar progressBar;

    private Long currentExecutionId = null;
    private Timer statusTimer;

    /**
     * GUIを初期化します。
     */
    public BatchLauncherGUI() {
        setTitle("Spring Batch データ移行ツール");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // メインパネル
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // コントロールパネル
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // ステータスパネル
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.CENTER);

        // ログパネル
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setLocationRelativeTo(null);
    }

    /**
     * コントロールパネルを作成します。
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("コントロール"));

        startButton = new JButton("バッチ開始");
        startButton.setPreferredSize(new Dimension(150, 40));
        startButton.setFont(new Font("MS Gothic", Font.BOLD, 14));
        startButton.addActionListener(new StartButtonListener());

        maskingCheckBox = new JCheckBox("データマスク有効化（テスト用）");
        maskingCheckBox.setFont(new Font("MS Gothic", Font.PLAIN, 12));

        upsertCheckBox = new JCheckBox("Upsert有効化（上書き実行）");
        upsertCheckBox.setFont(new Font("MS Gothic", Font.PLAIN, 12));
        upsertCheckBox.setToolTipText("同じデータで再実行する際、既存データを上書きします");

        panel.add(startButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(maskingCheckBox);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(upsertCheckBox);

        return panel;
    }

    /**
     * ステータスパネルを作成します。
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("実行状態"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("MS Gothic", Font.PLAIN, 12);
        Font valueFont = new Font("MS Gothic", Font.BOLD, 12);

        // 実行ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel label1 = new JLabel("実行ID:");
        label1.setFont(labelFont);
        panel.add(label1, gbc);

        gbc.gridx = 1;
        executionIdLabel = new JLabel("-");
        executionIdLabel.setFont(valueFont);
        panel.add(executionIdLabel, gbc);

        // ステータス
        gbc.gridx = 2;
        JLabel label2 = new JLabel("ステータス:");
        label2.setFont(labelFont);
        panel.add(label2, gbc);

        gbc.gridx = 3;
        statusLabel = new JLabel("待機中");
        statusLabel.setFont(valueFont);
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel, gbc);

        // 読込件数
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel label3 = new JLabel("読込件数:");
        label3.setFont(labelFont);
        panel.add(label3, gbc);

        gbc.gridx = 1;
        readCountLabel = new JLabel("0");
        readCountLabel.setFont(valueFont);
        panel.add(readCountLabel, gbc);

        // 書込件数
        gbc.gridx = 2;
        JLabel label4 = new JLabel("書込件数:");
        label4.setFont(labelFont);
        panel.add(label4, gbc);

        gbc.gridx = 3;
        writeCountLabel = new JLabel("0");
        writeCountLabel.setFont(valueFont);
        panel.add(writeCountLabel, gbc);

        // スキップ件数
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel label5 = new JLabel("スキップ件数:");
        label5.setFont(labelFont);
        panel.add(label5, gbc);

        gbc.gridx = 1;
        skipCountLabel = new JLabel("0");
        skipCountLabel.setFont(valueFont);
        skipCountLabel.setForeground(Color.RED);
        panel.add(skipCountLabel, gbc);

        // プログレスバー
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        panel.add(progressBar, gbc);

        return panel;
    }

    /**
     * ログパネルを作成します。
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("ログ"));
        panel.setPreferredSize(new Dimension(0, 200));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("MS Gothic", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * ログメッセージを追加します。
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] "
                    + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * バッチ開始ボタンのリスナー。
     */
    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            startButton.setEnabled(false);
            maskingCheckBox.setEnabled(false);
            upsertCheckBox.setEnabled(false);
            progressBar.setIndeterminate(true);
            log("バッチジョブを開始します...");

            new Thread(() -> {
                try {
                    URL url = new URL(API_BASE_URL + "/start");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // リクエストボディ
                    StringBuilder jsonBody = new StringBuilder("{");
                    if (maskingCheckBox.isSelected()) {
                        jsonBody.append("\"maskingEnabled\":\"true\"");
                    }
                    if (upsertCheckBox.isSelected()) {
                        if (jsonBody.length() > 1) {
                            jsonBody.append(",");
                        }
                        jsonBody.append("\"upsertEnabled\":\"true\"");
                    }
                    jsonBody.append("}");

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.toString().getBytes());
                        os.flush();
                    }

                    int responseCode = conn.getResponseCode();
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    if (responseCode == 200) {
                        String responseStr = response.toString();
                        // 簡易的なJSON解析（実行ID抽出）
                        int idStart = responseStr.indexOf("\"executionId\":") + 14;
                        int idEnd = responseStr.indexOf(",", idStart);
                        currentExecutionId = Long.parseLong(responseStr.substring(idStart, idEnd).trim());

                        SwingUtilities.invokeLater(() -> {
                            executionIdLabel.setText(currentExecutionId.toString());
                            statusLabel.setText("実行中");
                            statusLabel.setForeground(Color.ORANGE);
                        });

                        log("バッチジョブを開始しました（実行ID: " + currentExecutionId + "）");

                        // ステータス監視タイマーを開始
                        startStatusPolling();

                    } else {
                        log("エラー: バッチジョブの開始に失敗しました（HTTPコード: " + responseCode + "）");
                        SwingUtilities.invokeLater(() -> {
                            startButton.setEnabled(true);
                            maskingCheckBox.setEnabled(true);
                            upsertCheckBox.setEnabled(true);
                            progressBar.setIndeterminate(false);
                        });
                    }

                } catch (Exception ex) {
                    log("エラー: " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        startButton.setEnabled(true);
                        maskingCheckBox.setEnabled(true);
                        upsertCheckBox.setEnabled(true);
                        progressBar.setIndeterminate(false);
                    });
                }
            }).start();
        }
    }

    /**
     * ステータスポーリングを開始します。
     */
    private void startStatusPolling() {
        if (statusTimer != null) {
            statusTimer.cancel();
        }

        statusTimer = new Timer();
        statusTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateStatus();
            }
        }, 2000, 3000); // 2秒後から3秒ごとに実行
    }

    /**
     * ステータスを更新します。
     */
    private void updateStatus() {
        if (currentExecutionId == null) {
            return;
        }

        try {
            URL url = new URL(API_BASE_URL + "/status/" + currentExecutionId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String responseStr = response.toString();

            // 簡易的なJSON解析
            String status = extractValue(responseStr, "\"status\":\"", "\"");
            String readCount = extractValue(responseStr, "\"totalReadCount\":", ",");
            String writeCount = extractValue(responseStr, "\"totalWriteCount\":", ",");
            String skipCount = extractValue(responseStr, "\"totalSkipCount\":", ",");

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(status);

                if ("COMPLETED".equals(status)) {
                    statusLabel.setForeground(Color.GREEN);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    startButton.setEnabled(true);
                    maskingCheckBox.setEnabled(true);
                    upsertCheckBox.setEnabled(true);
                    log("バッチジョブが正常に完了しました");
                    if (statusTimer != null) {
                        statusTimer.cancel();
                    }
                } else if ("FAILED".equals(status)) {
                    statusLabel.setForeground(Color.RED);
                    progressBar.setIndeterminate(false);
                    startButton.setEnabled(true);
                    maskingCheckBox.setEnabled(true);
                    upsertCheckBox.setEnabled(true);
                    log("バッチジョブが失敗しました");
                    if (statusTimer != null) {
                        statusTimer.cancel();
                    }
                }

                if (readCount != null)
                    readCountLabel.setText(readCount);
                if (writeCount != null)
                    writeCountLabel.setText(writeCount);
                if (skipCount != null)
                    skipCountLabel.setText(skipCount);
            });

        } catch (Exception ex) {
            // ステータス取得エラーは無視（次回リトライ）
        }
    }

    /**
     * JSON文字列から値を抽出します（簡易実装）。
     */
    private String extractValue(String json, String startPattern, String endPattern) {
        try {
            int start = json.indexOf(startPattern);
            if (start == -1)
                return null;
            start += startPattern.length();

            int end = json.indexOf(endPattern, start);
            if (end == -1)
                end = json.length();

            return json.substring(start, end).trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * メイン関数（GUI起動）。
     */
    public static void main(String[] args) {
        try {
            // ルック&フィールをシステムデフォルトに設定
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            BatchLauncherGUI gui = new BatchLauncherGUI();
            gui.setVisible(true);
        });
    }
}
