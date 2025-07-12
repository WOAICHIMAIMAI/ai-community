<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'

// 获取认证状态管理
const authStore = useAuthStore()
const router = useRouter()

// 页面初始化时检查登录状态
onMounted(async () => {
  try {
    console.log('App组件挂载，检查登录状态')
    // 检查是否有token
    const hasToken = !!localStorage.getItem('token')
    
    if (hasToken) {
      // 有token，尝试获取用户信息
      await authStore.checkLoginStatus()
    } else {
      console.log('未检测到token，需要登录')
      // 如果当前不是登录页，重定向到登录页
      const currentPath = router.currentRoute.value.path
      if (currentPath !== '/login') {
        router.replace({
          path: '/login', 
          query: { redirect: currentPath }
        })
      }
    }
  } catch (error) {
    console.error('检查登录状态失败:', error)
  }
})
</script>

<style>
/* 全局过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
  font-size: 14px;
  color: #333;
  background-color: #f5f7fa;
}

#app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 改进表格样式 */
.el-table {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-header-text-color: #262626;
  --el-table-row-hover-bg-color: #e6f7ff;
  --el-table-border-color: #ebeef5;
  font-size: 14px;
}

.el-table th {
  font-weight: 600 !important;
}

.el-table td {
  color: #333;
}

/* 改进卡片样式 */
.el-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08) !important;
  transition: all 0.3s;
}

.el-card:hover {
  box-shadow: 0 4px 12px rgba(0, 21, 41, 0.12) !important;
}

/* 改进按钮样式 */
.el-button {
  font-weight: 500;
}

/* 改进标签样式 */
.el-tag {
  font-weight: 500;
}

/* 改进导航菜单样式 */
.el-menu-item, .el-sub-menu__title {
  height: 50px;
  line-height: 50px;
}

/* 改进表单样式 */
.el-form-item__label {
  font-weight: 500;
  color: #262626;
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 