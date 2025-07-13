import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // 禁用 v-slot 的编译警告
          isCustomElement: (tag) => false
        }
      }
    }),
    Components({
      resolvers: [VantResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8200',
        changeOrigin: true
      }
    },
    hmr: {
      overlay: false
    }
  },
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'vant']
  },
  build: {
    commonjsOptions: {
      include: [/node_modules/]
    }
  }
}) 