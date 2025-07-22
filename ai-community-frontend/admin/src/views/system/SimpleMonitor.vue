<template>
  <div class="monitor-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>📊 新闻去重系统监控</h2>
          <p>🛡️ 确保新闻内容不重复，提升用户阅读体验</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="loadData" :loading="loading" size="large">
            <span v-if="loading">🔄 加载中...</span>
            <span v-else>🔄 刷新状态</span>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card usage-card">
          <div class="metric-content">
            <div class="metric-icon">📈</div>
            <div class="metric-info">
              <div class="metric-value">{{ usageRate }}%</div>
              <div class="metric-label">存储使用率</div>
            </div>
          </div>
          <div class="progress-section">
            <el-progress
              :percentage="parseFloat(usageRate)"
              :color="progressColor"
              :stroke-width="8"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card processed-card">
          <div class="metric-content">
            <div class="metric-icon">📰</div>
            <div class="metric-info">
              <div class="metric-value">{{ processedCount }}</div>
              <div class="metric-label">已处理新闻</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card capacity-card">
          <div class="metric-content">
            <div class="metric-icon">💾</div>
            <div class="metric-info">
              <div class="metric-value">{{ totalCapacity }}</div>
              <div class="metric-label">总存储容量</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card status-card">
          <div class="metric-content">
            <div class="metric-icon">{{ statusIcon }}</div>
            <div class="metric-info">
              <div class="metric-value">
                <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
              </div>
              <div class="metric-label">系统状态</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细信息区域 -->
    <el-row :gutter="20" class="details-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📊 使用率趋势图</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="usage-chart">
              <!-- 简单的SVG图表 -->
              <div class="chart-header">
                <span class="chart-title">📊 使用率趋势</span>
                <span class="chart-value">{{ usageRate }}%</span>
              </div>

              <div class="chart-svg-container">
                <svg class="usage-trend-chart" viewBox="0 0 500 280" preserveAspectRatio="xMidYMid meet">
                  <!-- 背景网格 -->
                  <defs>
                    <pattern id="grid" width="50" height="28" patternUnits="userSpaceOnUse">
                      <path d="M 50 0 L 0 0 0 28" fill="none" stroke="#e0e6ed" stroke-width="1" opacity="0.3"/>
                    </pattern>
                  </defs>
                  <rect x="60" y="20" width="420" height="220" fill="url(#grid)" />

                  <!-- Y轴标签 -->
                  <text x="45" y="35" font-size="12" fill="#7f8c8d" text-anchor="end">100%</text>
                  <text x="45" y="90" font-size="12" fill="#7f8c8d" text-anchor="end">75%</text>
                  <text x="45" y="145" font-size="12" fill="#7f8c8d" text-anchor="end">50%</text>
                  <text x="45" y="200" font-size="12" fill="#7f8c8d" text-anchor="end">25%</text>
                  <text x="45" y="250" font-size="12" fill="#7f8c8d" text-anchor="end">0%</text>

                  <!-- X轴标签 -->
                  <text x="90" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">6天前</text>
                  <text x="150" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">5天前</text>
                  <text x="210" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">4天前</text>
                  <text x="270" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">3天前</text>
                  <text x="330" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">2天前</text>
                  <text x="390" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">昨天</text>
                  <text x="450" y="270" font-size="10" fill="#7f8c8d" text-anchor="middle">今天</text>

                  <!-- 趋势线 -->
                  <polyline
                    :points="trendLinePoints"
                    fill="none"
                    stroke="url(#lineGradient)"
                    stroke-width="3"
                    stroke-linecap="round"
                  />

                  <!-- 渐变定义 -->
                  <defs>
                    <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                      <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.3" />
                      <stop offset="100%" style="stop-color:#764ba2;stop-opacity:0.1" />
                    </linearGradient>
                  </defs>

                  <!-- 填充区域 -->
                  <polygon
                    :points="areaPoints"
                    fill="url(#areaGradient)"
                  />

                  <!-- 数据点 -->
                  <circle
                    v-for="(point, index) in dataPoints"
                    :key="index"
                    :cx="point.x"
                    :cy="point.y"
                    r="4"
                    fill="#667eea"
                    stroke="white"
                    stroke-width="2"
                  />

                  <!-- 当前值指示器 -->
                  <circle
                    :cx="currentPointX"
                    :cy="currentPointY"
                    r="6"
                    fill="#f56c6c"
                    stroke="white"
                    stroke-width="3"
                  >
                    <animate attributeName="r" values="6;8;6" dur="2s" repeatCount="indefinite"/>
                  </circle>
                </svg>
              </div>

              <div class="chart-footer">
                <div class="chart-stats">
                  <span class="stat-item">
                    <span class="stat-label">当前:</span>
                    <span class="stat-value">{{ usageRate }}%</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">状态:</span>
                    <span class="stat-value">{{ statusText }}</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-label">处理:</span>
                    <span class="stat-value">{{ processedCount }} 条</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>⚙️ 系统配置</span>
            </div>
          </template>
          <div class="config-list">
            <div class="config-item">
              <span class="config-icon">⏰</span>
              <span class="config-label">数据保留期:</span>
              <span class="config-value">{{ retentionDays }} 天</span>
            </div>
            <div class="config-item">
              <span class="config-icon">🎯</span>
              <span class="config-label">误判率:</span>
              <span class="config-value">< 1%</span>
            </div>
            <div class="config-item">
              <span class="config-icon">🔄</span>
              <span class="config-label">自动重建:</span>
              <span class="config-value">已启用</span>
            </div>
          </div>
        </el-card>

        <el-card class="action-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>🛠️ 系统操作</span>
            </div>
          </template>
          <div class="action-content">
            <div class="recommendation">
              <span class="rec-icon">💡</span>
              <span>{{ recommendation }}</span>
            </div>
            <el-button
              v-if="needOptimize"
              type="warning"
              @click="optimize"
              :loading="optimizing"
              size="large"
              style="width: 100%; margin-top: 15px;"
            >
              <span v-if="optimizing">⚡ 优化中...</span>
              <span v-else">⚡ 立即优化</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 页面底部信息 -->
    <div class="footer-info">
      <div class="update-time">
        <span class="time-icon">🕐</span>
        <span>最后更新: {{ lastUpdate }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)
