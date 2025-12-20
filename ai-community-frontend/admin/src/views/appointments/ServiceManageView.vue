<template>
  <div class="service-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>服务管理</h2>
      <p>管理用户申请的预约服务，进行审核、启用/禁用等操作</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ pendingCount }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleQuery">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-select
          v-model="queryParams.approvalStatus"
          placeholder="审核状态"
          clearable
          style="width: 140px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="服务状态"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索服务名称"
          style="width: 250px; margin-right: 10px;"
          @keyup.enter="handleQuery"
          @clear="handleQuery"
          clearable
        >
          <template #append>
            <el-button @click="handleQuery">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="服务名称" min-width="150">
          <template #default="{ row }">
            <div class="service-name">
              <span>{{ row.name }}</span>
              <el-tag 
                v-if="row.status === 0" 
                type="danger" 
                size="small" 
                style="margin-left: 8px;"
              >
                已禁用
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="服务类型" width="120">
          <template #default="{ row }">
            {{ getServiceTypeName(row.type) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="服务描述" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.description" placement="top">
              <div class="description-text">
                {{ truncateText(row.description, 50) }}
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="price" label="服务价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="unit" label="单位" width="80" />
        
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="{ row }">
            <span>{{ row.rating || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="approvalStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ getApprovalStatusText(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="服务状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
              :disabled="row.approvalStatus !== 1"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="isHot" label="热门" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isHot"
              :active-value="true"
              :inactive-value="false"
              @change="(val: boolean) => handleHotChange(row, val)"
              :disabled="row.approvalStatus !== 1 || row.status !== 1"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <template v-if="row.approvalStatus === 0">
                <el-button type="success" size="small" @click="handleApprove(row, true)">
                  <el-icon><Select /></el-icon>
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="handleApprove(row, false)">
                  <el-icon><Close /></el-icon>
                  拒绝
                </el-button>
              </template>
              <el-button type="info" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </div>

    <!-- 拒绝原因弹窗 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="服务详情"
      width="600px"
    >
      <service-detail
        v-if="viewDialogVisible"
        :service-id="currentService.id"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Search, Refresh, View, Delete, Select, Close } from '@element-plus/icons-vue'
import {
  getServiceList,
  approveService,
  updateServiceStatus,
  setHotService,
  deleteService,
  getPendingServiceCount
} from '@/api/appointment'
import ServiceDetail from './components/ServiceDetail.vue'

// 响应式数据
const loading = ref(false)
const initializing = ref(true) // 新增：初始化标志
const tableData = ref<any[]>([])
const total = ref(0)
const pendingCount = ref(0)

// 查询参数
const queryParams = reactive({
  approvalStatus: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
  page: 1,
  pageSize: 20
})

// 弹窗相关
const rejectDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentService = ref<any>({})
const rejectForm = reactive({
  reason: ''
})

// 页面初始化
onMounted(async () => {
  initializing.value = true
  await Promise.all([loadServices(), loadPendingCount()])
  // 延迟一下确保 DOM 完全渲染
  setTimeout(() => {
    initializing.value = false
  }, 300)
})

// 加载待审核数量
const loadPendingCount = async () => {
  try {
    const res = await getPendingServiceCount()
    if (res.code === 200) {
      pendingCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载待审核数量失败:', error)
  }
}

// 加载服务列表
const loadServices = async () => {
  try {
    loading.value = true
    const res = await getServiceList(queryParams)
    
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载服务列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = async () => {
  queryParams.page = 1
  initializing.value = true
  await loadServices()
  setTimeout(() => {
    initializing.value = false
  }, 200)
}

// 重置
const handleReset = async () => {
  Object.assign(queryParams, {
    approvalStatus: undefined,
    status: undefined,
    keyword: '',
    page: 1,
    pageSize: 20
  })
  initializing.value = true
  await loadServices()
  setTimeout(() => {
    initializing.value = false
  }, 200)
}

// 审核服务
const handleApprove = async (row: any, approved: boolean) => {
  if (approved) {
    try {
      await ElMessageBox.confirm('确定要通过此服务的审核吗？', '提示', {
        type: 'info'
      })
      
      await approveService(row.id, true)
      ElMessage.success('审核通过')
      loadServices()
      loadPendingCount()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '操作失败')
      }
    }
  } else {
    currentService.value = row
    rejectForm.reason = ''
    rejectDialogVisible.value = true
  }
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    await approveService(currentService.value.id, false, rejectForm.reason)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadServices()
    loadPendingCount()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 状态变更
const handleStatusChange = async (row: any, newStatus: number) => {
  // 如果正在初始化，忽略事件
  if (initializing.value) {
    return
  }
  
  const oldStatus = row.status
  
  // 先更新UI
  row.status = newStatus
  
  try {
    await updateServiceStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '状态更新失败')
    // 恢复原状态
    row.status = oldStatus
  }
}

// 热门设置
const handleHotChange = async (row: any, newHot: boolean) => {
  // 如果正在初始化，忽略事件
  if (initializing.value) {
    return
  }
  
  const oldHot = row.isHot
  
  // 先更新UI
  row.isHot = newHot
  
  try {
    // 将布尔值转换为整数：true -> 1, false -> 0
    await setHotService(row.id, newHot ? 1 : 0)
    ElMessage.success('设置成功')
  } catch (error: any) {
    ElMessage.error(error.message || '设置失败')
    // 恢复原状态
    row.isHot = oldHot
  }
}

// 查看
const handleView = (row: any) => {
  currentService.value = { ...row }
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个服务吗？', '提示', {
      type: 'warning'
    })

    await deleteService(row.id)
    ElMessage.success('删除成功')
    loadServices()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 获取审核状态类型
const getApprovalStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取审核状态文本
const getApprovalStatusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 获取服务类型名称
const getServiceTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    'pest': '除虫除害',
    'repair': '维修服务',
    'setting-on': '安装设置',
    'setting-off': '拆卸设置'
  }
  return typeMap[type] || type
}
</script>

<style scoped lang="scss">
.service-manage-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  h2 {
    margin: 0 0 8px 0;
    color: #303133;
  }
  
  p {
    margin: 0;
    color: #606266;
    font-size: 14px;
  }
}

.stats-cards {
  margin-bottom: 20px;
  
  .stat-item {
    display: flex;
    align-items: center;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      margin-right: 16px;
      
      &.pending {
        background: #fef0f0;
        color: #f56c6c;
      }
    }
    
    .stat-content {
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  .toolbar-left {
    display: flex;
    gap: 10px;
  }
  
  .toolbar-right {
    display: flex;
    align-items: center;
  }
}

.table-container {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.service-name {
  display: flex;
  align-items: center;
}

.description-text {
  line-height: 1.5;
  color: #606266;
}

.price {
  font-weight: 600;
  color: #f56c6c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>

