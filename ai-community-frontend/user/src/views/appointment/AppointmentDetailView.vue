<template>
  <div class="appointment-detail">
    <van-nav-bar title="预约详情" left-arrow @click-left="goBack" fixed />
    
    <div class="content">
      <van-loading v-if="loading" class="loading-container" />
      
      <div v-else-if="appointment" class="detail-container">
        <!-- 预约状态 -->
        <div class="status-section">
          <div class="status-card">
            <div class="status-icon" :class="getStatusClass(appointment.status)">
              <van-icon :name="getStatusIcon(appointment.status)" size="24" />
            </div>
            <div class="status-info">
              <h3>{{ getStatusText(appointment.status) }}</h3>
              <p>{{ getStatusDescription(appointment.status) }}</p>
            </div>
          </div>
        </div>

        <!-- 服务信息 -->
        <div class="service-section">
          <van-cell-group inset>
            <van-cell title="服务类型" :value="appointment.serviceName" />
            <van-cell title="预约时间" :value="formatDateTime(appointment.appointmentTime)" />
            <van-cell title="服务地址" :value="appointment.address" />
            <van-cell title="联系人" :value="appointment.contactName" />
            <van-cell title="联系电话" :value="appointment.contactPhone" />
            <van-cell title="订单编号" :value="appointment.orderNo" />
          </van-cell-group>
        </div>

        <!-- 特殊要求 -->
        <div class="requirements-section" v-if="appointment.requirements">
          <van-cell-group inset>
            <van-cell title="特殊要求" is-link @click="showRequirements = true">
              <template #value>
                <span class="requirements-preview">{{ truncateText(appointment.requirements, 20) }}</span>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

        <!-- 服务人员信息 -->
        <div class="worker-section" v-if="appointment.worker">
          <van-cell-group inset>
            <van-cell title="服务人员" center>
              <template #icon>
                <van-image
                  :src="appointment.worker.avatar"
                  round
                  width="40"
                  height="40"
                  class="worker-avatar"
                />
              </template>
              <template #title>
                <div class="worker-info">
                  <span class="worker-name">{{ appointment.worker.name }}</span>
                  <div class="worker-rating">
                    <van-rate v-model="workerRating" readonly size="12" />
                    <span class="rating-text">{{ workerRating }}.0</span>
                  </div>
                </div>
              </template>
              <template #right-icon>
                <van-button 
                  type="primary" 
                  size="small" 
                  @click="callWorker"
                  v-if="appointment.status === 'confirmed'"
                >
                  联系
                </van-button>
              </template>
            </van-cell>
          </van-cell-group>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <div class="action-buttons">
            <van-button 
              v-if="appointment.status === 'pending'"
              type="danger" 
              size="large" 
              @click="cancelAppointment"
              :loading="cancelling"
            >
              取消预约
            </van-button>
            
            <van-button 
              v-if="appointment.status === 'confirmed'"
              type="warning" 
              size="large" 
              @click="rescheduleAppointment"
            >
              改期
            </van-button>
            
            <van-button 
              v-if="appointment.status === 'completed' && !appointment.rated"
              type="primary" 
              size="large" 
              @click="rateService"
            >
              评价服务
            </van-button>
            
            <van-button 
              v-if="appointment.status === 'completed'"
              type="default" 
              size="large" 
              @click="bookAgain"
            >
              再次预约
            </van-button>
          </div>
        </div>
      </div>

      <van-empty v-else description="预约信息不存在" />
    </div>

    <!-- 特殊要求弹窗 -->
    <van-popup v-model:show="showRequirements" position="bottom" round>
      <div class="requirements-popup">
        <div class="popup-header">
          <h3>特殊要求</h3>
          <van-icon name="cross" @click="showRequirements = false" />
        </div>
        <div class="popup-content">
          <p>{{ appointment?.requirements }}</p>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const cancelling = ref(false)
const showRequirements = ref(false)
const workerRating = ref(4.8)

// 预约详情数据
const appointment = ref<any>(null)

