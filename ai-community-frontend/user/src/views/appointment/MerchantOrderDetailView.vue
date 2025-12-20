<template>
  <div class="merchant-order-detail">
    <van-nav-bar
      title="订单详情"
      left-arrow
      @click-left="goBack"
      fixed
    />

    <div class="content">
      <van-loading v-if="loading" class="loading-wrapper" />

      <div v-else class="detail-container">
        <!-- 订单状态卡片 -->
        <div class="status-card">
          <div class="status-icon" :class="getStatusClass(order.status)">
            <van-icon :name="getStatusIcon(order.status)" size="40" />
          </div>
          <div class="status-text">
            <h2>{{ getStatusText(order.status) }}</h2>
            <p>{{ getStatusDescription(order.status) }}</p>
          </div>
        </div>

        <!-- 服务信息 -->
        <div class="info-section">
          <div class="section-title">服务信息</div>
          <van-cell-group inset>
            <van-cell title="服务类型" :value="order.serviceName" />
            <van-cell title="预约时间" :value="formatDateTime(order.appointmentTime)" />
            <van-cell title="订单编号" :value="order.orderNo" />
          </van-cell-group>
        </div>

        <!-- 客户信息 -->
        <div class="info-section">
          <div class="section-title">客户信息</div>
          <van-cell-group inset>
            <van-cell title="客户姓名" :value="order.contactName" />
            <van-cell title="联系电话" :value="order.contactPhone">
              <template #right-icon>
                <van-button type="primary" size="small" @click="callCustomer">拨打电话</van-button>
              </template>
            </van-cell>
            <van-cell title="服务地址" :value="order.address" />
          </van-cell-group>
        </div>

        <!-- 特殊要求 -->
        <div class="info-section" v-if="order.requirements">
          <div class="section-title">特殊要求</div>
          <div class="requirements-box">
            {{ order.requirements }}
          </div>
        </div>

        <!-- 费用信息 -->
        <div class="info-section">
          <div class="section-title">费用信息</div>
          <van-cell-group inset>
            <van-cell title="预估金额" :value="'¥' + order.estimatedPrice" />
            <van-cell v-if="order.actualPrice" title="实际金额" :value="'¥' + order.actualPrice" />
          </van-cell-group>
        </div>

        <!-- 订单时间线 -->
        <div class="info-section">
          <div class="section-title">订单进度</div>
          <van-steps direction="vertical" :active="getTimelineActive(order.status)">
            <van-step v-if="order.createTime">
              <h3>订单创建</h3>
              <p>{{ formatDateTime(order.createTime) }}</p>
            </van-step>
            <van-step v-if="order.confirmTime">
              <h3>已确认接单</h3>
              <p>{{ formatDateTime(order.confirmTime) }}</p>
            </van-step>
            <van-step v-if="order.startTime">
              <h3>开始服务</h3>
              <p>{{ formatDateTime(order.startTime) }}</p>
            </van-step>
            <van-step v-if="order.finishTime">
              <h3>服务完成</h3>
              <p>{{ formatDateTime(order.finishTime) }}</p>
            </van-step>
            <van-step v-if="order.cancelTime">
              <h3>订单取消</h3>
              <p>{{ formatDateTime(order.cancelTime) }}</p>
              <p v-if="order.cancelReason" style="color: #ee0a24;">取消原因：{{ order.cancelReason }}</p>
            </van-step>
          </van-steps>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <van-button 
            v-if="order.status === 0" 
            type="primary" 
            block
            @click="confirmOrder"
          >
            确认接单
          </van-button>
          <van-button 
            v-if="order.status === 1" 
            type="success" 
            block
            @click="startService"
          >
            开始服务
          </van-button>
          <van-button 
            v-if="order.status === 2" 
            type="success" 
            block
            @click="finishService"
          >
            完成服务
          </van-button>
          <van-button 
            v-if="order.status === 0" 
            type="danger"
            plain 
            block
            @click="cancelOrder"
            style="margin-top: 12px;"
          >
            拒绝订单
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showDialog, showSuccessToast } from 'vant'
import {
  getMerchantOrderDetail,
  confirmMerchantOrder,
  startMerchantService,
  finishMerchantService,
  rejectMerchantOrder,
  type MerchantOrderVO
} from '@/api/merchantOrder'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const order = ref<MerchantOrderVO>({} as MerchantOrderVO)