const optimizing = ref(false)
const lastUpdate = ref('--')
const systemData = ref({
  usageRatio: 0,
  loadedUrlCount: 0,
  expectedInsertions: 0,
  dataRetentionDays: 0,
  alertLevel: { name: '正常' },
  alertRecommendation: '系统运行正常'
})

// 计算属性
const usageRate = computed(() => {
  return (systemData.value.usageRatio * 100).toFixed(1)
})

const statusType = computed(() => {
  const level = systemData.value.alertLevel?.name || '正常'
  const typeMap = {
    '正常': 'success',
    '注意': 'info', 
    '警告': 'warning',
    '危险': 'danger'
  }
  return typeMap[level] || 'success'
})

const statusText = computed(() => {
  return systemData.value.alertLevel?.name || '正常'
})

const processedCount = computed(() => {
  return systemData.value.loadedUrlCount?.toLocaleString() || '0'
})

const totalCapacity = computed(() => {
  return systemData.value.expectedInsertions?.toLocaleString() || '0'
})

const retentionDays = computed(() => {
  return systemData.value.dataRetentionDays || 0
})

const recommendation = computed(() => {
  return systemData.value.alertRecommendation || '系统运行正常'
})

const needOptimize = computed(() => {
  const level = systemData.value.alertLevel?.name
  return level === '警告' || level === '危险'
})

const progressColor = computed(() => {
  const usage = parseFloat(usageRate.value)
  if (usage < 50) return '#67c23a'
  if (usage < 80) return '#e6a23c'
  return '#f56c6c'
})

const statusIcon = computed(() => {
  const level = systemData.value.alertLevel?.name || '正常'
  const iconMap = {
    '正常': '✅',
    '注意': 'ℹ️',
    '警告': '⚠️',
    '危险': '🚨'
  }
  return iconMap[level] || '✅'
})

