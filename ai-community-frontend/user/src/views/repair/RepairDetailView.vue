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
            <div v-if="repair.completionTime && repair.status === 3" class="status-time">
              完成时间：{{ formatDate(repair.completionTime) }}
            </div>
          </div>
        </div>
        
        <!-- 维修工信息（如果已分配） -->
        <div v-if="repair.workerId && repair.status !== 4" class="info-card">
          <div class="card-title">维修工信息</div>
          
          <van-cell-group inset>
            <van-cell title="维修工" :value="repair.workerName" />
            <van-cell title="联系电话" :value="repair.workerPhone">
              <template #right-icon>
                <van-button 
                  size="small" 
                  type="primary" 
                  plain 
                  @click="callWorker"
                >
                  拨打电话
                </van-button>
              </template>
            </van-cell>
            <van-cell 
              v-if="repair.appointmentTime" 
              title="预约上门时间" 
              :value="formatDate(repair.appointmentTime)" 
            />
          </van-cell-group>
        </div>
        
        <!-- 报修信息 -->
        <div class="info-card">
          <div class="card-title">报修信息</div>
          
          <van-cell-group inset>
            <van-cell title="工单编号" :value="repair.orderNumber" />
            <van-cell title="报修类型" :value="repair.repairType" />
            <van-cell title="报修地址" :value="repair.addressDetail" />
            <van-cell title="联系电话" :value="repair.contactPhone" />
            <van-cell title="期望上门" :value="repair.expectedTime" />
            <van-cell title="问题描述" :label="repair.description" />
            
            <!-- 现场照片 -->
            <van-cell v-if="repair.images" title="现场照片">
              <template #default>
                <div class="images-preview">
                  <van-image
                    v-for="(img, index) in getImageList(repair.images)"
                    :key="index"
                    width="60"
                    height="60"
                    :src="img"
                    @click="previewImages(index)"
                  />
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
        
        <!-- 评价信息（如果已评价） -->
        <div v-if="repair.status === 3 && repair.satisfactionLevel" class="info-card">
          <div class="card-title">我的评价</div>
          
          <van-cell-group inset>
            <van-cell title="满意度">
              <template #default>
                <van-rate v-model="repair.satisfactionLevel" readonly />
              </template>
            </van-cell>
            <van-cell v-if="repair.feedback" title="评价内容" :label="repair.feedback" />
          </van-cell-group>
        </div>
        
        <!-- 操作按钮区域 -->
        <div v-if="showActionButtons" class="action-buttons">
          <!-- 取消工单按钮：状态为 0/1/2 时显示 -->
          <van-button 
            v-if="canCancel"
            block 
            type="default"
            @click="handleCancelOrder"
          >
            取消工单
          </van-button>
          
          <!-- 确认完成按钮：状态为 2（处理中）时显示 -->
          <van-button 
            v-if="canComplete"
            block 
            type="primary"
            @click="handleCompleteOrder"
          >
            确认完成
          </van-button>
          
          <!-- 评价按钮：状态为 3（已完成）且未评价时显示 -->
          <van-button 
            v-if="canEvaluate"
            block 
            type="success"
            @click="handleEvaluate"
          >
            评价服务
          </van-button>
        </div>
      </template>
      
      <template v-else>
        <div class="empty-data">
          <van-empty image="error" description="未找到报修信息" />
          <van-button type="primary" size="small" @click="goToList">返回列表</van-button>
        </div>
      </template>
    </div>
    
    <!-- 取消工单对话框 -->
    <van-dialog
      v-model:show="showCancelDialog"
      title="取消工单"
      show-cancel-button
      @confirm="confirmCancel"
    >
      <van-field
        v-model="cancelRemark"
        rows="3"
        autosize
        type="textarea"
        placeholder="请输入取消原因（选填）"
        style="margin: 16px 0;"
      />
    </van-dialog>
    
    <!-- 评价对话框 -->
    <van-dialog
      v-model:show="showEvaluateDialog"
      title="评价服务"
      show-cancel-button
      @confirm="confirmEvaluate"
    >
      <div style="padding: 16px;">
        <div style="margin-bottom: 12px; font-size: 14px; color: #646566;">满意度评分</div>
        <van-rate v-model="evaluateData.satisfactionLevel" :size="30" />
        
        <van-field
          v-model="evaluateData.feedback"
          rows="3"
          autosize
          type="textarea"
          placeholder="请输入评价内容（选填）"
          style="margin-top: 16px;"
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showFailToast, showSuccessToast, showConfirmDialog, showImagePreview } from 'vant'
import { getRepairOrderDetail, updateRepairOrderStatus, submitFeedback } from '@/api/repair'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const repair = ref<any>(null)
const showCancelDialog = ref(false)
const cancelRemark = ref('')
const showEvaluateDialog = ref(false)
const evaluateData = ref({
  satisfactionLevel: 5,
  feedback: ''
})

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

