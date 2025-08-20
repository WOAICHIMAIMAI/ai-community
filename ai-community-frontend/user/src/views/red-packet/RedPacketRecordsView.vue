<template>
  <div class="red-packet-records">
    <!-- 头部 -->
    <van-nav-bar title="我的红包" left-arrow @click-left="$router.go(-1)" />

    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stats-header">
        <h3>红包统计</h3>
        <van-button size="small" @click="refreshStats">刷新</van-button>
      </div>
      <van-grid :column-num="3" :border="false">
        <van-grid-item>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.totalRecords || 0 }}</div>
            <div class="stat-label">抢红包次数</div>
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

    <!-- 筛选器 -->
    <div class="filter-section">
      <van-dropdown-menu>
        <van-dropdown-item
          v-model="filterActivity"
          :options="activityOptions"
          @change="onFilterChange"
        />
        <van-dropdown-item
          v-model="sortType"
          :options="sortOptions"
          @change="onSortChange"
        />
      </van-dropdown-menu>
    </div>

    <!-- 记录列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="record in records" :key="record.id" class="record-card">
          <div class="record-header">
            <div class="activity-info">
              <h4 class="activity-name">{{ record.activityName }}</h4>
              <div class="record-time">{{ formatTime(record.grabTime) }}</div>
            </div>
            <div class="amount-info">
              <div class="amount">{{ record.amountYuan }}元</div>
              <van-tag v-if="record.isLuckiest" type="danger" size="small">
                手气最佳
              </van-tag>
            </div>
          </div>

          <div class="record-content">
            <van-cell-group>
              <van-cell title="红包序号" :value="`第${record.packetIndex}个`" />
              <van-cell title="交易流水" :value="record.transactionNo" />
              <van-cell title="账户状态">
                <template #value>
                  <van-tag :type="record.accountUpdated === 1 ? 'success' : 'warning'">
                    {{ record.accountUpdatedName }}
                  </van-tag>
                </template>
              </van-cell>
            </van-cell-group>
          </div>

          <div class="record-footer">
            <van-button size="small" @click="viewActivityDetail(record.activityId)">
              查看活动
            </van-button>
          </div>
        </div>

        <!-- 空状态 -->
        <van-empty v-if="!loading && records.length === 0" description="暂无红包记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import redPacketApi, { type RedPacketRecordVO } from '@/api/red-packet'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const size = 10

const records = ref<RedPacketRecordVO[]>([])
const userStats = reactive({
  totalRecords: 0,
  totalAmountYuan: '0.00',
  participatedActivities: 0,
  avgAmountYuan: '0.00',
  maxAmountYuan: '0.00',
  firstGrabTime: '',
  lastGrabTime: ''
})

// 筛选和排序
const filterActivity = ref(0)
const sortType = ref('time_desc')

const activityOptions = ref([
  { text: '全部活动', value: 0 }
])

const sortOptions = [
  { text: '时间倒序', value: 'time_desc' },
  { text: '时间正序', value: 'time_asc' },
  { text: '金额倒序', value: 'amount_desc' },
  { text: '金额正序', value: 'amount_asc' }
]

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

// 获取记录列表
const fetchRecords = async (pageNum = 1, reset = false) => {
  try {
    const response = await redPacketApi.getUserRecords({
      page: pageNum,
      size,
      activityId: filterActivity.value || undefined
    })

    if (response.code === 200) {
      const newRecords = response.data.records
      
      // 排序处理
      if (sortType.value !== 'time_desc') {
        newRecords.sort((a, b) => {
          switch (sortType.value) {
            case 'time_asc':
              return new Date(a.grabTime).getTime() - new Date(b.grabTime).getTime()
            case 'amount_desc':
              return b.amount - a.amount
            case 'amount_asc':
              return a.amount - b.amount
            default:
              return new Date(b.grabTime).getTime() - new Date(a.grabTime).getTime()
          }
        })
      }

      if (reset || pageNum === 1) {
        records.value = newRecords
      } else {
        records.value.push(...newRecords)
      }

      finished.value = newRecords.length < size
      
      // 更新活动选项
      updateActivityOptions()
    } else {
      showToast(response.message || '获取记录失败')
    }
  } catch (error) {
    console.error('获取红包记录失败:', error)
    showToast('获取记录失败')
  }
}

// 更新活动选项
const updateActivityOptions = () => {
  const activities = new Map()
  records.value.forEach(record => {
    if (!activities.has(record.activityId)) {
      activities.set(record.activityId, record.activityName)
    }
  })

  activityOptions.value = [
    { text: '全部活动', value: 0 },
    ...Array.from(activities.entries()).map(([id, name]) => ({
      text: name,
      value: id
    }))
  ]
}

// 下拉刷新
const onRefresh = async () => {
  page.value = 1
  finished.value = false
  await Promise.all([fetchUserStats(), fetchRecords(1, true)])
  refreshing.value = false
}

// 加载更多
const onLoad = async () => {
  if (page.value === 1) {
    await fetchRecords(1)
  } else {
    await fetchRecords(page.value)
  }
  page.value++
  loading.value = false
}

// 筛选变化
const onFilterChange = () => {
  page.value = 1
  finished.value = false
  fetchRecords(1, true)
}

// 排序变化
const onSortChange = () => {
  // 重新排序当前数据
  const allRecords = [...records.value]
  allRecords.sort((a, b) => {
    switch (sortType.value) {
      case 'time_asc':
        return new Date(a.grabTime).getTime() - new Date(b.grabTime).getTime()
      case 'amount_desc':
        return b.amount - a.amount
      case 'amount_asc':
        return a.amount - b.amount
      default:
        return new Date(b.grabTime).getTime() - new Date(a.grabTime).getTime()
    }
  })
  records.value = allRecords
}

// 刷新统计
const refreshStats = () => {
  fetchUserStats()
}

// 查看活动详情
const viewActivityDetail = (activityId: number) => {
  router.push(`/red-packet/${activityId}`)
}

// 工具函数
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) { // 1周内
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 初始化
onMounted(() => {
  fetchUserStats()
})
</script>

<style scoped lang="scss">
.red-packet-records {
  min-height: 100vh;
  background: #f7f8fa;

  .stats-card {
    background: white;
    margin: 12px;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .stats-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        color: #323233;
      }
    }

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: #323233;
        margin-bottom: 4px;

        &.amount {
          color: #ff976a;
        }
      }

      .stat-label {
        font-size: 12px;
        color: #969799;
      }
    }
  }

  .filter-section {
    margin: 0 12px 12px 12px;
  }

  .record-card {
    background: white;
    margin: 0 12px 12px 12px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .record-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 16px;
      background: linear-gradient(135deg, #ff6b6b, #ffa726);
      color: white;

      .activity-info {
        flex: 1;

        .activity-name {
          font-size: 16px;
          font-weight: bold;
          margin: 0 0 4px 0;
        }

        .record-time {
          font-size: 12px;
          opacity: 0.9;
        }
      }

      .amount-info {
        text-align: right;

        .amount {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 4px;
        }
      }
    }

    .record-content {
      .van-cell-group {
        background: transparent;
      }

      .van-cell {
        font-size: 14px;
      }
    }

    .record-footer {
      padding: 12px 16px;
      border-top: 1px solid #f2f3f5;
      text-align: right;
    }
  }
}
</style>
