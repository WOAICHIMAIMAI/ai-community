<template>
  <div class="repair-list-container">
    <van-nav-bar
      title="我的报修"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="repair-list">
            <template v-if="repairOrders.length > 0">
              <div
                v-for="order in repairOrders"
                :key="order.id"
                class="repair-item"
                @click="goToDetail(order.id)"
              >
                <div class="repair-header">
                  <div class="repair-title">{{ order.title }}</div>
                  <van-tag :type="getStatusTagType(order.status)" round>{{ getStatusText(order.status) }}</van-tag>
                </div>
                
                <div class="repair-info">
                  <div class="info-item">
                    <van-icon name="clock-o" />
                    <span>{{ formatDate(order.createTime) }}</span>
                  </div>
                  <div class="info-item">
                    <van-icon name="location-o" />
                    <span>{{ order.addressDetail }}</span>
                  </div>
                </div>
              </div>
            </template>
            
            <template v-else-if="!loading && finished">
              <div class="empty-list">
                <van-empty image="search" description="暂无报修记录" />
                <van-button type="primary" size="small" @click="goToCreate">新增报修</van-button>
              </div>
            </template>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <!-- 悬浮按钮 -->
    <div class="float-button" @click="goToCreate">
      <van-icon name="plus" size="20" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { pageRepairOrders } from '@/api/repair'

const router = useRouter()

// 分页参数
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

// 数据
const repairOrders = ref<any[]>([])

// 加载数据
const onLoad = async () => {
  try {
    const res = await pageRepairOrders({
      page: page.value,
      pageSize: pageSize.value
    })

    if (res && res.code === 200 && res.data) {
      const { records, total } = res.data

      // 追加数据
      if (page.value === 1) {
        repairOrders.value = records || []
      } else {
        repairOrders.value.push(...(records || []))
      }

      // 判断是否还有更多数据
      if (repairOrders.value.length >= total) {
        finished.value = true
      } else {
        page.value++
      }

      loading.value = false
    } else {
      showFailToast(res?.msg || '加载失败')
      finished.value = true
      loading.value = false
    }
  } catch (error) {
    console.error('加载报修列表失败:', error)
    showFailToast('加载失败，请稍后重试')
    finished.value = true
    loading.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  page.value = 1
  finished.value = false
  repairOrders.value = []
  refreshing.value = true
  
  onLoad().then(() => {
    refreshing.value = false
    showToast('刷新成功')
  })
}

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

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (24 * 60 * 60 * 1000))
  
  if (diffDays > 0) {
    return `${diffDays}天前`
  } else {
    const diffHours = Math.floor(diffMs / (60 * 60 * 1000))
    if (diffHours > 0) {
      return `${diffHours}小时前`
    } else {
      return '刚刚'
    }
  }
}

// 跳转到详情页
const goToDetail = (orderId: number) => {
  router.push(`/repair/detail/${orderId}`)
}

// 跳转到创建页
const goToCreate = () => {
  router.push('/repair/create')
}

// 返回上一页
const onClickLeft = () => {
  router.replace('/repair')
}

// 初始化
onMounted(() => {
  // 自动触发第一次加载
})
</script>

<style scoped>
.repair-list-container {
  padding-top: 46px;
  padding-bottom: 20px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 15px;
}

.repair-item {
  margin-bottom: 12px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.repair-title {
  font-size: 16px;
  font-weight: bold;
  flex: 1;
}

.repair-info {
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 14px;
  color: #646566;
}

.info-item .van-icon {
  margin-right: 6px;
  font-size: 14px;
}

.empty-list {
  padding: 40px 0;
  text-align: center;
}

.empty-list .van-button {
  margin-top: 16px;
}

.float-button {
  position: fixed;
  right: 16px;
  bottom: 80px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #1989fa;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 99;
}
</style> 