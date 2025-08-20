<template>
  <div class="analytics-detail-container">
    <div class="detail-header">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ name: 'Analytics' }">数据分析</el-breadcrumb-item>
        <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="header-actions">
        <el-button @click="exportChart" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出图表
        </el-button>
        <el-button @click="exportData" :loading="exportingData">
          <el-icon><Document /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 详细图表 -->
    <el-card class="chart-section">
      <template #header>
        <div class="chart-header">
          <h2>{{ chartTitle }}</h2>
          <div class="chart-controls">
            <el-select v-if="type === 'completion'" v-model="statisticsType" @change="loadData" size="small">
              <el-option label="按日统计" :value="1" />
              <el-option label="按周统计" :value="2" />
              <el-option label="按月统计" :value="3" />
              <el-option label="按季度统计" :value="4" />
              <el-option label="按年统计" :value="5" />
            </el-select>
            <el-select v-if="type === 'satisfaction'" v-model="serviceType" @change="loadData" size="small" clearable placeholder="选择服务类型">
              <el-option label="全部类型" value="" />
              <el-option label="家电维修" value="APPLIANCE_REPAIR" />
              <el-option label="清洁服务" value="CLEANING_SERVICE" />
              <el-option label="搬家服务" value="MOVING_SERVICE" />
            </el-select>
          </div>
        </div>
      </template>
      
      <div ref="chartContainer" class="chart-container" :class="{ loading: loading }">
        <el-loading v-if="loading" element-loading-text="加载中..." />
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="data-table-section">
      <template #header>
        <div class="section-header">
          <h3>详细数据</h3>
          <el-input
            v-model="searchText"
            placeholder="搜索数据..."
            style="width: 200px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <el-table 
        :data="filteredTableData" 
        style="width: 100%"
        :loading="loading"
        empty-text="暂无数据"
      >
        <el-table-column 
          v-for="column in tableColumns" 
          :key="column.key"
          :prop="column.key"
          :label="column.title"
          :formatter="(row, column, cellValue) => formatCellValue(cellValue, column.property)"
        />
      </el-table>

      <el-pagination
        v-if="tableData.length > 10"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="tableData.length"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 数据洞察 -->
    <el-card v-if="insights.length > 0" class="insights-section">
      <template #header>
        <h3>数据洞察</h3>
      </template>
      
      <div class="insights-grid">
        <div 
          v-for="(insight, index) in insights" 
          :key="index"
          class="insight-card"
        >
          <div class="insight-icon" :style="{ backgroundColor: insight.color }">
            <el-icon><component :is="insight.icon" /></el-icon>
          </div>
          <div class="insight-content">
            <div class="insight-title">{{ insight.title }}</div>
            <div class="insight-description">{{ insight.description }}</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Document, Search, TrendCharts, Warning, InfoFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const exporting = ref(false)
const exportingData = ref(false)
const type = ref(route.params.type as string)
const statisticsType = ref(3)
const serviceType = ref('')
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const chartContainer = ref()
let chartInstance: echarts.ECharts | null = null

const tableData = ref([])
const tableColumns = ref([])
const insights = ref([])

// 计算属性
const pageTitle = computed(() => {
  const titles = {
    'repair-type': '保修类型分析',
    'completion': '服务完成量分析',
    'activity': '用户活跃度分析',
    'satisfaction': '满意度分析',
    'response-time': '响应时长分析'
  }
  return titles[type.value] || '数据分析详情'
})

const chartTitle = computed(() => {
  const titles = {
    'repair-type': '保修类型分布详情',
    'completion': '服务完成量趋势详情',
    'activity': '用户活跃度变化详情',
    'satisfaction': '满意度评分详情',
    'response-time': '服务响应时长详情'
  }
  return titles[type.value] || '数据详情'
})

