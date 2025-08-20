<template>
  <div class="red-packet-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>红包管理</h2>
      <el-button type="primary" @click="goToCreate">
        <el-icon><Plus /></el-icon>
        创建红包活动
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="活动名称">
          <el-input
            v-model="searchForm.activityName"
            placeholder="请输入活动名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.createdTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 350px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="红包信息" min-width="180">
          <template #default="{ row }">
            <div>
              <div>总金额: <span class="amount">{{ row.totalAmountYuan }}元</span></div>
              <div>总数量: {{ row.totalCount }}个</div>
              <div>已抢: {{ row.grabbedCount }}个</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.grabRate"
              :color="getProgressColor(row.grabRate)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="活动时间" min-width="160">
          <template #default="{ row }">
            <div>
              <div>开始: {{ formatTime(row.startTime) }}</div>
              <div>结束: {{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建者" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              @click="operateActivity(row.id, 'start')"
            >
              开始
            </el-button>
            <el-button
              v-if="row.status === 1"
              size="small"
              type="warning"
              @click="operateActivity(row.id, 'end')"
            >
              结束
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="danger"
              @click="operateActivity(row.id, 'cancel')"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import redPacketApi, { type RedPacketActivityVO, type RedPacketActivityQueryDTO } from '@/api/red-packet'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const tableData = ref<RedPacketActivityVO[]>([])

// 搜索表单
const searchForm = reactive({
  activityName: '',
  status: undefined as number | undefined,
  createdTimeRange: [] as string[]
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const queryData: RedPacketActivityQueryDTO = {
      page: pagination.current,
      size: pagination.size,
      activityName: searchForm.activityName || undefined,
      status: searchForm.status,
      createdTimeBegin: searchForm.createdTimeRange[0] || undefined,
      createdTimeEnd: searchForm.createdTimeRange[1] || undefined,
      sortField: 'created_time',
      sortOrder: 'desc'
    }

    const response = await redPacketApi.getActivityPage(queryData)
    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取红包活动列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.activityName = ''
  searchForm.status = undefined
  searchForm.createdTimeRange = []
  pagination.current = 1
  fetchData()
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// 跳转到创建页面
const goToCreate = () => {
  router.push('/red-packet/create')
}

// 查看详情
const viewDetail = (id: number) => {
  router.push(`/red-packet/${id}`)
}

// 活动操作
const operateActivity = async (id: number, operation: 'start' | 'end' | 'cancel') => {
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
      activityId: id,
      operation
    })

    if (response.code === 200) {
      ElMessage.success(`${operationText}成功`)
      fetchData()
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

// 工具函数
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.red-packet-list {
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

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
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
}
</style>
