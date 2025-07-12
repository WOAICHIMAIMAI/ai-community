<template>
  <div class="repair-order-container">
    <h2 class="page-title">报修工单管理</h2>
    
    <!-- 工单统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card total">
          <div class="stats-value">{{ orderStats.total }}</div>
          <div class="stats-label">总工单数</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card pending">
          <div class="stats-value">{{ orderStats.pending }}</div>
          <div class="stats-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card processing">
          <div class="stats-value">{{ orderStats.processing }}</div>
          <div class="stats-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card completed">
          <div class="stats-value">{{ orderStats.completed }}</div>
          <div class="stats-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card cancelled">
          <div class="stats-value">{{ orderStats.cancelled }}</div>
          <div class="stats-label">已取消</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stats-card today">
          <div class="stats-value">{{ orderStats.todayNew }}</div>
          <div class="stats-label">今日新增</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="工单号">
          <el-input v-model="searchForm.orderId" placeholder="工单编号" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="用户名/手机号" clearable />
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="searchForm.repairType" placeholder="全部" clearable>
            <el-option v-for="item in repairTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度">
          <el-select v-model="searchForm.urgencyLevel" placeholder="全部" clearable>
            <el-option v-for="item in urgencyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><refresh-right /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>工单列表</span>
        </div>
      </template>
      
      <el-table
        v-loading="tableLoading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="id" label="工单号" width="80" />
        <el-table-column prop="repairTypeName" label="报修类型" width="120" />
        <el-table-column prop="description" label="问题描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户信息" width="180">
          <template #default="{ row }">
            {{ row.username }}<br />
            {{ row.userPhone }}
          </template>
        </el-table-column>
        <el-table-column label="维修工" width="180">
          <template #default="{ row }">
            <template v-if="row.workerId">
              {{ row.workerName }}<br />
              {{ row.workerPhone }}
            </template>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="urgencyLevel" label="紧急度" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.urgencyLevel === 3 ? 'danger' : row.urgencyLevel === 2 ? 'warning' : 'info'"
              :effect="row.urgencyLevel === 3 ? 'dark' : 'light'"
            >
              {{ getUrgencyText(row.urgencyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleAssignWorker(row.id)"
            >
              分配
            </el-button>
            <el-button 
              v-if="row.status !== 3 && row.status !== 4" 
              type="warning" 
              size="small" 
              @click="handleAddProgress(row.id)"
            >
              更新进度
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="工单详情"
      width="70%"
      destroy-on-close
    >
      <div class="order-detail" v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单号">{{ currentOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="报修类型">{{ currentOrder.repairTypeName }}</el-descriptions-item>
          <el-descriptions-item label="用户信息">
            {{ currentOrder.username }} ({{ currentOrder.userPhone }})
          </el-descriptions-item>
          <el-descriptions-item label="报修地址">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag 
              :type="currentOrder.urgencyLevel === 3 ? 'danger' : currentOrder.urgencyLevel === 2 ? 'warning' : 'info'"
              :effect="currentOrder.urgencyLevel === 3 ? 'dark' : 'light'"
            >
              {{ getUrgencyText(currentOrder.urgencyLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="维修工信息">
            <template v-if="currentOrder.workerId">
              {{ currentOrder.workerName }} ({{ currentOrder.workerPhone }})
            </template>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.createdTime }}</el-descriptions-item>
          <el-descriptions-item label="期望上门时间">{{ currentOrder.expectedTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">{{ currentOrder.description }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="order-images" v-if="currentOrder.images && currentOrder.images.length > 0">
          <h4>问题图片</h4>
          <div class="image-list">
            <div v-for="(image, index) in currentOrder.images" :key="index" class="image-item">
              <el-image
                :src="image"
                :preview-src-list="currentOrder.images"
                fit="cover"
              />
            </div>
          </div>
        </div>
        
        <div class="progress-timeline">
          <h4>进度记录</h4>
          <el-timeline v-if="progressList.length > 0">
            <el-timeline-item
              v-for="(progress, index) in progressList"
              :key="progress.id"
              :type="getProgressItemType(progress.status)"
              :icon="getProgressIcon(progress.status)"
              :color="getProgressColor(progress.status)"
              :timestamp="progress.createdTime"
            >
              <h5>{{ getProgressTitle(progress.status) }}</h5>
              <p class="progress-operator">操作人: {{ progress.operatorName }} ({{ getOperatorType(progress.operatorType) }})</p>
              <p class="progress-remark">{{ progress.remark }}</p>
              
              <div class="progress-images" v-if="progress.images && progress.images.length > 0">
                <div v-for="(image, imgIndex) in progress.images" :key="`${progress.id}-${imgIndex}`" class="progress-image-item">
                  <el-image
                    :src="image"
                    :preview-src-list="progress.images"
                    fit="cover"
                  />
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无进度记录" />
        </div>
      </div>
    </el-dialog>
    
    <!-- 分配维修工表单 -->
    <el-dialog
      v-model="assignFormVisible"
      title="分配维修工"
      width="60%"
      destroy-on-close
    >
      <div class="worker-assignment">
        <el-form :model="assignForm" label-width="100px">
          <el-form-item label="工单号">
            <span>{{ assignForm.orderId }}</span>
          </el-form-item>
          <el-form-item label="选择维修工">
            <el-select 
              v-model="assignForm.workerId" 
              placeholder="请选择维修工" 
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="worker in availableWorkers"
                :key="worker.id"
                :label="`${worker.name} (在接单数: ${worker.ongoingOrders})`"
                :value="worker.id"
              >
                <div class="worker-option">
                  <el-avatar :size="24" :src="worker.avatar">{{ worker.name?.substr(0, 1) }}</el-avatar>
                  <div class="worker-info">
                    <div>{{ worker.name }} ({{ worker.phone }})</div>
                    <div class="worker-skills">
                      <el-tag v-for="(skill, index) in worker.skills" :key="index" size="small" type="success" effect="plain" class="skill-tag">
                        {{ skill }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="worker-rating">
                    <el-rate v-model="worker.rating" disabled :colors="rateColors" />
                  </div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="assignFormVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!assignForm.workerId" @click="confirmAssign">确认分配</el-button>
      </template>
    </el-dialog>
    
    <!-- 添加进度表单 -->
    <el-dialog
      v-model="progressFormVisible"
      title="更新工单进度"
      width="60%"
      destroy-on-close
    >
      <div class="progress-form">
        <el-form
          ref="progressFormRef"
          :model="progressForm"
          :rules="progressRules"
          label-width="100px"
        >
          <el-form-item label="工单号">
            <span>{{ progressForm.orderId }}</span>
          </el-form-item>
          <el-form-item label="状态更新" prop="status">
            <el-select v-model="progressForm.status" placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="option in progressStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
                :disabled="option.disabled"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="备注说明" prop="remark">
            <el-input
              v-model="progressForm.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入备注说明"
            />
          </el-form-item>
          <el-form-item label="上传图片">
            <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="5"
              accept="image/*"
              :file-list="progressImageList"
              @change="handleImageChange"
            >
              <el-icon><plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="progressFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProgress">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { Check, SetUp, Finished, WarningFilled, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getRepairOrderList, getRepairOrderDetail, getRepairProgressList, assignRepairWorker, 
  addRepairProgress, getRepairWorkerList, getOrderStats } from '@/api/repair'
import type { RepairOrder, RepairProgress, RepairWorker, OrderStatsVO } from '@/api/repair'

// 报修类型选项
const repairTypeOptions = ref([
  { value: 1, label: '水电维修' },
  { value: 2, label: '家具维修' },
  { value: 3, label: '门窗维修' },
  { value: 4, label: '墙面维修' },
  { value: 5, label: '电器维修' },
  { value: 6, label: '管道疏通' },
  { value: 7, label: '其他' }
])

// 状态选项
const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '已分配' },
  { value: 2, label: '处理中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已取消' }
]

// 紧急程度选项
const urgencyOptions = [
  { value: 1, label: '一般' },
  { value: 2, label: '紧急' },
  { value: 3, label: '非常紧急' }
]

// 评分颜色
const rateColors = ['#99A9BF', '#F7BA2A', '#FF9900']

// 搜索表单
const searchForm = reactive({
  orderId: '',
  username: '',
  repairType: null as number | null,
  status: null as number | null,
  urgencyLevel: null as number | null
})

// 表格数据
const tableData = ref<RepairOrder[]>([])
const tableLoading = ref(false)

// 分页参数
const pageParams = reactive({
  page: 1,
  pageSize: 10
})
const total = ref(0)

// 工单统计数据
const orderStats = reactive<OrderStatsVO>({
  total: 0,
  pending: 0,
  processing: 0,
  completed: 0,
  cancelled: 0,
  todayNew: 0,
  todayCompleted: 0
})

// 工单详情
const detailVisible = ref(false)
const currentOrder = ref<RepairOrder | null>(null)
const progressList = ref<RepairProgress[]>([])

// 分配表单
const assignFormVisible = ref(false)
const assignForm = reactive({
  orderId: 0,
  workerId: null as number | null
})
const availableWorkers = ref<RepairWorker[]>([])

// 进度表单
const progressFormRef = ref<FormInstance>()
const progressFormVisible = ref(false)
const progressForm = reactive({
  orderId: 0,
  status: null as number | null,
  remark: ''
})
const progressImageList = ref<any[]>([])
const progressRules: FormRules = {
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入备注说明', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 进度状态选项（会根据当前工单状态动态生成）
const progressStatusOptions = ref<{value: number, label: string, disabled: boolean}[]>([])

// 获取紧急度文本
const getUrgencyText = (urgency: number): string => {
  switch (urgency) {
    case 1:
      return '一般'
    case 2:
      return '紧急'
    case 3:
      return '非常紧急'
    default:
      return '未知'
  }
}

// 获取状态标签类型
const getStatusType = (status: number): string => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'primary'
    case 3:
      return 'success'
    case 4:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: number): string => {
  switch (status) {
    case 0:
      return '待处理'
    case 1:
      return '已分配'
    case 2:
      return '处理中'
    case 3:
      return '已完成'
    case 4:
      return '已取消'
    default:
      return '未知'
  }
}

// 获取进度记录标题
const getProgressTitle = (status: number): string => {
  switch (status) {
    case 0:
      return '创建工单'
    case 1:
      return '分配维修工'
    case 2:
      return '开始处理'
    case 3:
      return '完成维修'
    case 4:
      return '取消工单'
    case 5:
      return '维修延期'
    default:
      return '状态更新'
  }
}

// 获取进度记录图标
const getProgressIcon = (status: number) => {
  switch (status) {
    case 0:
      return ''
    case 1:
      return SetUp
    case 2:
      return SetUp
    case 3:
      return CircleCheck
    case 4:
      return WarningFilled
    case 5:
      return ''
    default:
      return ''
  }
}

// 获取进度记录颜色
const getProgressColor = (status: number): string => {
  switch (status) {
    case 0:
      return '#909399'
    case 1:
      return '#E6A23C'
    case 2:
      return '#409EFF'
    case 3:
      return '#67C23A'
    case 4:
      return '#F56C6C'
    case 5:
      return '#909399'
    default:
      return '#909399'
  }
}

// 获取进度记录项类型
const getProgressItemType = (status: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'warning'
    case 2:
      return 'primary'
    case 3:
      return 'success'
    case 4:
      return 'danger'
    case 5:
      return 'info'
    default:
      return 'info'
  }
}

// 获取操作人类型文本
const getOperatorType = (type: number): string => {
  switch (type) {
    case 1:
      return '用户'
    case 2:
      return '管理员'
    case 3:
      return '维修工'
    default:
      return '未知'
  }
}

// 加载工单列表
const loadOrderList = async () => {
  try {
    tableLoading.value = true
    const params = {
      ...pageParams,
      ...searchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getRepairOrderList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取工单列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取工单列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载工单统计数据
const loadOrderStats = async () => {
  try {
    const res = await getOrderStats()
    
    if (res.code === 200) {
      console.log('工单统计数据:', res.data)
      
      // 映射后端字段到前端模型
      // 后端字段直接赋值
      Object.assign(orderStats, res.data)
      
      // 同时保证前端展示字段正确
      orderStats.total = res.data.totalCount || 0
      orderStats.pending = res.data.pendingCount || 0
      orderStats.processing = res.data.processingCount || 0
      orderStats.completed = res.data.completedCount || 0
      orderStats.cancelled = res.data.cancelledCount || 0
      orderStats.todayNew = res.data.todayCount || 0
      orderStats.todayCompleted = res.data.todayCompletedCount || 0
    } else {
      ElMessage.error(res.message || '获取统计数据失败')
    }
  } catch (error: any) {
    console.error('获取工单统计数据错误:', error)
    ElMessage.error(error.message || '获取统计数据失败')
  }
}

// 加载报修类型选项
const loadRepairTypeOptions = () => {
  // 这里通常应该从后端API获取报修类型选项
  // 如果没有对应API，可以使用静态数据
  repairTypeOptions.value = [
    { value: 1, label: '水电维修' },
    { value: 2, label: '家具维修' },
    { value: 3, label: '门窗维修' },
    { value: 4, label: '电器维修' },
    { value: 5, label: '其他' }
  ]
}

// 搜索
const handleSearch = () => {
  pageParams.page = 1
  loadOrderList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.orderId = ''
  searchForm.username = ''
  searchForm.repairType = null
  searchForm.status = null
  searchForm.urgencyLevel = null
  handleSearch()
}

// 查看工单详情
const handleViewDetail = async (orderId: number) => {
  try {
    const [orderRes, progressRes] = await Promise.all([
      getRepairOrderDetail(orderId),
      getRepairProgressList(orderId)
    ])
    
    if (orderRes.code === 200) {
      currentOrder.value = orderRes.data
    } else {
      ElMessage.error(orderRes.message || '获取工单详情失败')
      return
    }
    
    if (progressRes.code === 200) {
      progressList.value = progressRes.data
    } else {
      progressList.value = []
      ElMessage.warning(progressRes.message || '获取进度记录失败')
    }
    
    detailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取工单详情失败')
  }
}

// 分配维修工
const handleAssignWorker = async (orderId: number) => {
  try {
    assignForm.orderId = orderId
    assignForm.workerId = null
    
    // 获取可用维修工列表
    const res = await getRepairWorkerList({
      page: 1,
      pageSize: 100,
      status: 1 // 1=在职状态
    })
    
    if (res.code === 200) {
      availableWorkers.value = res.data.records
      if (availableWorkers.value.length === 0) {
        ElMessage.warning('当前没有可用的维修工')
        return
      }
    } else {
      ElMessage.error(res.message || '获取维修工列表失败')
      return
    }
    
    assignFormVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取维修工列表失败')
  }
}

// 确认分配维修工
const confirmAssign = async () => {
  if (!assignForm.workerId) {
    ElMessage.warning('请选择维修工')
    return
  }
  
  try {
    const res = await assignRepairWorker({
      orderId: assignForm.orderId,
      workerId: assignForm.workerId
    })
    
    if (res.code === 200 && res.data) {
      ElMessage.success('分配维修工成功')
      assignFormVisible.value = false
      loadOrderList()
      loadOrderStats()
    } else {
      ElMessage.error(res.message || '分配维修工失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '分配维修工失败')
  }
}

// 添加进度
const handleAddProgress = async (orderId: number) => {
  try {
    // 获取工单详情，确定当前状态
    const res = await getRepairOrderDetail(orderId)
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '获取工单详情失败')
      return
    }
    
    const order = res.data
    progressForm.orderId = orderId
    progressForm.status = null
    progressForm.remark = ''
    progressImageList.value = []
    
    // 根据当前状态设置可选的进度状态
    switch (order.status) {
      case 0: // 待处理
        progressStatusOptions.value = [
          { value: 1, label: '分配维修工', disabled: false },
          { value: 4, label: '取消工单', disabled: false }
        ]
        break
      case 1: // 已分配
        progressStatusOptions.value = [
          { value: 2, label: '开始处理', disabled: false },
          { value: 4, label: '取消工单', disabled: false },
          { value: 5, label: '维修延期', disabled: false }
        ]
        break
      case 2: // 处理中
        progressStatusOptions.value = [
          { value: 3, label: '完成维修', disabled: false },
          { value: 5, label: '维修延期', disabled: false }
        ]
        break
      default:
        progressStatusOptions.value = []
        ElMessage.warning('当前工单状态不支持添加进度')
        return
    }
    
    progressFormVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 处理图片变化
const handleImageChange = (file: any, fileList: any[]) => {
  progressImageList.value = fileList
}

// 提交进度
const submitProgress = () => {
  if (!progressFormRef.value) return
  
  progressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 实际项目中应该先上传图片，获取图片URL
        const imageUrls = progressImageList.value.map(item => URL.createObjectURL(item.raw))
        
        const res = await addRepairProgress({
          orderId: progressForm.orderId,
          status: progressForm.status as number,
          remark: progressForm.remark,
          images: imageUrls
        })
        
        if (res.code === 200 && res.data) {
          ElMessage.success('添加进度成功')
          progressFormVisible.value = false
          loadOrderList()
          loadOrderStats()
        } else {
          ElMessage.error(res.message || '添加进度失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '添加进度失败')
      }
    }
  })
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageParams.pageSize = size
  loadOrderList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.page = page
  loadOrderList()
}

// 初始化
onMounted(() => {
  // 加载报修类型选项
  loadRepairTypeOptions()
  
  // 加载工单列表
  loadOrderList()
  
  // 加载工单统计
  loadOrderStats()
})
</script>

<style scoped lang="scss">
.repair-order-container {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: var(--text-color);
  }
  
  .stats-row {
    margin-bottom: 20px;
    
    .stats-card {
      height: 100px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .stats-value {
        font-size: 28px;
        font-weight: 600;
        margin-bottom: 5px;
      }
      
      .stats-label {
        font-size: 14px;
        color: var(--text-color-secondary);
      }
      
      &.total .stats-value {
        color: #409EFF;
      }
      
      &.pending .stats-value {
        color: #E6A23C;
      }
      
      &.processing .stats-value {
        color: #409EFF;
      }
      
      &.completed .stats-value {
        color: #67C23A;
      }
      
      &.cancelled .stats-value {
        color: #F56C6C;
      }
      
      &.today .stats-value {
        color: #909399;
      }
    }
  }
  
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .order-detail {
    .order-images {
      margin-top: 20px;
      
      h4 {
        margin-bottom: 10px;
        font-size: 16px;
        font-weight: 500;
      }
      
      .image-list {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        
        .image-item {
          width: 150px;
          height: 150px;
          border-radius: var(--border-radius);
          overflow: hidden;
          
          .el-image {
            width: 100%;
            height: 100%;
          }
        }
      }
    }
    
    .progress-timeline {
      margin-top: 30px;
      
      h4 {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 500;
      }
      
      h5 {
        font-size: 16px;
        margin: 0 0 8px;
      }
      
      .progress-operator {
        font-size: 13px;
        color: var(--text-color-secondary);
        margin-bottom: 8px;
      }
      
      .progress-remark {
        margin-bottom: 10px;
        white-space: pre-wrap;
      }
      
      .progress-images {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .progress-image-item {
          width: 80px;
          height: 80px;
          border-radius: var(--border-radius);
          overflow: hidden;
          
          .el-image {
            width: 100%;
            height: 100%;
          }
        }
      }
    }
  }
  
  .worker-assignment {
    .worker-option {
      display: flex;
      align-items: center;
      
      .worker-info {
        flex: 1;
        margin: 0 10px;
        
        .worker-skills {
          margin-top: 4px;
          
          .skill-tag {
            margin-right: 5px;
            margin-bottom: 5px;
          }
        }
      }
    }
  }
}
</style> 