const filteredTableData = computed(() => {
  if (!searchText.value) return paginatedData.value
  
  return paginatedData.value.filter(row => {
    return Object.values(row).some(value => 
      String(value).toLowerCase().includes(searchText.value.toLowerCase())
    )
  })
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.value.slice(start, end)
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    // 使用模拟数据替代API调用
    switch (type.value) {
      case 'repair-type':
        await loadMockRepairTypeData()
        break
      case 'completion':
        await loadMockCompletionData()
        break
      case 'activity':
        await loadMockActivityData()
        break
      case 'satisfaction':
        await loadMockSatisfactionData()
        break
      case 'response-time':
        await loadMockResponseTimeData()
        break
    }
    
    generateInsights()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 模拟数据加载方法
const loadMockRepairTypeData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const data = [
        { repairType: '家电维修', count: 342, percentage: 35.2 },
        { repairType: '管道疏通', count: 287, percentage: 29.5 },
        { repairType: '电路维修', count: 198, percentage: 20.3 },
        { repairType: '门锁维修', count: 89, percentage: 9.1 },
        { repairType: '墙面修补', count: 67, percentage: 6.9 },
        { repairType: '水龙头维修', count: 45, percentage: 4.6 },
        { repairType: '其他', count: 43, percentage: 4.4 }
      ]
      tableData.value = data
      tableColumns.value = [
        { key: 'repairType', title: '保修类型' },
        { key: 'count', title: '数量' },
        { key: 'percentage', title: '占比(%)' }
      ]
      renderRepairTypeChart(data)
      resolve(true)
    }, 500)
  })
}

const loadMockCompletionData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const data = []
      const today = new Date()
      
      // 根据统计类型生成不同的时间周期数据
      let periods = 12
      let dateFormat = (date: Date, i: number) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      
      if (statisticsType.value === 1) { // 按日
        periods = 30
        dateFormat = (date: Date, i: number) => {
          const d = new Date(date.getTime() - i * 24 * 60 * 60 * 1000)
          return `${d.getMonth() + 1}-${d.getDate()}`
        }
      } else if (statisticsType.value === 2) { // 按周
        periods = 20
        dateFormat = (date: Date, i: number) => `第${20-i}周`
      } else if (statisticsType.value === 4) { // 按季度
        periods = 8
        dateFormat = (date: Date, i: number) => `${date.getFullYear()}Q${Math.floor((date.getMonth() - i * 3) / 3) + 1}`
      } else if (statisticsType.value === 5) { // 按年
        periods = 5
        dateFormat = (date: Date, i: number) => `${date.getFullYear() - i}`
      }
      
      for (let i = periods - 1; i >= 0; i--) {
        const periodLabel = dateFormat(today, i)
        data.push({
          periodLabel,
          completedCount: Math.floor(Math.random() * 80) + 60,
          pendingCount: Math.floor(Math.random() * 25) + 10,
          avgRating: Math.random() * 1.5 + 3.5
        })
      }
      
      tableData.value = data
      tableColumns.value = [
        { key: 'periodLabel', title: '时间周期' },
        { key: 'completedCount', title: '完成数量' },
        { key: 'pendingCount', title: '待处理数量' },
        { key: 'avgRating', title: '平均评分' }
      ]
      renderCompletionChart(data)
      resolve(true)
    }, 500)
  })
}

const loadMockActivityData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const data = []
      const today = new Date()
      
      for (let i = 60; i >= 0; i--) {
        const date = new Date(today.getTime() - i * 24 * 60 * 60 * 1000)
        const activityDate = `${date.getMonth() + 1}-${date.getDate()}`
        data.push({
          activityDate,
          activeUsers: Math.floor(Math.random() * 300) + 150,
          totalActivities: Math.floor(Math.random() * 800) + 400
        })
      }
      
      tableData.value = data
      tableColumns.value = [
        { key: 'activityDate', title: '日期' },
        { key: 'activeUsers', title: '活跃用户数' },
        { key: 'totalActivities', title: '总活动数' }
      ]
      renderActivityChart(data)
      resolve(true)
    }, 500)
  })
}

