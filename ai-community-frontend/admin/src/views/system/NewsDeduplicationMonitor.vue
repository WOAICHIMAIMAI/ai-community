<template>
  <div class="news-deduplication-monitor">
    <h2>新闻去重系统监控</h2>
    <p>确保新闻内容不重复，提升用户阅读体验</p>

    <el-button type="primary" @click="refreshData" :loading="loading">
      刷新状态
    </el-button>

    <div v-if="loading" style="margin-top: 20px;">
      <el-loading-text>正在加载系统状态...</el-loading-text>
    </div>

    <div v-else style="margin-top: 20px;">
      <el-card>
        <h3>存储空间使用情况</h3>
        <p>使用率: {{ usagePercentage }}%</p>
        <p>状态: {{ alertLevel }}</p>
      </el-card>

      <el-card style="margin-top: 20px;">
        <h3>数据统计</h3>
        <p>已处理新闻: {{ processedNewsCount }}</p>
        <p>总存储容量: {{ storageCapacityText }}</p>
        <p>数据保留期: {{ dataRetentionText }}</p>
      </el-card>

      <el-card style="margin-top: 20px;" v-if="needsAction">
        <h3>系统建议</h3>
        <p>{{ translatedRecommendation }}</p>
        <el-button type="warning" @click="optimizeSystem" :loading="optimizing">
          {{ optimizing ? '优化中...' : '立即优化系统' }}
        </el-button>
      </el-card>
    </div>

    <p style="margin-top: 20px; color: #666;">
      最后更新时间: {{ lastUpdateTime }}
    </p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBloomFilterStats, rebuildBloomFilter } from '@/api/system'

// 响应式数据
const loading = ref(false)
const optimizing = ref(false)
const stats = ref({})
const lastUpdateTime = ref('--')
let autoRefreshTimer = null

// 计算属性
const usagePercentage = computed(() => {
  return stats.value.usageRatio ? (stats.value.usageRatio * 100).toFixed(1) : '0.0'
})

const processedNewsCount = computed(() => {
  return stats.value.loadedUrlCount ? `${stats.value.loadedUrlCount.toLocaleString()} 条` : '-- 条'
})

const storageCapacityText = computed(() => {
  return stats.value.expectedInsertions ? `${stats.value.expectedInsertions.toLocaleString()} 条` : '-- 条'
})

const dataRetentionText = computed(() => {
  return stats.value.dataRetentionDays ? `${stats.value.dataRetentionDays} 天` : '-- 天'
})

const alertLevel = computed(() => {
  return stats.value.alertLevel?.name || '正常'
})

const translatedRecommendation = computed(() => {
  if (!stats.value.alertRecommendation) return '系统运行正常'

  return stats.value.alertRecommendation
    .replace(/布隆过滤器/g, '去重系统')
    .replace(/使用率/g, '存储空间使用率')
    .replace(/重建/g, '系统优化')
    .replace(/容量/g, '存储容量')
    .replace(/URL/g, '新闻链接')
})

const needsAction = computed(() => {
  return alertLevel.value === '警告' || alertLevel.value === '危险'
})

// 方法
const fetchData = async () => {
  try {
    loading.value = true
    const response = await getBloomFilterStats()
    stats.value = response.data
    lastUpdateTime.value = new Date().toLocaleString()
  } catch (error) {
    console.error('获取系统状态失败:', error)
    ElMessage.error('获取系统状态失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  fetchData()
}

const optimizeSystem = async () => {
  try {
    optimizing.value = true
    await rebuildBloomFilter()
    ElMessage.success('系统优化完成！')
    setTimeout(() => fetchData(), 1000)
  } catch (error) {
    ElMessage.error('系统优化失败: ' + (error.message || '未知错误'))
  } finally {
    optimizing.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.news-deduplication-monitor {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.page-description {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.header-actions {
  margin-bottom: 30px;
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.status-card {
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.status-card.status-healthy {
  border-left: 4px solid #67c23a;
}

.status-card.status-attention {
  border-left: 4px solid #409eff;
}

.status-card.status-warning {
  border-left: 4px solid #e6a23c;
}

.status-card.status-critical {
  border-left: 4px solid #f56c6c;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #409eff;
}

.card-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.card-title p {
  margin: 0;
  font-size: 14px;
  color: #7f8c8d;
}

.metric-display {
  text-align: center;
  padding: 20px 0;
}

.usage-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  border: 4px solid;
  position: relative;
}

.usage-circle.status-healthy {
  border-color: #67c23a;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.05));
}

.usage-circle.status-attention {
  border-color: #409eff;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.05));
}

.usage-circle.status-warning {
  border-color: #e6a23c;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(230, 162, 60, 0.05));
}

.usage-circle.status-critical {
  border-color: #f56c6c;
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.1), rgba(245, 108, 108, 0.05));
}

.usage-percentage {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.usage-label {
  font-size: 12px;
  color: #7f8c8d;
  margin-top: 4px;
}

.status-description {
  font-size: 14px;
  font-weight: 500;
}

.text-success { color: #67c23a; }
.text-info { color: #409eff; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }

.system-status {
  text-align: center;
  padding: 20px 0;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.status-icon {
  font-size: 24px;
  margin-right: 8px;
}

.indicator-success .status-icon { color: #67c23a; }
.indicator-info .status-icon { color: #409eff; }
.indicator-warning .status-icon { color: #e6a23c; }
.indicator-danger .status-icon { color: #f56c6c; }

.status-text {
  font-size: 18px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.recommendation-panel {
  margin-bottom: 30px;
}

.panel-header {
  display: flex;
  align-items: center;
}

.panel-icon {
  font-size: 20px;
  margin-right: 8px;
  color: #e6a23c;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.help-alert {
  margin-bottom: 20px;
}

.recommendation-content {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  border-left: 4px solid #409eff;
}

.recommendation-text {
  line-height: 1.8;
  color: #555;
  white-space: pre-line;
  margin-bottom: 20px;
}

.action-buttons {
  text-align: center;
}

.last-update {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .status-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
