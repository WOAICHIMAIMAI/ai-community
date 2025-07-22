<template>
  <div class="payment-detail-container">
    <van-nav-bar
      title="账单详情"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <template v-if="loading">
        <van-skeleton title :row="10" />
      </template>
      
      <template v-else-if="billDetail">
        <!-- 账单状态卡片 -->
        <div class="status-card">
          <div class="status-header">
            <div class="bill-icon" :class="getBillTypeClass(billDetail.type)">
              <van-icon :name="getBillIcon(billDetail.type)" />
            </div>
            <div class="status-info">
              <h2>{{ billDetail.title }}</h2>
              <div class="status-tag">
                <van-tag :type="getBillStatusType(billDetail.status)" size="large">
                  {{ getBillStatusText(billDetail.status) }}
                </van-tag>
              </div>
            </div>
          </div>
          
          <div class="amount-info">
            <div class="amount-label">应缴金额</div>
            <div class="amount-value">¥{{ billDetail.amount }}</div>
          </div>
        </div>

        <!-- 账单信息 -->
        <div class="bill-info-card">
          <div class="card-title">账单信息</div>
          <van-cell-group inset>
            <van-cell title="账单周期" :value="billDetail.period" />
            <van-cell title="到期日期" :value="formatDate(billDetail.dueDate)" />
            <van-cell title="账单生成时间" :value="formatDate(billDetail.createTime)" />
            <van-cell title="房屋地址" :value="billDetail.address" />
            <van-cell title="房屋面积" :value="`${billDetail.area}㎡`" />
          </van-cell-group>
        </div>

        <!-- 费用明细 -->
        <div class="detail-card" v-if="billDetail.details">
          <div class="card-title">费用明细</div>
          <van-cell-group inset>
            <van-cell
              v-for="detail in billDetail.details"
              :key="detail.name"
              :title="detail.name"
              :label="detail.description"
              :value="`¥${detail.amount}`"
            />
            <van-cell
              title="合计"
              :value="`¥${billDetail.amount}`"
              class="total-cell"
            />
          </van-cell-group>
        </div>

        <!-- 缴费记录 -->
        <div class="payment-record-card" v-if="billDetail.paymentRecord">
          <div class="card-title">缴费记录</div>
          <van-cell-group inset>
            <van-cell
              title="支付时间"
              :value="formatDate(billDetail.paymentRecord.payTime)"
            />
            <van-cell
              title="支付方式"
              :value="billDetail.paymentRecord.payMethod"
            />
            <van-cell
              title="交易单号"
              :value="billDetail.paymentRecord.transactionId"
            />
            <van-cell
              title="支付金额"
              :value="`¥${billDetail.paymentRecord.amount}`"
            />
          </van-cell-group>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons" v-if="billDetail.status === 'pending' || billDetail.status === 'overdue'">
          <van-button
            block
            type="primary"
            size="large"
            @click="payNow"
            :loading="paying"
          >
            立即缴费 ¥{{ billDetail.amount }}
          </van-button>
        </div>

        <!-- 已缴费状态 -->
        <div class="paid-status" v-else-if="billDetail.status === 'paid'">
          <van-icon name="success" color="#07c160" size="48" />
          <div class="paid-text">该账单已缴费</div>
          <van-button
            block
            type="default"
            size="large"
            @click="downloadReceipt"
          >
            下载缴费凭证
          </van-button>
        </div>
      </template>
      
      <template v-else>
        <div class="empty-data">
          <van-empty image="error" description="账单不存在" />
          <van-button type="primary" size="small" @click="goBack">返回列表</van-button>
        </div>
      </template>
    </div>

    <!-- 支付弹窗 -->
    <van-popup
      v-model:show="showPayPopup"
      round
      position="bottom"
      :style="{ height: '50%' }"
    >
      <div class="pay-popup">
        <div class="popup-header">
          <h3>选择支付方式</h3>
          <van-icon name="cross" @click="showPayPopup = false" />
        </div>
        
        <div class="popup-content">
          <div class="pay-amount">
            <span>支付金额：</span>
            <span class="amount">¥{{ billDetail?.amount }}</span>
          </div>
          
          <van-radio-group v-model="selectedPaymentMethod">
            <van-radio
              v-for="method in paymentMethods"
              :key="method.id"
              :name="method.id"
              class="payment-option"
            >
              <div class="method-info">
                <div class="method-icon" :class="method.type">
                  <van-icon :name="method.icon" />
                </div>
                <div class="method-details">
                  <div class="method-name">{{ method.name }}</div>
                  <div class="method-desc">{{ method.description }}</div>
                </div>
              </div>
            </van-radio>
          </van-radio-group>
        </div>
        
        <div class="popup-footer">
          <van-button
            block
            type="primary"
            size="large"
            @click="confirmPay"
            :loading="paying"
          >
            确认支付
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(true)
const billDetail = ref<any>(null)
const showPayPopup = ref(false)
const selectedPaymentMethod = ref('wechat')
const paying = ref(false)

// 模拟支付方式数据
const paymentMethods = ref([
  {
    id: 'wechat',
    name: '微信支付',
    description: '推荐使用',
    icon: 'wechat',
    type: 'wechat'
  },
  {
    id: 'alipay',
    name: '支付宝',
    description: '快速支付',
    icon: 'alipay',
    type: 'alipay'
  },
  {
    id: 'bank',
    name: '银行卡',
    description: '工商银行 ****1234',
    icon: 'credit-pay',
    type: 'bank'
  }
])

// 页面初始化
onMounted(() => {
  const billId = Number(route.params.id)
  if (billId) {
    loadBillDetail(billId)
  } else {
    showToast('账单ID无效')
    router.back()
  }
})

