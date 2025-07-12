<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      <span v-if="index === breadcrumbs.length - 1">{{ item.title }}</span>
      <router-link v-else :to="item.path">{{ item.title }}</router-link>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

interface BreadcrumbItem {
  title: string
  path: string
}

const route = useRoute()
const breadcrumbs = ref<BreadcrumbItem[]>([])

// 计算面包屑导航
const getBreadcrumbs = () => {
  const { matched, params } = route
  const result: BreadcrumbItem[] = []
  
  matched.forEach((item) => {
    if (item.meta?.title && item.path !== '/') {
      let title = item.meta.title as string
      
      // 处理动态路由参数
      if (item.path.includes(':')) {
        const dynamicSegments = item.path.match(/:\w+/g) || []
        dynamicSegments.forEach((segment) => {
          const paramName = segment.substring(1)
          if (params[paramName]) {
            title = title.replace(`:${paramName}`, params[paramName] as string)
          }
        })
      }
      
      result.push({
        title,
        path: item.path,
      })
    }
  })
  
  return result
}

// 初始化面包屑
breadcrumbs.value = getBreadcrumbs()

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  () => {
    breadcrumbs.value = getBreadcrumbs()
  }
)
</script>

<style scoped lang="scss">
.el-breadcrumb {
  font-size: 14px;
}
</style> 