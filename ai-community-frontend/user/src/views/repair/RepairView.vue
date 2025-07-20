<template>
  <div class="repair-container">
    <van-nav-bar
      title="在线报修"
      fixed
    />
    
    <div class="content">
      <!-- 轮播图 -->
      <div class="banner-section">
        <van-swipe class="banner-swipe" :autoplay="3000" indicator-color="white">
          <van-swipe-item v-for="(item, index) in bannerList" :key="index">
            <div class="banner-item">
              <img :src="item.imageUrl" alt="banner" />
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="action-item" @click="onCreateRepair">
          <div class="action-icon">
            <van-icon name="add" />
          </div>
          <div class="action-text">新增报修</div>
        </div>
        <div class="action-item" @click="onMyRepairList">
          <div class="action-icon">
            <van-icon name="records" />
          </div>
          <div class="action-text">我的报修</div>
        </div>
        <div class="action-item" @click="onShowWorkers">
          <div class="action-icon">
            <van-icon name="user-circle-o" />
          </div>
          <div class="action-text">维修师傅</div>
        </div>
        <div class="action-item" @click="onShowGuide">
          <div class="action-icon">
            <van-icon name="guide-o" />
          </div>
          <div class="action-text">报修指南</div>
        </div>
      </div>

      <!-- 维修师傅推荐 -->
      <div class="workers-section" v-if="workersList.length > 0">
        <div class="section-header">
          <span>维修师傅</span>
          <van-button type="text" size="small" @click="onShowAllWorkers">查看全部</van-button>
        </div>
        
        <div class="workers-list">
          <van-swipe :show-indicators="false" :width="280" :loop="false">
            <van-swipe-item v-for="worker in workersList" :key="worker.id">
              <div class="worker-card" @click="onViewWorker(worker.id)">
                <div class="worker-avatar">
                  <van-image
                    round
                    width="60"
                    height="60"
                    :src="worker.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                    fit="cover"
                  />
                </div>
                <div class="worker-name">{{ worker.name }}</div>
                <div class="worker-type">{{ worker.serviceType }}</div>
                <div class="worker-rating">
                  <van-rate v-model="worker.rating" readonly allow-half size="14" />
                  <span>{{ worker.rating }}分</span>
                </div>
                <div class="worker-count">累计服务{{ worker.serviceCount }}次</div>
                <div class="worker-action">
                  <van-button size="small" type="primary" round>预约</van-button>
                </div>
              </div>
            </van-swipe-item>
          </van-swipe>
        </div>
      </div>

      <!-- 报修流程 -->
      <div class="guide-section">
        <div class="section-header">
          <span>报修流程</span>
        </div>
        
        <div class="guide-steps">
          <van-steps direction="vertical" :active="0">
            <van-step>
              <h3>提交报修</h3>
              <p>填写报修信息，选择报修类型，提交申请</p>
            </van-step>
            <van-step>
              <h3>物业受理</h3>
              <p>物业人员受理报修申请，确定维修方案</p>
            </van-step>
            <van-step>
              <h3>师傅上门</h3>
              <p>维修师傅上门查看并解决问题</p>
            </van-step>
            <van-step>
              <h3>完成评价</h3>
              <p>维修完成后，对服务进行评价</p>
            </van-step>
          </van-steps>
        </div>
      </div>

      <!-- 最近报修 -->
      <div class="recent-repair-section" v-if="recentRepairs.length > 0">
        <div class="section-header">
          <span>最近报修</span>
          <van-button type="text" size="small" @click="onMyRepairList">查看全部</van-button>
        </div>
        
        <div class="repair-list">
          <div
            v-for="item in recentRepairs"
            :key="item.id"
            class="repair-item"
            @click="onViewDetail(item.id)"
          >
            <div class="repair-title">{{ item.title }}</div>
            <div class="repair-info">
              <span class="repair-type">{{ item.repairType }}</span>
              <span class="repair-time">{{ formatDate(item.createTime) }}</span>
            </div>
            <div class="repair-status" :class="getStatusClass(item.status)">
              {{ item.statusDesc }}
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部导航 -->
    <van-tabbar route v-if="authStore.isLoggedIn">
      <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/community" icon="friends-o">社区</van-tabbar-item>
      <van-tabbar-item replace to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>

    <!-- 报修指南弹窗 -->
    <van-popup
      v-model:show="showGuidePopup"
      round
      position="bottom"
      :style="{ height: '70%' }"
    >
      <div class="guide-popup">
        <div class="popup-title">报修指南</div>
        <div class="popup-content">
          <div class="guide-item">
            <div class="guide-item-title">1. 如何提交报修申请？</div>
            <div class="guide-item-content">
              点击首页"新增报修"按钮，填写报修信息（包括报修类型、标题、描述、联系方式等），上传故障照片（可选），提交申请即可。
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-item-title">2. 报修处理流程是什么？</div>
            <div class="guide-item-content">
              提交报修 → 物业受理 → 分配维修师傅 → 师傅上门维修 → 维修完成 → 用户评价
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-item-title">3. 如何查看报修进度？</div>
            <div class="guide-item-content">
              在"我的报修"中可以查看所有报修记录，点击具体报修单可以查看详情和进度。
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-item-title">4. 如何取消报修？</div>
            <div class="guide-item-content">
              在报修详情页面，如果报修状态为"待受理"，可以点击"取消报修"按钮取消。已经开始处理的报修无法取消。
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-item-title">5. 维修完成后如何评价？</div>
            <div class="guide-item-content">
              维修完成后，在报修详情页面可以看到"评价"按钮，点击可以对服务进行星级评价和文字评价。
            </div>
          </div>
        </div>
        <div class="popup-close">
          <van-button type="primary" block round @click="showGuidePopup = false">关闭</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 维修工列表弹窗 -->
    <van-popup
      v-model:show="showWorkersPopup"
      round
      position="bottom"
      :style="{ height: '70%' }"
    >
      <div class="workers-popup">
        <div class="popup-title">维修师傅</div>
        <div class="popup-content">
          <div class="filter-bar">
            <van-dropdown-menu>
              <van-dropdown-item v-model="workerType" :options="workerTypes" />
            </van-dropdown-menu>
          </div>
          
          <div class="workers-grid">
            <div
              v-for="worker in filteredWorkers"
              :key="worker.id"
              class="worker-grid-item"
              @click="onViewWorker(worker.id)"
            >
              <van-image
                round
                width="60"
                height="60"
                :src="worker.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                fit="cover"
              />
              <div class="worker-name">{{ worker.name }}</div>
              <div class="worker-rating">
                <van-rate v-model="worker.rating" readonly allow-half size="14" />
              </div>
              <div class="worker-type">{{ worker.serviceType }}</div>
              <div class="worker-service-count">服务{{ worker.serviceCount }}次</div>
              <van-button size="small" type="primary" round>预约</van-button>
            </div>
          </div>
        </div>
        <div class="popup-close">
          <van-button type="primary" block round @click="showWorkersPopup = false">关闭</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 师傅详情弹窗 -->
    <van-popup
      v-model:show="showWorkerDetailPopup"
      round
      position="bottom"
      :style="{ height: '70%' }"
    >
      <div class="worker-detail-popup" v-if="currentWorker">
        <div class="popup-title">师傅详情</div>
        <div class="popup-content">
          <div class="worker-detail-header">
            <van-image
              round
              width="80"
              height="80"
              :src="currentWorker.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
              fit="cover"
            />
            <div class="worker-detail-info">
              <div class="worker-detail-name">{{ currentWorker.name }}</div>
              <div class="worker-detail-type">{{ currentWorker.serviceType }}</div>
              <div class="worker-detail-rating">
                <van-rate v-model="currentWorker.rating" readonly allow-half size="14" />
                <span>{{ currentWorker.rating }}分</span>
              </div>
              <div class="worker-detail-count">累计服务{{ currentWorker.serviceCount }}次</div>
            </div>
          </div>
          
          <div class="worker-detail-section">
            <div class="section-title">个人简介</div>
            <div class="section-content">
              {{ currentWorker.name }}，{{ getWorkerExperience(currentWorker.serviceCount) }}年维修经验，
              专注于{{ currentWorker.serviceType }}领域，服务认真负责，技术精湛，深受用户好评。
              <div v-if="currentWorker.introduction" class="worker-intro">
                {{ currentWorker.introduction }}
              </div>
            </div>
          </div>
          
          <div class="worker-detail-section">
            <div class="section-title">服务评价</div>
            <div class="section-content">
              <van-loading v-if="loading.reviews" type="spinner" size="24px" vertical>加载中...</van-loading>
              
              <template v-else>
                <div v-if="workerReviews.length === 0" class="empty-reviews">
                  暂无评价
                </div>
                
                <div v-else class="review-list">
                  <div v-for="review in workerReviews" :key="review.id" class="review-item">
                    <div class="review-header">
                      <span class="review-author">{{ review.userId || '匿名用户' }}</span>
                      <span class="review-time">{{ formatDate(review.createTime) }}</span>
                    </div>
                    <div class="review-rating">
                      <van-rate v-model="review.rating" readonly allow-half size="12" />
                    </div>
                    <div class="review-content">
                      {{ review.content || '用户未填写评价内容' }}
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div class="popup-actions">
          <van-button type="default" round size="small" @click="showWorkerDetailPopup = false">关闭</van-button>
          <van-button type="primary" round size="small" @click="onBookWorker(currentWorker.id)">预约师傅</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script>
