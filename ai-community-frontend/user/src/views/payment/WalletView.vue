<template>
  <div class="wallet-container">
    <van-nav-bar
      title="我的钱包"
      left-arrow
      @click-left="goBack"
      fixed
      class="custom-nav-bar"
    />

    <div class="content">
      <!-- 钱包余额卡片 -->
      <div class="wallet-card">
        <div class="wallet-header">
          <div class="balance-section">
            <div class="balance-label">账户余额（元）</div>
            <div class="balance-amount">{{ accountInfo.balance || '0.00' }}</div>
            <div class="balance-detail">
              <span>可用余额: ¥{{ accountInfo.availableBalance || '0.00' }}</span>
              <span v-if="accountInfo.frozenAmount > 0" class="frozen">冻结: ¥{{ accountInfo.frozenAmount || '0.00' }}</span>
            </div>
          </div>
        </div>

        <div class="wallet-actions">
          <div class="action-item" @click="handleRecharge">
            <van-icon name="plus" />
            <span>充值</span>
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-value">¥{{ accountInfo.totalRecharge || '0.00' }}</div>
          <div class="stat-label">累计充值</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">¥{{ accountInfo.totalConsumption || '0.00' }}</div>
          <div class="stat-label">累计消费</div>
        </div>
      </div>

      <!-- 筛选器 -->
      <div class="filter-section">
        <div class="section-title">账户流水</div>
        <div class="filter-buttons">
          <van-button
            v-for="type in transactionTypes"
            :key="type.value"
            size="small"
            :type="queryParams.transactionType === type.value ? 'primary' : 'default'"
            @click="handleTypeChange(type.value)"
          >
            {{ type.label }}
          </van-button>
        </div>
      </div>

      <!-- 流水列表 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
          :immediate-check="false"
        >
          <div class="transaction-list">
            <div
              class="transaction-item"
              v-for="item in transactionList"
              :key="item.id"
            >
              <div class="transaction-left">
                <div class="transaction-icon" :class="getIconClass(item.transactionType)">
                  <van-icon :name="getIconName(item.transactionType)" />
                </div>
                <div class="transaction-info">
                  <div class="transaction-type">{{ item.transactionTypeName }}</div>
                  <div class="transaction-desc">{{ item.description }}</div>
                  <div class="transaction-time">{{ formatTime(item.createdTime) }}</div>
                </div>
              </div>
              <div class="transaction-right">
                <div class="transaction-amount" :class="getAmountClass(item.transactionType)">
                  {{ getAmountPrefix(item.transactionType) }}¥{{ item.amount }}
                </div>
                <div class="transaction-balance">余额: ¥{{ item.balanceAfter }}</div>
              </div>
            </div>
          </div>

          <van-empty v-if="!loading && transactionList.length === 0" description="暂无流水记录" />
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getUserAccount, getAccountTransactions, type AccountTransaction } from '@/api/payment'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const accountInfo = reactive({
  balance: '0.00',
  availableBalance: '0.00',
  frozenAmount: 0,
  totalRecharge: '0.00',
  totalConsumption: '0.00'
})
const transactionList = ref<AccountTransaction[]>([])
const queryParams = reactive({
  transactionType: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

// 交易类型选项
const transactionTypes = [
  { label: '全部', value: undefined },
  { label: '充值', value: 1 },
  { label: '消费', value: 2 },
  { label: '退款', value: 3 }
]

// 事件处理
const goBack = () => {
  router.back()
}

const handleRecharge = () => {
  showDialog({
    title: '充值提示',
    message: '充值功能开发中，敬请期待'
  })
}

const handleTypeChange = (type: number | undefined) => {
  queryParams.transactionType = type
  queryParams.page = 1
  transactionList.value = []
  finished.value = false
  fetchTransactions()
}

// 获取账户信息
const fetchAccountInfo = async () => {
  try {
    const response = await getUserAccount()
    if (response?.code === 200 && response?.data) {
      const data = response.data
      accountInfo.balance = (data.balance || 0).toFixed(2)
      accountInfo.availableBalance = (data.availableBalance || 0).toFixed(2)
      accountInfo.frozenAmount = data.frozenAmount || 0
      accountInfo.totalRecharge = (data.totalRecharge || 0).toFixed(2)
      accountInfo.totalConsumption = (data.totalConsumption || 0).toFixed(2)
    }
  } catch (error) {
    console.error('获取账户信息失败:', error)
    showToast('获取账户信息失败')
  }
}

// 获取流水记录
const fetchTransactions = async () => {
  try {
    const response = await getAccountTransactions(queryParams)
    if (response?.code === 200 && response?.data) {
      const { records, current, pages } = response.data
      
      if (queryParams.page === 1) {
        transactionList.value = records
      } else {
        transactionList.value.push(...records)
      }
      
      loading.value = false
      refreshing.value = false
      
      // 判断是否还有更多数据
      if (current >= pages) {
        finished.value = true
      } else {
        queryParams.page++
      }
    } else {
      loading.value = false
      refreshing.value = false
      showToast(response?.msg || '获取流水记录失败')
    }
  } catch (error) {
    console.error('获取流水记录失败:', error)
    loading.value = false
    refreshing.value = false
    showToast('获取流水记录失败')
  }
}

// 下拉刷新
const onRefresh = () => {
  queryParams.page = 1
  finished.value = false
  fetchAccountInfo()
  fetchTransactions()
}

// 上拉加载
const onLoad = () => {
  if (!finished.value) {
    fetchTransactions()
  }
}

// 辅助函数
const getIconName = (type: number) => {
  const iconMap: Record<number, string> = {
    1: 'cash-on-deliver',
    2: 'shopping-cart-o',
    3: 'refund-o',
    4: 'lock',
    5: 'unlock'
  }
  return iconMap[type] || 'bill-o'
}

const getIconClass = (type: number) => {
  const classMap: Record<number, string> = {
    1: 'icon-recharge',
    2: 'icon-consume',
    3: 'icon-refund',
    4: 'icon-freeze',
    5: 'icon-unfreeze'
  }
  return classMap[type] || ''
}

const getAmountClass = (type: number) => {
  return type === 1 || type === 3 ? 'amount-income' : 'amount-expense'
}

const getAmountPrefix = (type: number) => {
  return type === 1 || type === 3 ? '+' : '-'
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 今天
  if (date.toDateString() === now.toDateString()) {
    return time.substring(11, 16)
  }
  
  // 昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天 ' + time.substring(11, 16)
  }
  
  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    return time.substring(5, 16)
  }
  
  // 其他
  return time.substring(0, 16)
}

