<template>
  <div class="appointment-list-container">
    <van-nav-bar
      title="我的预约"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 状态筛选 -->
      <van-tabs v-model:active="activeStatus" @change="onStatusChange" sticky>
        <van-tab title="全部" name="all"></van-tab>
        <van-tab title="待确认" name="pending"></van-tab>
        <van-tab title="已确认" name="confirmed"></van-tab>
        <van-tab title="服务中" name="inProgress"></van-tab>
        <van-tab title="已完成" name="completed"></van-tab>
        <van-tab title="已取消" name="cancelled"></van-tab>
      </van-tabs>

      <!-- 预约列表 -->
      <div class="appointment-list">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="appointment in appointmentList"
            :key="appointment.id"
            class="appointment-card"
          >
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="service-info">
                <div class="service-icon" :class="appointment.serviceType">
                  <van-icon :name="getServiceIcon(appointment.serviceType)" />
                </div>
                <div class="service-details">
                  <h4>{{ appointment.serviceName }}</h4>
                  <p>预约单号：{{ appointment.orderNo }}</p>
                </div>
              </div>
              <van-tag :type="getStatusTagType(appointment.status)" size="large">
                {{ getStatusText(appointment.status) }}
              </van-tag>
            </div>

            <!-- 服务信息 -->
            <div class="service-content">
              <div class="info-row">
                <van-icon name="clock-o" />
                <span>{{ formatDateTime(appointment.appointmentTime) }}</span>
              </div>
              <div class="info-row">
                <van-icon name="location-o" />
                <span>{{ appointment.address }}</span>
              </div>
              <div class="info-row">
                <van-icon name="contact" />
                <span>{{ appointment.contactName }} {{ appointment.contactPhone }}</span>
              </div>
              <div class="info-row" v-if="appointment.requirements">
                <van-icon name="notes-o" />
                <span>{{ appointment.requirements }}</span>
              </div>
            </div>

            <!-- 服务人员信息 -->
            <div class="worker-info" v-if="appointment.worker">
              <div class="worker-avatar">
                <van-image
                  round
                  width="32"
                  height="32"
                  :src="appointment.worker.avatar"
                  fit="cover"
                />
              </div>
              <div class="worker-details">
                <div class="worker-name">{{ appointment.worker.name }}</div>
                <div class="worker-phone">{{ appointment.worker.phone }}</div>
              </div>
              <van-button
                size="small"
                type="primary"
                @click="callWorker(appointment.worker.phone)"
              >
                联系
              </van-button>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <template v-if="appointment.status === 'pending'">
                <van-button
                  size="small"
                  @click="cancelAppointment(appointment.id)"
                >
                  取消预约
                </van-button>
                <van-button
                  size="small"
                  type="primary"
                  @click="modifyAppointment(appointment.id)"
                >
                  修改预约
                </van-button>
              </template>
              
              <template v-else-if="appointment.status === 'confirmed'">
                <van-button
                  size="small"
                  @click="cancelAppointment(appointment.id)"
                >
                  取消预约
                </van-button>
                <van-button
                  size="small"
                  type="primary"
                  @click="viewDetail(appointment.id)"
                >
                  查看详情
                </van-button>
              </template>
              
              <template v-else-if="appointment.status === 'completed'">
                <van-button
                  size="small"
                  @click="rateService(appointment.id)"
                  v-if="!appointment.rated"
                >
                  评价服务
                </van-button>
                <van-button
                  size="small"
                  type="primary"
                  @click="bookAgain(appointment)"
                >
                  再次预约
                </van-button>
              </template>
              
              <template v-else>
                <van-button
                  size="small"
                  type="primary"
                  @click="viewDetail(appointment.id)"
                >
                  查看详情
                </van-button>
              </template>
            </div>
          </div>
          
          <van-empty v-if="!loading && appointmentList.length === 0" description="暂无预约记录" />
        </van-list>
      </div>
    </div>

    <!-- 取消预约弹窗 -->
    <van-popup
      v-model:show="showCancelPopup"
      round
      position="bottom"
      :style="{ height: '40%' }"
    >
      <div class="cancel-popup">
        <div class="popup-header">
          <h3>取消预约</h3>
          <van-icon name="cross" @click="showCancelPopup = false" />
        </div>
        
        <div class="popup-content">
          <div class="cancel-reason">
            <div class="reason-title">请选择取消原因</div>
            <van-radio-group v-model="cancelReason">
              <van-radio name="time">时间冲突</van-radio>
              <van-radio name="price">价格问题</van-radio>
              <van-radio name="service">服务不满意</van-radio>
              <van-radio name="other">其他原因</van-radio>
            </van-radio-group>
          </div>
          
          <van-field
            v-if="cancelReason === 'other'"
            v-model="cancelNote"
            type="textarea"
            placeholder="请说明取消原因"
            rows="3"
            maxlength="100"
            show-word-limit
          />
        </div>
        
        <div class="popup-footer">
          <van-button
            block
            type="danger"
            @click="confirmCancel"
            :loading="cancelling"
          >
            确认取消
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
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'

const router = useRouter()

// 响应式数据
const activeStatus = ref('all')
const loading = ref(false)
const finished = ref(false)
const currentPage = ref(1)
const pageSize = 10

const showCancelPopup = ref(false)
const cancelReason = ref('')
const cancelNote = ref('')
const cancelling = ref(false)
const currentCancelId = ref(0)

