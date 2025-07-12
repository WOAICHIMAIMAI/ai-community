<template>
  <div class="not-found">
    <el-empty
      description="页面不存在"
      :image-size="200"
    >
      <template #image>
        <el-image src="https://element-plus.org/images/404.png" />
      </template>
      <template #description>
        <p>抱歉，您访问的页面不存在</p>
      </template>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;

  p {
    font-size: 16px;
    color: #666;
    margin: 20px 0;
  }
}
</style> 