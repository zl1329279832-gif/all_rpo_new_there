import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath, URL } from 'node:url';
import path from 'node:path';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());

  return {
    plugins: [vue()],
    resolve: {
      alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@platform/shared-types': path.resolve(__dirname, '../../packages/shared-types/src/index.ts'),
      '@platform/shared-utils': path.resolve(__dirname, '../../packages/shared-utils/src/index.ts'),
      '@platform/shared-components': path.resolve(__dirname, '../../packages/shared-components/src/index.ts'),
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:3000',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
    build: {
      sourcemap: mode === 'development',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            elementPlus: ['element-plus', '@element-plus/icons-vue'],
            echarts: ['echarts'],
            utils: ['dayjs', 'axios', 'nprogress'],
          },
        },
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/styles/variables.scss";`,
        },
      },
    },
  };
});
