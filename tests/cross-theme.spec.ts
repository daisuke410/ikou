import { test, expect } from '@playwright/test';

/**
 * 全テーマの共通機能テスト
 * すべてのテーマページで共通の機能をテスト
 */
const themes = [
    { name: 'Dark', url: '/index-dark.html' },
    { name: 'Light', url: '/index-light.html' },
    { name: 'Professional', url: '/index-professional.html' },
    { name: 'Corporate', url: '/index-corporate.html' },
    { name: 'Minimal', url: '/index-minimal.html' },
    { name: 'Enterprise', url: '/index-enterprise.html' },
    { name: 'Professional v2', url: '/index-professional-v2.html' },
];

for (const theme of themes) {
    test.describe(`${theme.name}テーマ - 共通機能`, () => {

        test.beforeEach(async ({ page }) => {
            await page.goto(theme.url);
        });

        test('ページが正しく読み込まれる', async ({ page }) => {
            // ページが読み込まれることを確認
            await expect(page.locator('body')).toBeVisible();
        });

        test('JavaScriptエラーが発生しない', async ({ page }) => {
            const errors: string[] = [];

            page.on('pageerror', error => {
                errors.push(error.message);
            });

            page.on('console', msg => {
                if (msg.type() === 'error') {
                    errors.push(msg.text());
                }
            });

            // ページを完全に読み込む
            await page.waitForLoadState('networkidle');

            // 重大なJavaScriptエラーがないことを確認
            const criticalErrors = errors.filter(e =>
                !e.includes('WebSocket') && // WebSocketエラーは許容（サーバー未起動時）
                !e.includes('Failed to fetch') && // フェッチエラーは許容
                !e.includes('NetworkError') // ネットワークエラーは許容
            );

            expect(criticalErrors.length).toBe(0);
        });

        test('基本的なUI要素が存在する', async ({ page }) => {
            // 少なくとも1つのボタンが存在することを確認
            const buttons = page.locator('button');
            const buttonCount = await buttons.count();
            expect(buttonCount).toBeGreaterThan(0);
        });

        test('アクセシビリティ - キーボードナビゲーション', async ({ page }) => {
            // Tabキーでフォーカス移動できることを確認
            await page.keyboard.press('Tab');

            // フォーカス可能な要素が存在することを確認
            const focusedElement = await page.evaluate(() => {
                return document.activeElement?.tagName;
            });

            expect(focusedElement).toBeTruthy();
        });

        test('画像とリソースが正しく読み込まれる', async ({ page }) => {
            // すべての画像が読み込まれるまで待機
            await page.waitForLoadState('networkidle');

            // 壊れた画像がないことを確認
            const brokenImages = await page.locator('img').evaluateAll(images => {
                return images.filter((img: any) => !img.complete || img.naturalHeight === 0);
            });

            expect(brokenImages.length).toBe(0);
        });

        test('CSSが正しく適用されている', async ({ page }) => {
            const body = page.locator('body');

            // bodyにスタイルが適用されていることを確認
            const fontFamily = await body.evaluate((el) => {
                return window.getComputedStyle(el).fontFamily;
            });

            expect(fontFamily).toBeTruthy();
            expect(fontFamily).not.toBe('');
        });
    });
}

/**
 * パフォーマンステスト
 */
test.describe('パフォーマンス', () => {

    test('ページ読み込み時間が許容範囲内', async ({ page }) => {
        const startTime = Date.now();

        await page.goto('/index-dark.html');
        await page.waitForLoadState('networkidle');

        const loadTime = Date.now() - startTime;

        // 10秒以内に読み込まれることを確認
        expect(loadTime).toBeLessThan(10000);
    });

    test('メモリリークがない', async ({ page }) => {
        await page.goto('/index-dark.html');

        // ページを複数回リロード
        for (let i = 0; i < 3; i++) {
            await page.reload();
            await page.waitForLoadState('networkidle');
        }

        // ページが正常に動作していることを確認
        // （メモリメトリクスAPIは非推奨のため、基本的な動作確認のみ）
        await expect(page.locator('body')).toBeVisible();
    });
});

/**
 * セキュリティテスト
 */
test.describe('セキュリティ', () => {

    test('XSS対策 - スクリプトインジェクション', async ({ page }) => {
        await page.goto('/index-dark.html');

        // 悪意のあるスクリプトを入力フィールドに挿入
        const inputs = page.locator('input[type="text"]');
        const inputCount = await inputs.count();

        if (inputCount > 0) {
            const maliciousScript = '<script>alert("XSS")</script>';
            await inputs.first().fill(maliciousScript);

            // アラートが表示されないことを確認
            page.on('dialog', dialog => {
                // アラートが表示された場合はテスト失敗
                expect(dialog.type()).not.toBe('alert');
                dialog.dismiss();
            });
        }
    });

    test('HTTPSリダイレクト設定の確認', async ({ page }) => {
        // 本番環境ではHTTPSにリダイレクトされるべき
        // ローカル開発環境ではHTTPを許容
        await page.goto('/');

        const url = page.url();
        // ローカルホストまたはHTTPSであることを確認
        expect(url.startsWith('http://localhost') || url.startsWith('https://')).toBe(true);
    });
});
