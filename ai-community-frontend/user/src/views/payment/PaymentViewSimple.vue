<template>
  <div class="payment-container">
    <van-nav-bar
      title="费用缴纳"
      left-arrow
      @click-left="goBack"
      fixed
      class="custom-nav-bar"
    />

    <div class="content">
      <!-- 账户余额卡片 -->
      <div class="balance-card">
        <div class="balance-content">
          <div class="balance-left">
            <div class="balance-label">账户余额</div>
            <div class="balance-amount">¥{{ accountBalance }}</div>
          </div>
        </div>
      </div>

      <!-- 待缴费用 -->
      <div class="pending-section">
        <div class="section-header">
          <div class="section-title">
            <van-icon name="warning-o" color="#ff6b35" />
            <span>待缴费用</span>
          </div>
          <div class="total-amount">
            共 {{ billCount || 0 }} 项，¥{{ totalAmount || '0.00' }}
          </div>
        </div>

        <div class="bill-list" v-if="billList.length > 0">
          <div
            class="bill-item"
            v-for="bill in billList"
            :key="bill.id"
          >
            <div class="bill-left">
              <div class="bill-info">
                <div class="bill-title">{{ bill.title }}</div>
                <div class="bill-period">{{ bill.period }}</div>
              </div>
            </div>
            <div class="bill-right">
              <div class="bill-amount">¥{{ bill.amount }}</div>
            </div>
          </div>
        </div>

        <van-empty v-else-if="!isLoading" description="暂无待缴费用" />
        <van-loading v-if="isLoading" type="spinner" color="#1989fa">
          加载中...
        </van-loading>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import { getUserAccount, getPendingBills } from '@/api/payment'

const router = useRouter()

// 响应式数据
const isLoading = ref(true)
const accountBalance = ref('0.00')
const billList = ref([])

// 计算属性
const billCount = computed(() => billList.value?.length || 0)
const totalAmount = computed(() => {
  if (!billList.value || billList.value.length === 0) {
    return '0.00'
  }
  return billList.value.reduce((sum, bill) => {
    const amount = bill?.amount ? parseFloat(bill.amount) : 0
    return sum + (isNaN(amount) ? 0 : amount)
  }, 0).toFixed(2)
})

// 事件处理函数
const goBack = () => {
  router.back()
}

// 获取账户信息
const fetchAccountInfo = async () => {
  try {
    const response = await getUserAccount()
    if (response?.code === 200 && response?.data) {
      accountBalance.value = (response.data.balance || 0).toFixed(2)
    } else {
      showToast(response?.msg || '获取账户信息失败')
    }
  } catch (error) {
    console.error('获取账户信息失败:', error)
    showToast('获取账户信息失败')
    accountBalance.value = '0.00'
  }
}

// 获取待缴费账单
const fetchPendingBills = async () => {
  try {
    const response = await getPendingBills()
    if (response?.code === 200 && response?.data) {
      const bills = Array.isArray(response.data) ? response.data : []
      billList.value = bills.map(bill => ({
        id: bill?.id || 0,
        title: bill?.billTitle || '未知账单',
        period: bill?.billingPeriod || '',
        amount: bill?.amount ? bill.amount.toFixed(2) : '0.00',
        type: bill?.billType || 1,
        statusText: bill?.statusName || '待缴费'
      }))
    } else {
      billList.value = []
      showToast(response?.msg || '获取账单失败')
    }
  } catch (error) {
    console.error('获取账单失败:', error)
    billList.value = []
    showToast('获取账单失败')
  }
}

// 页面加载时获取数据
onMounted(async () => {
  try {
    await Promise.all([
      fetchAccountInfo(),
      fetchPendingBills()
    ])
  } catch (error) {
    console.error('页面初始化失败:', error)
    billList.value = []
  } finally {
    isLoading.value = false
  }
})
</script>

<style scoped lang="scss">
.payment-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 46px;
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

.balance-card {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  color: white;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);

  .balance-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .balance-left {
    flex: 1;
  }

  .balance-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .balance-amount {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 12px;
  }
}

.pending-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px 12px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;

  .van-icon {
    margin-right: 6px;
  }
}

.total-amount {
  font-size: 14px;
  color: #666;
}

.bill-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.bill-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.bill-info {
  flex: 1;
}

.bill-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.bill-period {
  font-size: 12px;
  color: #999;
}

.bill-right {
  text-align: right;
}

.bill-amount {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b35;
}
</style>
