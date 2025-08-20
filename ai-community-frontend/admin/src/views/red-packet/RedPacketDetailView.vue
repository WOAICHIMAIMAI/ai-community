<template>
  <div class="red-packet-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>红包活动详情</h2>
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div v-loading="loading">
      <!-- 活动基本信息 -->
      <el-card class="info-card" v-if="activityInfo">
        <template #header>
          <div class="card-header">
            <span>活动信息</span>
            <div class="header-actions">
              <el-tag :type="getStatusType(activityInfo.status)" size="large">
                {{ activityInfo.statusName }}
              </el-tag>
              <el-button
                v-if="activityInfo.status === 0"
                type="success"
                size="small"
                @click="operateActivity('start')"
              >
                开始活动
              </el-button>
              <el-button
                v-if="activityInfo.status === 1"
                type="warning"
                size="small"
                @click="operateActivity('end')"
              >
                结束活动
              </el-button>
              <el-button
                v-if="activityInfo.status === 0"
                type="danger"
                size="small"
                @click="operateActivity('cancel')"
              >
                取消活动
              </el-button>
            </div>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="info-item">
              <span class="label">活动名称：</span>
              <span class="value">{{ activityInfo.activityName }}</span>
            </div>
            <div class="info-item">
              <span class="label">活动描述：</span>
              <span class="value">{{ activityInfo.activityDesc || '无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建者：</span>
              <span class="value">{{ activityInfo.creatorName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatTime(activityInfo.createdTime) }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ formatTime(activityInfo.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">结束时间：</span>
              <span class="value">{{ formatTime(activityInfo.endTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">活动状态：</span>
              <span class="value">{{ activityInfo.statusName }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatTime(activityInfo.updatedTime) }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 红包统计信息 -->
      <el-row :gutter="20" class="stats-row" v-if="activityInfo">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value total">{{ activityInfo.totalAmountYuan }}元</div>
              <div class="stat-label">红包总金额</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value total">{{ activityInfo.totalCount }}个</div>
              <div class="stat-label">红包总数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value grabbed">{{ activityInfo.grabbedCount }}个</div>
              <div class="stat-label">已抢数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value grabbed">{{ activityInfo.grabbedAmountYuan }}元</div>
              <div class="stat-label">已抢金额</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 进度条 -->
      <el-card class="progress-card" v-if="activityInfo">
        <div class="progress-info">
          <span>抢红包进度</span>
          <span>{{ activityInfo.grabRate }}%</span>
        </div>
        <el-progress
          :percentage="activityInfo.grabRate"
          :stroke-width="20"
          :color="getProgressColor(activityInfo.grabRate)"
        />
        <div class="progress-detail">
          <span>剩余：{{ activityInfo.remainingCount }}个 / {{ activityInfo.remainingAmountYuan }}元</span>
        </div>
      </el-card>

      <!-- 抢红包记录 -->
      <el-card class="records-card">
        <template #header>
          <div class="card-header">
            <span>抢红包记录</span>
            <el-button @click="refreshRecords" size="small">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <el-table
          v-loading="recordsLoading"
          :data="recordsData"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="ranking" label="排名" width="80" />
          <el-table-column label="用户" min-width="120">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="30" :src="row.avatar" />
                <span class="nickname">{{ row.nickname }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="红包金额" width="120">
            <template #default="{ row }">
              <span class="amount">{{ row.amountYuan }}元</span>
            </template>
          </el-table-column>
          <el-table-column prop="packetIndex" label="红包序号" width="100" />
          <el-table-column label="抢红包时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.grabTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="transactionNo" label="交易流水号" min-width="200" />
          <el-table-column label="账户状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.accountUpdated === 1 ? 'success' : 'warning'">
                {{ row.accountUpdatedName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="手气" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isLuckiest" type="danger" size="small">最佳</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="recordsPagination.current"
            v-model:page-size="recordsPagination.size"
            :page-sizes="[10, 20, 50]"
            :total="recordsPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleRecordsSizeChange"
            @current-change="handleRecordsCurrentChange"
          />
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <el-card class="actions-card" v-if="activityInfo">
        <el-button @click="preloadActivity" :loading="preloading">
          预加载到Redis
        </el-button>
        <el-button @click="clearCache" :loading="clearing">
          清理缓存
        </el-button>
        <el-button @click="processUnprocessed" :loading="processing">
          处理未更新记录
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import redPacketApi, { type RedPacketActivityVO, type RedPacketRecordVO } from '@/api/red-packet'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const recordsLoading = ref(false)
const preloading = ref(false)
const clearing = ref(false)
const processing = ref(false)

const activityInfo = ref<RedPacketActivityVO | null>(null)
const recordsData = ref<RedPacketRecordVO[]>([])

// 分页信息
const recordsPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取活动详情
const fetchActivityDetail = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const response = await redPacketApi.getActivityDetail(id)
    if (response.code === 200) {
      activityInfo.value = response.data
    } else {
      ElMessage.error(response.message || '获取活动详情失败')
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

// 获取抢红包记录
const fetchRecords = async () => {
  if (!activityInfo.value) return
  
  recordsLoading.value = true
  try {
    const response = await redPacketApi.getActivityRecords({
      activityId: activityInfo.value.id,
      page: recordsPagination.current,
      size: recordsPagination.size
    })
    if (response.code === 200) {
      recordsData.value = response.data.records
      recordsPagination.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取记录失败')
    }
  } catch (error) {
    console.error('获取抢红包记录失败:', error)
    ElMessage.error('获取记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 活动操作
const operateActivity = async (operation: 'start' | 'end' | 'cancel') => {
  if (!activityInfo.value) return

  const operationText = {
    start: '开始',
    end: '结束',
    cancel: '取消'
  }[operation]

  try {
    await ElMessageBox.confirm(
      `确定要${operationText}这个红包活动吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await redPacketApi.operateActivity({
      activityId: activityInfo.value.id,
      operation
    })

    if (response.code === 200) {
      ElMessage.success(`${operationText}成功`)
      fetchActivityDetail()
    } else {
      ElMessage.error(response.message || `${operationText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${operationText}活动失败:`, error)
      ElMessage.error(`${operationText}失败`)
    }
  }
}

// 预加载活动
const preloadActivity = async () => {
  if (!activityInfo.value) return

  preloading.value = true
  try {
    const response = await redPacketApi.preloadActivity(activityInfo.value.id)
    if (response.code === 200) {
      ElMessage.success('预加载成功')
    } else {
      ElMessage.error(response.message || '预加载失败')
    }
  } catch (error) {
    console.error('预加载失败:', error)
    ElMessage.error('预加载失败')
  } finally {
    preloading.value = false
  }
}

// 清理缓存
const clearCache = async () => {
  if (!activityInfo.value) return

  clearing.value = true
  try {
    const response = await redPacketApi.clearActivityCache(activityInfo.value.id)
    if (response.code === 200) {
      ElMessage.success('清理缓存成功')
    } else {
      ElMessage.error(response.message || '清理缓存失败')
    }
  } catch (error) {
    console.error('清理缓存失败:', error)
    ElMessage.error('清理缓存失败')
  } finally {
    clearing.value = false
  }
}

// 处理未更新记录
const processUnprocessed = async () => {
  processing.value = true
  try {
    const response = await redPacketApi.processUnprocessedRecords(100)
    if (response.code === 200) {
      ElMessage.success(`处理了 ${response.data} 条记录`)
      fetchRecords()
    } else {
      ElMessage.error(response.message || '处理失败')
    }
  } catch (error) {
    console.error('处理未更新记录失败:', error)
    ElMessage.error('处理失败')
  } finally {
    processing.value = false
  }
}

// 刷新记录
const refreshRecords = () => {
  fetchRecords()
}

// 分页变化
const handleRecordsSizeChange = (size: number) => {
  recordsPagination.size = size
  recordsPagination.current = 1
  fetchRecords()
}

const handleRecordsCurrentChange = (current: number) => {
  recordsPagination.current = current
  fetchRecords()
}

// 工具函数
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status: number) => {
  const types = ['info', 'success', 'warning', 'danger']
  return types[status] || 'info'
}

const getProgressColor = (percentage: number) => {
  if (percentage < 30) return '#909399'
  if (percentage < 70) return '#E6A23C'
  return '#67C23A'
}

// 返回列表
const goBack = () => {
  router.push('/red-packet')
}

// 初始化
onMounted(async () => {
  await fetchActivityDetail()
  if (activityInfo.value) {
    fetchRecords()
  }
})
</script>

<style scoped lang="scss">
.red-packet-detail {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .info-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-actions {
        display: flex;
        gap: 10px;
        align-items: center;
      }
    }

    .info-item {
      display: flex;
      margin-bottom: 12px;

      .label {
        width: 100px;
        color: #666;
      }

      .value {
        color: #333;
        flex: 1;
      }
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-item {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 8px;

          &.total {
            color: #409EFF;
          }

          &.grabbed {
            color: #67C23A;
          }
        }

        .stat-label {
          color: #666;
          font-size: 14px;
        }
      }
    }
  }

  .progress-card {
    margin-bottom: 20px;

    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      font-weight: bold;
    }

    .progress-detail {
      margin-top: 10px;
      text-align: center;
      color: #666;
      font-size: 14px;
    }
  }

  .records-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .nickname {
        font-size: 14px;
      }
    }

    .amount {
      color: #E6A23C;
      font-weight: bold;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }

  .actions-card {
    text-align: center;

    .el-button {
      margin: 0 10px;
    }
  }
}
</style>