const loadMockSatisfactionData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const services = [
        '家电维修', '管道疏通', '电路维修', '保洁服务', '搬家服务', 
        '装修监理', '门锁维修', '水龙头维修', '空调维修', '网络维修'
      ]
      
      const data = services.map(serviceName => {
        const totalRatings = Math.floor(Math.random() * 200) + 50
        const avgRating = Math.random() * 2 + 3
        const highRatings = Math.floor(totalRatings * (avgRating >= 4 ? 0.6 : 0.3))
        const lowRatings = Math.floor(totalRatings * (avgRating < 3 ? 0.4 : 0.1))
        const mediumRatings = totalRatings - highRatings - lowRatings
        
        return {
          serviceType: serviceName.includes('维修') ? '维修服务' : '生活服务',
          serviceName,
          avgRating: Number(avgRating.toFixed(2)),
          totalRatings,
          highRatings,
          mediumRatings,
          lowRatings
        }
      })
      
      tableData.value = data
      tableColumns.value = [
        { key: 'serviceType', title: '服务类型' },
        { key: 'serviceName', title: '服务名称' },
        { key: 'avgRating', title: '平均评分' },
        { key: 'totalRatings', title: '评分总数' },
        { key: 'highRatings', title: '高分(4-5分)' },
        { key: 'mediumRatings', title: '中分(3分)' },
        { key: 'lowRatings', title: '低分(1-2分)' }
      ]
      renderSatisfactionChart(data)
      resolve(true)
    }, 500)
  })
}

const loadMockResponseTimeData = async () => {
  return new Promise(resolve => {
    setTimeout(() => {
      const services = [
        '家电维修', '管道疏通', '电路维修', '保洁服务', '搬家服务', 
        '装修监理', '门锁维修', '水龙头维修', '空调维修', '网络维修',
        '木工维修', '瓦工维修', '油漆维修', '卫浴维修', '灯具维修'
      ]
      
      const data = services.map(serviceName => {
        const avgResponse = Math.random() * 8 + 1
        const minResponse = avgResponse * 0.3
        const maxResponse = avgResponse * 2.5
        
        return {
          serviceType: serviceName.includes('维修') ? '维修服务' : '生活服务',
          serviceName,
          avgResponseHours: Number(avgResponse.toFixed(2)),
          minResponseHours: Number(minResponse.toFixed(2)),
          maxResponseHours: Number(maxResponse.toFixed(2)),
          totalOrders: Math.floor(Math.random() * 150) + 50
        }
      })
      
      tableData.value = data
      tableColumns.value = [
        { key: 'serviceType', title: '服务类型' },
        { key: 'serviceName', title: '服务名称' },
        { key: 'avgResponseHours', title: '平均响应时长(小时)' },
        { key: 'minResponseHours', title: '最快响应(小时)' },
        { key: 'maxResponseHours', title: '最慢响应(小时)' },
        { key: 'totalOrders', title: '订单总数' }
      ]
      renderResponseTimeChart(data)
      resolve(true)
    }, 500)
  })
}

// 图表渲染方法
const renderRepairTypeChart = (data: any[]) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
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
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: data.map(item => ({
        name: item.repairType,
        value: item.count
      }))
    }]
  }
  
  chartInstance.setOption(option)
}

