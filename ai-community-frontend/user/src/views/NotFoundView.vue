<template>
  <div class="not-found">
    <van-empty
      image="error"
      description="页面不存在"
    />
    <van-button round type="primary" @click="goHome">返回首页</van-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 20px;
}

.van-button {
  margin-top: 20px;
  width: 60%;
}
</style> 