// 加载账单详情
const loadBillDetail = async (id: number) => {
  try {
    loading.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    const mockData: Record<number, any> = {
      1: {
        id: 1,
        title: '物业管理费',
        period: '2024年1月',
        amount: '380.00',
        type: 'property',
        status: 'pending',
        dueDate: '2024-01-31T23:59:59',
        createTime: '2024-01-01T00:00:00',
        address: '阳光小区1号楼1单元101室',
        area: 95,
        details: [
          { name: '基础物业费', description: '95㎡ × 3.5元/㎡', amount: '332.50' },
          { name: '公共维修基金', description: '按面积计算', amount: '28.50' },
          { name: '垃圾清运费', description: '固定费用', amount: '19.00' }
        ]
      },
      2: {
        id: 2,
        title: '停车费',
        period: '2024年1月',
        amount: '200.00',
        type: 'parking',
        status: 'pending',
        dueDate: '2024-01-31T23:59:59',
        createTime: '2024-01-01T00:00:00',
        address: '阳光小区地下车库A区A001',
        area: 0,
        details: [
          { name: '固定车位费', description: '月租费用', amount: '200.00' }
        ]
      }
    }
    
    billDetail.value = mockData[id]
    
    if (!billDetail.value) {
      showToast('账单不存在')
      router.back()
    }
  } catch (error) {
    console.error('加载账单详情失败:', error)
    showToast('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 返回列表
const goBack = () => {
  router.replace('/payment')
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

// 获取账单状态类型
const getBillStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    overdue: 'danger',
    paid: 'success'
  }
  return typeMap[status] || 'default'
}

// 获取账单状态文本
const getBillStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待缴费',
    overdue: '已逾期',
    paid: '已缴费'
  }
  return textMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

// 立即缴费
const payNow = () => {
  showPayPopup.value = true
}

// 确认支付
const confirmPay = async () => {
  paying.value = true
  
  try {
    // 模拟支付过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    showSuccessToast('支付成功')
    showPayPopup.value = false
    
    // 更新账单状态
    if (billDetail.value) {
      billDetail.value.status = 'paid'
      billDetail.value.paymentRecord = {
        payTime: new Date().toISOString(),
        payMethod: getPaymentMethodName(selectedPaymentMethod.value),
        transactionId: generateTransactionId(),
        amount: billDetail.value.amount
      }
    }
  } catch (error) {
    showToast('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

// 获取支付方式名称
const getPaymentMethodName = (methodId: string) => {
  const method = paymentMethods.value.find(m => m.id === methodId)
  return method?.name || '未知支付方式'
}

// 生成交易单号
const generateTransactionId = () => {
  return 'TXN' + Date.now() + Math.random().toString(36).substr(2, 6).toUpperCase()
}

// 下载缴费凭证
const downloadReceipt = () => {
  showToast('缴费凭证下载功能开发中')
}
</script>

<style scoped lang="scss">
.payment-detail-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 16px;
}

.status-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  
  .status-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .bill-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      font-size: 24px;
      
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
    }
    
    .status-info {
      flex: 1;
      
      h2 {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 600;
      }
    }
  }
  
  .amount-info {
    text-align: center;
    padding: 16px;
    background-color: #f7f8fa;
    border-radius: 12px;
    
    .amount-label {
      font-size: 14px;
      color: #646566;
      margin-bottom: 8px;
    }
    
    .amount-value {
      font-size: 32px;
      font-weight: bold;
      color: #ff6b35;
    }
  }
}

.bill-info-card,
.detail-card,
.payment-record-card {
  margin-bottom: 16px;
  
  .card-title {
    font-size: 16px;
    font-weight: 500;
    color: #323233;
    margin-bottom: 12px;
  }
}

.total-cell {
  :deep(.van-cell__title) {
    font-weight: 600;
  }
  
  :deep(.van-cell__value) {
    font-weight: 600;
    color: #ff6b35;
  }
}

.action-buttons {
  margin-top: 24px;
}

.paid-status {
  text-align: center;
  padding: 40px 20px;
  
  .paid-text {
    font-size: 16px;
    color: #07c160;
    margin: 16px 0 24px 0;
  }
}

.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  
  .van-button {
    margin-top: 16px;
  }
}

// 支付弹窗样式
.pay-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #ebedf0;
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
    }
    
    .van-icon {
      font-size: 20px;
      color: #969799;
    }
  }
  
  .popup-content {
    flex: 1;
    padding: 20px;
  }
  
  .pay-amount {
    text-align: center;
    margin-bottom: 24px;
    font-size: 16px;
    
    .amount {
      color: #ff6b35;
      font-weight: bold;
      font-size: 20px;
    }
  }
  
  .payment-option {
    margin-bottom: 16px;
    
    .method-info {
      display: flex;
      align-items: center;
      width: 100%;
      
      .method-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        font-size: 20px;
        
        &.wechat {
          background-color: #e8f5e8;
          color: #07c160;
        }
        
        &.alipay {
          background-color: #e6f7ff;
          color: #1677ff;
        }
        
        &.bank {
          background-color: #f6ffed;
          color: #52c41a;
        }
      }
      
      .method-details {
        flex: 1;
        
        .method-name {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 2px;
        }
        
        .method-desc {
          font-size: 12px;
          color: #969799;
        }
      }
    }
  }
  
  .popup-footer {
    padding: 20px;
    border-top: 1px solid #ebedf0;
  }
}

:deep(.van-cell-group) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-skeleton) {
  padding: 20px;
}
</style>
