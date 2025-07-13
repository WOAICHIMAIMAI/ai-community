import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { Toast } from 'vant'

// 引入Vant样式
import 'vant/lib/index.css'

// 引入全局样式
import './styles/main.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Toast)

// 添加全局属性
app.config.globalProperties.$router = router
app.config.globalProperties.$toast = Toast

app.mount('#app') 