<template>
  <div class="red-packet-list">
    <!-- 头部 -->
    <van-nav-bar title="红包活动" left-arrow @click-left="$router.go(-1)" />

    <!-- 统计信息 -->
    <div class="stats-section">
      <van-grid :column-num="3" :border="false">
        <van-grid-item>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.totalRecords || 0 }}</div>
            <div class="stat-label">已抢红包</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-item">
            <div class="stat-value amount">{{ userStats.totalAmountYuan || '0.00' }}元</div>
            <div class="stat-label">累计金额</div>
          </div>
        </van-grid-item>
        <van-grid-item>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.participatedActivities || 0 }}</div>
            <div class="stat-label">参与活动</div>
          </div>
        </van-grid-item>
      </van-grid>
    </div>

    <!-- 活动列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="activity in activities" :key="activity.id" class="activity-card">
          <div class="card-header">
            <div class="activity-info">
              <h3 class="activity-name">{{ activity.activityName }}</h3>
              <p class="activity-desc">{{ activity.activityDesc || '精彩红包活动等你来抢！' }}</p>
            </div>
            <van-tag :type="getStatusType(activity.status)" size="large">
              {{ activity.statusName }}
            </van-tag>
          </div>

          <div class="card-content">
            <div class="amount-info">
              <div class="total-amount">
                <span class="label">总金额</span>
                <span class="value">{{ activity.totalAmountYuan }}元</span>
              </div>
              <div class="total-count">
                <span class="label">总数量</span>
                <span class="value">{{ activity.totalCount }}个</span>
              </div>
            </div>

            <!-- 进度条 -->
            <div class="progress-section">
              <div class="progress-info">
                <span>已抢 {{ activity.grabbedCount }}/{{ activity.totalCount }}</span>
                <span>{{ activity.grabRate }}%</span>
              </div>
              <van-progress
                :percentage="activity.grabRate"
                :color="getProgressColor(activity.grabRate)"
                stroke-width="6px"
              />
            </div>

            <!-- 时间信息 -->
            <div class="time-info">
              <div class="time-item">
                <van-icon name="clock-o" />
                <span>{{ formatTimeRange(activity.startTime, activity.endTime) }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="user-status">
              <van-tag v-if="activity.hasGrabbed" type="success" size="small">
                已抢到
              </van-tag>
              <span v-else-if="!activity.canGrab" class="cannot-grab">
                {{ getCannotGrabReason(activity) }}
              </span>
            </div>
            
            <div class="action-buttons">
              <van-button size="small" @click="viewDetail(activity.id)">
                详情
              </van-button>
              <van-button
                v-if="activity.canGrab && !activity.hasGrabbed"
                type="primary"
                size="small"
                @click="quickGrab(activity)"
                :loading="grabbing === activity.id"
              >
                立即抢
              </van-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <van-empty v-if="!loading && activities.length === 0" description="暂无红包活动" />
      </van-list>
    </van-pull-refresh>

    <!-- 底部导航 -->
    <div class="bottom-actions">
      <van-button type="primary" block @click="goToRecords">
        我的红包记录
      </van-button>
    </div>

    <!-- 抢红包结果弹窗 -->
    <van-dialog
      v-model:show="showGrabResult"
      :title="grabResult.success ? '恭喜你！' : '很遗憾'"
      :show-cancel-button="false"
      confirm-button-text="确定"
      @confirm="showGrabResult = false"
    >
      <div class="grab-result">
        <div v-if="grabResult.success" class="success-result">
          <van-icon name="success" color="#07c160" size="48px" />
          <div class="amount">{{ grabResult.amountYuan }}元</div>
          <div class="message">{{ grabResult.message }}</div>
          <div v-if="grabResult.isLuckiest" class="lucky-tag">
            <van-tag type="danger">手气最佳</van-tag>
          </div>
        </div>
        <div v-else class="fail-result">
          <van-icon name="cross" color="#ee0a24" size="48px" />
          <div class="message">{{ grabResult.message }}</div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import redPacketApi, { type RedPacketActivityListVO, type RedPacketGrabVO } from '@/api/red-packet'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const grabbing = ref<number | null>(null)
const showGrabResult = ref(false)

const activities = ref<RedPacketActivityListVO[]>([])
const userStats = reactive({
  totalRecords: 0,
  totalAmountYuan: '0.00',
  participatedActivities: 0
})

const grabResult = reactive<RedPacketGrabVO>({
  success: false,
  message: '',
  amountYuan: 0,
  isLuckiest: false
})

// 获取活动列表
const fetchActivities = async () => {
  try {
    const response = await redPacketApi.getActiveActivities()
    if (response.code === 200) {
      activities.value = response.data
    } else {
      showToast(response.message || '获取活动列表失败')
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    showToast('获取活动列表失败')
  }
}

// 获取用户统计
const fetchUserStats = async () => {
  try {
    const response = await redPacketApi.getUserStats()
    if (response.code === 200) {
      Object.assign(userStats, response.data)
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

// 下拉刷新
const onRefresh = async () => {
  finished.value = false
  await Promise.all([fetchActivities(), fetchUserStats()])
  refreshing.value = false
}

// 加载更多
const onLoad = async () => {
  if (activities.value.length === 0) {
    await fetchActivities()
  }
  loading.value = false
  finished.value = true
}

// 快速抢红包
const quickGrab = async (activity: RedPacketActivityListVO) => {
  try {
    await showConfirmDialog({
      title: '确认抢红包',
      message: `确定要抢 "${activity.activityName}" 的红包吗？`,
    })

    grabbing.value = activity.id
    const response = await redPacketApi.grabRedPacket({ activityId: activity.id })
    
    if (response.code === 200) {
      Object.assign(grabResult, response.data)
      showGrabResult.value = true
      
      // 更新活动状态
      activity.hasGrabbed = true
      activity.canGrab = false
      activity.grabbedCount += 1
      activity.grabRate = Math.round((activity.grabbedCount / activity.totalCount) * 100)
      
      // 刷新统计
      fetchUserStats()
    } else {
      Object.assign(grabResult, {
        success: false,
        message: response.message || '抢红包失败'
      })
      showGrabResult.value = true
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('抢红包失败:', error)
      showToast('抢红包失败')
    }
  } finally {
    grabbing.value = null
  }
}

// 查看详情
const viewDetail = (id: number) => {
  router.push(`/red-packet/${id}`)
}

// 跳转到记录页面
const goToRecords = () => {
  router.push('/red-packet/records')
}

// 工具函数
const getStatusType = (status: number) => {
  const types = ['default', 'success', 'warning', 'danger']
  return types[status] || 'default'
}

const getProgressColor = (percentage: number) => {
  if (percentage < 30) return '#969799'
  if (percentage < 70) return '#ff976a'
  return '#07c160'
}

const formatTimeRange = (startTime: string, endTime: string) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const now = new Date()
  
  if (now < start) {
    return `${start.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })} 开始`
  } else if (now > end) {
    return '已结束'
  } else {
    return `${end.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })} 结束`
  }
}

const getCannotGrabReason = (activity: RedPacketActivityListVO) => {
  if (activity.status === 0) return '未开始'
  if (activity.status === 2) return '已结束'
  if (activity.status === 3) return '已取消'
  if (activity.remainingCount <= 0) return '已抢完'
  return '不可抢'
}

// 初始化
onMounted(() => {
  fetchUserStats()
})
</script>

<style scoped lang="scss">
.red-packet-list {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;

  .stats-section {
    background: linear-gradient(135deg, #ff6b6b, #ffa726);
    padding: 20px;
    margin-bottom: 10px;

    .stat-item {
      text-align: center;
      color: white;

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        margin-bottom: 4px;

        &.amount {
          color: #fff3cd;
        }
      }

      .stat-label {
        font-size: 12px;
        opacity: 0.9;
      }
    }
  }

  .activity-card {
    background: white;
    margin: 10px;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;

      .activity-info {
        flex: 1;

        .activity-name {
          font-size: 16px;
          font-weight: bold;
          margin: 0 0 4px 0;
          color: #323233;
        }

        .activity-desc {
          font-size: 12px;
          color: #969799;
          margin: 0;
        }
      }
    }

    .card-content {
      .amount-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;

        .total-amount,
        .total-count {
          text-align: center;

          .label {
            display: block;
            font-size: 12px;
            color: #969799;
            margin-bottom: 4px;
          }

          .value {
            font-size: 16px;
            font-weight: bold;
            color: #323233;
          }
        }

        .total-amount .value {
          color: #ff976a;
        }
      }

      .progress-section {
        margin-bottom: 12px;

        .progress-info {
          display: flex;
          justify-content: space-between;
          font-size: 12px;
          color: #969799;
          margin-bottom: 6px;
        }
      }

      .time-info {
        .time-item {
          display: flex;
          align-items: center;
          font-size: 12px;
          color: #969799;

          .van-icon {
            margin-right: 4px;
          }
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f2f3f5;

      .user-status {
        .cannot-grab {
          font-size: 12px;
          color: #969799;
        }
      }

      .action-buttons {
        display: flex;
        gap: 8px;
      }
    }
  }

  .bottom-actions {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 16px;
    background: white;
    border-top: 1px solid #ebedf0;
  }

  .grab-result {
    text-align: center;
    padding: 20px;

    .success-result,
    .fail-result {
      .amount {
        font-size: 24px;
        font-weight: bold;
        color: #07c160;
        margin: 12px 0;
      }

      .message {
        font-size: 14px;
        color: #646566;
        margin: 8px 0;
      }

      .lucky-tag {
        margin-top: 12px;
      }
    }
  }
}
</style>
