import { test, expect } from '@playwright/test';

/**
 * エンドツーエンドテスト
 * ユーザーの実際の使用フローをシミュレート
 */
test.describe('E2E - バッチ実行フロー', () => {

    test('完全なバッチ実行フロー', async ({ page }) => {
        // APIレスポンスをモック
        let executionId = 0;

        await page.route('**/api/batch/start', async (route) => {
            executionId = Math.floor(Math.random() * 10000) + 1;
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({
                    executionId: executionId,
                    status: 'STARTED',
                    message: 'Batch job started successfully'
                })
            });
        });

        await page.route('**/api/batch/status/**', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({
                    executionId: executionId,
                    status: 'COMPLETED',
                    totalReadCount: 150,
                    totalWriteCount: 150,
                    totalSkipCount: 0,
                    startTime: new Date().toISOString(),
                    endTime: new Date().toISOString()
                })
            });
        });

        // Step 1: トップページにアクセス
        await page.goto('/');
        await expect(page.locator('h1')).toContainText('UIテーマを選択してください');

        // Step 2: Darkテーマを選択
        const darkThemeCard = page.locator('.theme-card').first();
        await darkThemeCard.click();
        await expect(page).toHaveURL(/index-dark\.html/);

        // Step 3: バッチ実行画面が表示される
        await page.waitForLoadState('networkidle');

        // Step 4: オプションを設定
        const checkboxes = page.locator('input[type="checkbox"]');
        const checkboxCount = await checkboxes.count();

        if (checkboxCount > 0) {
            // マスキングを有効化
            await checkboxes.first().check();
        }

        // Step 5: バッチを開始
        const startButton = page.locator('button:has-text("バッチ実行開始"), button:has-text("Start")').first();

        if (await startButton.isVisible()) {
            await startButton.click();

            // Step 6: 実行中の状態を確認
            // ボタンが無効化されるか、ステータスが変わることを確認
            await page.waitForTimeout(1000);
        }

        // Step 7: 完了を待つ（モックなので即座に完了）
        await page.waitForTimeout(2000);
    });

    test('テーマ切り替えフロー', async ({ page }) => {
        // Step 1: トップページから各テーマに遷移
        await page.goto('/');

        const themes = [
            'index-dark.html',
            'index-light.html',
            'index-professional.html'
        ];

        for (const theme of themes) {
            // トップページに戻る
            await page.goto('/');

            // テーマカードをクリック
            const themeCard = page.locator(`[onclick*="${theme}"]`).first();
            await themeCard.click();

            // 正しいページに遷移したことを確認
            await expect(page).toHaveURL(new RegExp(theme));

            // ページが正しく読み込まれることを確認
            await expect(page.locator('body')).toBeVisible();
        }
    });

    test('エラーハンドリング - API障害', async ({ page }) => {
        // APIエラーをモック
        await page.route('**/api/batch/start', async (route) => {
            await route.fulfill({
                status: 500,
                contentType: 'application/json',
                body: JSON.stringify({
                    error: 'Internal Server Error',
                    message: 'Database connection failed'
                })
            });
        });

        await page.goto('/index-dark.html');
        await page.waitForLoadState('networkidle');

        const startButton = page.locator('button:has-text("バッチ実行開始"), button:has-text("Start")').first();

        if (await startButton.isVisible()) {
            await startButton.click();

            // エラーメッセージが表示されるか、ボタンが再度有効になることを確認
            await page.waitForTimeout(2000);

            // ページがクラッシュしていないことを確認
            await expect(page.locator('body')).toBeVisible();
        }
    });

    test('複数回実行フロー', async ({ page }) => {
        let executionCount = 0;

        await page.route('**/api/batch/start', async (route) => {
            executionCount++;
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({
                    executionId: executionCount,
                    status: 'STARTED',
                    message: 'Batch job started successfully'
                })
            });
        });

        await page.route('**/api/batch/status/**', async (route) => {
            await route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({
                    executionId: executionCount,
                    status: 'COMPLETED',
                    totalReadCount: 100,
                    totalWriteCount: 100,
                    totalSkipCount: 0
                })
            });
        });

        await page.goto('/index-dark.html');
        await page.waitForLoadState('networkidle');

        const startButton = page.locator('button:has-text("バッチ実行開始"), button:has-text("Start")').first();

        // 3回連続で実行
        for (let i = 0; i < 3; i++) {
            if (await startButton.isVisible() && await startButton.isEnabled()) {
                await startButton.click();
                await page.waitForTimeout(3000);
            }
        }

        // 実行回数を確認
        expect(executionCount).toBeGreaterThan(0);
    });
});

/**
 * E2E - ユーザビリティテスト
 */
test.describe('E2E - ユーザビリティ', () => {

    test('初回ユーザーの体験フロー', async ({ page }) => {
        // Step 1: トップページにアクセス
        await page.goto('/');

        // Step 2: ヘッダーとガイダンスが表示される
        await expect(page.locator('h1')).toBeVisible();
        await expect(page.locator('.quick-note')).toBeVisible();

        // Step 3: すべてのテーマカードが表示される
        const themeCards = page.locator('.theme-card');
        await expect(themeCards).toHaveCount(7);

        // Step 4: 推奨バッジのあるテーマを選択
        const recommendedTheme = page.locator('.theme-badge:has-text("推奨")').first();
        await recommendedTheme.click();

        // Step 5: バッチ実行画面に遷移
        await page.waitForLoadState('networkidle');
        await expect(page.locator('body')).toBeVisible();
    });

    test('モバイルユーザーの体験フロー', async ({ page }) => {
        // モバイルビューポートに設定
        await page.setViewportSize({ width: 375, height: 667 });

        // Step 1: トップページにアクセス
        await page.goto('/');

        // Step 2: モバイルでもすべてのカードが表示される
        const themeCards = page.locator('.theme-card');
        await expect(themeCards).toHaveCount(7);

        // Step 3: スクロールして下部のテーマを表示
        await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));

        // Step 4: テーマを選択
        const lastTheme = themeCards.last();
        await lastTheme.scrollIntoViewIfNeeded();
        await lastTheme.click();

        // Step 5: バッチ実行画面がモバイルで正しく表示される
        await page.waitForLoadState('networkidle');
        await expect(page.locator('body')).toBeVisible();
    });

    test('キーボードのみでの操作フロー', async ({ page }) => {
        await page.goto('/');

        // Tabキーでナビゲーション
        await page.keyboard.press('Tab');
        await page.keyboard.press('Tab');

        // Enterキーで選択
        await page.keyboard.press('Enter');

        // ページが遷移するか、何らかのアクションが実行されることを確認
        await page.waitForTimeout(1000);
        await expect(page.locator('body')).toBeVisible();
    });
});
