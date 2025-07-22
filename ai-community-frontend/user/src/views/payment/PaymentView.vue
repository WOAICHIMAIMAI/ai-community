<template>
  <div class="payment-container">
    <van-nav-bar
      title="费用缴纳"
      left-arrow
      @click-left="goBack"
      fixed
      class="custom-nav-bar"
    >
      <template #right>
        <van-button size="small" @click="$router.push('/test/buttons')" style="margin-right: 8px;">按钮</van-button>
        <van-button size="small" @click="$router.push('/test/api')">API</van-button>
      </template>
    />

    <div class="content">
      <!-- 账户余额卡片 -->
      <div class="balance-card">
        <div class="balance-content">
          <div class="balance-left">
            <div class="balance-label">账户余额</div>
            <div class="balance-amount">¥{{ accountBalance }}</div>
            <div class="balance-tips">
              <van-icon name="info-o" />
              <span>余额不足时将自动使用绑定的支付方式</span>
            </div>
          </div>
          <div class="balance-right">
            <van-button
              size="small"
              type="primary"
              round
              @click="handleRecharge"
              class="recharge-btn"
            >
              充值
            </van-button>
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
            @click="handleBillDetail(bill.id)"
          >
            <div class="bill-left">
              <div class="bill-icon">
                <van-icon :name="getBillIcon(bill.type)" />
              </div>
              <div class="bill-info">
                <div class="bill-title">{{ bill.title }}</div>
                <div class="bill-period">{{ bill.period }}</div>
              </div>
            </div>
            <div class="bill-right">
              <div class="bill-amount">¥{{ bill.amount }}</div>
              <div class="bill-status">
                <van-tag color="#ff6b35" text-color="white" size="small">
                  {{ bill.statusText }}
                </van-tag>
              </div>
            </div>
          </div>
        </div>

        <van-empty v-else-if="!isLoading" description="暂无待缴费用" />

        <van-loading v-if="isLoading" type="spinner" color="#1989fa">
          加载中...
        </van-loading>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="section-title">快捷操作</div>
        <div class="action-grid">
          <div class="action-item" @click="handlePayAll">
            <div class="action-icon">
              <van-icon name="gold-coin-o" />
            </div>
            <div class="action-text">一键缴费</div>
          </div>
          <div class="action-item" @click="handleHistory">
            <div class="action-icon">
              <van-icon name="records" />
            </div>
            <div class="action-text">缴费历史</div>
          </div>
          <div class="action-item" @click="handleReminder">
            <div class="action-icon">
              <van-icon name="bell-o" />
            </div>
            <div class="action-text">缴费提醒</div>
          </div>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="payment-methods">
        <div class="section-title">支付方式</div>
        <div class="method-list" v-if="methodList.length > 0">
          <div
            class="method-item"
            v-for="method in methodList"
            :key="method.id"
            :class="{ active: selectedMethod === method.id }"
            @click="selectedMethod = method.id"
          >
            <div class="method-left">
              <div class="method-icon">
                <van-icon :name="method.icon" />
              </div>
              <div class="method-info">
                <div class="method-name">{{ method.name }}</div>
                <div class="method-desc">{{ method.description }}</div>
                <div class="method-balance" v-if="method.type === 4 && method.balance">
                  余额：¥{{ typeof method.balance === 'number' ? method.balance.toFixed(2) : method.balance }}
                </div>
              </div>
            </div>
            <div class="method-right">
              <van-radio :name="method.id" v-model="selectedMethod" />
            </div>
          </div>
        </div>
        <van-loading v-if="isLoadingMethods" type="spinner" color="#52c41a">
          加载中...
        </van-loading>
      </div>

      <!-- 最近缴费记录 -->
      <div class="recent-payments">
        <div class="section-header">
          <div class="section-title">最近缴费</div>
          <MoreButton text="查看全部" @click="handleHistory" />
        </div>

        <div class="bill-list" v-if="recentList.length > 0">
          <div
            class="bill-item"
            v-for="payment in recentList"
            :key="payment.id"
          >
            <div class="bill-left">
              <div class="payment-success-icon">
                <van-icon name="success" />
              </div>
              <div class="bill-info">
                <div class="bill-title">{{ payment.title }}</div>
                <div class="bill-period">{{ payment.time }}</div>
              </div>
            </div>
            <div class="bill-right">
              <div class="bill-amount success">¥{{ payment.amount }}</div>
            </div>
          </div>
        </div>

        <van-empty v-else description="暂无缴费记录" />
      </div>
    </div>

    <!-- 一键缴费弹窗 -->
    <van-popup
      v-model:show="showPayPopup"
      position="bottom"
      :style="{ height: '60%' }"
    >
      <div class="pay-popup">
        <div class="popup-header">
          <h3>一键缴费</h3>
          <van-icon name="cross" @click="showPayPopup = false" />
        </div>
        
        <div class="popup-content">
          <div class="total-info">
            <div class="total-label">本次缴费总额</div>
            <div class="total-amount">¥{{ totalAmount || '0.00' }}</div>
          </div>

          <div class="bill-list" v-if="billList && billList.length > 0">
            <div class="bill-item" v-for="bill in billList" :key="bill?.id || Math.random()">
              <span>{{ bill?.title || '未知账单' }}</span>
              <span>¥{{ bill?.amount || '0.00' }}</span>
            </div>
          </div>
          
          <div class="payment-method-select">
            <div class="method-label">支付方式</div>
            <van-radio-group v-model="selectedMethod">
              <van-radio
                v-for="method in methodList"
                :key="method.id"
                :name="method.id"
              >
                <div class="method-option">
                  <van-icon :name="method.icon" />
                  <div class="method-info">
                    <span class="method-name">{{ method.name }}</span>
                    <span class="method-desc">{{ method.desc }}</span>
                  </div>
                </div>
              </van-radio>
            </van-radio-group>
          </div>
        </div>
        
        <div class="popup-footer">
          <van-button
            block
            type="primary"
            size="large"
            @click="handleConfirmPay"
            :loading="isPaying"
            :disabled="!billList || billList.length === 0"
          >
            确认支付 ¥{{ totalAmount || '0.00' }}
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import MoreButton from '@/components/MoreButton.vue'
import {
  getUserAccount,
  getPendingBills,
  getPaymentRecordPage,
  getPaymentMethods,
  createPaymentOrder,
  confirmPayment
} from '@/api/payment'