// 模拟数据
const mockAppointment = {
  id: 1,
  orderNo: 'APT202401250001',
  serviceName: '家政保洁',
  serviceType: 'cleaning',
  appointmentTime: '2024-01-25T14:00:00',
  address: '阳光小区1号楼1单元101室',
  contactName: '张先生',
  contactPhone: '138****8888',
  requirements: '需要深度清洁厨房和卫生间，特别注意油烟机和马桶的清洁，家里有小孩请使用环保清洁剂',
  status: 'confirmed',
  worker: {
    name: '李阿姨',
    phone: '139****9999',
    avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  },
  rated: false
}

const goBack = () => {
  router.go(-1)
}

const getStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    pending: 'status-pending',
    confirmed: 'status-confirmed',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return classMap[status] || 'status-default'
}

const getStatusIcon = (status: string) => {
  const iconMap: Record<string, string> = {
    pending: 'clock-o',
    confirmed: 'checked',
    completed: 'success',
    cancelled: 'cross'
  }
  return iconMap[status] || 'info-o'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || '未知状态'
}

const getStatusDescription = (status: string) => {
  const descMap: Record<string, string> = {
    pending: '我们正在为您安排服务人员',
    confirmed: '服务人员已确认，请准时等待',
    completed: '服务已完成，感谢您的使用',
    cancelled: '预约已取消'
  }
  return descMap[status] || ''
}

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const cancelAppointment = async () => {
  try {
    await showConfirmDialog({
      title: '确认取消',
      message: '确定要取消这个预约吗？取消后不可恢复。'
    })
    
    cancelling.value = true
    // 模拟取消请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    appointment.value.status = 'cancelled'
    showToast('预约已取消')
  } catch {
    // 用户取消操作
  } finally {
    cancelling.value = false
  }
}

const rescheduleAppointment = () => {
  showToast('改期功能开发中')
}

const rateService = () => {
  showToast('评价功能开发中')
}

const bookAgain = () => {
  router.push(`/appointment/booking/${appointment.value.serviceType}`)
}

const callWorker = () => {
  showToast(`拨打电话：${appointment.value.worker.phone}`)
}

onMounted(async () => {
  try {
    // 模拟加载数据
    await new Promise(resolve => setTimeout(resolve, 500))
    appointment.value = mockAppointment
  } catch (error) {
    console.error('加载预约详情失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.appointment-detail {
  min-height: 100vh;
  padding-top: 46px;
  background-color: #f7f8fa;
}

.content {
  padding: 16px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.detail-container {
  .status-section {
    margin-bottom: 16px;
    
    .status-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      
      .status-icon {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        
        &.status-pending {
          background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%);
        }
        
        &.status-confirmed {
          background: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
        }
        
        &.status-completed {
          background: linear-gradient(135deg, #66bb6a 0%, #4caf50 100%);
        }
        
        &.status-cancelled {
          background: linear-gradient(135deg, #ef5350 0%, #f44336 100%);
        }
      }
      
      .status-info {
        flex: 1;
        
        h3 {
          font-size: 18px;
          font-weight: bold;
          color: #2c3e50;
          margin: 0 0 4px 0;
        }
        
        p {
          font-size: 14px;
          color: #7f8c8d;
          margin: 0;
          line-height: 1.4;
        }
      }
    }
  }
  
  .service-section,
  .requirements-section,
  .worker-section {
    margin-bottom: 16px;
  }
  
  .requirements-preview {
    color: #7f8c8d;
    font-size: 14px;
  }
  
  .worker-avatar {
    margin-right: 12px;
  }
  
  .worker-info {
    .worker-name {
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
      display: block;
      margin-bottom: 4px;
    }
    
    .worker-rating {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .rating-text {
        font-size: 12px;
        color: #f39c12;
        font-weight: 500;
      }
    }
  }
  
  .action-section {
    margin-top: 24px;
    
    .action-buttons {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
  }
}

.requirements-popup {
  padding: 20px;
  
  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      font-size: 18px;
      font-weight: bold;
      color: #2c3e50;
      margin: 0;
    }
  }
  
  .popup-content {
    p {
      font-size: 14px;
      color: #2c3e50;
      line-height: 1.6;
      margin: 0;
    }
  }
}
</style>
