<template>
  <div class="appointment-container">
    <van-nav-bar
      title="预约服务"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <!-- 轮播图 -->
      <div class="banner-section">
        <van-swipe class="banner-swipe" :autoplay="3000" indicator-color="white">
          <van-swipe-item v-for="(banner, index) in bannerList" :key="index">
            <div class="banner-item">
              <img :src="banner.imageUrl" :alt="banner.title" />
              <div class="banner-overlay">
                <h3>{{ banner.title }}</h3>
                <p>{{ banner.subtitle }}</p>
              </div>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 服务类型 -->
      <div class="services-section">
        <div class="section-title">
          <van-icon name="service-o" />
          <span>选择服务类型</span>
        </div>
        
        <van-grid :column-num="2" :border="false" :gutter="12">
          <van-grid-item
            v-for="service in serviceTypes"
            :key="service.type"
            @click="selectService(service)"
            class="service-item"
          >
            <template #icon>
              <div class="service-icon" :class="service.type">
                <van-icon :name="service.icon" size="32" />
              </div>
            </template>
            <template #text>
              <div class="service-info">
                <div class="service-name">{{ service.name }}</div>
                <div class="service-price">¥{{ service.price }}/{{ service.unit || '次' }}起</div>
                <div class="service-desc">{{ service.description }}</div>
              </div>
            </template>
          </van-grid-item>
        </van-grid>
      </div>

      <!-- 我的预约 -->
      <div class="my-appointments-section">
        <div class="section-header">
          <div class="section-title">
            <van-icon name="calendar-o" />
            <span>我的预约</span>
          </div>
          <MoreButton text="查看全部" @click="goToAppointmentList" />
        </div>

        <van-cell-group inset v-if="recentAppointments.length > 0">
          <van-cell
            v-for="appointment in recentAppointments"
            :key="appointment.id"
            :title="appointment.serviceName"
            :label="formatDateTime(appointment.appointmentTime)"
            is-link
            @click="goToAppointmentDetail(appointment.id)"
          >
            <template #icon>
              <div class="appointment-icon" :class="appointment.serviceType">
                <van-icon :name="getServiceIcon(appointment.serviceType)" />
              </div>
            </template>
            <template #value>
              <van-tag :type="getStatusTagType(appointment.status)" size="small">
                {{ getStatusText(appointment.status) }}
              </van-tag>
            </template>
          </van-cell>
        </van-cell-group>

        <van-empty v-else description="暂无预约记录" />
      </div>

      <!-- 服务须知 -->
      <div class="notice-section">
        <div class="section-title">
          <van-icon name="info-o" />
          <span>服务须知</span>
        </div>
        
        <div class="notice-card">
          <div class="notice-item">
            <van-icon name="clock-o" color="#4fc08d" />
            <div class="notice-content">
              <div class="notice-title">服务时间</div>
              <div class="notice-desc">周一至周日 8:00-20:00</div>
            </div>
          </div>
          
          <div class="notice-item">
            <van-icon name="phone-o" color="#4fc08d" />
            <div class="notice-content">
              <div class="notice-title">提前预约</div>
              <div class="notice-desc">建议提前1-3天预约，确保服务质量</div>
            </div>
          </div>
          
          <div class="notice-item">
            <van-icon name="gold-coin-o" color="#4fc08d" />
            <div class="notice-content">
              <div class="notice-title">费用说明</div>
              <div class="notice-desc">服务完成后统一结算，支持多种支付方式</div>
            </div>
          </div>
          
          <div class="notice-item">
            <van-icon name="shield-o" color="#4fc08d" />
            <div class="notice-content">
              <div class="notice-title">服务保障</div>
              <div class="notice-desc">专业团队，服务质量有保障，不满意可重做</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 联系客服 -->
      <div class="contact-section">
        <van-button
          block
          type="default"
          @click="contactService"
        >
          <van-icon name="service-o" />
          联系客服
        </van-button>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import MoreButton from '@/components/MoreButton.vue'
import { getServiceTypes, getRecentAppointments } from '@/api/appointment'
import type { AppointmentService, AppointmentRecord } from '@/api/appointment'

const router = useRouter()

