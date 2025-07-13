<template>
  <router-view v-slot="{ Component }">
    <transition name="fade-slide" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
// App根组件
</script>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 全局按钮样式优化 */
.van-button--default {
  border: none !important;
  background: transparent linear-gradient(to right, var(--primary-color-light), var(--primary-color)) !important;
  color: #fff !important;
  box-shadow: 0 4px 8px rgba(79, 192, 141, 0.3) !important;
}

.van-button--primary {
  background: transparent linear-gradient(to right, var(--primary-color-light), var(--primary-color)) !important;
  border: none !important;
  box-shadow: 0 4px 8px rgba(79, 192, 141, 0.3) !important;
}

.van-button--danger {
  background: transparent linear-gradient(to right, #ff7875, var(--danger-color)) !important;
  border: none !important;
  box-shadow: 0 4px 8px rgba(238, 10, 36, 0.3) !important;
}

.van-tabbar {
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05) !important;
}
</style> 