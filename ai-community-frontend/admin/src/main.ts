import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 先导入Element Plus样式
import 'element-plus/dist/index.css'
import 'nprogress/nprogress.css'
import './styles/main.scss'

// 再导入组件
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import router from './router'

// 创建Vue应用
const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 先使用pinia和router插件
app.use(createPinia())
app.use(router)

// 然后使用ElementPlus
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 挂载应用
app.mount('#app')

// 输出环境信息，便于调试
console.log('环境模式:', import.meta.env.MODE)
console.log('基础URL:', import.meta.env.BASE_URL)
console.log('API代理:', import.meta.env.VITE_API_URL || '/api') 