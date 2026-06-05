const { writeFile } = require('./scripts/file-utils');

const frontendDeps = {
  "name": "@platform/web",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc --noEmit && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "element-plus": "^2.4.4",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.5",
    "echarts": "^5.4.3",
    "vue-echarts": "^6.6.8",
    "dayjs": "^1.11.10",
    "nprogress": "^0.2.0",
    "pinia-plugin-persistedstate": "^3.2.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "typescript": "^5.3.3",
    "vite": "^5.0.10",
    "vue-tsc": "^1.8.25",
    "@types/node": "^20.10.6",
    "@types/nprogress": "^0.2.3",
    "sass": "^1.69.5"
  }
};

writeFile('apps/web/package.json', JSON.stringify(frontendDeps, null, 2));

const viteConfig = `import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@components': path.resolve(__dirname, '../../packages/components/src'),
      '@shared': path.resolve(__dirname, '../../packages/shared/src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "@/styles/variables.scss";'
      }
    }
  }
});
`;

writeFile('apps/web/vite.config.ts', viteConfig);

const tsConfig = {
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["../../packages/components/src/*"],
      "@shared/*": ["../../packages/shared/src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
};

writeFile('apps/web/tsconfig.json', JSON.stringify(tsConfig, null, 2));

const tsConfigNode = {
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
};

writeFile('apps/web/tsconfig.node.json', JSON.stringify(tsConfigNode, null, 2));

const indexHtml = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <link rel="icon" type="image/svg+xml" href="/vite.svg" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>集团运营中台</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
`;

writeFile('apps/web/index.html', indexHtml);

const envTypes = `/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

declare module 'nprogress';
`;

writeFile('apps/web/src/env.d.ts', envTypes);

console.log('Frontend base config created successfully!');
