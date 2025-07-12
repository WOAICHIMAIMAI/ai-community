<template>
  <div class="dashboard-container">
    <h2 class="page-title">控制台</h2>
    
    <!-- 数据概览卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-icon" style="background-color: rgba(24, 144, 255, 0.15)">
            <el-icon color="#1890ff"><user /></el-icon>
          </div>
          <div class="data-info">
            <div class="data-title">用户总数</div>
            <div class="data-value">{{ dashboardData.userCount }}</div>
            <div class="data-compare">
              <span>较昨日</span>
              <span :class="[dashboardData.userIncrease > 0 ? 'up' : 'down']">
                {{ Math.abs(dashboardData.userIncrease) }}%
                <el-icon v-if="dashboardData.userIncrease > 0">
                  <caret-top />
                </el-icon>
                <el-icon v-else>
                  <caret-bottom />
                </el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-icon" style="background-color: rgba(82, 196, 26, 0.15)">
            <el-icon color="#52c41a"><document /></el-icon>
          </div>
          <div class="data-info">
            <div class="data-title">帖子总数</div>
            <div class="data-value">{{ dashboardData.postCount }}</div>
            <div class="data-compare">
              <span>较昨日</span>
              <span :class="[dashboardData.postIncrease > 0 ? 'up' : 'down']">
                {{ Math.abs(dashboardData.postIncrease) }}%
                <el-icon v-if="dashboardData.postIncrease > 0">
                  <caret-top />
                </el-icon>
                <el-icon v-else>
                  <caret-bottom />
                </el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-icon" style="background-color: rgba(250, 173, 20, 0.15)">
            <el-icon color="#faad14"><tools /></el-icon>
          </div>
          <div class="data-info">
            <div class="data-title">待处理工单</div>
            <div class="data-value">{{ dashboardData.pendingOrderCount }}</div>
            <div class="data-compare">
              <span>较昨日</span>
              <span :class="[dashboardData.orderIncrease > 0 ? 'up' : 'down']">
                {{ Math.abs(dashboardData.orderIncrease) }}%
                <el-icon v-if="dashboardData.orderIncrease > 0">
                  <caret-top />
                </el-icon>
                <el-icon v-else>
                  <caret-bottom />
                </el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-icon" style="background-color: rgba(255, 77, 79, 0.15)">
            <el-icon color="#ff4d4f"><warning /></el-icon>
          </div>
          <div class="data-info">
            <div class="data-title">待审核内容</div>
            <div class="data-value">{{ dashboardData.reviewCount }}</div>
            <div class="data-compare">
              <span>较昨日</span>
              <span :class="[dashboardData.reviewIncrease > 0 ? 'up' : 'down']">
                {{ Math.abs(dashboardData.reviewIncrease) }}%
                <el-icon v-if="dashboardData.reviewIncrease > 0">
                  <caret-top />
                </el-icon>
                <el-icon v-else>
                  <caret-bottom />
                </el-icon>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="userGrowthChartRef" class="chart"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>报修工单类型分布</span>
            </div>
          </template>
          <div ref="repairTypeChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最新工单</span>
              <el-link type="primary" :underline="false" href="#/repairs">查看全部</el-link>
            </div>
          </template>
          <el-table :data="latestOrders" style="width: 100%" size="large">
            <el-table-column prop="title" label="工单标题" show-overflow-tooltip />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <div class="card-header">
              <span>待审核内容</span>
              <el-link type="primary" :underline="false" href="#/posts">查看全部</el-link>
            </div>
          </template>
          <el-table :data="pendingReviews" style="width: 100%" size="large">
            <el-table-column prop="title" label="内容标题" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'post' ? 'success' : 'warning'">
                  {{ row.type === 'post' ? '帖子' : '评论' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { User, Document, Tools, Warning, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 导入仪表盘API
import { 
  getDashboardOverview, 
  getUserGrowthTrend, 
  getRepairTypeDistribution, 
  getLatestOrders, 
  getPendingReviews 
} from '@/api/dashboard'

// 完整引入echarts，而不是按需引入
import * as echarts from 'echarts'

// 时间范围选择
const timeRange = ref('week')

// 图表引用
const userGrowthChartRef = ref<HTMLDivElement | null>(null)
const repairTypeChartRef = ref<HTMLDivElement | null>(null)
let userGrowthChart: echarts.ECharts | null = null
let repairTypeChart: echarts.ECharts | null = null

// 仪表盘数据 - 初始化为默认值，后续会被API数据替换
const dashboardData = reactive({
  userCount: 0,
  userIncrease: 0,
  postCount: 0,
  postIncrease: 0,
  pendingOrderCount: 0,
  orderIncrease: 0,
  reviewCount: 0,
  reviewIncrease: 0,
})

// 最新工单数据
const latestOrders = ref([])

// 待审核内容数据
const pendingReviews = ref([])

// 加载状态
const loading = ref({
  overview: false,
  userGrowth: false,
  repairTypes: false,
  latestOrders: false,
  pendingReviews: false
})

// 获取工单状态样式类型
const getOrderStatusType = (status: string): 'warning' | 'primary' | 'success' | 'info' => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'processing':
      return 'primary'
    case 'completed':
      return 'success'
    default:
      return 'info'
  }
}

// 获取工单状态文本
const getOrderStatusText = (status: string): string => {
  switch (status) {
    case 'pending':
      return '待处理'
    case 'processing':
      return '处理中'
    case 'completed':
      return '已完成'
    default:
      return '未知'
  }
}

// 加载仪表盘概览数据
const loadDashboardOverview = async () => {
  try {
    loading.value.overview = true
    const res = await getDashboardOverview()
    if (res.code === 200 && res.data) {
      Object.assign(dashboardData, res.data)
    } else {
      ElMessage.warning('获取仪表盘数据失败：' + (res.message || '未知错误'))
    }
  } catch (error: any) {
    console.error('获取仪表盘数据出错:', error)
    ElMessage.error('获取仪表盘数据出错：' + (error.message || '未知错误'))
  } finally {
    loading.value.overview = false
  }
}

// 初始化用户增长趋势图表
const initUserGrowthChart = async () => {
  if (!userGrowthChartRef.value) {
    console.error('用户增长图表容器不存在')
    return
  }
  
  console.log('初始化用户增长图表，容器尺寸:', userGrowthChartRef.value.clientWidth, userGrowthChartRef.value.clientHeight)
  
  // 销毁旧图表实例
  if (userGrowthChart) {
    userGrowthChart.dispose()
  }
  
  // 创建新图表实例
  try {
    loading.value.userGrowth = true
    userGrowthChart = echarts.init(userGrowthChartRef.value)
    
    // 从API获取用户增长数据
    const res = await getUserGrowthTrend(timeRange.value)
    
    let xAxisData: string[] = []
    let userData: number[] = []
    
    if (res.code === 200 && res.data) {
      xAxisData = res.data.dates
      userData = res.data.counts
    } else {
      // 如果API失败，使用默认数据
      console.warn('获取用户增长数据失败，使用默认数据')
      switch (timeRange.value) {
        case 'week':
          xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          userData = [30, 40, 35, 50, 45, 60, 70]
          break
        case 'month':
          xAxisData = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)
          userData = Array.from({ length: 30 }, () => Math.floor(Math.random() * 50) + 20)
          break
        case 'year':
          xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
          userData = [320, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850]
          break
      }
    }
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: xAxisData
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '新增用户',
          type: 'line',
          smooth: true,
          data: userData,
          itemStyle: {
            color: '#1890ff'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(24,144,255,0.3)' },
                { offset: 1, color: 'rgba(24,144,255,0.1)' }
              ]
            }
          }
        }
      ]
    }
    
    // 设置图表配置
    userGrowthChart.setOption(option)
    console.log('用户增长图表初始化完成')
  } catch (error) {
    console.error('用户增长图表初始化失败:', error)
  } finally {
    loading.value.userGrowth = false
  }
}