const router = useRouter()

// 响应式数据
const isLoading = ref(true)
const isLoadingMethods = ref(true)
const isPaying = ref(false)
const showPayPopup = ref(false)
const selectedMethod = ref(1)

// 账户余额
const accountBalance = ref('0.00')

// 待缴费账单
const billList = ref([])

// 最近缴费记录
const recentList = ref([])

// 支付方式
const methodList = ref([])

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

// 获取账单图标
const getBillIcon = (type: number) => {
  const iconMap: Record<number, string> = {
    1: 'home-o',
    2: 'drop',
    3: 'fire',
    4: 'phone-o'
  }
  return iconMap[type] || 'bill-o'
}

// 格式化支付时间
const formatPaymentTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 事件处理函数
const goBack = () => {
  router.back()
}

const handleRecharge = () => {
  // 跳转到充值页面或显示充值弹窗
  showToast('充值功能开发中')
}

const handleBillDetail = (billId: number) => {
  router.push(`/payment/detail/${billId}`)
}

const handlePayAll = () => {
  if (billList.value.length === 0) {
    showToast('暂无待缴费用')
    return
  }
  showPayPopup.value = true
}

const handleHistory = () => {
  router.push('/payment/history')
}

const handleReminder = () => {
  showToast('缴费提醒功能开发中')
}

