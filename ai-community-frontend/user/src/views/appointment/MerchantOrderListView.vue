<template>
  <div class="merchant-orders-container">
    <van-nav-bar
      title="我的服务订单"
      left-arrow
      @click-left="goBack"
      fixed
    />

    <div class="content">
      <!-- 统计卡片 -->
      <div class="stats-section">
        <div class="stats-card">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.total }}</div>
            <div class="stat-label">总订单</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value pending">{{ statistics.pending }}</div>
            <div class="stat-label">待确认</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value processing">{{ statistics.processing }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value completed">{{ statistics.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </div>

      <!-- 筛选标签 -->
      <div class="filter-section">
        <van-tabs v-model:active="activeTab" @change="onTabChange" sticky offset-top="46px">
          <van-tab title="全部" name="all"></van-tab>
          <van-tab title="待确认" name="0"></van-tab>
          <van-tab title="已确认" name="1"></van-tab>
          <van-tab title="服务中" name="2"></van-tab>
          <van-tab title="已完成" name="3"></van-tab>
          <van-tab title="已取消" name="4"></van-tab>
        </van-tabs>
      </div>

      <!-- 订单列表 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div class="orders-list">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadOrders"
          >
            <div
              v-for="order in orderList"
              :key="order.id"
              class="order-card"
              @click="goToOrderDetail(order.id)"
            >
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-no">订单号：{{ order.orderNo }}</div>
                <van-tag :type="getStatusTagType(order.status)">
                  {{ getStatusText(order.status) }}
                </van-tag>
              </div>

              <!-- 服务信息 -->
              <div class="order-content">
                <div class="service-icon" :style="{ background: getServiceGradient(order.serviceType) }">
                  <van-icon :name="getServiceIcon(order.serviceType)" size="24" />
                </div>
                <div class="order-info">
                  <div class="service-name">{{ order.serviceName }}</div>
                  <div class="appointment-time">
                    <van-icon name="clock-o" size="14" />
                    {{ formatDateTime(order.appointmentTime) }}
                  </div>
                  <div class="order-address">
                    <van-icon name="location-o" size="14" />
                    {{ order.address }}
                  </div>
                </div>
              </div>

              <!-- 客户信息 -->
              <div class="customer-info">
                <div class="info-item">
                  <span class="label">客户：</span>
                  <span class="value">{{ order.contactName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">电话：</span>
                  <span class="value">{{ order.contactPhone }}</span>
                </div>
              </div>

              <!-- 特殊要求 -->
              <div class="requirements" v-if="order.requirements">
                <van-icon name="warning-o" size="14" />
                <span>{{ order.requirements }}</span>
              </div>

              <!-- 订单底部 -->
              <div class="order-footer">
                <div class="price-info">
                  <span class="price-label">预估金额：</span>
                  <span class="price-value">¥{{ order.estimatedPrice }}</span>
                </div>
                <div class="order-actions">
                  <van-button 
                    v-if="order.status === 0" 
                    type="primary" 
                    size="small"
                    @click.stop="confirmOrder(order)"
                  >
                    确认接单
                  </van-button>
                  <van-button 
                    v-if="order.status === 1" 
                    type="success" 
                    size="small"
                    @click.stop="startService(order)"
                  >
                    开始服务
                  </van-button>
                  <van-button 
                    v-if="order.status === 2" 
                    type="success" 
                    size="small"
                    @click.stop="finishService(order)"
                  >
                    完成服务
                  </van-button>
                  <van-button 
                    v-if="order.status === 0" 
                    plain 
                    size="small"
                    @click.stop="cancelOrder(order)"
                  >
                    拒绝订单
                  </van-button>
                </div>
              </div>
            </div>
          </van-list>

          <!-- 空状态 -->
          <van-empty v-if="!loading && orderList.length === 0" description="暂无订单" />
        </div>
      </van-pull-refresh>
    </div>

    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog, showSuccessToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import { 
  getMerchantOrderPage, 
  getMerchantOrderStats,
  confirmMerchantOrder,
  startMerchantService,
  finishMerchantService,
  rejectMerchantOrder,
  type MerchantOrderVO,
  type MerchantOrderStats
} from '@/api/merchantOrder'

const router = useRouter()

// 响应式数据
const activeTab = ref('all')
const refreshing = ref(false)
const loading = ref(false)
const finished = ref(false)
const orderList = ref<MerchantOrderVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const statistics = ref<MerchantOrderStats>({
  total: 0,
  pending: 0,
  processing: 0,
  completed: 0
})

// 服务类型配置
const serviceConfig: Record<string, any> = {
  cleaning: {
    icon: 'brush-o',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  repair: {
    icon: 'setting-o',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  appliance: {
    icon: 'tv-o',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  moving: {
    icon: 'logistics',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  gardening: {
    icon: 'flower-o',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  pest: {
    icon: 'delete-o',
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  }
}

// 页面初始化
onMounted(async () => {
  console.log('页面初始化')
  loadStatistics()
  // 手动触发第一次加载
  loadOrders()
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 将字符串状态转为数字
const getStatusValue = (statusKey: string): number | undefined => {
  if (statusKey === 'all') {
    return undefined
  }
  const statusMap: Record<string, number> = {
    '0': 0,  // 待确认
    '1': 1,  // 已确认
    '2': 2,  // 服务中
    '3': 3,  // 已完成
    '4': 4   // 已取消
  }
  return statusMap[statusKey]
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await getMerchantOrderStats()
    if (res.code === 200 && res.data) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载订单列表
const loadOrders = async () => {
  if (loading.value) {
    console.log('已在加载中，跳过')
    return
  }
  
  loading.value = true
  console.log('开始加载订单列表')
  
  try {
    console.log('加载订单列表，当前页:', currentPage.value, '状态:', activeTab.value)
    
    const res = await getMerchantOrderPage({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: getStatusValue(activeTab.value)
    })
    
    console.log('订单列表响应:', res)
    console.log('res.code:', res.code)
    console.log('res.data:', res.data)
    
    if (res.code === 200 && res.data) {
      console.log('响应成功，开始处理数据')
      const newOrders = res.data.records || []
      console.log('新订单数据:', newOrders)
      console.log('新订单数量:', newOrders.length)
      
      if (currentPage.value === 1) {
        orderList.value = newOrders
        console.log('第一页，直接赋值')
      } else {
        orderList.value = [...orderList.value, ...newOrders]
        console.log('追加数据')
      }
      
      console.log('赋值后 orderList.value:', orderList.value)
      console.log('orderList.value.length:', orderList.value.length)
      
      finished.value = orderList.value.length >= res.data.total
      console.log('finished:', finished.value)
      
      if (!finished.value) {
        currentPage.value++
      }
    } else {
      console.error('响应失败:', res)
      showToast(res.msg || '加载失败')
    }
  } catch (error: any) {
    console.error('加载订单列表失败:', error)
    showToast(error.message || '加载失败，请重试')
  } finally {
    console.log('加载完成，设置 loading = false')
    loading.value = false
    refreshing.value = false
  }
}

// 切换标签
const onTabChange = () => {
  currentPage.value = 1
  orderList.value = []
  finished.value = false
  loadOrders()
}

// 下拉刷新
const onRefresh = () => {
  currentPage.value = 1
  orderList.value = []
  finished.value = false
  loadStatistics()
  loadOrders()
}

// 跳转订单详情
const goToOrderDetail = (orderId: number) => {
  router.push(`/appointment/merchant/order/${orderId}`)
}

// 确认接单
const confirmOrder = async (order: MerchantOrderVO) => {
  showDialog({
    title: '确认接单',
    message: `确定接受订单 ${order.orderNo} 吗？`,
  }).then(async () => {
    try {
      const res = await confirmMerchantOrder(order.id)
      if (res.code === 200) {
        showSuccessToast('接单成功')
        onRefresh()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      console.error('确认订单失败:', error)
      showToast(error.message || '操作失败，请重试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 开始服务
const startService = async (order: MerchantOrderVO) => {
  showDialog({
    title: '开始服务',
    message: `确定开始为客户 ${order.contactName} 提供服务吗？`,
  }).then(async () => {
    try {
      const res = await startMerchantService(order.id)
      if (res.code === 200) {
        showSuccessToast('服务已开始')
        onRefresh()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      console.error('开始服务失败:', error)
      showToast(error.message || '操作失败，请重试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 完成服务
const finishService = async (order: MerchantOrderVO) => {
  showDialog({
    title: '完成服务',
    message: '确定已完成服务吗？完成后订单将等待客户确认。',
  }).then(async () => {
    try {
      const res = await finishMerchantService(order.id)
      if (res.code === 200) {
        showSuccessToast('服务已完成')
        onRefresh()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      console.error('完成服务失败:', error)
      showToast(error.message || '操作失败，请重试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 拒绝订单
const cancelOrder = async (order: MerchantOrderVO) => {
  showDialog({
    title: '拒绝订单',
    message: '确定拒绝该订单吗？请谨慎操作。',
    showCancelButton: true,
  }).then(async () => {
    try {
      const res = await rejectMerchantOrder(order.id, '商家拒绝接单')
      if (res.code === 200) {
        showSuccessToast('已拒绝订单')
        onRefresh()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      console.error('拒绝订单失败:', error)
      showToast(error.message || '操作失败，请重试')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 获取服务图标
const getServiceIcon = (type: string) => {
  return serviceConfig[type]?.icon || 'service-o'
}

// 获取服务渐变色
const getServiceGradient = (type: string) => {
  return serviceConfig[type]?.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '服务中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number): any => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'default',
    4: 'danger'
  }
  return typeMap[status] || 'default'
}

// 格式化日期时间
const formatDateTime = (dateStr: string | number) => {
  if (!dateStr) return ''
  
  // 将时间戳或字符串转换为 Date 对象
  const date = new Date(dateStr)
  
  // 格式化为 YYYY-MM-DD HH:mm
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  const formattedDate = `${year}-${month}-${day} ${hours}:${minutes}`
  
  // 计算是否是今天或明天
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((targetDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return `今天 ${hours}:${minutes}`
  } else if (diffDays === 1) {
    return `明天 ${hours}:${minutes}`
  } else if (diffDays === -1) {
    return `昨天 ${hours}:${minutes}`
  } else {
    return `${month}-${day} ${hours}:${minutes}`
  }
}
</script>

<style scoped lang="scss">
.merchant-orders-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
  padding-bottom: 60px;
}

.content {
  padding-bottom: 20px;
}

// 统计卡片
.stats-section {
  padding: 16px;
  background: #fff;
  margin-bottom: 12px;
}

.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.stat-item {
  flex: 1;
  text-align: center;
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 8px;
    
    &.pending {
      color: #ffd21e;
    }
    
    &.processing {
      color: #4fc08d;
    }
    
    &.completed {
      color: #e8f4ff;
    }
  }
  
  .stat-label {
    font-size: 13px;
    opacity: 0.9;
  }
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

// 筛选标签
.filter-section {
  :deep(.van-tabs__wrap) {
    background: #fff;
  }
}

// 订单列表
.orders-list {
  padding: 12px 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  &:active {
    opacity: 0.8;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  
  .order-no {
    font-size: 13px;
    color: #969799;
  }
}

.order-content {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.service-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  
  .service-name {
    font-size: 16px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 6px;
  }
  
  .appointment-time,
  .order-address {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #969799;
    margin-bottom: 4px;
    
    :deep(.van-icon) {
      color: #969799;
    }
  }
}

.customer-info {
  display: flex;
  gap: 20px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  margin-bottom: 12px;
  
  .info-item {
    font-size: 14px;
    
    .label {
      color: #969799;
    }
    
    .value {
      color: #323233;
      font-weight: 500;
    }
  }
}

.requirements {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #ff976a;
  padding: 8px 12px;
  background: #fff7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  
  :deep(.van-icon) {
    margin-top: 2px;
    flex-shrink: 0;
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.price-info {
  .price-label {
    font-size: 13px;
    color: #969799;
  }
  
  .price-value {
    font-size: 18px;
    font-weight: bold;
    color: #ee0a24;
    margin-left: 4px;
  }
}

.order-actions {
  display: flex;
  gap: 8px;
  
  :deep(.van-button) {
    min-width: 80px;
  }
}
</style>

