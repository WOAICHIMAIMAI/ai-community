<template>
  <div class="analytics-container">
    <div class="analytics-header">
      <h1>数据分析</h1>
      <div class="controls">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="onDateRangeChange"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button @click="exportReport" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="summary-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="summary-card">
            <div class="card-content">
              <div class="card-icon total-orders">
                <el-icon><Document /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ summary.totalOrders || 0 }}</div>
                <div class="card-label">总订单数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="summary-card">
            <div class="card-content">
              <div class="card-icon completed-orders">
                <el-icon><Check /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ summary.completedOrders || 0 }}</div>
                <div class="card-label">已完成订单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="summary-card">
            <div class="card-content">
              <div class="card-icon active-users">
                <el-icon><User /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ summary.activeUsers || 0 }}</div>
                <div class="card-label">活跃用户</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="summary-card">
            <div class="card-content">
              <div class="card-icon satisfaction">
                <el-icon><Star /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ summary.avgSatisfactionScore || 0 }}</div>
                <div class="card-label">平均满意度</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 保修类型分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>保修类型分布</span>
                <el-button type="text" @click="viewDetail('repair-type')">查看详情</el-button>
              </div>
            </template>
            <div ref="repairTypeChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 服务完成量趋势 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>服务完成量趋势</span>
                <div class="chart-controls">
                  <el-select v-model="completionStatsType" @change="loadServiceCompletionStats" size="small">
                    <el-option label="按日" :value="1" />
                    <el-option label="按周" :value="2" />
                    <el-option label="按月" :value="3" />
                    <el-option label="按季度" :value="4" />
                    <el-option label="按年" :value="5" />
                  </el-select>
                  <el-button type="text" @click="viewDetail('completion')">查看详情</el-button>
                </div>
              </div>
            </template>
            <div ref="completionChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 用户活跃度 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>用户活跃度统计</span>
                <el-button type="text" @click="viewDetail('activity')">查看详情</el-button>
              </div>
            </template>
            <div ref="activityChart" class="chart-container"></div>
          </el-card>
        </el-col>

        <!-- 满意度分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <span>满意度评分分布</span>
                <el-button type="text" @click="viewDetail('satisfaction')">查看详情</el-button>
              </div>
            </template>
            <div ref="satisfactionChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="data-tables">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="服务统计" name="service">
          <el-table :data="serviceStats" style="width: 100%">
            <el-table-column prop="serviceType" label="服务类型" />
            <el-table-column prop="serviceName" label="服务名称" />
            <el-table-column prop="totalCount" label="总数量" />
            <el-table-column prop="completedCount" label="已完成" />
            <el-table-column prop="avgRating" label="平均评分">
              <template #default="scope">
                <el-rate
                  :model-value="scope.row.avgRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </template>
            </el-table-column>
            <el-table-column prop="avgResponseHours" label="平均响应时长(小时)" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="保修统计" name="repair">
          <el-table :data="repairStats" style="width: 100%">
            <el-table-column prop="repairType" label="保修类型" />
            <el-table-column prop="totalCount" label="总数量" />
            <el-table-column prop="completedCount" label="已完成" />
            <el-table-column prop="pendingCount" label="待处理" />
            <el-table-column prop="avgCompletionHours" label="平均完成时长(小时)" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Refresh, Document, Check, User, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const dateRange = ref<[string, string]>(['2024-01-01', '2024-12-31'])
const completionStatsType = ref(3) // 默认按月统计
const activeTab = ref('service')

// 图表实例
const repairTypeChart = ref()
const completionChart = ref()
const activityChart = ref()
const satisfactionChart = ref()

// 数据
const summary = reactive({
  totalOrders: 0,
  completedOrders: 0,
  activeUsers: 0,
  avgSatisfactionScore: 0
})

const serviceStats = ref([])
const repairStats = ref([])

// 图表实例存储
let repairTypeChartInstance: echarts.ECharts | null = null
let completionChartInstance: echarts.ECharts | null = null
let activityChartInstance: echarts.ECharts | null = null
let satisfactionChartInstance: echarts.ECharts | null = null

// 方法
const onDateRangeChange = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    loadAllData()
  }
}

const refreshData = () => {
  loadAllData()
}

const loadAllData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  loading.value = true
  try {
    // 使用模拟数据替代API调用
    await Promise.all([
      loadMockSummaryData(),
      loadMockRepairTypeDistribution(),
      loadMockServiceCompletionStats(),
      loadMockUserActivityStats(),
      loadMockSatisfactionStats(),
      loadMockServiceStats(),
      loadMockRepairStats()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 模拟数据加载方法
const loadMockSummaryData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      Object.assign(summary, {
        totalOrders: 1247,
        completedOrders: 1089,
        activeUsers: 456,
        avgSatisfactionScore: 4.2
      })
      resolve(true)
    }, 300)
  })
}

