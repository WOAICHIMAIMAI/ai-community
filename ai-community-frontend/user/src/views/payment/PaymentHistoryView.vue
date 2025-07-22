<template>
  <div class="payment-history-container">
    <van-nav-bar
      title="缴费历史"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 筛选条件 -->
      <div class="filter-section">
        <van-dropdown-menu>
          <van-dropdown-item
            v-model="selectedYear"
            :options="yearOptions"
            @change="onYearChange"
          />
          <van-dropdown-item
            v-model="selectedType"
            :options="typeOptions"
            @change="onTypeChange"
          />
          <van-dropdown-item
            v-model="selectedStatus"
            :options="statusOptions"
            @change="onStatusChange"
          />
        </van-dropdown-menu>
      </div>

      <!-- 统计信息 -->
      <div class="statistics-card">
        <div class="stat-item">
          <div class="stat-value">¥{{ totalAmount }}</div>
          <div class="stat-label">总缴费金额</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ totalCount }}</div>
          <div class="stat-label">缴费次数</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">¥{{ avgAmount }}</div>
          <div class="stat-label">平均金额</div>
        </div>
      </div>

      <!-- 历史记录列表 -->
      <div class="history-list">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <!-- 按月分组显示 -->
          <div
            v-for="(group, month) in groupedPayments"
            :key="month"
            class="month-group"
          >
            <div class="month-header">
              <span class="month-title">{{ month }}</span>
              <span class="month-amount">¥{{ getMonthTotal(group) }}</span>
            </div>
            
            <van-cell-group inset>
              <van-cell
                v-for="payment in group"
                :key="payment.id"
                :title="payment.title"
                :label="formatDate(payment.payTime)"
                :value="`¥${payment.amount}`"
                is-link
                @click="goToDetail(payment.billId)"
              >
                <template #icon>
                  <div class="payment-icon" :class="getBillTypeClass(payment.type)">
                    <van-icon :name="getBillIcon(payment.type)" />
                  </div>
                </template>
                <template #right-icon>
                  <div class="payment-info">
                    <div class="payment-method">{{ payment.payMethod }}</div>
                    <van-icon name="arrow" />
                  </div>
                </template>
              </van-cell>
            </van-cell-group>
          </div>
          
          <van-empty v-if="!loading && Object.keys(groupedPayments).length === 0" description="暂无缴费记录" />
        </van-list>
      </div>
    </div>

    <!-- 导出功能 -->
    <div class="export-section">
      <van-button
        block
        type="default"
        @click="exportHistory"
        :loading="exporting"
      >
        <van-icon name="down" />
        导出缴费记录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const finished = ref(false)
const exporting = ref(false)
const currentPage = ref(1)
const pageSize = 20

// 筛选条件
const selectedYear = ref('2024')
const selectedType = ref('all')
const selectedStatus = ref('all')

// 筛选选项
const yearOptions = ref([
  { text: '2024年', value: '2024' },
  { text: '2023年', value: '2023' },
  { text: '2022年', value: '2022' },
  { text: '全部', value: 'all' }
])

const typeOptions = ref([
  { text: '全部类型', value: 'all' },
  { text: '物业费', value: 'property' },
  { text: '停车费', value: 'parking' },
  { text: '水费', value: 'water' },
  { text: '电费', value: 'electric' },
  { text: '燃气费', value: 'gas' }
])

const statusOptions = ref([
  { text: '全部状态', value: 'all' },
  { text: '已缴费', value: 'paid' },
  { text: '已退款', value: 'refunded' }
])

// 模拟数据 - 缴费历史
const paymentHistory = ref([
  {
    id: 1,
    billId: 1,
    title: '物业管理费',
    amount: '380.00',
    type: 'property',
    payTime: '2024-01-15T10:30:00',
    payMethod: '微信支付',
    status: 'paid'
  },
  {
    id: 2,
    billId: 2,
    title: '停车费',
    amount: '200.00',
    type: 'parking',
    payTime: '2024-01-15T10:28:00',
    payMethod: '支付宝',
    status: 'paid'
  },
  {
    id: 3,
    billId: 3,
    title: '水费',
    amount: '45.80',
    type: 'water',
    payTime: '2023-12-20T14:20:00',
    payMethod: '微信支付',
    status: 'paid'
  },
  {
    id: 4,
    billId: 4,
    title: '电费',
    amount: '128.50',
    type: 'electric',
    payTime: '2023-12-18T16:45:00',
    payMethod: '银行卡',
    status: 'paid'
  },
  {
    id: 5,
    billId: 5,
    title: '物业管理费',
    amount: '380.00',
    type: 'property',
    payTime: '2023-12-15T09:15:00',
    payMethod: '微信支付',
    status: 'paid'
  },
  {
    id: 6,
    billId: 6,
    title: '停车费',
    amount: '200.00',
    type: 'parking',
    payTime: '2023-12-15T09:12:00',
    payMethod: '支付宝',
    status: 'paid'
  }
])