// 是否显示操作按钮
const showActionButtons = computed(() => {
  if (!repair.value) return false
  const status = repair.value.status
  // 状态为已取消(4)时不显示任何按钮
  return status !== 4
})

// 是否可以取消：状态为 0/1/2
const canCancel = computed(() => {
  if (!repair.value) return false
  const status = repair.value.status
  return status === 0 || status === 1 || status === 2
})

// 是否可以确认完成：状态为 2（处理中）
const canComplete = computed(() => {
  if (!repair.value) return false
  return repair.value.status === 2
})

// 是否可以评价：状态为 3（已完成）且未评价
const canEvaluate = computed(() => {
  if (!repair.value) return false
  return repair.value.status === 3 && !repair.value.satisfactionLevel
})

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

// 取消工单
const handleCancelOrder = () => {
  cancelRemark.value = ''
  showCancelDialog.value = true
}

// 确认取消
const confirmCancel = async () => {
  try {
    const res: any = await updateRepairOrderStatus({
      orderId: repair.value.id,
      status: 4, // 已取消
      remark: cancelRemark.value || '用户取消'
    })
    
    if (res && res.code === 200) {
      showSuccessToast('工单已取消')
      // 重新加载详情
      await fetchRepairDetail()
    } else {
      showFailToast(res?.message || '取消失败')
    }
  } catch (error) {
    console.error('取消工单失败:', error)
    showFailToast('取消失败，请重试')
  }
}

// 确认完成
const handleCompleteOrder = async () => {
  showConfirmDialog({
    title: '确认完成',
    message: '确认维修工作已完成？',
  }).then(async () => {
    try {
      const res: any = await updateRepairOrderStatus({
        orderId: repair.value.id,
        status: 3, // 已完成
        remark: '用户确认完成'
      })
      
      if (res && res.code === 200) {
        showSuccessToast('已确认完成')
        // 重新加载详情
        await fetchRepairDetail()
      } else {
        showFailToast(res?.message || '操作失败')
      }
    } catch (error) {
      console.error('确认完成失败:', error)
      showFailToast('操作失败，请重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 评价服务
const handleEvaluate = () => {
  evaluateData.value = {
    satisfactionLevel: 5,
    feedback: ''
  }
  showEvaluateDialog.value = true
}

// 确认评价
const confirmEvaluate = async () => {
  if (!evaluateData.value.satisfactionLevel) {
    showToast('请选择满意度评分')
    return
  }
  
  try {
    const res: any = await submitFeedback({
      orderId: repair.value.id,
      satisfactionLevel: evaluateData.value.satisfactionLevel,
      feedback: evaluateData.value.feedback
    })
    
    if (res && res.code === 200) {
      showSuccessToast('评价成功')
      // 重新加载详情
      await fetchRepairDetail()
    } else {
      showFailToast(res?.message || '评价失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    showFailToast('评价失败，请重试')
  }
}

// 拨打维修工电话
const callWorker = () => {
  if (repair.value.workerPhone) {
    window.location.href = `tel:${repair.value.workerPhone}`
  }
}

// 获取图片列表
const getImageList = (images: string) => {
  if (!images) return []
  return images.split(',').filter(img => img.trim())
}

// 预览图片
const previewImages = (startPosition: number) => {
  const images = getImageList(repair.value.images)
  showImagePreview({
    images,
    startPosition
  })
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

<style scoped lang="scss">
.repair-detail-container {
  padding-top: 46px;
  padding-bottom: 80px;
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
  margin-top: 4px;
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

.images-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  
  .van-image {
    border-radius: 4px;
    overflow: hidden;
  }
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
  z-index: 100;
  
  .van-button {
    flex: 1;
  }
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.empty-data .van-button {
  margin-top: 16px;
}
</style> 