onMounted(() => {
  loadOrderDetail()
})

const goBack = () => {
  router.back()
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id as string
    const res = await getMerchantOrderDetail(Number(orderId))
    if (res.code === 200 && res.data) {
      order.value = res.data
    } else {
      showToast(res.msg || '加载失败')
      router.back()
    }
  } catch (error: any) {
    console.error('加载订单详情失败:', error)
    showToast(error.message || '加载失败，请重试')
    router.back()
  } finally {
    loading.value = false
  }
}

const callCustomer = () => {
  window.location.href = `tel:${order.value.contactPhone}`
}

const confirmOrder = () => {
  showDialog({
    title: '确认接单',
    message: '确定接受该订单吗？',
  }).then(async () => {
    try {
      const res = await confirmMerchantOrder(order.value.id)
      if (res.code === 200) {
        showSuccessToast('接单成功')
        await loadOrderDetail()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      showToast(error.message || '操作失败')
    }
  })
}

const startService = () => {
  showDialog({
    title: '开始服务',
    message: '确定开始服务吗？',
  }).then(async () => {
    try {
      const res = await startMerchantService(order.value.id)
      if (res.code === 200) {
        showSuccessToast('服务已开始')
        await loadOrderDetail()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      showToast(error.message || '操作失败')
    }
  })
}

const finishService = () => {
  showDialog({
    title: '完成服务',
    message: '确定已完成服务吗？',
  }).then(async () => {
    try {
      const res = await finishMerchantService(order.value.id)
      if (res.code === 200) {
        showSuccessToast('服务已完成')
        await loadOrderDetail()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      showToast(error.message || '操作失败')
    }
  })
}

const cancelOrder = () => {
  showDialog({
    title: '拒绝订单',
    message: '确定拒绝该订单吗？',
    showCancelButton: true,
  }).then(async () => {
    try {
      const res = await rejectMerchantOrder(order.value.id, '商家拒绝接单')
      if (res.code === 200) {
        showSuccessToast('已拒绝订单')
        await loadOrderDetail()
      } else {
        showToast(res.msg || '操作失败')
      }
    } catch (error: any) {
      showToast(error.message || '操作失败')
    }
  })
}

const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'completed',
    4: 'danger'
  }
  return classMap[status] || 'default'
}

const getStatusIcon = (status: number) => {
  const iconMap: Record<number, string> = {
    0: 'clock-o',
    1: 'passed',
    2: 'fire-o',
    3: 'checked',
    4: 'cross'
  }
  return iconMap[status] || 'question-o'
}

const getStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '服务中',
    3: '已完成',
    4: '已取消'
  }
  return textMap[status] || '未知状态'
}

const getStatusDescription = (status: number) => {
  const descMap: Record<number, string> = {
    0: '客户已下单，请尽快确认',
    1: '请按时前往提供服务',
    2: '服务进行中，请保持联系',
    3: '服务已完成，感谢您的付出',
    4: '订单已取消'
  }
  return descMap[status] || ''
}

const getTimelineActive = (status: number) => {
  if (status === 4) return -1 // 取消状态
  return status
}

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
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<style scoped lang="scss">
.merchant-order-detail {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding: 16px 16px 80px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60vh;
}

.status-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.status-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  
  &.warning {
    background: linear-gradient(135deg, #ff976a 0%, #ff6b35 100%);
  }
  
  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &.success {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.completed {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
  
  &.danger {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
}

.status-text {
  flex: 1;
  
  h2 {
    font-size: 20px;
    font-weight: bold;
    margin: 0 0 8px 0;
    color: #323233;
  }
  
  p {
    font-size: 14px;
    color: #969799;
    margin: 0;
  }
}

.info-section {
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid #1989fa;
}

.requirements-box {
  background: white;
  border-radius: 12px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #646566;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: white;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.van-steps) {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  
  .van-step__title {
    font-size: 15px;
    font-weight: 600;
  }
}

:deep(.van-cell-group) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>

