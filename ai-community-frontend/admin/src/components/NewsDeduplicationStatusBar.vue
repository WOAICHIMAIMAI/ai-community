<template>
  <div class="deduplication-status-bar" :class="statusBarClass">
    <div class="status-content" @click="toggleDetails">
      <el-icon class="status-icon">
        <component :is="statusIcon" />
      </el-icon>
      <span class="status-text">新闻去重系统</span>
      <el-tag :type="tagType" size="small" class="usage-tag">
        {{ usagePercentage }}%
      </el-tag>
      <el-icon class="toggle-icon" :class="{ 'expanded': showDetails }">
        <ArrowDown />
      </el-icon>
    </div>
    
    <el-collapse-transition>
      <div v-show="showDetails" class="status-details">
        <div class="detail-row">
          <span class="detail-label">系统状态:</span>
          <span class="detail-value" :class="statusTextClass">{{ systemStatus }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">已处理新闻:</span>
          <span class="detail-value">{{ processedCount }} 条</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">存储容量:</span>
          <span class="detail-value">{{ totalCapacity }} 条</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">最后更新:</span>
          <span class="detail-value">{{ lastUpdateTime }}</span>
        </div>
        
        <div class="detail-actions">
          <el-button size="small" @click="refreshStatus" :loading="loading">
            刷新
          </el-button>
          <el-button size="small" type="primary" @click="goToMonitorPage">
            详情
          </el-button>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  SuccessFilled,
  InfoFilled,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import { getBloomFilterStats } from '@/api/system'

// Props
const props = defineProps({
  autoRefresh: {
    type: Boolean,
    default: true
  },
  refreshInterval: {
    type: Number,
    default: 5 * 60 * 1000 // 5分钟
  }
})

// 响应式数据
const loading = ref(false)
const showDetails = ref(false)
const stats = ref({})
const lastUpdateTime = ref('--')
let autoRefreshTimer = null

const router = useRouter()

// 计算属性
const usagePercentage = computed(() => {
  return stats.value.usageRatio ? (stats.value.usageRatio * 100).toFixed(1) : '0.0'
})

const processedCount = computed(() => {
  return stats.value.loadedUrlCount ? stats.value.loadedUrlCount.toLocaleString() : '0'
})

const totalCapacity = computed(() => {
  return stats.value.expectedInsertions ? stats.value.expectedInsertions.toLocaleString() : '0'
})

const alertLevel = computed(() => {
  return stats.value.alertLevel?.name || '正常'
})

const statusInfo = computed(() => {
  const statusMap = {
    '正常': {
      barClass: 'status-normal',
      textClass: 'text-success',
      tagType: 'success',
      icon: 'SuccessFilled',
      status: '运行良好'
    },
    '注意': {
      barClass: 'status-info',
      textClass: 'text-info',
      tagType: 'info',
      icon: 'InfoFilled',
      status: '需要关注'
    },
    '警告': {
      barClass: 'status-warning',
      textClass: 'text-warning',
      tagType: 'warning',
      icon: 'WarningFilled',
      status: '需要处理'
    },
    '危险': {
      barClass: 'status-danger',
      textClass: 'text-danger',
      tagType: 'danger',
      icon: 'CircleCloseFilled',
      status: '紧急处理'
    }
  }
  
  return statusMap[alertLevel.value] || statusMap['正常']
})

const statusBarClass = computed(() => statusInfo.value.barClass)
const statusTextClass = computed(() => statusInfo.value.textClass)
const tagType = computed(() => statusInfo.value.tagType)
const statusIcon = computed(() => statusInfo.value.icon)
const systemStatus = computed(() => statusInfo.value.status)

// 方法
const fetchStats = async () => {
  try {
    loading.value = true
    const response = await getBloomFilterStats()
    stats.value = response.data
    lastUpdateTime.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('获取去重系统状态失败:', error)
  } finally {
    loading.value = false
  }
}

const refreshStatus = () => {
  fetchStats()
}

const toggleDetails = () => {
  showDetails.value = !showDetails.value
}

const goToMonitorPage = () => {
  router.push('/system/news-deduplication')
}

const startAutoRefresh = () => {
  if (props.autoRefresh) {
    autoRefreshTimer = setInterval(() => {
      fetchStats()
    }, props.refreshInterval)
  }
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

// 生命周期
onMounted(() => {
  fetchStats()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.deduplication-status-bar {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 1px solid #e4e7ed;
  overflow: hidden;
  transition: all 0.3s ease;
}

.deduplication-status-bar:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.status-normal {
  border-left: 4px solid #67c23a;
}

.status-info {
  border-left: 4px solid #409eff;
}

.status-warning {
  border-left: 4px solid #e6a23c;
}

.status-danger {
  border-left: 4px solid #f56c6c;
}

.status-content {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.status-content:hover {
  background-color: #f5f7fa;
}

.status-icon {
  font-size: 16px;
  margin-right: 8px;
}

.status-text {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.usage-tag {
  margin-right: 8px;
}

.toggle-icon {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s ease;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.status-details {
  border-top: 1px solid #e4e7ed;
  padding: 12px 16px;
  background-color: #fafafa;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-row:last-of-type {
  margin-bottom: 12px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  font-weight: 500;
  color: #303133;
}

.text-success { color: #67c23a; }
.text-info { color: #409eff; }
.text-warning { color: #e6a23c; }
.text-danger { color: #f56c6c; }

.detail-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}
</style>
