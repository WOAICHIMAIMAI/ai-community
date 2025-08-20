<template>
  <div class="appointment-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>预约服务管理</h2>
      <p>管理社区预约服务记录，包括预约状态跟踪、工作人员分配和服务完成确认</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalAppointments }}</div>
              <div class="stat-label">总预约数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.pendingCount }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon confirmed">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.confirmedCount }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon progress">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.inProgressCount }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon completed">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.completedCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-icon cancelled">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.cancelledCount }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增预约
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-select
          v-model="queryParams.serviceType"
          placeholder="服务类型"
          clearable
          style="width: 140px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option
            v-for="type in serviceTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="预约状态"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option
            v-for="status in statusOptions"
            :key="status.value"
            :label="status.label"
            :value="status.value"
          />
        </el-select>
        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px; margin-right: 10px;"
          @change="handleDateRangeChange"
        />
        
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索用户名、手机号或地址"
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
        <el-table-column prop="id" label="预约ID" width="80" />
        
        <el-table-column prop="username" label="用户信息" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="username">{{ row.username }}</div>
              <div class="phone">{{ row.userPhone }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="serviceName" label="服务信息" min-width="180">
          <template #default="{ row }">
            <div class="service-info">
              <div class="service-name">{{ row.serviceName }}</div>
              <el-tag :type="getServiceTypeTagType(row.serviceType)" size="small">
                {{ getServiceTypeName(row.serviceType) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="appointmentTime" label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.appointmentTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="address" label="服务地址" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.address" placement="top">
              <div class="address-text">{{ truncateText(row.address, 30) }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="workerName" label="工作人员" width="120">
          <template #default="{ row }">
            <span v-if="row.workerName">{{ row.workerName }}</span>
            <span v-else class="no-worker">未分配</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="price" label="服务费用" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 查看按钮始终显示 -->
              <el-button type="primary" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              
              <!-- 根据状态显示不同的操作按钮组 -->
              <template v-if="row.status === 0">
                <!-- 待处理状态：确认、分配、取消 -->
                <el-button-group>
                  <el-button type="success" size="small" @click="handleConfirm(row)">
                    <el-icon><Select /></el-icon>
                    确认
                  </el-button>
                  <el-button type="info" size="small" @click="handleAssign(row)">
                    <el-icon><User /></el-icon>
                    分配
                  </el-button>
                </el-button-group>
                <el-button type="danger" size="small" @click="handleCancel(row)">
                  <el-icon><Close /></el-icon>
                  取消
                </el-button>
              </template>
              
              <template v-else-if="row.status === 1">
                <!-- 已确认状态：分配、取消 -->
                <el-button-group>
                  <el-button type="info" size="small" @click="handleAssign(row)">
                    <el-icon><User /></el-icon>
                    分配
                  </el-button>
                  <el-button type="danger" size="small" @click="handleCancel(row)">
                    <el-icon><Close /></el-icon>
                    取消
                  </el-button>
                </el-button-group>
              </template>
              
              <template v-else-if="row.status === 2">
                <!-- 进行中状态：完成 -->
                <el-button type="warning" size="small" @click="handleComplete(row)">
                  <el-icon><CircleCheck /></el-icon>
                  完成
                </el-button>
              </template>
              
              <!-- 已完成或已取消状态：只显示查看按钮 -->
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

    <!-- 新增/编辑预约弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <appointment-form
        ref="appointmentFormRef"
        :form-data="currentAppointment"
        @submit="handleFormSubmit"
      />
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="预约详情"
      width="900px"
    >
      <appointment-detail
        v-if="viewDialogVisible"
        :appointment="currentAppointment"
      />
    </el-dialog>

    <!-- 分配工作人员弹窗 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配工作人员"
      width="500px"
    >
      <worker-assign
        v-if="assignDialogVisible"
        :appointment="currentAppointment"
        @submit="handleAssignSubmit"
      />
    </el-dialog>

    <!-- 取消预约弹窗 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预约"
      width="500px"
    >
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="取消原因" required>
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleCancelSubmit">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Download, Search, Refresh, Calendar, Clock, Check, 
  Tools, CircleCheck, CircleClose, View, Select, Close, User
} from '@element-plus/icons-vue'
import {
  type AppointmentRecord,
  type AppointmentQueryParams,
  type AppointmentStats,
  AppointmentType,
  AppointmentStatus
} from '@/api/appointment'
import AppointmentForm from './components/AppointmentForm.vue'
import AppointmentDetail from './components/AppointmentDetail.vue'
import WorkerAssign from './components/WorkerAssign.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<AppointmentRecord[]>([])
const total = ref(0)
const stats = ref<AppointmentStats>({
  totalAppointments: 0,
  pendingCount: 0,
  confirmedCount: 0,
  inProgressCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  todayAppointments: 0,
  weekAppointments: 0,
  monthAppointments: 0
})

// 查询参数
const queryParams = reactive<AppointmentQueryParams>({
  serviceType: undefined,
  status: undefined,
  keyword: '',
  startDate: undefined,
  endDate: undefined,
  page: 1,
  pageSize: 20
})

const dateRange = ref<[Date, Date] | null>(null)

// 弹窗相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const currentAppointment = ref<Partial<AppointmentRecord>>({})
const appointmentFormRef = ref()

// 取消表单
const cancelForm = reactive({
  reason: ''
})

// 服务类型选项
const serviceTypes = [
  { value: AppointmentType.MAINTENANCE, label: '维修服务' },
  { value: AppointmentType.CLEANING, label: '保洁服务' },
  { value: AppointmentType.SECURITY, label: '安保服务' },
  { value: AppointmentType.DELIVERY, label: '快递代收' },
  { value: AppointmentType.OTHER, label: '其他服务' }
]

// 状态选项
const statusOptions = [
  { value: AppointmentStatus.PENDING, label: '待处理' },
  { value: AppointmentStatus.CONFIRMED, label: '已确认' },
  { value: AppointmentStatus.IN_PROGRESS, label: '进行中' },
  { value: AppointmentStatus.COMPLETED, label: '已完成' },
  { value: AppointmentStatus.CANCELLED, label: '已取消' }
]

// 计算属性
const dialogTitle = computed(() => {
  return currentAppointment.value.id ? '编辑预约' : '新增预约'
})

// 页面初始化
onMounted(() => {
  loadStats()
  loadAppointments()
})

// 加载统计数据
const loadStats = async () => {
  try {
    // 使用模拟数据
    stats.value = {
      totalAppointments: 128,
      pendingCount: 15,
      confirmedCount: 32,
      inProgressCount: 8,
      completedCount: 65,
      cancelledCount: 8,
      todayAppointments: 12,
      weekAppointments: 45,
      monthAppointments: 128
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载预约列表
const loadAppointments = async () => {
  try {
    loading.value = true

    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 使用模拟数据
    const mockData: AppointmentRecord[] = [
      {
        id: 1,
        userId: 1,
        username: '张三',
        userPhone: '13800138001',
        serviceId: 1,
        serviceName: '水管维修',
        serviceType: AppointmentType.MAINTENANCE,
        appointmentTime: '2024-01-20 14:00:00',
        address: '阳光小区1栋2单元301室',
        contactPhone: '13800138001',
        description: '厨房水管漏水，需要紧急维修',
        status: AppointmentStatus.PENDING,
        price: 150,
        createdAt: '2024-01-19 10:30:00',
        updatedAt: '2024-01-19 10:30:00'
      },
      {
        id: 2,
        userId: 2,
        username: '李四',
        userPhone: '13800138002',
        serviceId: 2,
        serviceName: '家庭保洁',
        serviceType: AppointmentType.CLEANING,
        appointmentTime: '2024-01-21 09:00:00',
        address: '阳光小区2栋1单元201室',
        contactPhone: '13800138002',
        description: '全屋深度清洁',
        status: AppointmentStatus.CONFIRMED,
        price: 200,
        workerId: 1,
        workerName: '王师傅',
        workerPhone: '13900139001',
        createdAt: '2024-01-19 11:00:00',
        updatedAt: '2024-01-19 15:30:00'
      },
      {
        id: 3,
        userId: 3,
        username: '王五',
        userPhone: '13800138003',
        serviceId: 3,
        serviceName: '电器维修',
        serviceType: AppointmentType.MAINTENANCE,
        appointmentTime: '2024-01-19 16:00:00',
        address: '阳光小区3栋3单元101室',
        contactPhone: '13800138003',
        description: '空调不制冷',
        status: AppointmentStatus.IN_PROGRESS,
        price: 300,
        workerId: 2,
        workerName: '刘师傅',
        workerPhone: '13900139002',
        createdAt: '2024-01-18 14:20:00',
        updatedAt: '2024-01-19 16:00:00'
      },
      {
        id: 4,
        userId: 4,
        username: '赵六',
        userPhone: '13800138004',
        serviceId: 4,
        serviceName: '快递代收',
        serviceType: AppointmentType.DELIVERY,
        appointmentTime: '2024-01-18 10:00:00',
        address: '阳光小区1栋1单元401室',
        contactPhone: '13800138004',
        description: '重要文件，需要本人签收',
        status: AppointmentStatus.COMPLETED,
        price: 10,
        workerId: 3,
        workerName: '陈师傅',
        workerPhone: '13900139003',
        createdAt: '2024-01-17 09:00:00',
        updatedAt: '2024-01-18 10:30:00',
        completedAt: '2024-01-18 10:30:00',
        rating: 5,
        feedback: '服务很好，很及时'
      },
      {
        id: 5,
        userId: 5,
        username: '孙七',
        userPhone: '13800138005',
        serviceId: 1,
        serviceName: '水管维修',
        serviceType: AppointmentType.MAINTENANCE,
        appointmentTime: '2024-01-22 15:00:00',
        address: '阳光小区2栋2单元102室',
        contactPhone: '13800138005',
        description: '卫生间水龙头漏水',
        status: AppointmentStatus.CANCELLED,
        price: 150,
        createdAt: '2024-01-19 16:00:00',
        updatedAt: '2024-01-19 17:00:00',
        cancelledAt: '2024-01-19 17:00:00',
        cancelReason: '用户临时有事，需要改期'
      }
    ]

    // 根据查询条件过滤数据
    let filteredData = mockData

    if (queryParams.serviceType !== undefined) {
      filteredData = filteredData.filter(item => item.serviceType === queryParams.serviceType)
    }

    if (queryParams.status !== undefined) {
      filteredData = filteredData.filter(item => item.status === queryParams.status)
    }

    if (queryParams.keyword) {
      const keyword = queryParams.keyword.toLowerCase()
      filteredData = filteredData.filter(item =>
        item.username.toLowerCase().includes(keyword) ||
        item.userPhone.includes(keyword) ||
        item.address.toLowerCase().includes(keyword)
      )
    }

    if (queryParams.startDate && queryParams.endDate) {
      filteredData = filteredData.filter(item => {
        const appointmentDate = new Date(item.appointmentTime)
        const startDate = new Date(queryParams.startDate!)
        const endDate = new Date(queryParams.endDate!)
        return appointmentDate >= startDate && appointmentDate <= endDate
      })
    }

    // 分页处理
    const start = (queryParams.page! - 1) * queryParams.pageSize!
    const end = start + queryParams.pageSize!
    const pageData = filteredData.slice(start, end)

    tableData.value = pageData
    total.value = filteredData.length

  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  loadAppointments()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    serviceType: undefined,
    status: undefined,
    keyword: '',
    startDate: undefined,
    endDate: undefined,
    page: 1,
    pageSize: 20
  })
  dateRange.value = null
  loadAppointments()
}

// 日期范围变更
const handleDateRangeChange = (dates: [Date, Date] | null) => {
  if (dates) {
    queryParams.startDate = dates[0].toISOString().split('T')[0]
    queryParams.endDate = dates[1].toISOString().split('T')[0]
  } else {
    queryParams.startDate = undefined
    queryParams.endDate = undefined
  }
  handleQuery()
}

// 新增
const handleCreate = () => {
  currentAppointment.value = {}
  dialogVisible.value = true
}

// 查看
const handleView = (row: AppointmentRecord) => {
  currentAppointment.value = { ...row }
  viewDialogVisible.value = true
}

// 确认预约
const handleConfirm = async (row: AppointmentRecord) => {
  try {
    await ElMessageBox.confirm('确定要确认这个预约吗？', '提示', {
      type: 'warning'
    })

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('预约确认成功')
    loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('预约确认失败')
    }
  }
}

// 分配工作人员
const handleAssign = (row: AppointmentRecord) => {
  currentAppointment.value = { ...row }
  assignDialogVisible.value = true
}

// 完成预约
const handleComplete = async (row: AppointmentRecord) => {
  try {
    await ElMessageBox.confirm('确定要将此预约标记为完成吗？', '提示', {
      type: 'warning'
    })

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('预约完成确认成功')
    loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('预约完成确认失败')
    }
  }
}

// 取消预约
const handleCancel = (row: AppointmentRecord) => {
  currentAppointment.value = { ...row }
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

// 导出数据
const handleExport = async () => {
  try {
    ElMessage.success('导出功能开发中...')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 表单提交
const handleFormSubmit = () => {
  dialogVisible.value = false
  loadAppointments()
}

// 分配提交
const handleAssignSubmit = () => {
  assignDialogVisible.value = false
  loadAppointments()
}

// 取消提交
const handleCancelSubmit = async () => {
  if (!cancelForm.reason.trim()) {
    ElMessage.error('请输入取消原因')
    return
  }

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('预约取消成功')
    cancelDialogVisible.value = false
    loadAppointments()
  } catch (error) {
    ElMessage.error('预约取消失败')
  }
}

// 弹窗关闭
const handleDialogClose = () => {
  currentAppointment.value = {}
}

// 获取服务类型名称
const getServiceTypeName = (type: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: '维修服务',
    [AppointmentType.CLEANING]: '保洁服务',
    [AppointmentType.SECURITY]: '安保服务',
    [AppointmentType.DELIVERY]: '快递代收',
    [AppointmentType.OTHER]: '其他服务'
  }
  return typeMap[type] || '未知'
}

// 获取服务类型标签类型
const getServiceTypeTagType = (type: AppointmentType) => {
  const typeMap = {
    [AppointmentType.MAINTENANCE]: 'danger',
    [AppointmentType.CLEANING]: 'success',
    [AppointmentType.SECURITY]: 'warning',
    [AppointmentType.DELIVERY]: 'info',
    [AppointmentType.OTHER]: ''
  }
  return typeMap[type] || ''
}

// 获取状态名称
const getStatusName = (status: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: '待处理',
    [AppointmentStatus.CONFIRMED]: '已确认',
    [AppointmentStatus.IN_PROGRESS]: '进行中',
    [AppointmentStatus.COMPLETED]: '已完成',
    [AppointmentStatus.CANCELLED]: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: AppointmentStatus) => {
  const statusMap = {
    [AppointmentStatus.PENDING]: 'warning',
    [AppointmentStatus.CONFIRMED]: 'primary',
    [AppointmentStatus.IN_PROGRESS]: 'info',
    [AppointmentStatus.COMPLETED]: 'success',
    [AppointmentStatus.CANCELLED]: 'danger'
  }
  return statusMap[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style scoped lang="scss">
.appointment-list-container {
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
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      .el-icon {
        font-size: 24px;
        color: #fff;
      }
      
      &.total {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.pending {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.confirmed {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      
      &.progress {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
      
      &.completed {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }
      
      &.cancelled {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      }
    }
    
    .stat-content {
      flex: 1;
      
      .stat-number {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
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

.user-info {
  .username {
    font-weight: 500;
    color: #303133;
    margin-bottom: 2px;
  }
  
  .phone {
    font-size: 12px;
    color: #909399;
  }
}

.service-info {
  .service-name {
    font-weight: 500;
    color: #303133;
    margin-bottom: 4px;
  }
}

.address-text {
  line-height: 1.5;
  color: #606266;
}

.no-worker {
  color: #c0c4cc;
  font-style: italic;
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
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  
  .el-button {
    margin: 0;
    
    &.el-button--small {
      padding: 5px 8px;
      font-size: 12px;
      
      .el-icon {
        margin-right: 2px;
      }
    }
  }
  
  .el-button-group {
    .el-button {
      border-radius: 0;
      
      &:first-child {
        border-top-left-radius: 4px;
        border-bottom-left-radius: 4px;
      }
      
      &:last-child {
        border-top-right-radius: 4px;
        border-bottom-right-radius: 4px;
      }
    }
  }
}
</style>