const loadMockRepairTypeDistribution = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const mockData = [
        { repairType: '家电维修', count: 342, percentage: 35.2 },
        { repairType: '管道疏通', count: 287, percentage: 29.5 },
        { repairType: '电路维修', count: 198, percentage: 20.3 },
        { repairType: '门锁维修', count: 89, percentage: 9.1 },
        { repairType: '其他', count: 57, percentage: 5.9 }
      ]
      renderRepairTypeChart(mockData)
      resolve(true)
    }, 300)
  })
}

const loadMockServiceCompletionStats = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const mockData = []
      const today = new Date()
      
      // 生成最近12个月的数据
      for (let i = 11; i >= 0; i--) {
        const date = new Date(today.getFullYear(), today.getMonth() - i, 1)
        const periodLabel = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
        mockData.push({
          periodLabel,
          completedCount: Math.floor(Math.random() * 50) + 80,
          pendingCount: Math.floor(Math.random() * 20) + 10,
          avgRating: Math.random() * 1.5 + 3.5
        })
      }
      
      renderCompletionChart(mockData)
      resolve(true)
    }, 300)
  })
}

const loadMockUserActivityStats = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const mockData = []
      const today = new Date()
      
      // 生成最近30天的数据
      for (let i = 29; i >= 0; i--) {
        const date = new Date(today.getTime() - i * 24 * 60 * 60 * 1000)
        const activityDate = `${date.getMonth() + 1}-${date.getDate()}`
        mockData.push({
          activityDate,
          activeUsers: Math.floor(Math.random() * 200) + 100,
          totalActivities: Math.floor(Math.random() * 500) + 200
        })
      }
      
      renderActivityChart(mockData)
      resolve(true)
    }, 300)
  })
}

const loadMockSatisfactionStats = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const mockData = [
        { serviceName: '家电维修', avgRating: 4.5, totalRatings: 156 },
        { serviceName: '管道疏通', avgRating: 4.2, totalRatings: 123 },
        { serviceName: '电路维修', avgRating: 4.3, totalRatings: 98 },
        { serviceName: '保洁服务', avgRating: 4.6, totalRatings: 89 },
        { serviceName: '搬家服务', avgRating: 4.1, totalRatings: 67 },
        { serviceName: '装修监理', avgRating: 4.4, totalRatings: 45 }
      ]
      renderSatisfactionChart(mockData)
      resolve(true)
    }, 300)
  })
}

const loadMockServiceStats = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      serviceStats.value = [
        {
          serviceType: '维修服务',
          serviceName: '家电维修',
          totalCount: 342,
          completedCount: 289,
          avgRating: 4.5,
          avgResponseHours: 2.3
        },
        {
          serviceType: '维修服务', 
          serviceName: '管道疏通',
          totalCount: 287,
          completedCount: 256,
          avgRating: 4.2,
          avgResponseHours: 1.8
        },
        {
          serviceType: '维修服务',
          serviceName: '电路维修', 
          totalCount: 198,
          completedCount: 167,
          avgRating: 4.3,
          avgResponseHours: 3.1
        },
        {
          serviceType: '生活服务',
          serviceName: '保洁服务',
          totalCount: 156,
          completedCount: 145,
          avgRating: 4.6,
          avgResponseHours: 1.2
        },
        {
          serviceType: '生活服务',
          serviceName: '搬家服务',
          totalCount: 89,
          completedCount: 78,
          avgRating: 4.1,
          avgResponseHours: 4.5
        }
      ]
      resolve(true)
    }, 300)
  })
}

const loadMockRepairStats = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      repairStats.value = [
        {
          repairType: '家电维修',
          totalCount: 342,
          completedCount: 289,
          pendingCount: 53,
          avgCompletionHours: 6.5
        },
        {
          repairType: '管道疏通',
          totalCount: 287,
          completedCount: 256,
          pendingCount: 31,
          avgCompletionHours: 3.2
        },
        {
          repairType: '电路维修',
          totalCount: 198,
          completedCount: 167,
          pendingCount: 31,
          avgCompletionHours: 8.7
        },
        {
          repairType: '门锁维修',
          totalCount: 89,
          completedCount: 78,
          pendingCount: 11,
          avgCompletionHours: 2.1
        },
        {
          repairType: '其他',
          totalCount: 57,
          completedCount: 49,
          pendingCount: 8,
          avgCompletionHours: 4.3
        }
      ]
      resolve(true)
    }, 300)
  })
}