// 计算属性
const filteredPayments = computed(() => {
  return paymentHistory.value.filter(payment => {
    const payDate = new Date(payment.payTime)
    const payYear = payDate.getFullYear().toString()
    
    // 年份筛选
    if (selectedYear.value !== 'all' && payYear !== selectedYear.value) {
      return false
    }
    
    // 类型筛选
    if (selectedType.value !== 'all' && payment.type !== selectedType.value) {
      return false
    }
    
    // 状态筛选
    if (selectedStatus.value !== 'all' && payment.status !== selectedStatus.value) {
      return false
    }
    
    return true
  })
})

const groupedPayments = computed(() => {
  const groups: Record<string, any[]> = {}
  
  filteredPayments.value.forEach(payment => {
    const date = new Date(payment.payTime)
    const monthKey = `${date.getFullYear()}年${date.getMonth() + 1}月`
    
    if (!groups[monthKey]) {
      groups[monthKey] = []
    }
    groups[monthKey].push(payment)
  })
  
  // 按月份排序（最新的在前）
  const sortedGroups: Record<string, any[]> = {}
  Object.keys(groups)
    .sort((a, b) => {
      const dateA = new Date(a.replace('年', '-').replace('月', '-01'))
      const dateB = new Date(b.replace('年', '-').replace('月', '-01'))
      return dateB.getTime() - dateA.getTime()
    })
    .forEach(key => {
      sortedGroups[key] = groups[key].sort((a, b) => 
        new Date(b.payTime).getTime() - new Date(a.payTime).getTime()
      )
    })
  
  return sortedGroups
})

const totalAmount = computed(() => {
  return filteredPayments.value.reduce((total, payment) => {
    return total + parseFloat(payment.amount)
  }, 0).toFixed(2)
})

const totalCount = computed(() => {
  return filteredPayments.value.length
})

const avgAmount = computed(() => {
  if (filteredPayments.value.length === 0) return '0.00'
  return (parseFloat(totalAmount.value) / filteredPayments.value.length).toFixed(2)
})

// 页面初始化
onMounted(() => {
  // 这里可以加载数据
  finished.value = true // 模拟数据已全部加载
})

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 筛选条件变化
const onYearChange = () => {
  // 重新加载数据
}

const onTypeChange = () => {
  // 重新加载数据
}

const onStatusChange = () => {
  // 重新加载数据
}

// 加载更多
const onLoad = () => {
  // 模拟加载更多数据
  setTimeout(() => {
    loading.value = false
    finished.value = true
  }, 1000)
}

// 获取月份总金额
const getMonthTotal = (payments: any[]) => {
  return payments.reduce((total, payment) => {
    return total + parseFloat(payment.amount)
  }, 0).toFixed(2)
}

// 获取账单图标
const getBillIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    property: 'home-o',
    parking: 'logistics',
    water: 'drop',
    electric: 'lightning',
    gas: 'fire-o'
  }
  return iconMap[type] || 'gold-coin-o'
}

// 获取账单类型样式
const getBillTypeClass = (type: string) => {
  return `bill-type-${type}`
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 跳转到详情
const goToDetail = (billId: number) => {
  router.push(`/payment/detail/${billId}`)
}

// 导出历史记录
const exportHistory = async () => {
  exporting.value = true
  
  try {
    // 模拟导出过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    showSuccessToast('导出成功')
  } catch (error) {
    showToast('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped lang="scss">
.payment-history-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 0 16px 16px;
}

.filter-section {
  margin-bottom: 16px;
  
  :deep(.van-dropdown-menu) {
    border-radius: 8px;
    overflow: hidden;
  }
}

.statistics-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  
  .stat-item {
    flex: 1;
    text-align: center;
    
    .stat-value {
      font-size: 20px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 4px;
    }
    
    .stat-label {
      font-size: 12px;
      color: #969799;
    }
  }
  
  .stat-divider {
    width: 1px;
    height: 32px;
    background-color: #ebedf0;
    margin: 0 16px;
  }
}

.history-list {
  .month-group {
    margin-bottom: 16px;
    
    .month-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 4px 8px;
      
      .month-title {
        font-size: 16px;
        font-weight: 500;
        color: #323233;
      }
      
      .month-amount {
        font-size: 14px;
        color: #4fc08d;
        font-weight: 500;
      }
    }
  }
}

.payment-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  
  &.bill-type-property {
    background-color: #e8f5e8;
    color: #4fc08d;
  }
  
  &.bill-type-parking {
    background-color: #e6f7ff;
    color: #1890ff;
  }
  
  &.bill-type-water {
    background-color: #f0f9ff;
    color: #0ea5e9;
  }
  
  &.bill-type-electric {
    background-color: #fef3c7;
    color: #f59e0b;
  }
  
  &.bill-type-gas {
    background-color: #fee2e2;
    color: #ef4444;
  }
}

.payment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .payment-method {
    font-size: 12px;
    color: #969799;
  }
}

.export-section {
  padding: 16px;
  background-color: #fff;
  border-top: 1px solid #ebedf0;
  
  .van-button {
    border: 1px solid #ebedf0;
    color: #646566;
    
    .van-icon {
      margin-right: 6px;
    }
  }
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-dropdown-menu__bar) {
  background-color: #fff;
  box-shadow: none;
  border-bottom: 1px solid #ebedf0;
}
</style>
