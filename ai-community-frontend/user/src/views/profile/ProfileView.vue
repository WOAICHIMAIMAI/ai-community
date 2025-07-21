<template>
  <div class="profile-layout">
    <van-nav-bar
      :title="pageTitle"
      left-arrow
      @click-left="goBack"
      fixed
    />
    
    <div class="profile-content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" @update-title="updatePageTitle" />
        </transition>
      </router-view>
    </div>
    
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import BottomTabbar from '@/components/BottomTabbar.vue'

const router = useRouter()
const route = useRoute()
const pageTitle = ref('个人中心')
const authStore = useAuthStore()

// 更新标题
const updateTitleFromRoute = () => {
  pageTitle.value = route.meta.title as string || '个人中心'
}

// 子组件可以更新标题
const updatePageTitle = (title: string) => {
  pageTitle.value = title
}

// 返回上一页
const goBack = () => {
  if (route.path === '/profile') {
    router.push('/')
  } else {
    router.back()
  }
}

onMounted(() => {
  // 初始化设置标题
  updateTitleFromRoute()
})

// 监听路由变化，更新标题
watchEffect(() => {
  if (route.meta.title) {
    updateTitleFromRoute()
  }
})
</script>

<style scoped lang="scss">
.profile-layout {
  min-height: 100vh;
  padding-top: 46px; // 导航栏高度
  padding-bottom: 50px; // 底部标签栏高度
  background-color: var(--background-color);
  
  .profile-content {
    height: calc(100vh - 96px); // 减去导航栏和标签栏高度
    overflow-y: auto;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style> 