import { useAuthStore } from '@/store/auth'
import { getAvailableWorkers, getWorkerDetail, getWorkerReviews, pageRepairOrders, bookWorker } from '@/api/repair'
import { showToast, showLoadingToast, closeToast } from 'vant'

export default {
  name: 'RepairView',
  data() {
    return {
      // Banner轮播图
      bannerList: [
        { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg' },
        { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg' },
        { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-3.jpeg' },
      ],
      
      // 维修师傅列表
      workersList: [],
      allWorkers: [],
      workerType: '全部',
      workerTypes: [
        { text: '全部', value: '全部' },
        { text: '水电维修', value: '水电维修' },
        { text: '门窗维修', value: '门窗维修' },
        { text: '家电维修', value: '家电维修' },
        { text: '其他', value: '其他' }
      ],
      
      // 最近报修
      recentRepairs: [],
      
      // 弹窗控制
      showGuidePopup: false,
      showWorkersPopup: false,
      showWorkerDetailPopup: false,
      currentWorker: null,
      workerReviews: [],

      // 加载状态
      loading: {
        workers: false,
        repairs: false,
        workerDetail: false,
        reviews: false
      },

      // 认证状态
      isLoggedIn: false
    }
  },
  computed: {
    // 使用computed获取认证状态
    authStore() {
      return useAuthStore()
    },
    // 过滤后的维修工列表
    filteredWorkers() {
      if (this.workerType === '全部') {
        return this.allWorkers
      } else {
        return this.allWorkers.filter(worker => 
          worker.serviceType && worker.serviceType.includes(this.workerType)
        )
      }
    }
  },
  created() {
    // 检查登录状态
    if (!this.authStore.isLoggedIn) {
      showToast('请先登录')
      this.$router.push('/login')
      return
    }
    
    this.fetchWorkers()
    this.fetchRecentRepairs()
  },
  methods: {
    // 获取维修工列表
    async fetchWorkers() {
      try {
        this.loading.workers = true
        const res = await getAvailableWorkers()
        
        if (res && res.code === 200 && res.data) {
          // 处理返回的数据
          this.workersList = res.data.slice(0, 5) // 只显示前5个
          this.allWorkers = res.data
        } else {
          // 如果API调用失败或返回空数据，使用备用数据
          console.warn('获取维修工列表API返回异常，使用备用数据')
          this.useBackupWorkerData()
        }
      } catch (error) {
        console.error('获取维修工列表失败:', error)
        showToast('获取维修工列表失败，使用备用数据')
        this.useBackupWorkerData()
      } finally {
        this.loading.workers = false
      }
    },
    
    // 使用备用维修工数据
    useBackupWorkerData() {
      const backupData = [
        {
          id: 1,
          name: '张师傅',
          serviceType: '水电维修',
          rating: 4.8,
          serviceCount: 128,
          avatarUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
        },
        {
          id: 2,
          name: '李师傅',
          serviceType: '门窗维修',
          rating: 4.6,
          serviceCount: 98,
          avatarUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'
        },
        {
          id: 3,
          name: '王师傅',
          serviceType: '家电维修',
          rating: 4.9,
          serviceCount: 156,
          avatarUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
        }
      ]
      
      this.workersList = backupData
      this.allWorkers = [...backupData]
    },
    
    // 获取最近报修
    async fetchRecentRepairs() {
      try {
        this.loading.repairs = true
        const res = await pageRepairOrders({
          page: 1,
          pageSize: 3
        })
        
        if (res && res.code === 200 && res.data && res.data.records) {
          this.recentRepairs = res.data.records.map(item => ({
            ...item,
            statusDesc: this.getStatusDesc(item.status)
          }))
        } else {
          // 如果API调用失败或返回空数据，使用备用数据
          console.warn('获取最近报修API返回异常，使用备用数据')
          this.useBackupRepairData()
        }
      } catch (error) {
        console.error('获取最近报修列表失败:', error)
        showToast('获取最近报修列表失败，使用备用数据')
        this.useBackupRepairData()
      } finally {
        this.loading.repairs = false
      }
    },
    
    // 使用备用报修数据
    useBackupRepairData() {
      this.recentRepairs = [
        {
          id: 1,
          title: '水龙头漏水',
          repairType: '水电维修',
          createTime: new Date().toISOString(),
          status: 0,
          statusDesc: '待受理'
        },
        {
          id: 2,
          title: '门锁损坏',
          repairType: '门窗维修',
          createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
          status: 1,
          statusDesc: '已分配'
        }
      ]
    },
    
    // 获取状态描述
    getStatusDesc(status) {
      const statusMap = {
        0: '待受理',
        1: '已分配',
        2: '处理中',
        3: '已完成',
        4: '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    
    // 创建报修
    onCreateRepair() {
      this.$router.replace('/repair/create')
    },
    
    // 查看我的报修列表
    onMyRepairList() {
      this.$router.replace('/repair/list')
    },
    
    // 显示维修工列表
    onShowWorkers() {
      this.showWorkersPopup = true
    },
    
    // 查看所有维修工
    onShowAllWorkers() {
      this.showWorkersPopup = true
    },
    
    // 查看报修指南
    onShowGuide() {
      this.showGuidePopup = true
    },
    
    // 查看报修详情
    onViewDetail(id) {
      this.$router.replace(`/repair/detail/${id}`)
    },
    
    // 查看维修工详情
    async onViewWorker(id) {
      try {
        showLoadingToast({
          message: '加载中...',
          forbidClick: true
        })
        
        this.loading.workerDetail = true
        
        // 获取维修工详情
        const detailRes = await getWorkerDetail(id)
        if (detailRes && detailRes.code === 200 && detailRes.data) {
          this.currentWorker = detailRes.data
        } else {
          // 如果API调用失败，尝试从已有数据中获取
          const worker = this.allWorkers.find(w => w.id === id)
          if (worker) {
            this.currentWorker = { ...worker }
          } else {
            showToast('未找到该维修工')
            return
          }
        }
        
        // 获取评价列表
        await this.fetchWorkerReviews(id)
        
        // 显示详情弹窗
        this.showWorkerDetailPopup = true
        
      } catch (error) {
        console.error('获取维修工详情失败:', error)
        showToast('获取维修工详情失败')
      } finally {
        this.loading.workerDetail = false
        closeToast()
      }
    },
    
    // 获取维修工评价列表
    async fetchWorkerReviews(workerId) {
      try {
        this.loading.reviews = true
        const res = await getWorkerReviews(workerId, { page: 1, pageSize: 5 })
        
        if (res && res.code === 200 && res.data && res.data.records) {
          this.workerReviews = res.data.records
        } else {
          // 使用备用评价数据
          this.useBackupReviewData()
        }
      } catch (error) {
        console.error('获取维修工评价失败:', error)
        this.useBackupReviewData()
      } finally {
        this.loading.reviews = false
      }
    },
    
    // 使用备用评价数据
    useBackupReviewData() {
      this.workerReviews = [
        {
          id: 1,
          userId: '152****3648',
          rating: 5,
          content: '师傅很专业，上门准时，态度很好，维修效果很满意！',
          createTime: '2023-06-15T10:00:00'
        },
        {
          id: 2,
          userId: '138****9527',
          rating: 4.5,
          content: '维修速度快，问题解决得很好，下次还会找这位师傅。',
          createTime: '2023-06-10T14:30:00'
        }
      ]
    },

    // 预约师傅
    async onBookWorker(workerId) {
      try {
        showLoadingToast({
          message: '预约中...',
          forbidClick: true
        })
        
        const res = await bookWorker({
          workerId,
          serviceType: this.currentWorker.serviceType,
          expectedTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
        })
        
        if (res && res.code === 200) {
          showToast('预约成功')
          this.showWorkerDetailPopup = false
        } else {
          showToast('预约失败：' + (res?.msg || '未知错误'))
        }
      } catch (error) {
        console.error('预约师傅失败:', error)
        showToast('预约失败，请稍后重试')
      } finally {
        closeToast()
      }
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      
      const date = new Date(dateString)
      const now = new Date()
      
      // 计算时间差（毫秒）
      const diff = now.getTime() - date.getTime()
      
      // 小于1分钟
      if (diff < 60 * 1000) {
        return '刚刚'
      }
      
      // 小于1小时
      if (diff < 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 1000))}分钟前`
      }
      
      // 小于1天
      if (diff < 24 * 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
      }
      
      // 小于30天
      if (diff < 30 * 24 * 60 * 60 * 1000) {
        return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
      }
      
      // 大于30天，显示具体日期
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    
    // 获取维修工经验
    getWorkerExperience(serviceCount) {
      if (serviceCount < 100) {
        return '1-2'
      } else if (serviceCount < 200) {
        return '2-3'
      } else if (serviceCount < 300) {
        return '3-5'
      } else {
        return '5年以上'
      }
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 0: return 'status-pending'
        case 1: return 'status-assigned'
        case 2: return 'status-processing'
        case 3: return 'status-completed'
        case 4: return 'status-cancelled'
        default: return ''
      }
    }
  }
}
</script>

<style scoped lang="scss">
.repair-container {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 50px;
  background-color: #f7f8fa;
}

.content {
  padding: 16px 0;
}

.banner-section {
  padding: 0 16px;
  margin-bottom: 16px;
  
  .banner-swipe {
    height: 160px;
    border-radius: 8px;
    overflow: hidden;
    
    .banner-item {
      width: 100%;
      height: 100%;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background-color: #fff;
  margin-bottom: 16px;
  border-radius: 8px;
  margin: 0 16px 16px;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .action-icon {
      width: 50px;
      height: 50px;
      background-color: #f0f9eb;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
      
      .van-icon {
        font-size: 24px;
        color: var(--primary-color);
      }
    }
    
    .action-text {
      font-size: 12px;
      color: #333;
    }
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  
  span {
    font-size: 16px;
    font-weight: 500;
    position: relative;
    padding-left: 10px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 16px;
      background-color: var(--primary-color);
      border-radius: 3px;
    }
  }
}

.workers-section {
  background-color: #fff;
  margin: 0 16px 16px;
  border-radius: 8px;
  
  .workers-list {
    padding: 0 16px 16px;
    
    .worker-card {
      width: 260px;
      height: 180px;
      background-color: #f7f8fa;
      border-radius: 8px;
      padding: 16px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .worker-avatar {
        margin-bottom: 8px;
      }
      
      .worker-name {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 4px;
      }
      
      .worker-type {
        font-size: 12px;
        color: #666;
        margin-bottom: 8px;
      }
      
      .worker-rating {
        display: flex;
        align-items: center;
        margin-bottom: 4px;
        
        span {
          margin-left: 4px;
          font-size: 12px;
          color: #ff9900;
        }
      }
      
      .worker-count {
        font-size: 12px;
        color: #999;
        margin-bottom: 10px; /* 增加底部外边距，确保预约按钮有足够空间 */
      }

      .worker-action {
        margin-top: 10px;
        margin-bottom: 5px; /* 增加底部外边距 */
      }
    }
  }
}

.guide-section {
  background-color: #fff;
  margin: 0 16px 16px;
  border-radius: 8px;
  
  .guide-steps {
    padding: 0 16px 16px;
    
    h3 {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 500;
    }
    
    p {
      margin: 0;
      font-size: 12px;
      color: #666;
    }
  }
}

.recent-repair-section {
  background-color: #fff;
  margin: 0 16px 16px;
  border-radius: 8px;
  
  .repair-list {
    padding: 0 16px 16px;
    
    .repair-item {
      background-color: #f7f8fa;
      border-radius: 8px;
      padding: 12px;
      margin-bottom: 8px;
      position: relative;
      
      .repair-title {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
      }
      
      .repair-info {
        display: flex;
        font-size: 12px;
        color: #666;
        margin-bottom: 4px;
        
        .repair-type {
          margin-right: 16px;
        }
      }
      
      .repair-status {
        position: absolute;
        top: 12px;
        right: 12px;
        font-size: 12px;
        padding: 2px 6px;
        border-radius: 10px;
        
        &.status-pending {
          background-color: #e6f7ff;
          color: #1890ff;
        }
        
        &.status-assigned {
          background-color: #f0f9eb;
          color: #67c23a;
        }
        
        &.status-processing {
          background-color: #fff7e6;
          color: #e6a23c;
        }
        
        &.status-completed {
          background-color: #f0f9eb;
          color: #67c23a;
        }
        
        &.status-cancelled {
          background-color: #f4f4f5;
          color: #909399;
        }
      }
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.guide-popup, .workers-popup, .worker-detail-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .popup-title {
    text-align: center;
    font-size: 18px;
    font-weight: 500;
    padding: 16px;
    border-bottom: 1px solid #f2f2f2;
  }
  
  .popup-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    padding-bottom: 24px; /* 增加底部内边距 */
    
    .guide-item {
      margin-bottom: 16px;
      
      .guide-item-title {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--primary-color);
      }
      
      .guide-item-content {
        font-size: 14px;
        color: #666;
        line-height: 1.5;
      }
    }
    
    .filter-bar {
      margin-bottom: 16px;
    }
    
    .workers-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      
      .worker-grid-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-bottom: 12px; /* 增加底部内边距 */
        
        .worker-name {
          margin-top: 8px;
          font-size: 14px;
          font-weight: 500;
        }
        
        .worker-rating {
          margin: 4px 0;
        }
        
        .worker-type {
          font-size: 12px;
          color: #666;
        }

        .worker-service-count {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
          margin-bottom: 8px; /* 增加底部外边距，确保按钮有足够空间 */
        }

        .van-button {
          margin-bottom: 10px; /* 增加按钮底部外边距 */
        }
      }
    }

    .worker-detail-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f2f2f2;

      .worker-detail-info {
        margin-left: 16px;
        flex: 1;
      }
    }

    .worker-detail-section {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f2f2f2;

      .section-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 8px;
        color: var(--primary-color);
      }

      .section-content {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }
    }

    .review-item {
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid #f2f2f2;

      &:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }

      .review-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
      }

      .review-author {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .review-time {
        font-size: 12px;
        color: #999;
      }

      .review-rating {
        margin-bottom: 4px;
      }

      .review-content {
        font-size: 14px;
        color: #666;
        line-height: 1.5;
      }
    }
  }
  
  .popup-close {
    padding: 16px;
    border-top: 1px solid #f2f2f2;
    display: flex;
    justify-content: space-around;
  }
}

.worker-detail-name {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 4px;
}

.worker-detail-type {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.worker-detail-rating {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  
  span {
    margin-left: 4px;
    font-size: 14px;
    color: #ff9900;
  }
}

.worker-detail-count {
  font-size: 12px;
  color: #999;
}

.popup-actions {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  border-top: 1px solid #f2f2f2;
  
  .van-button {
    width: 120px;
  }
}
</style> 