// 初始化报修工单类型分布图表
const initRepairTypeChart = async () => {
  if (!repairTypeChartRef.value) {
    console.error('工单类型图表容器不存在')
    return
  }
  
  console.log('初始化工单类型图表，容器尺寸:', repairTypeChartRef.value.clientWidth, repairTypeChartRef.value.clientHeight)
  
  // 销毁旧图表实例
  if (repairTypeChart) {
    repairTypeChart.dispose()
  }
  
  // 创建新图表实例
  try {
    loading.value.repairTypes = true
    repairTypeChart = echarts.init(repairTypeChartRef.value)
    
    // 从API获取工单类型分布数据
    const res = await getRepairTypeDistribution()
    
    let typeData = []
    let legendData = []
    
    if (res.code === 200 && res.data) {
      legendData = res.data.types
      typeData = res.data.types.map((type, index) => ({
        value: res.data.counts[index],
        name: type
      }))
    } else {
      // 如果API失败，使用默认数据
      console.warn('获取工单类型分布数据失败，使用默认数据')
      legendData = ['水电故障', '设备损坏', '环境问题', '安全隐患', '其他']
      typeData = [
        { value: 35, name: '水电故障' },
        { value: 25, name: '设备损坏' },
        { value: 20, name: '环境问题' },
        { value: 15, name: '安全隐患' },
        { value: 5, name: '其他' }
      ]
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'horizontal',
        bottom: 10,
        data: legendData
      },
      series: [
        {
          name: '工单类型',
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
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: typeData
        }
      ]
    }
    
    // 设置图表配置
    repairTypeChart.setOption(option)
    console.log('工单类型图表初始化完成')
  } catch (error) {
    console.error('工单类型图表初始化失败:', error)
  } finally {
    loading.value.repairTypes = false
  }
}

