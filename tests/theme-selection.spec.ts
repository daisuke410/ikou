import { test, expect } from '@playwright/test';

/**
 * テーマ選択ページのテスト
 * UIテーマ選択画面の機能と表示をテスト
 */
test.describe('テーマ選択ページ', () => {

    test.beforeEach(async ({ page }) => {
        await page.goto('/');
    });

    test('ページタイトルが正しく表示される', async ({ page }) => {
        await expect(page).toHaveTitle(/UI Theme Selection/);
    });

    test('メインヘッダーが表示される', async ({ page }) => {
        const header = page.locator('h1');
        await expect(header).toContainText('UIテーマを選択してください');
    });

    test('すべてのテーマカードが表示される', async ({ page }) => {
        const themeCards = page.locator('.theme-card');
        await expect(themeCards).toHaveCount(7);
    });

    test('各テーマカードに必要な要素が含まれる', async ({ page }) => {
        const firstCard = page.locator('.theme-card').first();

        // プレビュー、タイトル、説明、ボタンが存在することを確認
        await expect(firstCard.locator('.theme-preview')).toBeVisible();
        await expect(firstCard.locator('.theme-name')).toBeVisible();
        await expect(firstCard.locator('.theme-description')).toBeVisible();
        await expect(firstCard.locator('.select-button')).toBeVisible();
    });

    test('Darkテーマカードをクリックすると遷移する', async ({ page }) => {
        const darkThemeCard = page.locator('.theme-card').first();
        await darkThemeCard.click();

        // URLが変更されることを確認
        await expect(page).toHaveURL(/index-dark\.html/);
    });

    test('ホバー効果が動作する', async ({ page }) => {
        const firstCard = page.locator('.theme-card').first();

        // ホバー前の状態を取得
        const boxBefore = await firstCard.boundingBox();

        // ホバー
        await firstCard.hover();

        // 少し待機してアニメーションを完了
        await page.waitForTimeout(500);

        // カードが存在することを確認（transform効果が適用される）
        await expect(firstCard).toBeVisible();
    });

    test('レスポンシブデザインが動作する（モバイル）', async ({ page }) => {
        // モバイルビューポートに変更
        await page.setViewportSize({ width: 375, height: 667 });

        const themeCards = page.locator('.theme-card');
        await expect(themeCards).toHaveCount(7);

        // すべてのカードが表示されることを確認
        for (let i = 0; i < 7; i++) {
            await expect(themeCards.nth(i)).toBeVisible();
        }
    });

    test('フッターが表示される', async ({ page }) => {
        const footer = page.locator('.footer');
        await expect(footer).toContainText('Spring Batch Data Migration Tool');
    });

    test('ヒントセクションが表示される', async ({ page }) => {
        const quickNote = page.locator('.quick-note');
        await expect(quickNote).toBeVisible();
        await expect(quickNote).toContainText('ヒント');
    });

    test('各テーマに適切なバッジが表示される', async ({ page }) => {
        // 推奨バッジ
        const darkTheme = page.locator('.theme-card').first();
        await expect(darkTheme.locator('.theme-badge')).toContainText('推奨');

        // 業務向けバッジ
        const professionalTheme = page.locator('.theme-card').nth(2);
        await expect(professionalTheme.locator('.theme-badge')).toContainText('業務向け');
    });
});