// 模拟数据 - 轮播图
const bannerList = ref([
  {
    imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg',
    title: '专业保洁服务',
    subtitle: '让您的家焕然一新'
  },
  {
    imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg',
    title: '快速维修服务',
    subtitle: '专业师傅，上门服务'
  },
  {
    imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-3.jpeg',
    title: '贴心搬家服务',
    subtitle: '安全快捷，省心省力'
  }
])

// 服务类型数据
const serviceTypes = ref<AppointmentService[]>([])

// 最近预约数据
const recentAppointments = ref<AppointmentRecord[]>([])

// 页面初始化
onMounted(async () => {
  await loadData()
})

// 加载数据
const loadData = async () => {
  try {
    // 并行加载服务类型和最近预约数据
    const [servicesRes, appointmentsRes] = await Promise.all([
      getServiceTypes(),
      getRecentAppointments(2)
    ])
    
    if (servicesRes.code === 200) {
      serviceTypes.value = servicesRes.data
    }
    
    if (appointmentsRes.code === 200) {
      recentAppointments.value = appointmentsRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    showToast('加载数据失败，请稍后重试')
  }
}

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 选择服务
const selectService = (service: any) => {
  router.push(`/appointment/booking/${service.type}`)
}

// 跳转到预约列表
const goToAppointmentList = () => {
  router.push('/appointment/list')
}

// 跳转到预约详情
const goToAppointmentDetail = (id: number) => {
  router.push(`/appointment/detail/${id}`)
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

// 联系客服
const contactService = () => {
  showToast('客服功能开发中')
}
</script>

<style scoped lang="scss">
.appointment-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px;
}

.content {
  padding-bottom: 16px;
}

.banner-section {
  margin-bottom: 20px;
  
  .banner-swipe {
    height: 180px;
    border-radius: 0 0 16px 16px;
    overflow: hidden;
  }
  
  .banner-item {
    position: relative;
    height: 100%;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .banner-overlay {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
      color: white;
      padding: 20px;
      
      h3 {
        margin: 0 0 4px 0;
        font-size: 18px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.services-section {
  padding: 0 16px;
  margin-bottom: 20px;
  
  .section-title {
    margin-bottom: 16px;
  }
  
  .service-item {
    background-color: #fff;
    border-radius: 12px;
    height: 140px;
    
    .service-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
      
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
    
    .service-info {
      text-align: center;
      
      .service-name {
        font-size: 14px;
        font-weight: 500;
        color: #323233;
        margin-bottom: 4px;
      }
      
      .service-price {
        font-size: 12px;
        color: #ff6b35;
        font-weight: 500;
        margin-bottom: 4px;
      }
      
      .service-desc {
        font-size: 11px;
        color: #969799;
        line-height: 1.3;
      }
    }
  }
}

.my-appointments-section {
  margin-bottom: 20px;
}

.appointment-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  
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
}

.notice-section {
  padding: 0 16px;
  margin-bottom: 20px;
  
  .section-title {
    margin-bottom: 16px;
  }
  
  .notice-card {
    background-color: #fff;
    border-radius: 12px;
    padding: 16px;
    
    .notice-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .van-icon {
        margin-top: 2px;
        font-size: 18px;
      }
      
      .notice-content {
        flex: 1;
        
        .notice-title {
          font-size: 14px;
          font-weight: 500;
          color: #323233;
          margin-bottom: 2px;
        }
        
        .notice-desc {
          font-size: 12px;
          color: #646566;
          line-height: 1.4;
        }
      }
    }
  }
}

.contact-section {
  padding: 0 16px;
  
  .van-button {
    border: 1px solid #ebedf0;
    color: #646566;
    
    .van-icon {
      margin-right: 6px;
    }
  }
}

:deep(.van-cell-group) {
  margin: 0 16px;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.van-cell) {
  padding: 12px 16px;
}

:deep(.van-grid-item__content) {
  padding: 16px 8px;
  flex-direction: column;
  justify-content: center;
}

:deep(.van-swipe__indicator) {
  background-color: rgba(255, 255, 255, 0.5);
  
  &--active {
    background-color: white;
  }
}
</style>