// 图表数据计算
const chartData = computed(() => {
  // 模拟7天的使用率数据
  const currentUsage = parseFloat(usageRate.value)
  const baseData = [
    Math.max(0.1, currentUsage * 0.6),
    Math.max(0.2, currentUsage * 0.7),
    Math.max(0.3, currentUsage * 0.8),
    Math.max(0.5, currentUsage * 0.9),
    Math.max(0.8, currentUsage * 0.95),
    Math.max(1.0, currentUsage * 0.98),
    currentUsage
  ]

  return baseData.map((value, index) => ({
    x: 90 + index * 60, // X坐标，从90开始，间隔60
    y: 240 - (value * 2.2), // Y坐标 (反转，从240开始，乘以2.2放大)
    value: value
  }))
})

const trendLinePoints = computed(() => {
  return chartData.value.map(point => `${point.x},${point.y}`).join(' ')
})

const areaPoints = computed(() => {
  const points = chartData.value.map(point => `${point.x},${point.y}`).join(' ')
  const firstX = chartData.value[0]?.x || 90
  const lastX = chartData.value[chartData.value.length - 1]?.x || 450
  return `${firstX},240 ${points} ${lastX},240`
})

const dataPoints = computed(() => {
  return chartData.value
})

const currentPointX = computed(() => {
  return chartData.value[chartData.value.length - 1]?.x || 450
})

const currentPointY = computed(() => {
  return chartData.value[chartData.value.length - 1]?.y || 240
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    // 使用fetch直接调用API
    const response = await fetch('/api/admin/news/bloom-filter/stats', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.code === 200) {
        systemData.value = result.data
        lastUpdate.value = new Date().toLocaleString()
        ElMessage.success('数据加载成功')
      } else {
        throw new Error(result.message || '获取数据失败')
      }
    } else {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const optimize = async () => {
  optimizing.value = true
  try {
    const response = await fetch('/api/admin/news/bloom-filter/rebuild', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      const result = await response.json()
      if (result.code === 200) {
        ElMessage.success('系统优化完成')
        setTimeout(() => loadData(), 1000)
      } else {
        throw new Error(result.message || '优化失败')
      }
    } else {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
  } catch (error) {
    console.error('系统优化失败:', error)
    ElMessage.error('系统优化失败: ' + error.message)
  } finally {
    optimizing.value = false
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.monitor-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 120px);
}

/* 页面头部 */
.page-header {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  color: #2c3e50;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.title-section p {
  color: #7f8c8d;
  margin: 0;
  font-size: 16px;
}

/* 指标卡片 */
.metrics-row {
  margin-bottom: 24px;
}

.metric-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.metric-icon {
  font-size: 32px;
  margin-right: 16px;
  width: 48px;
  text-align: center;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1.2;
}

.metric-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-top: 4px;
}

.progress-section {
  margin-top: 16px;
}

/* 特定卡片颜色 */
.usage-card .metric-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.processed-card .metric-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.capacity-card .metric-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.status-card .metric-icon {
  font-size: 28px;
}

/* 详细信息区域 */
.details-row {
  margin-bottom: 24px;
}

.chart-card, .info-card, .action-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

/* 图表区域 */
.chart-container {
  height: 400px;
  padding: 0;
}

.usage-chart {
  height: 100%;
  background: white;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.chart-value {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

.chart-svg-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 15px 0;
  min-height: 280px;
}

.usage-trend-chart {
  width: 100%;
  height: 100%;
  max-height: 280px;
}

.chart-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.chart-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

/* 配置列表 */
.config-list {
  padding: 8px 0;
}

.config-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.config-item:last-child {
  border-bottom: none;
}

.config-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.config-label {
  flex: 1;
  color: #7f8c8d;
  font-size: 14px;
}

.config-value {
  font-weight: 600;
  color: #2c3e50;
}

/* 操作区域 */
.action-content {
  padding: 8px 0;
}

.recommendation {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.rec-icon {
  font-size: 18px;
  margin-right: 12px;
  margin-top: 2px;
}

/* 页面底部 */
.footer-info {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.update-time {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7f8c8d;
  font-size: 14px;
}

.time-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .metrics-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .action-section {
    width: 100%;
  }

  .action-section .el-button {
    width: 100%;
  }
}
</style>