// 加载最新工单数据
const loadLatestOrders = async () => {
  try {
    loading.value.latestOrders = true
    const res = await getLatestOrders(4) // 限制4条数据
    if (res.code === 200 && res.data) {
      latestOrders.value = res.data
    } else {
      // 如果API失败，使用默认数据
      console.warn('获取最新工单数据失败，使用默认数据')
      latestOrders.value = [
        { id: 1, title: '1号楼电梯故障', userName: '张三', status: 'pending', createdAt: '2023-06-12 10:32:15' },
        { id: 2, title: '小区健身器材损坏', userName: '李四', status: 'processing', createdAt: '2023-06-11 15:47:23' },
        { id: 3, title: '下水道堵塞', userName: '王五', status: 'pending', createdAt: '2023-06-11 09:15:42' },
        { id: 4, title: '路灯不亮', userName: '赵六', status: 'completed', createdAt: '2023-06-10 18:30:11' },
      ]
    }
  } catch (error) {
    console.error('获取最新工单数据失败:', error)
  } finally {
    loading.value.latestOrders = false
  }
}

// 加载待审核内容数据
const loadPendingReviews = async () => {
  try {
    loading.value.pendingReviews = true
    const res = await getPendingReviews(4) // 限制4条数据
    if (res.code === 200 && res.data) {
      pendingReviews.value = res.data
    } else {
      // 如果API失败，使用默认数据
      console.warn('获取待审核内容数据失败，使用默认数据')
      pendingReviews.value = [
        { id: 1, title: '小区环境整治建议', type: 'post', userName: '张三', createdAt: '2023-06-12 10:32:15' },
        { id: 2, title: '业主大会意见反馈', type: 'post', userName: '李四', createdAt: '2023-06-11 15:47:23' },
        { id: 3, title: '这个问题已经解决了', type: 'comment', userName: '王五', createdAt: '2023-06-11 09:15:42' },
        { id: 4, title: '建议增加垃圾分类箱', type: 'post', userName: '赵六', createdAt: '2023-06-10 18:30:11' },
      ]
    }
  } catch (error) {
    console.error('获取待审核内容数据失败:', error)
  } finally {
    loading.value.pendingReviews = false
  }
}

// 窗口大小变化处理函数
const handleResize = () => {
  console.log('窗口大小变化，重绘图表')
  if (userGrowthChart) {
    userGrowthChart.resize()
  }
  if (repairTypeChart) {
    repairTypeChart.resize()
  }
}

// 监听时间范围变化，更新用户增长趋势图表
watch(timeRange, () => {
  console.log('时间范围变化:', timeRange.value)
  nextTick(() => {
    initUserGrowthChart()
  })
})

// 组件挂载后添加窗口大小变化监听
onMounted(() => {
  console.log('Dashboard 组件已挂载')
  
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
  
  // 加载数据
  loadDashboardOverview()
  loadLatestOrders()
  loadPendingReviews()
  
  // 使用nextTick确保DOM已完全渲染
  nextTick(() => {
    try {
      if (!userGrowthChartRef.value || !repairTypeChartRef.value) {
        console.error('图表容器未找到')
        return
      }
      
      console.log('开始初始化图表')
      initUserGrowthChart()
      initRepairTypeChart()
      console.log('图表初始化完成')
    } catch (error) {
      console.error('初始化图表失败:', error)
    }
  })
})

// 组件卸载时清理事件监听和图表实例
onUnmounted(() => {
  console.log('Dashboard 组件卸载')
  window.removeEventListener('resize', handleResize)
  
  if (userGrowthChart) {
    userGrowthChart.dispose()
    userGrowthChart = null
  }
  
  if (repairTypeChart) {
    repairTypeChart.dispose()
    repairTypeChart = null
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
    color: #262626;
  }

  // 数据卡片样式
  .data-card {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .data-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      
      .el-icon {
        font-size: 28px;
      }
    }
    
    .data-info {
      flex: 1;
      
      .data-title {
        font-size: 15px;
        color: #595959;
        margin-bottom: 6px;
        font-weight: 500;
      }
      
      .data-value {
        font-size: 26px;
        font-weight: bold;
        color: #262626;
        margin-bottom: 6px;
      }
      
      .data-compare {
        font-size: 13px;
        color: #8c8c8c;
        
        .up {
          color: #52c41a;
          margin-left: 5px;
          font-weight: 500;
        }
        
        .down {
          color: #ff4d4f;
          margin-left: 5px;
          font-weight: 500;
        }
      }
    }
  }
  
  // 图表行样式
  .chart-row {
    margin-bottom: 20px;
  }
  
  // 图表卡片样式
  .chart-card {
    margin-bottom: 20px;
    border-radius: 8px;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 20px;
      border-bottom: 1px solid #f0f0f0;
      font-weight: 500;
      color: #262626;
    }
    
    .chart {
      height: 350px;
      width: 100%;
      min-height: 200px;
      padding: 10px;
    }
  }
  
  // 活动卡片样式
  .activity-card {
    margin-bottom: 20px;
    border-radius: 8px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 20px;
      border-bottom: 1px solid #f0f0f0;
      font-weight: 500;
      color: #262626;
    }
    
    :deep(.el-table) {
      .el-table__header th {
        background-color: #fafafa;
        font-weight: 500;
        color: #262626;
      }
      
      .el-table__row:hover > td {
        background-color: #e6f7ff;
      }
    }
  }
}

// 响应式样式
@media screen and (max-width: 768px) {
  .dashboard-container {
    .chart {
      height: 250px !important;
    }
  }
}
</style> 