// 模拟数据 - 预约列表
const appointmentList = ref([
  {
    id: 1,
    orderNo: 'APT202401250001',
    serviceName: '家政保洁',
    serviceType: 'cleaning',
    appointmentTime: '2024-01-25T14:00:00',
    address: '阳光小区1号楼1单元101室',
    contactName: '张先生',
    contactPhone: '138****8888',
    requirements: '需要深度清洁厨房和卫生间',
    status: 'confirmed',
    worker: {
      name: '李阿姨',
      phone: '139****9999',
      avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
    },
    rated: false
  },
  {
    id: 2,
    orderNo: 'APT202401230002',
    serviceName: '水龙头维修',
    serviceType: 'repair',
    appointmentTime: '2024-01-23T10:00:00',
    address: '阳光小区1号楼1单元101室',
    contactName: '张先生',
    contactPhone: '138****8888',
    requirements: '厨房水龙头漏水',
    status: 'completed',
    worker: {
      name: '王师傅',
      phone: '137****7777',
      avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
    },
    rated: true
  },
  {
    id: 3,
    orderNo: 'APT202401220003',
    serviceName: '搬家服务',
    serviceType: 'moving',
    appointmentTime: '2024-01-22T09:00:00',
    address: '阳光小区1号楼1单元101室',
    contactName: '张先生',
    contactPhone: '138****8888',
    requirements: '搬到新小区，物品较多',
    status: 'pending',
    worker: null,
    rated: false
  }
])

// 计算属性
const filteredAppointments = computed(() => {
  if (activeStatus.value === 'all') {
    return appointmentList.value
  }
  return appointmentList.value.filter(item => item.status === activeStatus.value)
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

// 状态切换
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

// 获取服务图标
const getServiceIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    cleaning: 'brush-o',
    repair: 'setting-o',
    moving: 'logistics',
    appliance: 'tv-o',
    gardening: 'flower-o',
    pest: 'delete-o'
  }
  return iconMap[type] || 'service-o'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    confirmed: 'primary',
    inProgress: 'success',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    inProgress: '服务中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 联系服务人员
const callWorker = (phone: string) => {
  showConfirmDialog({
    title: '联系服务人员',
    message: `确定要拨打 ${phone} 吗？`
  }).then(() => {
    // 这里可以调用拨号功能
    showToast('正在拨号...')
  })
}

// 取消预约
const cancelAppointment = (id: number) => {
  currentCancelId.value = id
  showCancelPopup.value = true
}

// 确认取消
const confirmCancel = async () => {
  if (!cancelReason.value) {
    showToast('请选择取消原因')
    return
  }
  
  cancelling.value = true
  
  try {
    // 模拟取消过程
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 更新状态
    const appointment = appointmentList.value.find(item => item.id === currentCancelId.value)
    if (appointment) {
      appointment.status = 'cancelled'
    }
    
    showSuccessToast('预约已取消')
    showCancelPopup.value = false
    cancelReason.value = ''
    cancelNote.value = ''
  } catch (error) {
    showToast('取消失败，请重试')
  } finally {
    cancelling.value = false
  }
}

// 修改预约
const modifyAppointment = (id: number) => {
  showToast('修改预约功能开发中')
}

// 查看详情
const viewDetail = (id: number) => {
  showToast('预约详情功能开发中')
}

// 评价服务
const rateService = (id: number) => {
  showToast('服务评价功能开发中')
}

// 再次预约
const bookAgain = (appointment: any) => {
  router.push(`/appointment/booking/${appointment.serviceType}`)
}
</script>

<style scoped lang="scss">
.appointment-list-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding-bottom: 16px;
}

.appointment-list {
  padding: 0 16px;
}

.appointment-card {
  background-color: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #f7f8fa;
    
    .service-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .service-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        
        &.cleaning {
          background-color: #e8f5e8;
          color: #4fc08d;
        }
        
        &.repair {
          background-color: #e6f7ff;
          color: #1890ff;
        }
        
        &.moving {
          background-color: #fff7e6;
          color: #fa8c16;
        }
        
        &.appliance {
          background-color: #f6ffed;
          color: #52c41a;
        }
        
        &.gardening {
          background-color: #f0f9ff;
          color: #0ea5e9;
        }
        
        &.pest {
          background-color: #fee2e2;
          color: #ef4444;
        }
      }
      
      .service-details {
        h4 {
          margin: 0 0 4px 0;
          font-size: 16px;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          font-size: 12px;
          color: #969799;
        }
      }
    }
  }
  
  .service-content {
    padding: 16px;
    
    .info-row {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 8px;
      font-size: 14px;
      color: #646566;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .van-icon {
        margin-top: 2px;
        color: #969799;
        font-size: 16px;
      }
      
      span {
        flex: 1;
        line-height: 1.4;
      }
    }
  }
  
  .worker-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background-color: #f7f8fa;
    
    .worker-details {
      flex: 1;
      
      .worker-name {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 2px;
      }
      
      .worker-phone {
        font-size: 12px;
        color: #969799;
      }
    }
  }
  
  .card-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 16px;
    border-top: 1px solid #f7f8fa;
  }
}

// 取消弹窗样式
.cancel-popup {
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
    
    .cancel-reason {
      .reason-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 16px;
      }
      
      :deep(.van-radio) {
        margin-bottom: 16px;
      }
    }
  }
  
  .popup-footer {
    padding: 20px;
    border-top: 1px solid #ebedf0;
  }
}

:deep(.van-tabs__wrap) {
  background-color: #fff;
  margin-bottom: 16px;
}

:deep(.van-tabs__content) {
  padding: 0;
}
</style>
