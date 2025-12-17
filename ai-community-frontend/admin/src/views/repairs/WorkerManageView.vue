<template>
  <div class="worker-manage-container">
    <h2 class="page-title">维修工管理</h2>
    
    <!-- 绩效统计表格 -->
    <el-card shadow="never" class="stats-card">
      <template #header>
        <div class="card-header">
          <span>维修工绩效统计（Top 10）</span>
        </div>
      </template>
      
      <el-table
        v-loading="statsLoading"
        :data="workerStats"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <el-tag v-if="$index === 0" type="danger" effect="dark">🥇</el-tag>
            <el-tag v-else-if="$index === 1" type="warning" effect="dark">🥈</el-tag>
            <el-tag v-else-if="$index === 2" type="success" effect="dark">🥉</el-tag>
            <span v-else style="font-weight: 500">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="维修工" width="200">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 12px">
              <el-avatar :size="40" :src="row.avatar">{{ row.name?.charAt(0) }}</el-avatar>
              <span style="font-weight: 500">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="评分" width="180" align="center">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; justify-content: center; gap: 8px">
              <el-rate 
                :model-value="row.rating" 
                disabled 
                :colors="rateColors"
                size="small"
              />
              <span style="font-weight: 600; color: #f7ba2a">{{ row.rating?.toFixed(1) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="完成工单" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain">{{ row.completedCount }} 单</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="好评数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" effect="plain">{{ row.goodReviews }} 个</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="本月服务" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="plain">{{ row.monthlyServiceCount || 0 }} 单</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="平均完成时间" width="140" align="center">
          <template #default="{ row }">
            <span style="color: #606266">{{ row.avgCompletionTime }}小时</span>
          </template>
        </el-table-column>
        
        <el-table-column label="好评率" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.completedCount > 0 ? Math.round((row.goodReviews / row.completedCount) * 100) : 0"
              :color="getProgressColor(row.completedCount > 0 ? (row.goodReviews / row.completedCount) * 100 : 0)"
            />
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="workerStats.length === 0 && !statsLoading" description="暂无绩效数据" />
    </el-card>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/手机号" clearable />
        </el-form-item>
        <el-form-item label="技能">
          <el-select v-model="searchForm.skill" placeholder="全部" clearable style="width: 180px">
            <el-option v-for="item in skillOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作状态">
          <el-select v-model="searchForm.workStatus" placeholder="全部" clearable style="width: 180px">
            <el-option v-for="item in workStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
          <span>维修工列表</span>
          <div class="header-operations">
            <el-button type="primary" @click="handleAddWorker">
              <el-icon><plus /></el-icon>添加维修工
            </el-button>
          </div>
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
        <el-table-column label="维修工" width="200">
          <template #default="{ row }">
            <div class="worker-avatar-info">
              <el-avatar :size="36" :src="row.avatarUrl">{{ row.name?.charAt(0) }}</el-avatar>
              <div class="worker-basic-info">
                <div class="name">{{ row.name }}</div>
                <div class="phone">{{ row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled :colors="rateColors" />
            <span class="rating-text">{{ row.rating.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="服务类型" width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="(type, index) in getServiceTypeLabels(row.serviceType)" 
              :key="index"
              style="margin-right: 5px; margin-bottom: 5px"
              size="small"
            >
              {{ type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="员工添加日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="工作状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getWorkStatusType(row.workStatus)">
              {{ getWorkStatusText(row.workStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditWorker(row)">
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeleteWorker(row)"
            >
              删除
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
    
    <!-- 添加/编辑维修工表单 -->
    <el-dialog
      v-model="formVisible"
      :title="formType === 'add' ? '添加维修工' : '编辑维修工'"
      width="50%"
      destroy-on-close
    >
      <el-form
        ref="workerFormRef"
        :model="workerForm"
        :rules="workerRules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="workerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="workerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNumber">
          <el-input v-model="workerForm.idCardNumber" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select
            v-model="workerForm.serviceType"
            multiple
            filterable
            collapse-tags
            placeholder="请选择服务类型"
            style="width: 100%"
          >
            <el-option label="水电维修" value="water_electricity" />
            <el-option label="家具维修" value="furniture" />
            <el-option label="门窗维修" value="doors_windows" />
            <el-option label="墙面维修" value="walls" />
            <el-option label="电器维修" value="appliances" />
            <el-option label="管道疏通" value="plumbing" />
            <el-option label="安装服务" value="installation" />
          </el-select>
        </el-form-item>
        <el-form-item label="个人介绍">
          <el-input
            v-model="workerForm.introduction"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入个人介绍"
          />
        </el-form-item>
        <el-form-item label="工作状态" prop="workStatus">
          <el-select 
            v-model="workerForm.workStatus" 
            placeholder="请选择工作状态"
            style="width: 100%"
          >
            <el-option 
              v-for="item in workStatusOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value"
            >
              <span style="float: left">{{ item.label }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ item.value === 0 ? '不接新单' : item.value === 1 ? '可接新单' : '处理中' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWorkerForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getRepairWorkerList, updateWorkerStatus, getAllWorkerStats, deleteWorker, addWorker, updateWorker } from '@/api/repair'
import type { RepairWorker, WorkerStatusParams, WorkerStatsVO } from '@/api/repair'

// 评分颜色
const rateColors = ['#99A9BF', '#F7BA2A', '#FF9900']

// 技能选项（使用英文代码作为value，与后端一致）
const skillOptions = [
  { value: 'water_electricity', label: '水电维修' },
  { value: 'furniture', label: '家具维修' },
  { value: 'doors_windows', label: '门窗维修' },
  { value: 'walls', label: '墙面维修' },
  { value: 'appliances', label: '电器维修' },
  { value: 'plumbing', label: '管道疏通' },
  { value: 'installation', label: '安装服务' }
]

// 工作状态选项
const workStatusOptions = [
  { value: 0, label: '休息' },
  { value: 1, label: '可接单' },
  { value: 2, label: '忙碌' }
]

// 搜索表单
const searchForm = reactive({
  keyword: '',
  skill: '',
  workStatus: null as number | null
})

// 表格数据
const tableData = ref<RepairWorker[]>([])
const tableLoading = ref(false)

// 绩效统计
const workerStats = ref<WorkerStatsVO[]>([])
const statsLoading = ref(false)

// 分页参数
const pageParams = reactive({
  page: 1,
  pageSize: 10
})
const total = ref(0)

// 表单相关
const formType = ref<'add' | 'edit'>('add')
const formVisible = ref(false)
const workerFormRef = ref<FormInstance>()
const avatarUrl = ref('')
const workerForm = reactive<{
  id: number
  name: string
  phone: string
  avatarUrl: string
  serviceType: string | string[]  // 服务类型，后端需要字符串（逗号分隔），前端表单用数组
  idCardNumber: string
  introduction: string
  workStatus: number
}>({
  id: 0,
  name: '',
  phone: '',
  avatarUrl: '',
  serviceType: [],  // 前端表单用数组
  idCardNumber: '',  // 身份证号
  introduction: '',  // 个人介绍
  workStatus: 1  // 工作状态：0-休息 1-可接单 2-忙碌，默认为可接单
})

// 表单验证规则
const workerRules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  serviceType: [
    { required: true, message: '请输入服务类型', trigger: 'blur' }
  ],
  idCardNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入有效的身份证号', trigger: 'blur' }
  ],
  workStatus: [
    { required: true, message: '请选择工作状态', trigger: 'change' }
  ]
}

// 加载维修工列表
const loadWorkerList = async () => {
  try {
    tableLoading.value = true
    const params: any = {
      ...pageParams
    }
    
    // 处理关键词搜索：将keyword映射到name字段
    if (searchForm.keyword) {
      params.name = searchForm.keyword
    }
    
    // 处理技能搜索：将skill映射到serviceType字段
    if (searchForm.skill) {
      params.serviceType = searchForm.skill
    }
    
    // 处理工作状态
    if (searchForm.workStatus !== null && searchForm.workStatus !== undefined) {
      params.workStatus = searchForm.workStatus
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getRepairWorkerList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取维修工列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取维修工列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 加载维修工绩效统计
const loadWorkerStats = async () => {
  try {
    statsLoading.value = true
    // 获取所有维修工的绩效统计，默认获取前10名
    const res = await getAllWorkerStats(10)
    
    if (res.code === 200) {
      // 将后端返回的 workerId 映射为前端需要的 id
      workerStats.value = res.data.map((item: any) => ({
        ...item,
        id: item.workerId || item.id
      }))
    } else {
      ElMessage.error(res.message || '获取绩效统计失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取绩效统计失败')
  } finally {
    statsLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.page = 1
  loadWorkerList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.skill = ''
  searchForm.workStatus = null
  handleSearch()
}

// 添加维修工
const handleAddWorker = () => {
  formType.value = 'add'
  workerForm.id = 0
  workerForm.name = ''
  workerForm.phone = ''
  workerForm.avatarUrl = ''
  workerForm.serviceType = []
  workerForm.idCardNumber = ''
  workerForm.introduction = ''
  workerForm.workStatus = 1
  avatarUrl.value = ''
  formVisible.value = true
}

// 编辑维修工
const handleEditWorker = (row: RepairWorker) => {
  formType.value = 'edit'
  workerForm.id = row.id
  workerForm.name = row.name
  workerForm.phone = row.phone
  workerForm.avatarUrl = row.avatarUrl || ''
  // serviceType如果是字符串需要转换为数组用于el-select多选
  workerForm.serviceType = row.serviceType ? row.serviceType.split(',') : []
  workerForm.idCardNumber = row.idCardNumber || ''
  workerForm.introduction = row.introduction || ''
  workerForm.workStatus = row.workStatus || 1
  avatarUrl.value = row.avatarUrl || ''
  formVisible.value = true
}

// 切换维修工状态
const handleToggleStatus = (row: RepairWorker) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '停用'
  
  ElMessageBox.confirm(
    `确定要将维修工 "${row.name}" ${actionText}吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const params: WorkerStatusParams = {
        workerId: row.id,
        status: newStatus
      }
      
      const res = await updateWorkerStatus(params)
      
      if (res.code === 200 && res.data) {
        ElMessage.success(`${actionText}成功`)
        loadWorkerList()
      } else {
        ElMessage.error(res.message || `${actionText}失败`)
      }
    } catch (error: any) {
      ElMessage.error(error.message || `${actionText}失败`)
    }
  }).catch(() => {
    // 取消操作
  })
}

// 删除维修工
const handleDeleteWorker = (row: RepairWorker) => {
  ElMessageBox.confirm(
    `确定要删除维修工 "${row.name}" 吗？删除后将无法恢复！`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    }
  ).then(async () => {
    try {
      const res = await deleteWorker(row.id)
      
      if (res.code === 200) {
        ElMessage.success('删除成功')
        // 刷新列表
        loadWorkerList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 头像变更
const handleAvatarChange = (file: any) => {
  // 实际项目中应该上传到服务器
  avatarUrl.value = URL.createObjectURL(file.raw)
  workerForm.avatarUrl = avatarUrl.value
}

// 提交表单
const submitWorkerForm = () => {
  if (!workerFormRef.value) return
  
  workerFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 准备数据，serviceType需要转换为逗号分隔的字符串
        const submitData = {
          ...workerForm,
          serviceType: Array.isArray(workerForm.serviceType) 
            ? workerForm.serviceType.join(',') 
            : workerForm.serviceType
        }
        
        if (formType.value === 'add') {
          // 添加维修工
          delete submitData.id  // 添加时不需要传id
          await addWorker(submitData)
          ElMessage.success('添加维修工成功')
        } else {
          // 更新维修工
          await updateWorker(submitData)
          ElMessage.success('更新维修工信息成功')
        }
        
        formVisible.value = false
        loadWorkerList()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageParams.pageSize = size
  loadWorkerList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.page = page
  loadWorkerList()
}

// 格式化日期时间
const formatDateTime = (timestamp: number | string | undefined): string => {
  if (!timestamp) return '-'
  
  const time = typeof timestamp === 'string' ? parseInt(timestamp) : timestamp
  const date = new Date(time)
  
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取工作状态文本
const getWorkStatusText = (workStatus: number | undefined): string => {
  if (workStatus === undefined || workStatus === null) return '未知'
  
  switch (workStatus) {
    case 0:
      return '休息'
    case 1:
      return '可接单'
    case 2:
      return '忙碌'
    default:
      return '未知'
  }
}

// 获取工作状态标签类型
const getWorkStatusType = (workStatus: number | undefined): 'success' | 'warning' | 'danger' | 'info' => {
  if (workStatus === undefined || workStatus === null) return 'info'
  
  switch (workStatus) {
    case 0:
      return 'info'     // 休息 - 灰色
    case 1:
      return 'success'  // 可接单 - 绿色
    case 2:
      return 'warning'  // 忙碌 - 橙色
    default:
      return 'info'
  }
}

// 服务类型映射（英文代码 -> 中文标签）
const serviceTypeMap: Record<string, string> = {
  'water_electricity': '水电维修',
  'furniture': '家具维修',
  'doors_windows': '门窗维修',
  'walls': '墙面维修',
  'appliances': '电器维修',
  'plumbing': '管道疏通',
  'installation': '安装服务'
}

// 将服务类型字符串转换为中文标签数组
const getServiceTypeLabels = (serviceType: string | undefined): string[] => {
  if (!serviceType) return []
  
  // 如果是逗号分隔的字符串，拆分后转换
  return serviceType.split(',')
    .map(type => type.trim())
    .filter(type => type)
    .map(type => serviceTypeMap[type] || type)
}

// 根据好评率获取进度条颜色
const getProgressColor = (percentage: number): string => {
  if (percentage >= 90) return '#67c23a'  // 绿色 - 优秀
  if (percentage >= 80) return '#e6a23c'  // 橙色 - 良好
  if (percentage >= 70) return '#f56c6c'  // 红色 - 一般
  return '#909399'  // 灰色 - 较差
}

// 组件挂载后加载数据
onMounted(() => {
  loadWorkerList()
  loadWorkerStats()
})
</script>

<style scoped lang="scss">
.worker-manage-container {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: var(--text-color);
  }
  
  .stats-card {
    margin-bottom: 20px;
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
  
  .worker-avatar-info {
    display: flex;
    align-items: center;
    
    .worker-basic-info {
      margin-left: 10px;
      
      .name {
        font-weight: 500;
        margin-bottom: 3px;
      }
      
      .phone {
        font-size: 12px;
        color: var(--text-color-secondary);
      }
    }
  }
  
  .skill-tag {
    margin-right: 5px;
    margin-bottom: 5px;
  }
  
  .rating-text {
    margin-left: 5px;
    font-size: 13px;
    color: var(--text-color-secondary);
  }
  
  .avatar-uploader {
    width: 100px;
    height: 100px;
    border: 1px dashed var(--border-color);
    border-radius: var(--border-radius);
    cursor: pointer;
    position: relative;
    overflow: hidden;
    
    .avatar {
      width: 100%;
      height: 100%;
      display: block;
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100%;
      height: 100%;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
}
</style> 