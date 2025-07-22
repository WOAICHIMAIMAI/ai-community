<template>
  <div class="appointment-home">
    <van-nav-bar title="预约服务" left-arrow @click-left="goBack" fixed />
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <!-- 轮播横幅 -->
        <div class="banner-section">
          <van-swipe class="service-banner" :autoplay="4000" indicator-color="white">
            <van-swipe-item v-for="(banner, index) in bannerList" :key="index">
              <div class="banner-item" :style="{ background: banner.gradient }">
                <div class="banner-content">
                  <div class="banner-text">
                    <h3>{{ banner.title }}</h3>
                    <p>{{ banner.subtitle }}</p>
                  </div>
                  <div class="banner-icon">
                    <van-icon :name="banner.icon" size="40" />
                  </div>
                </div>
              </div>
            </van-swipe-item>
          </van-swipe>
        </div>

        <!-- 快速预约 -->
        <div class="quick-booking-section">
          <div class="section-header">
            <h2>快速预约</h2>
            <span class="section-subtitle">一键预约热门服务</span>
          </div>
          <div class="quick-services">
            <div 
              v-for="service in quickServices" 
              :key="service.type"
              class="quick-service-item"
              @click="quickBook(service)"
            >
              <div class="service-icon" :style="{ background: service.color }">
                <van-icon :name="service.icon" size="24" />
              </div>
              <span class="service-name">{{ service.name }}</span>
            </div>
          </div>
        </div>

        <!-- 服务分类 -->
        <div class="services-section">
          <div class="section-header">
            <h2>全部服务</h2>
            <span class="section-subtitle">专业服务，品质保障</span>
          </div>
          <div class="services-grid">
            <div 
              v-for="service in allServices" 
              :key="service.type"
              class="service-card"
              @click="bookService(service)"
            >
              <div class="service-card-header">
                <div class="service-icon-large" :style="{ background: service.gradient }">
                  <van-icon :name="service.icon" size="28" />
                </div>
                <div class="service-badge" v-if="service.isHot">
                  <van-tag type="danger" size="small">热门</van-tag>
                </div>
              </div>
              <div class="service-info">
                <h3 class="service-title">{{ service.name }}</h3>
                <p class="service-desc">{{ service.description }}</p>
                <div class="service-meta">
                  <span class="service-price">¥{{ service.price }}/次</span>
                  <span class="service-rating">
                    <van-icon name="star" size="12" />
                    {{ service.rating }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近预约 -->
        <div class="recent-section" v-if="recentAppointments.length > 0">
          <div class="section-header">
            <h2>最近预约</h2>
            <van-button 
              type="primary" 
              size="small" 
              plain 
              @click="goToAppointmentList"
            >
              查看全部
            </van-button>
          </div>
          <div class="recent-list">
            <div 
              v-for="appointment in recentAppointments" 
              :key="appointment.id"
              class="recent-item"
              @click="goToAppointmentDetail(appointment.id)"
            >
              <div class="recent-icon">
                <van-icon :name="getServiceIcon(appointment.serviceType)" size="20" />
              </div>
              <div class="recent-info">
                <h4>{{ appointment.serviceName }}</h4>
                <p class="recent-time">{{ formatAppointmentTime(appointment.appointmentTime) }}</p>
              </div>
              <div class="recent-status">
                <van-tag 
                  :type="getStatusType(appointment.status)" 
                  size="small"
                >
                  {{ getStatusText(appointment.status) }}
                </van-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 服务推荐 -->
        <div class="recommend-section">
          <div class="section-header">
            <h2>为您推荐</h2>
            <span class="section-subtitle">基于您的使用习惯</span>
          </div>
          <div class="recommend-list">
            <div 
              v-for="item in recommendServices" 
              :key="item.type"
              class="recommend-item"
              @click="bookService(item)"
            >
              <div class="recommend-content">
                <div class="recommend-icon">
                  <van-icon :name="item.icon" size="24" />
                </div>
                <div class="recommend-info">
                  <h4>{{ item.name }}</h4>
                  <p>{{ item.reason }}</p>
                </div>
              </div>
              <div class="recommend-action">
                <van-button type="primary" size="small" round>立即预约</van-button>
              </div>
            </div>
          </div>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import BottomTabbar from '@/components/BottomTabbar.vue'
import appointmentApi, { type AppointmentService, type AppointmentRecord } from '@/api/appointment'

const router = useRouter()
const refreshing = ref(false)

// 轮播横幅数据
const bannerList = ref([
  {
    title: '专业家政服务',
    subtitle: '深度清洁，焕然一新',
    icon: 'brush-o',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '快速维修服务',
    subtitle: '24小时响应，专业维修',
    icon: 'setting-o',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '搬家无忧',
    subtitle: '专业团队，安全可靠',
    icon: 'logistics',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
])

// 快速预约服务
const quickServices = ref([
  {
    type: 'cleaning',
    name: '家政保洁',
    icon: 'brush-o',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    type: 'repair',
    name: '维修服务',
    icon: 'setting-o',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    type: 'appliance',
    name: '家电维修',
    icon: 'tv-o',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    type: 'moving',
    name: '搬家服务',
    icon: 'logistics',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// 全部服务
const allServices = ref([
  {
    type: 'cleaning',
    name: '家政保洁',
    icon: 'brush-o',
    description: '深度清洁，专业保洁团队',
    price: '80',
    rating: '4.8',
    isHot: true,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    type: 'repair',
    name: '维修服务',
    icon: 'setting-o',
    description: '水电维修，家具安装',
    price: '50',
    rating: '4.9',
    isHot: true,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    type: 'appliance',
    name: '家电维修',
    icon: 'tv-o',
    description: '家电故障，快速维修',
    price: '60',
    rating: '4.7',
    isHot: false,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    type: 'moving',
    name: '搬家服务',
    icon: 'logistics',
    description: '专业搬家，安全可靠',
    price: '200',
    rating: '4.6',
    isHot: false,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  {
    type: 'gardening',
    name: '园艺服务',
    icon: 'flower-o',
    description: '花草养护，园艺设计',
    price: '100',
    rating: '4.5',
    isHot: false,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    type: 'pest',
    name: '除虫服务',
    icon: 'delete-o',
    description: '专业除虫，安全环保',
    price: '120',
    rating: '4.4',
    isHot: false,
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  }
])

// 最近预约记录
const recentAppointments = ref([
  {
    id: 1,
    orderNo: 'APT202401250001',
    serviceName: '家政保洁',
    serviceType: 'cleaning',
    appointmentTime: '2024-01-25T14:00:00',
    status: 'confirmed'
  },
  {
    id: 2,
    orderNo: 'APT202401230002',
    serviceName: '水龙头维修',
    serviceType: 'repair',
    appointmentTime: '2024-01-23T10:00:00',
    status: 'completed'
  },
  {
    id: 3,
    orderNo: 'APT202401220003',
    serviceName: '搬家服务',
    serviceType: 'moving',
    appointmentTime: '2024-01-22T09:00:00',
    status: 'pending'
  }
])

// 推荐服务
const recommendServices = ref([
  {
    type: 'cleaning',
    name: '家政保洁',
    icon: 'brush-o',
    reason: '您上次预约的服务，体验很棒'
  },
  {
    type: 'appliance',
    name: '家电维修',
    icon: 'tv-o',
    reason: '冬季家电使用频繁，建议定期检修'
  }
])

// 方法
const goBack = () => {
  router.go(-1)
}

const onRefresh = async () => {
  try {
    // 重新加载数据
    await loadServiceData()
    showToast('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    showFailToast('刷新失败')
  } finally {
    refreshing.value = false
  }
}

const quickBook = (service: any) => {
  router.push(`/appointment/booking?type=${service.type}`)
}

const bookService = (service: any) => {
  router.push(`/appointment/booking?type=${service.type}`)
}

const goToAppointmentList = () => {
  router.push('/appointment/list')
}

const goToAppointmentDetail = (id: number) => {
  router.push(`/appointment/detail/${id}`)
}

const getServiceIcon = (serviceType: string) => {
  const iconMap: Record<string, string> = {
    cleaning: 'brush-o',
    repair: 'setting-o',
    moving: 'logistics',
    appliance: 'tv-o',
    gardening: 'flower-o',
    pest: 'delete-o'
  }
  return iconMap[serviceType] || 'service-o'
}

const formatAppointmentTime = (timeString: string) => {
  const date = new Date(timeString)
  const now = new Date()
  const diffTime = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '明天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === -1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    confirmed: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || '未知'
}

// 数据加载函数
const loadServiceData = async () => {
  try {
    // 并行加载各种数据
    const [hotServicesRes, recommendRes, recentRes] = await Promise.allSettled([
      appointmentApi.getHotServices(),
      appointmentApi.getRecommendServices(),
      appointmentApi.getRecentAppointments(3)
    ])

    // 处理热门服务数据
    if (hotServicesRes.status === 'fulfilled' && hotServicesRes.value?.code === 200) {
      const hotData = hotServicesRes.value.data
      if (hotData && hotData.length > 0) {
        // 更新快速预约服务
        quickServices.value = hotData.slice(0, 4)
        // 更新全部服务（标记热门）
        allServices.value = allServices.value.map(service => ({
          ...service,
          isHot: hotData.some((hot: any) => hot.type === service.type)
        }))
      }
    }

    // 处理推荐服务数据
    if (recommendRes.status === 'fulfilled' && recommendRes.value?.code === 200) {
      const recommendData = recommendRes.value.data
      if (recommendData && recommendData.length > 0) {
        recommendServices.value = recommendData
      }
    }

    // 处理最近预约数据
    if (recentRes.status === 'fulfilled' && recentRes.value?.code === 200) {
      const recentData = recentRes.value.data
      if (recentData && recentData.length > 0) {
        recentAppointments.value = recentData
      }
    }
  } catch (error) {
    console.error('加载服务数据失败:', error)
    showFailToast('加载数据失败，显示默认内容')
  }
}

onMounted(async () => {
  // 页面初始化，加载数据
  await loadServiceData()
})
</script>

<style scoped lang="scss">
.appointment-home {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 50px;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
}

.content {
  padding-bottom: 20px;
}

// 轮播横幅
.banner-section {
  padding: 16px;

  .service-banner {
    height: 140px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .banner-item {
      width: 100%;
      height: 100%;
      border-radius: 12px;

      .banner-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 100%;
        padding: 20px;
        color: white;

        .banner-text {
          flex: 1;

          h3 {
            font-size: 20px;
            font-weight: bold;
            margin: 0 0 8px 0;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
          }

          p {
            font-size: 14px;
            margin: 0;
            opacity: 0.9;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
          }
        }

        .banner-icon {
          opacity: 0.8;
        }
      }
    }
  }
}

// 区块标题
.section-header {
  padding: 20px 16px 12px;

  h2 {
    font-size: 18px;
    font-weight: bold;
    color: #2c3e50;
    margin: 0 0 4px 0;
  }

  .section-subtitle {
    font-size: 13px;
    color: #7f8c8d;
  }

  // 带按钮的标题
  &:has(.van-button) {
    display: flex;
    justify-content: space-between;
    align-items: center;

    div {
      display: flex;
      flex-direction: column;
    }
  }
}

// 快速预约
.quick-booking-section {
  margin-bottom: 8px;

  .quick-services {
    display: flex;
    justify-content: space-around;
    padding: 0 16px;

    .quick-service-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px;
      cursor: pointer;
      transition: transform 0.2s ease;

      &:active {
        transform: scale(0.95);
      }

      .service-icon {
        width: 56px;
        height: 56px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        margin-bottom: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .service-name {
        font-size: 12px;
        color: #2c3e50;
        font-weight: 500;
      }
    }
  }
}

// 服务分类
.services-section {
  margin-bottom: 8px;

  .services-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 0 16px;

    .service-card {
      background: white;
      border-radius: 16px;
      padding: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;

      &:active {
        transform: translateY(2px);
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      }

      .service-card-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .service-icon-large {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
        }

        .service-badge {
          position: absolute;
          top: -4px;
          right: -4px;
        }
      }

      .service-info {
        .service-title {
          font-size: 16px;
          font-weight: bold;
          color: #2c3e50;
          margin: 0 0 6px 0;
        }

        .service-desc {
          font-size: 13px;
          color: #7f8c8d;
          margin: 0 0 12px 0;
          line-height: 1.4;
        }

        .service-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .service-price {
            font-size: 16px;
            font-weight: bold;
            color: #e74c3c;
          }

          .service-rating {
            display: flex;
            align-items: center;
            gap: 2px;
            font-size: 12px;
            color: #f39c12;
            font-weight: 500;
          }
        }
      }
    }
  }
}

// 最近预约
.recent-section {
  margin-bottom: 8px;

  .recent-list {
    padding: 0 16px;

    .recent-item {
      background: white;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.2s ease;

      &:active {
        transform: scale(0.98);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .recent-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
      }

      .recent-info {
        flex: 1;

        h4 {
          font-size: 15px;
          font-weight: 600;
          color: #2c3e50;
          margin: 0 0 4px 0;
        }

        .recent-time {
          font-size: 13px;
          color: #7f8c8d;
          margin: 0;
        }
      }

      .recent-status {
        // 状态标签样式由 van-tag 控制
      }
    }
  }
}

// 服务推荐
.recommend-section {
  .recommend-list {
    padding: 0 16px;

    .recommend-item {
      background: white;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.2s ease;

      &:active {
        transform: scale(0.98);
      }

      &:last-child {
        margin-bottom: 0;
      }

      .recommend-content {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;

        .recommend-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        }

        .recommend-info {
          h4 {
            font-size: 15px;
            font-weight: 600;
            color: #2c3e50;
            margin: 0 0 4px 0;
          }

          p {
            font-size: 13px;
            color: #7f8c8d;
            margin: 0;
            line-height: 1.4;
          }
        }
      }

      .recommend-action {
        // 按钮样式由 van-button 控制
      }
    }
  }
}

// 响应式设计
@media (max-width: 375px) {
  .services-grid {
    gap: 8px !important;

    .service-card {
      padding: 12px !important;
    }
  }

  .quick-services {
    .quick-service-item {
      padding: 8px !important;

      .service-icon {
        width: 48px !important;
        height: 48px !important;
      }
    }
  }
}

// 深色模式支持
@media (prefers-color-scheme: dark) {
  .appointment-home {
    background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 100%);
  }

  .service-card,
  .recent-item,
  .recommend-item {
    background: #2d2d2d !important;

    .service-title,
    h4 {
      color: #ffffff !important;
    }

    .service-desc,
    .recent-time,
    p {
      color: #b0b0b0 !important;
    }
  }

  .section-header {
    h2 {
      color: #ffffff !important;
    }

    .section-subtitle {
      color: #b0b0b0 !important;
    }
  }

  .quick-service-item {
    .service-name {
      color: #ffffff !important;
    }
  }
}
</style>
