<template>
  <div class="repair-detail-container">
    <van-nav-bar
      title="报修详情"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <template v-if="loading">
        <van-skeleton title :row="10" />
      </template>
      
      <template v-else-if="repair">
        <!-- 状态卡片 -->
        <div class="status-card">
          <div class="status-header">
            <van-tag :type="getStatusTagType(repair.status)" size="large" round>{{ getStatusText(repair.status) }}</van-tag>
          </div>
          
          <div class="status-content">
            <div class="status-title">{{ repair.title }}</div>
            <div class="status-time">报修时间：{{ formatDate(repair.createTime) }}</div>
          </div>
        </div>
        
        <!-- 报修信息 -->
        <div class="info-card">
          <div class="card-title">报修信息</div>
          
          <van-cell-group inset>
            <van-cell title="报修类型" :value="repair.repairType" />
            <van-cell title="报修地址" :value="repair.addressDetail" />
            <van-cell title="联系电话" :value="repair.contactPhone" />
            <van-cell title="期望上门" :value="repair.expectedTime" />
            <van-cell title="问题描述" :label="repair.description" />
          </van-cell-group>
        </div>
      </template>
      
      <template v-else>
        <div class="empty-data">
          <van-empty image="error" description="未找到报修信息" />
          <van-button type="primary" size="small" @click="goToList">返回列表</van-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { getRepairOrderDetail } from '@/api/repair'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const repair = ref<any>(null)

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待受理',
    1: '已分配',
    2: '处理中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'primary',
    1: 'warning',
    2: 'warning',
    3: 'success',
    4: 'default'
  }
  return typeMap[status] || 'default'
}

// 获取报修详情
const fetchRepairDetail = async () => {
  loading.value = true
  
  try {
    const orderId = Number(route.params.id)
    
    if (!orderId || isNaN(orderId)) {
      showFailToast('工单ID无效')
      loading.value = false
      return
    }
    
    const res = await getRepairOrderDetail(orderId)
    
    if (res && res.code === 200 && res.data) {
      repair.value = res.data
    } else {
      showFailToast(res?.msg || '获取工单详情失败')
    }
  } catch (error) {
    console.error('获取报修详情失败:', error)
    showFailToast('获取详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goToList = () => {
  router.replace('/repair/list')
}

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 日期格式化
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchRepairDetail()
})
</script>

<style scoped>
.repair-detail-container {
  padding-top: 46px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 15px;
}

.status-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.status-header {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.status-content {
  text-align: center;
}

.status-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.status-time {
  font-size: 14px;
  color: #969799;
}

.info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #1989fa;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.empty-data .van-button {
  margin-top: 16px;
}
</style> 