const handleConfirmPay = async () => {
  if (!billList.value || billList.value.length === 0) {
    showToast('没有待缴费账单')
    return
  }

  isPaying.value = true

  try {
    // 创建缴费订单
    const billIds = billList.value.map(bill => bill?.id).filter(id => id)
    const paymentAmount = parseFloat(totalAmount.value) || 0

    if (billIds.length === 0 || paymentAmount <= 0) {
      showToast('账单数据异常，请刷新重试')
      return
    }

    const createResponse = await createPaymentOrder({
      billIds,
      paymentMethod: selectedMethod.value,
      paymentAmount,
      remark: '一键缴费'
    })

    if (createResponse.code === 200) {
      const record = createResponse.data

      // 模拟第三方支付过程
      await new Promise(resolve => setTimeout(resolve, 2000))

      // 确认支付（模拟第三方支付成功）
      const transactionId = `TXN${Date.now()}`
      const confirmResponse = await confirmPayment(record.id, transactionId)

      if (confirmResponse.code === 200) {
        showSuccessToast('支付成功')
        showPayPopup.value = false

        // 刷新数据
        await Promise.all([
          fetchAccountInfo(),
          fetchPendingBills(),
          fetchRecentPayments()
        ])
      } else {
        showToast(confirmResponse.msg || '支付确认失败')
      }
    } else {
      showToast(createResponse.msg || '创建订单失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    showToast('支付失败，请重试')
  } finally {
    isPaying.value = false
  }
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
    // 设置默认值
    accountBalance.value = '0.00'
  }
}

// 获取待缴费账单
const fetchPendingBills = async () => {
  try {
    const response = await getPendingBills()
    if (response?.code === 200 && response?.data) {
      // 确保数据是数组且每个元素都有必要的属性
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

// 获取最近缴费记录
const fetchRecentPayments = async () => {
  try {
    const response = await getPaymentRecordPage({ page: 1, pageSize: 5 })
    if (response.code === 200) {
      // 确保数据结构完整
      const records = response.data?.records || []
      recentList.value = records.map(record => ({
        id: record?.id || 0,
        title: record?.billTitle || '未知账单',
        time: formatPaymentTime(record?.paymentTime),
        amount: record?.paymentAmount ? record.paymentAmount.toFixed(2) : '0.00'
      }))
    } else {
      recentList.value = []
      showToast(response.msg || '获取缴费记录失败')
    }
  } catch (error) {
    console.error('获取缴费记录失败:', error)
    recentList.value = []
    showToast('获取缴费记录失败')
  }
}

// 获取支付方式列表
const fetchPaymentMethods = async () => {
  try {
    isLoadingMethods.value = true
    const response = await getPaymentMethods()
    if (response.code === 200) {
      methodList.value = response.data || []
      // 设置默认选中第一个可用的支付方式
      if (methodList.value.length > 0) {
        const defaultMethod = methodList.value.find(m => m.isDefault) || methodList.value[0]
        selectedMethod.value = defaultMethod.id
      }
    } else {
      showToast(response.msg || '获取支付方式失败')
    }
  } catch (error) {
    console.error('获取支付方式失败:', error)
    showToast('获取支付方式失败')
  } finally {
    isLoadingMethods.value = false
  }
}

// 页面初始化
onMounted(async () => {
  try {
    // 确保数组初始化
    billList.value = []
    recentList.value = []
    methodList.value = []

    // 并行加载数据
    await Promise.all([
      fetchAccountInfo(),
      fetchPendingBills(),
      fetchRecentPayments(),
      fetchPaymentMethods()
    ])
  } catch (error) {
    console.error('页面初始化失败:', error)
    // 确保在错误情况下数据结构完整
    billList.value = []
    recentList.value = []
    methodList.value = []
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

  .balance-tips {
    display: flex;
    align-items: center;
    font-size: 12px;
    opacity: 0.8;

    .van-icon {
      margin-right: 4px;
      font-size: 12px;
    }
  }

  .recharge-btn {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: white;
    font-size: 12px;
    padding: 6px 16px;

    &:active {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.pending-section, .quick-actions, .payment-methods, .recent-payments {
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
    font-size: 16px;
  }
}

.total-amount {
  font-size: 14px;
  color: #ff6b35;
  font-weight: 500;
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
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background-color 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: #f8f8f8;
  }

  .bill-left {
    display: flex;
    align-items: center;
    flex: 1;
  }

  .bill-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f0f9ff;
    margin-right: 12px;

    .van-icon {
      font-size: 16px;
      color: #1890ff;
    }
  }

  .bill-info {
    flex: 1;

    .bill-title {
      font-size: 15px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .bill-period {
      font-size: 13px;
      color: #999;
    }
  }

  .bill-right {
    text-align: right;

    .bill-amount {
      font-size: 16px;
      font-weight: 600;
      color: #ff6b35;
      margin-bottom: 4px;

      &.success {
        color: #52c41a;
      }
    }

    .bill-status {
      display: flex;
      justify-content: flex-end;
    }
  }
}

.action-grid {
  display: flex;
  gap: 12px;
}

.action-item {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
    background-color: #f8f8f8;
  }

  .action-icon {
    margin-bottom: 8px;

    .van-icon {
      font-size: 20px;
      color: #52c41a;
    }
  }

  .action-text {
    font-size: 13px;
    color: #333;
    font-weight: 500;
  }
}

// 支付方式样式
.payment-methods {
  .method-list {
    background: white;
    border-radius: 8px;
    overflow: hidden;
  }

  .method-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background-color 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: #f8f8f8;
    }

    &.active {
      background-color: #f6ffed;
      border-color: #52c41a;
    }

    .method-left {
      display: flex;
      align-items: center;
      flex: 1;
    }

    .method-icon {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f0f9ff;
      margin-right: 12px;

      .van-icon {
        font-size: 18px;
        color: #52c41a;
      }
    }

    .method-info {
      flex: 1;

      .method-name {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
      }

      .method-desc {
        font-size: 13px;
        color: #999;
        margin-bottom: 2px;
      }

      .method-balance {
        font-size: 12px;
        color: #52c41a;
        font-weight: 500;
      }
    }

    .method-right {
      margin-left: 12px;
    }
  }
}

.payment-success-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f6ffed;
  margin-right: 12px;

  .van-icon {
    color: #52c41a;
    font-size: 16px;
  }
}

.pay-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: white;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .van-icon {
      font-size: 18px;
      color: #999;
      padding: 4px;
    }
  }

  .popup-content {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }

  .total-info {
    text-align: center;
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 8px;

    .total-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .total-amount {
      font-size: 28px;
      font-weight: bold;
      color: #ff6b35;
    }
  }

  .bill-list {
    margin-bottom: 20px;
    background: white;
    border-radius: 8px;
    padding: 16px;

    .bill-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f5f5f5;
      font-size: 14px;

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      &:first-child {
        padding-top: 0;
      }
    }
  }

  .payment-method-select {
    background: white;
    border-radius: 8px;
    padding: 16px;

    .method-label {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #333;
    }

    .method-option {
      display: flex;
      align-items: center;
      padding: 12px 0;

      .van-icon {
        font-size: 20px;
        margin-right: 12px;
        color: #52c41a;
      }

      .method-info {
        flex: 1;

        .method-name {
          display: block;
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 2px;
          color: #333;
        }

        .method-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }

  .popup-footer {
    padding: 16px 20px;
    background: white;
    border-top: 1px solid #f0f0f0;
  }
}

:deep(.van-radio-group) {
  .van-radio {
    width: 100%;
    margin: 0;

    .van-radio__label {
      width: 100%;
      margin-left: 0;
    }
  }
}

:deep(.van-button--primary) {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;

  &:active {
    opacity: 0.8;
  }
}

:deep(.van-button--large) {
  height: 44px;
  font-size: 16px;
}

:deep(.van-empty) {
  padding: 40px 20px;
}

:deep(.van-loading) {
  padding: 40px 20px;
}

// 最近缴费记录样式
.recent-payments {
  .bill-item {
    .bill-icon {
      background-color: #f6ffed;

      .van-icon {
        color: #52c41a;
      }
    }

    .bill-amount {
      color: #52c41a;
    }
  }
}
</style>
