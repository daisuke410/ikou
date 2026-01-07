#!/usr/bin/env node

/**
 * Playwright Test Runner - スタンドアロン実行用
 * オフライン環境でもPlaywrightテストを実行できるようにするためのランナー
 */

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

// 実行時の引数を取得
const args = process.argv.slice(2);

// デフォルト設定
const defaultArgs = [
    'test',
    '--config=playwright.config.ts',
    '--project=chromium'
];

// ユーザー指定の引数がある場合は、それを使用
// 引数がない場合はデフォルトを使用
let playwrightArgs;
if (args.length > 0) {
    // 引数がある場合、'test'コマンドを先頭に追加
    playwrightArgs = ['test', ...args];
} else {
    playwrightArgs = defaultArgs;
}

console.log('='.repeat(60));
console.log('Playwright Test Runner - Standalone Edition');
console.log('='.repeat(60));
console.log(`実行ディレクトリ: ${process.cwd()}`);
console.log(`実行コマンド: playwright ${playwrightArgs.join(' ')}`);
console.log('='.repeat(60));
console.log('');

// Playwrightの実行パスを決定
let playwrightBin;

if (process.pkg) {
    // pkg でバンドルされた場合
    console.log('📦 スタンドアロンモードで実行中...');

    // 実行ファイルと同じディレクトリにあるnode_modulesを使用
    const exeDir = path.dirname(process.execPath);
    const nodeModulesPath = path.join(exeDir, 'node_modules');

    if (fs.existsSync(nodeModulesPath)) {
        playwrightBin = path.join(nodeModulesPath, '.bin', 'playwright.cmd');
        console.log(`✅ node_modules検出: ${nodeModulesPath}`);
    } else {
        console.error('❌ エラー: node_modulesが見つかりません');
        console.error(`   期待されるパス: ${nodeModulesPath}`);
        console.error('');
        console.error('💡 解決方法:');
        console.error('   1. EXEファイルと同じディレクトリにnode_modulesをコピーしてください');
        console.error('   2. または、setup-offline.batを実行してください');
        process.exit(1);
    }
} else {
    // 通常のNode.js実行
    console.log('🔧 開発モードで実行中...');
    playwrightBin = path.join(__dirname, 'node_modules', '.bin', 'playwright.cmd');
}

// Playwrightを実行
const playwright = spawn(playwrightBin, playwrightArgs, {
    stdio: 'inherit',
    shell: true,
    cwd: process.cwd()
});

playwright.on('error', (error) => {
    console.error('❌ エラーが発生しました:', error.message);
    process.exit(1);
});

playwright.on('close', (code) => {
    console.log('');
    console.log('='.repeat(60));
    if (code === 0) {
        console.log('✅ テストが正常に完了しました');
    } else {
        console.log(`⚠️  テストが終了しました (終了コード: ${code})`);
    }
    console.log('='.repeat(60));
    process.exit(code);
});
