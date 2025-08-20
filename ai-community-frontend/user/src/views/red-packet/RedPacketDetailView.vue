<template>
  <div class="red-packet-detail">
    <!-- 头部 -->
    <van-nav-bar title="红包详情" left-arrow @click-left="$router.go(-1)" />

    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" />
    </div>

    <div v-else-if="activity" class="detail-content">
      <!-- 红包主体 -->
      <div class="red-packet-main">
        <div class="packet-header">
          <h2 class="activity-name">{{ activity.activityName }}</h2>
          <p class="activity-desc">{{ activity.activityDesc || '精彩红包活动等你来抢！' }}</p>
          <van-tag :type="getStatusType(activity.status)" size="large">
            {{ activity.statusName }}
          </van-tag>
        </div>

        <div class="packet-body">
          <div class="amount-display">
            <div class="total-amount">
              <span class="amount-value">{{ activity.totalAmountYuan }}</span>
              <span class="amount-unit">元</span>
            </div>
            <div class="packet-count">共{{ activity.totalCount }}个红包</div>
          </div>

          <!-- 用户抢红包状态 -->
          <div v-if="activity.hasGrabbed" class="user-grabbed">
            <van-icon name="success" color="#07c160" size="24px" />
            <span class="grabbed-text">你已抢到 {{ activity.userGrabbedAmountYuan }}元</span>
            <div class="grab-time">{{ formatTime(activity.userGrabTime) }}</div>
          </div>

          <!-- 抢红包按钮 -->
          <div v-else class="grab-section">
            <van-button
              v-if="activity.canGrab"
              type="primary"
              size="large"
              round
              @click="grabRedPacket"
              :loading="grabbing"
              class="grab-button"
            >
              立即抢红包
            </van-button>
            <div v-else class="cannot-grab">
              <van-icon name="info-o" />
              <span>{{ activity.cannotGrabReason || '无法抢红包' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 活动信息 -->
      <van-cell-group title="活动信息" class="info-section">
        <van-cell title="活动时间">
          <template #value>
            <div class="time-info">
              <div>开始：{{ formatTime(activity.startTime) }}</div>
              <div>结束：{{ formatTime(activity.endTime) }}</div>
            </div>
          </template>
        </van-cell>
        <van-cell title="活动进度">
          <template #value>
            <div class="progress-info">
              <div class="progress-text">
                已抢 {{ activity.grabbedCount }}/{{ activity.totalCount }} ({{ activity.grabRate }}%)
              </div>
              <van-progress
                :percentage="activity.grabRate"
                :color="getProgressColor(activity.grabRate)"
                stroke-width="6px"
              />
            </div>
          </template>
        </van-cell>
        <van-cell title="剩余红包" :value="`${activity.remainingCount}个`" />
        <van-cell title="剩余金额" :value="`${activity.remainingAmountYuan}元`" />
      </van-cell-group>

      <!-- 抢红包记录 -->
      <van-cell-group title="抢红包记录" class="records-section">
        <div v-if="records.length === 0" class="empty-records">
          <van-empty description="暂无记录" />
        </div>
        <div v-else class="records-list">
          <div v-for="(record, index) in records" :key="record.id" class="record-item">
            <div class="record-info">
              <div class="user-info">
                <van-image
                  :src="record.avatar || '/default-avatar.png'"
                  round
                  width="32px"
                  height="32px"
                />
                <span class="nickname">{{ record.nickname }}</span>
                <van-tag v-if="record.isLuckiest" type="danger" size="small">手气最佳</van-tag>
              </div>
              <div class="record-detail">
                <span class="amount">{{ record.amountYuan }}元</span>
                <span class="time">{{ formatTime(record.grabTime) }}</span>
              </div>
            </div>
            <div class="ranking">#{{ record.ranking || index + 1 }}</div>
          </div>
        </div>
        
        <div v-if="hasMoreRecords" class="load-more">
          <van-button
            type="primary"
            size="small"
            @click="loadMoreRecords"
            :loading="loadingRecords"
          >
            加载更多
          </van-button>
        </div>
      </van-cell-group>
    </div>

    <!-- 抢红包结果弹窗 -->
    <van-dialog
      v-model:show="showGrabResult"
      :title="grabResult.success ? '恭喜你！' : '很遗憾'"
      :show-cancel-button="false"
      confirm-button-text="确定"
      @confirm="handleGrabResultConfirm"
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
import { useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import redPacketApi, { type RedPacketActivityVO, type RedPacketRecordVO, type RedPacketGrabVO } from '@/api/red-packet'

const route = useRoute()

// 响应式数据
const loading = ref(true)
const grabbing = ref(false)
const loadingRecords = ref(false)
const showGrabResult = ref(false)
const hasMoreRecords = ref(true)

const activity = ref<RedPacketActivityVO | null>(null)
const records = ref<RedPacketRecordVO[]>([])
const recordsPage = ref(1)
const recordsSize = 20

const grabResult = reactive<RedPacketGrabVO>({
  success: false,
  message: '',
  amountYuan: 0,
  isLuckiest: false
})

// 获取活动详情
const fetchActivityDetail = async () => {
  try {
    const id = Number(route.params.id)
    const response = await redPacketApi.getActivityDetail(id)
    if (response.code === 200) {
      activity.value = response.data
    } else {
      showToast(response.message || '获取活动详情失败')
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    showToast('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

// 获取抢红包记录
const fetchRecords = async (page = 1) => {
  if (!activity.value) return

  try {
    loadingRecords.value = true
    const response = await redPacketApi.getActivityRecords({
      activityId: activity.value.id,
      page,
      size: recordsSize
    })
    
    if (response.code === 200) {
      if (page === 1) {
        records.value = response.data.records
      } else {
        records.value.push(...response.data.records)
      }
      hasMoreRecords.value = response.data.records.length === recordsSize
    } else {
      showToast(response.message || '获取记录失败')
    }
  } catch (error) {
    console.error('获取抢红包记录失败:', error)
    showToast('获取记录失败')
  } finally {
    loadingRecords.value = false
  }
}

// 加载更多记录
const loadMoreRecords = () => {
  recordsPage.value++
  fetchRecords(recordsPage.value)
}

// 抢红包
const grabRedPacket = async () => {
  if (!activity.value) return

  try {
    await showConfirmDialog({
      title: '确认抢红包',
      message: `确定要抢 "${activity.value.activityName}" 的红包吗？`,
    })

    grabbing.value = true
    const response = await redPacketApi.grabRedPacket({ activityId: activity.value.id })
    
    if (response.code === 200) {
      Object.assign(grabResult, response.data)
      showGrabResult.value = true
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
    grabbing.value = false
  }
}

// 处理抢红包结果确认
const handleGrabResultConfirm = () => {
  showGrabResult.value = false
  if (grabResult.success) {
    // 刷新活动详情和记录
    fetchActivityDetail()
    fetchRecords(1)
  }
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

const formatTime = (time: string | undefined) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 初始化
onMounted(async () => {
  await fetchActivityDetail()
  if (activity.value) {
    fetchRecords(1)
  }
})
</script>

<style scoped lang="scss">
.red-packet-detail {
  min-height: 100vh;
  background: #f7f8fa;

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .detail-content {
    .red-packet-main {
      background: linear-gradient(135deg, #ff6b6b, #ffa726);
      padding: 20px;
      color: white;

      .packet-header {
        text-align: center;
        margin-bottom: 20px;

        .activity-name {
          font-size: 20px;
          font-weight: bold;
          margin: 0 0 8px 0;
        }

        .activity-desc {
          font-size: 14px;
          opacity: 0.9;
          margin: 0 0 12px 0;
        }
      }

      .packet-body {
        text-align: center;

        .amount-display {
          margin-bottom: 20px;

          .total-amount {
            .amount-value {
              font-size: 36px;
              font-weight: bold;
            }

            .amount-unit {
              font-size: 18px;
              margin-left: 4px;
            }
          }

          .packet-count {
            font-size: 14px;
            opacity: 0.9;
            margin-top: 8px;
          }
        }

        .user-grabbed {
          background: rgba(255, 255, 255, 0.2);
          border-radius: 12px;
          padding: 16px;

          .grabbed-text {
            font-size: 16px;
            font-weight: bold;
            margin-left: 8px;
          }

          .grab-time {
            font-size: 12px;
            opacity: 0.8;
            margin-top: 4px;
          }
        }

        .grab-section {
          .grab-button {
            width: 200px;
            height: 44px;
            font-size: 16px;
            font-weight: bold;
          }

          .cannot-grab {
            display: flex;
            align-items: center;
            justify-content: center;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 12px;
            padding: 12px;
            font-size: 14px;

            .van-icon {
              margin-right: 6px;
            }
          }
        }
      }
    }

    .info-section {
      margin: 12px 0;

      .time-info {
        text-align: right;
        font-size: 12px;
        color: #969799;

        div {
          margin-bottom: 2px;
        }
      }

      .progress-info {
        text-align: right;

        .progress-text {
          font-size: 12px;
          color: #969799;
          margin-bottom: 6px;
        }
      }
    }

    .records-section {
      margin: 12px 0;

      .empty-records {
        padding: 20px;
      }

      .records-list {
        .record-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 16px;
          border-bottom: 1px solid #f2f3f5;

          &:last-child {
            border-bottom: none;
          }

          .record-info {
            flex: 1;

            .user-info {
              display: flex;
              align-items: center;
              margin-bottom: 4px;

              .nickname {
                margin-left: 8px;
                font-size: 14px;
                font-weight: 500;
              }

              .van-tag {
                margin-left: 8px;
              }
            }

            .record-detail {
              display: flex;
              justify-content: space-between;
              font-size: 12px;
              color: #969799;

              .amount {
                color: #ff976a;
                font-weight: bold;
              }
            }
          }

          .ranking {
            font-size: 12px;
            color: #969799;
            font-weight: bold;
          }
        }
      }

      .load-more {
        text-align: center;
        padding: 16px;
      }
    }
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
