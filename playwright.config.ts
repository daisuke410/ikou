import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright設定ファイル
 * Spring Batch Data Migration Applicationのテスト設定
 */
export default defineConfig({
  testDir: './tests',
  
  /* テストの並列実行数 */
  fullyParallel: true,
  
  /* CI環境でのみテスト失敗時にリトライ */
  forbidOnly: !!process.env.CI,
  
  /* テスト失敗時のリトライ回数 */
  retries: process.env.CI ? 2 : 0,
  
  /* 並列ワーカー数 */
  workers: process.env.CI ? 1 : undefined,
  
  /* レポーター設定 */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
    ['json', { outputFile: 'test-results.json' }]
  ],
  
  /* 共通設定 */
  use: {
    /* ベースURL */
    baseURL: 'http://localhost:8080',
    
    /* スクリーンショット設定 */
    screenshot: 'only-on-failure',
    
    /* ビデオ録画設定 */
    video: 'retain-on-failure',
    
    /* トレース設定 */
    trace: 'on-first-retry',
    
    /* タイムアウト設定 */
    actionTimeout: 10000,
  },

  /* テストタイムアウト */
  timeout: 60000,

  /* プロジェクト設定（複数ブラウザでテスト） */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },

    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },

    /* モバイルビューポートのテスト */
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],

  /* ローカル開発サーバーの起動設定 */
  webServer: {
    command: 'mvn spring-boot:run',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
});