// 页面加载
onMounted(async () => {
  await fetchAccountInfo()
  await fetchTransactions()
})
</script>

<style scoped lang="scss">
.wallet-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 46px;
  padding-bottom: 16px;
}

.content {
  padding: 12px;
}

.custom-nav-bar {
  background: white !important;
  border-bottom: 1px solid #f0f0f0;

  :deep(.van-nav-bar__title) {
    color: #333 !important;
    font-weight: 600;
  }

  :deep(.van-nav-bar__left .van-icon) {
    color: #333 !important;
  }
}

.wallet-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 12px;
  color: white;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.wallet-header {
  margin-bottom: 20px;
}

.balance-section {
  .balance-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .balance-amount {
    font-size: 36px;
    font-weight: bold;
    margin-bottom: 8px;
  }

  .balance-detail {
    display: flex;
    gap: 16px;
    font-size: 12px;
    opacity: 0.9;

    .frozen {
      color: #ffe58f;
    }
  }
}

.wallet-actions {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    transition: transform 0.2s;

    &:active {
      transform: scale(0.95);
    }

    .van-icon {
      font-size: 24px;
    }

    span {
      font-size: 12px;
    }
  }
}

.stats-section {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 12px;
      color: #999;
    }
  }

  .stat-divider {
    width: 1px;
    background: #f0f0f0;
  }
}

.filter-section {
  margin-bottom: 12px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }

  .filter-buttons {
    display: flex;
    gap: 8px;

    .van-button {
      flex: 1;
    }
  }
}

.transaction-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.transaction-left {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 12px;
}

.transaction-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  .van-icon {
    font-size: 20px;
    color: white;
  }

  &.icon-recharge {
    background: linear-gradient(135deg, #52c41a, #73d13d);
  }

  &.icon-consume {
    background: linear-gradient(135deg, #ff6b35, #ff8c69);
  }

  &.icon-refund {
    background: linear-gradient(135deg, #1890ff, #40a9ff);
  }

  &.icon-freeze {
    background: linear-gradient(135deg, #8c8c8c, #bfbfbf);
  }

  &.icon-unfreeze {
    background: linear-gradient(135deg, #faad14, #ffc53d);
  }
}

.transaction-info {
  flex: 1;

  .transaction-type {
    font-size: 15px;
    color: #333;
    margin-bottom: 4px;
    font-weight: 500;
  }

  .transaction-desc {
    font-size: 12px;
    color: #999;
    margin-bottom: 2px;
  }

  .transaction-time {
    font-size: 11px;
    color: #ccc;
  }
}

.transaction-right {
  text-align: right;

  .transaction-amount {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 4px;

    &.amount-income {
      color: #52c41a;
    }

    &.amount-expense {
      color: #ff6b35;
    }
  }

  .transaction-balance {
    font-size: 11px;
    color: #999;
  }
}
</style>