const renderCompletionChart = (data: any[]) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }
  
  const option = {
    title: {
      text: '服务完成量趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['完成数量', '待处理数量', '平均评分']
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.periodLabel)
    },
    yAxis: [
      {
        type: 'value',
        name: '数量'
      },
      {
        type: 'value',
        name: '评分',
        max: 5
      }
    ],
    series: [
      {
        name: '完成数量',
        type: 'bar',
        data: data.map(item => item.completedCount)
      },
      {
        name: '待处理数量',
        type: 'bar',
        data: data.map(item => item.pendingCount)
      },
      {
        name: '平均评分',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(item => item.avgRating)
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const renderActivityChart = (data: any[]) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }
  
  const option = {
    title: {
      text: '用户活跃度变化',
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
    series: [
      {
        name: '活跃用户数',
        type: 'line',
        smooth: true,
        data: data.map(item => item.activeUsers)
      },
      {
        name: '总活动数',
        type: 'bar',
        data: data.map(item => item.totalActivities)
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const renderSatisfactionChart = (data: any[]) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }
  
  const option = {
    title: {
      text: '满意度评分分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.serviceName),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      max: 5
    },
    series: [{
      name: '平均评分',
      type: 'bar',
      data: data.map(item => item.avgRating),
      itemStyle: {
        color: function(params) {
          const score = params.data
          if (score >= 4) return '#67C23A'
          if (score >= 3) return '#E6A23C'
          return '#F56C6C'
        }
      }
    }]
  }
  
  chartInstance.setOption(option)
}

const renderResponseTimeChart = (data: any[]) => {
  if (!chartInstance) {
    chartInstance = echarts.init(chartContainer.value)
  }
  
  const option = {
    title: {
      text: '服务响应时长分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.serviceName),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '小时'
    },
    series: [{
      name: '平均响应时长',
      type: 'bar',
      data: data.map(item => item.avgResponseHours)
    }]
  }
  
  chartInstance.setOption(option)
}

const formatCellValue = (value: any, key: string) => {
  if (value === null || value === undefined) return '-'
  
  if (key.includes('Rating') || key.includes('avgRating')) {
    return Number(value).toFixed(2)
  }
  
  if (key.includes('percentage')) {
    return `${Number(value).toFixed(2)}%`
  }
  
  if (key.includes('Hours')) {
    return `${Number(value).toFixed(2)}小时`
  }
  
  return value
}

const generateInsights = () => {
  insights.value = []
  
  if (tableData.value.length === 0) return
  
  switch (type.value) {
    case 'repair-type':
      generateRepairTypeInsights()
      break
    case 'completion':
      generateCompletionInsights()
      break
    case 'activity':
      generateActivityInsights()
      break
    case 'satisfaction':
      generateSatisfactionInsights()
      break
    case 'response-time':
      generateResponseTimeInsights()
      break
  }
}

const generateRepairTypeInsights = () => {
  const total = tableData.value.reduce((sum, item) => sum + item.count, 0)
  const maxType = tableData.value.reduce((max, item) => item.count > max.count ? item : max)
  
  insights.value.push({
    icon: TrendCharts,
    color: '#409EFF',
    title: '最常见保修类型',
    description: `${maxType.repairType}占总量的${maxType.percentage}%，共${maxType.count}次`
  })
  
  if (tableData.value.length > 3) {
    insights.value.push({
      icon: InfoFilled,
      color: '#67C23A',
      title: '类型多样性',
      description: `共有${tableData.value.length}种不同的保修类型，说明服务范围较广`
    })
  }
}

const generateCompletionInsights = () => {
  const avgCompleted = tableData.value.reduce((sum, item) => sum + item.completedCount, 0) / tableData.value.length
  const avgRating = tableData.value.reduce((sum, item) => sum + (item.avgRating || 0), 0) / tableData.value.length
  
  insights.value.push({
    icon: TrendCharts,
    color: '#67C23A',
    title: '平均完成量',
    description: `每个周期平均完成${avgCompleted.toFixed(0)}个订单`
  })
  
  if (avgRating >= 4) {
    insights.value.push({
      icon: InfoFilled,
      color: '#67C23A',
      title: '服务质量优秀',
      description: `平均满意度${avgRating.toFixed(1)}分，服务质量较高`
    })
  } else if (avgRating < 3) {
    insights.value.push({
      icon: Warning,
      color: '#F56C6C',
      title: '服务质量待改善',
      description: `平均满意度${avgRating.toFixed(1)}分，建议优化服务流程`
    })
  }
}

const generateSatisfactionInsights = () => {
  const highRatingServices = tableData.value.filter(item => item.avgRating >= 4)
  const lowRatingServices = tableData.value.filter(item => item.avgRating < 3)
  
  if (highRatingServices.length > 0) {
    insights.value.push({
      icon: InfoFilled,
      color: '#67C23A',
      title: '优质服务',
      description: `有${highRatingServices.length}个服务的评分在4分以上`
    })
  }
  
  if (lowRatingServices.length > 0) {
    insights.value.push({
      icon: Warning,
      color: '#F56C6C',
      title: '需要改进',
      description: `有${lowRatingServices.length}个服务的评分低于3分，需要重点关注`
    })
  }
}

const generateActivityInsights = () => {
  const maxActivity = tableData.value.reduce((max, item) => item.activeUsers > max.activeUsers ? item : max)
  const minActivity = tableData.value.reduce((min, item) => item.activeUsers < min.activeUsers ? item : min)
  
  insights.value.push({
    icon: TrendCharts,
    color: '#409EFF',
    title: '最活跃日期',
    description: `${maxActivity.activityDate}活跃用户最多，共${maxActivity.activeUsers}人`
  })
  
  if (maxActivity.activeUsers > minActivity.activeUsers * 2) {
    insights.value.push({
      icon: Warning,
      color: '#E6A23C',
      title: '活跃度波动较大',
      description: `最高与最低活跃度相差${maxActivity.activeUsers - minActivity.activeUsers}人，建议分析原因`
    })
  }
}

const generateResponseTimeInsights = () => {
  const avgResponse = tableData.value.reduce((sum, item) => sum + item.avgResponseHours, 0) / tableData.value.length
  const fastestService = tableData.value.reduce((min, item) => item.avgResponseHours < min.avgResponseHours ? item : min)
  const slowestService = tableData.value.reduce((max, item) => item.avgResponseHours > max.avgResponseHours ? item : max)
  
  insights.value.push({
    icon: TrendCharts,
    color: '#409EFF',
    title: '平均响应时长',
    description: `整体平均响应时长为${avgResponse.toFixed(1)}小时`
  })
  
  insights.value.push({
    icon: InfoFilled,
    color: '#67C23A',
    title: '响应最快服务',
    description: `${fastestService.serviceName}响应最快，平均${fastestService.avgResponseHours.toFixed(1)}小时`
  })
  
  if (slowestService.avgResponseHours > avgResponse * 2) {
    insights.value.push({
      icon: Warning,
      color: '#F56C6C',
      title: '响应较慢服务',
      description: `${slowestService.serviceName}响应较慢，平均${slowestService.avgResponseHours.toFixed(1)}小时，建议优化`
    })
  }
}

const exportChart = async () => {
  if (!chartInstance) {
    ElMessage.warning('图表未加载完成')
    return
  }
  
  exporting.value = true
  try {
    const url = chartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    
    const link = document.createElement('a')
    link.href = url
    link.download = `${chartTitle.value}.png`
    link.click()
    
    ElMessage.success('图表导出成功')
  } catch (error) {
    console.error('导出图表失败:', error)
    ElMessage.error('导出图表失败')
  } finally {
    exporting.value = false
  }
}

const exportData = async () => {
  if (tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  exportingData.value = true
  try {
    // 这里可以调用后端API导出Excel文件
    // 或者前端生成CSV文件
    const csvContent = generateCSV()
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `${pageTitle.value}_数据.csv`
    link.click()
    
    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出数据失败:', error)
    ElMessage.error('导出数据失败')
  } finally {
    exportingData.value = false
  }
}

const generateCSV = () => {
  const headers = tableColumns.value.map(col => col.title).join(',')
  const rows = tableData.value.map(row => 
    tableColumns.value.map(col => row[col.key]).join(',')
  )
  return [headers, ...rows].join('\n')
}

// 监听路由变化
watch(() => route.params.type, (newType) => {
  type.value = newType as string
  loadData()
})

watch(() => [statisticsType.value, serviceType.value], () => {
  loadData()
})

// 生命周期
onMounted(() => {
  nextTick(() => {
    loadData()
  })
})
</script>

<style scoped lang="scss">
.analytics-detail-container {
  padding: 20px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .chart-section {
    margin-bottom: 20px;

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
      }

      .chart-controls {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }

    .chart-container {
      height: 400px;
      width: 100%;
      
      &.loading {
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .data-table-section {
    margin-bottom: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h3 {
        margin: 0;
      }
    }
  }

  .insights-section {
    h3 {
      margin: 0 0 16px 0;
    }

    .insights-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;

      .insight-card {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        border: 1px solid #ebeef5;
        border-radius: 6px;
        background: #fafafa;

        .insight-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 18px;
        }

        .insight-content {
          flex: 1;

          .insight-title {
            font-weight: bold;
            color: #303133;
            margin-bottom: 4px;
          }

          .insight-description {
            color: #606266;
            font-size: 14px;
            line-height: 1.4;
          }
        }
      }
    }
  }
}
</style>
