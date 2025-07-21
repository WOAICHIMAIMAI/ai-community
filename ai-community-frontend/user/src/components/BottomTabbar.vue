<template>
  <van-tabbar route fixed v-if="authStore.isLoggedIn">
    <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item replace to="/community" icon="friends-o">社区</van-tabbar-item>
    <van-tabbar-item replace to="/chat-list" icon="chat-o">消息</van-tabbar-item>
    <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
</script>