// 图表渲染方法
const renderRepairTypeChart = (data: any[]) => {
  if (!repairTypeChartInstance) {
    repairTypeChartInstance = echarts.init(repairTypeChart.value)
  }
  
  const option = {
    title: {
      text: '保修类型分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: '50%',
      data: data.map(item => ({
        name: item.repairType,
        value: item.count
      }))
    }]
  }
  
  repairTypeChartInstance.setOption(option)
}

const renderCompletionChart = (data: any[]) => {
  if (!completionChartInstance) {
    completionChartInstance = echarts.init(completionChart.value)
  }
  
  const option = {
    title: {
      text: '服务完成量趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.periodLabel)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '完成数量',
      type: 'line',
      data: data.map(item => item.completedCount)
    }]
  }
  
  completionChartInstance.setOption(option)
}

const renderActivityChart = (data: any[]) => {
  if (!activityChartInstance) {
    activityChartInstance = echarts.init(activityChart.value)
  }
  
  const option = {
    title: {
      text: '用户活跃度',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.activityDate)
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '活跃用户',
      type: 'bar',
      data: data.map(item => item.activeUsers)
    }]
  }
  
  activityChartInstance.setOption(option)
}

const renderSatisfactionChart = (data: any[]) => {
  if (!satisfactionChartInstance) {
    satisfactionChartInstance = echarts.init(satisfactionChart.value)
  }
  
  const option = {
    title: {
      text: '满意度分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.serviceName)
    },
    yAxis: {
      type: 'value',
      max: 5
    },
    series: [{
      name: '平均评分',
      type: 'bar',
      data: data.map(item => item.avgRating)
    }]
  }
  
  satisfactionChartInstance.setOption(option)
}

const viewDetail = (type: string) => {
  router.push({
    name: 'AnalyticsDetail',
    params: { type },
    query: {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
  })
}

const exportReport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请先选择日期范围')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认导出数据分析报告吗？', '确认导出', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    exporting.value = true
    const [startDate, endDate] = dateRange.value
    
    // 生成模拟报告数据
    const reportData = generateMockReportData()
    const csvContent = generateReportCSV(reportData)
    
    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `数据分析报告_${startDate}_${endDate}.csv`
    link.click()
    
    ElMessage.success('报告导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出报告失败:', error)
      ElMessage.error('导出报告失败')
    }
  } finally {
    exporting.value = false
  }
}

const generateMockReportData = () => {
  return {
    summary: summary,
    serviceStats: serviceStats.value,
    repairStats: repairStats.value
  }
}

const generateReportCSV = (data: any) => {
  let csv = '数据分析报告\n\n'
  
  // 摘要数据
  csv += '数据摘要\n'
  csv += '指标,数值\n'
  csv += `总订单数,${data.summary.totalOrders}\n`
  csv += `已完成订单,${data.summary.completedOrders}\n`
  csv += `活跃用户,${data.summary.activeUsers}\n`
  csv += `平均满意度,${data.summary.avgSatisfactionScore}\n\n`
  
  // 服务统计
  csv += '服务统计\n'
  csv += '服务类型,服务名称,总数量,已完成,平均评分,平均响应时长(小时)\n'
  data.serviceStats.forEach((item: any) => {
    csv += `${item.serviceType},${item.serviceName},${item.totalCount},${item.completedCount},${item.avgRating},${item.avgResponseHours}\n`
  })
  
  csv += '\n保修统计\n'
  csv += '保修类型,总数量,已完成,待处理,平均完成时长(小时)\n'
  data.repairStats.forEach((item: any) => {
    csv += `${item.repairType},${item.totalCount},${item.completedCount},${item.pendingCount},${item.avgCompletionHours}\n`
  })
  
  return csv
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    loadAllData()
  })
})
</script>

<style scoped lang="scss">
.analytics-container {
  padding: 20px;

  .analytics-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h1 {
      margin: 0;
      color: #303133;
    }

    .controls {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .summary-cards {
    margin-bottom: 30px;

    .summary-card {
      .card-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .card-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 24px;

          &.total-orders {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.completed-orders {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.active-users {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          }

          &.satisfaction {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          }
        }

        .card-info {
          .card-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .card-label {
            color: #909399;
            font-size: 14px;
          }
        }
      }
    }
  }

  .charts-section {
    margin-bottom: 30px;

    .chart-card {
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .chart-controls {
          display: flex;
          gap: 8px;
          align-items: center;
        }
      }

      .chart-container {
        height: 300px;
        width: 100%;
      }
    }
  }

  .data-tables {
    background: white;
    border-radius: 8px;
    padding: 20px;
  }
}
</style>
