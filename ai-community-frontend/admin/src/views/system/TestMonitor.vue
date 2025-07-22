<template>
  <div style="padding: 20px;">
    <h2>系统监控测试页面</h2>
    <p>如果你能看到这个页面，说明路由配置正常！</p>
    
    <el-button type="primary" @click="testApi">测试API连接</el-button>
    
    <div v-if="loading" style="margin-top: 20px;">
      <el-text>正在测试API连接...</el-text>
    </div>
    
    <div v-if="result" style="margin-top: 20px;">
      <el-alert :title="result.title" :type="result.type" show-icon>
        {{ result.message }}
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const result = ref(null)

const testApi = async () => {
  loading.value = true
  result.value = null

  try {
    // 使用项目的request工具测试API
    const response = await request({
      url: '/admin/news/bloom-filter/stats',
      method: 'get'
    })

    result.value = {
      title: 'API连接成功',
      type: 'success',
      message: `成功获取数据！使用率: ${(response.data.usageRatio * 100).toFixed(1)}%`
    }
  } catch (error) {
    console.error('API测试失败:', error)
    result.value = {
      title: 'API连接失败',
      type: 'error',
      message: `错误信息: ${error.message || '未知错误'}`
    }
  } finally {
    loading.value = false
  }
}
</script>
