<template>
  <div class="worker-manage-container">
    <h2 class="page-title">维修工管理</h2>
    
    <!-- 绩效统计卡片 -->
    <el-card shadow="never" class="stats-card">
      <template #header>
        <div class="card-header">
          <span>维修工绩效统计</span>
        </div>
      </template>
      
      <div class="worker-stats">
        <div class="stats-list" v-loading="statsLoading">
          <el-empty v-if="workerStats.length === 0" description="暂无数据" />
          <div class="stats-item" v-for="worker in workerStats" :key="worker.id">
            <div class="worker-avatar">
              <el-avatar :size="60" :src="worker.avatar">{{ worker.name?.charAt(0) }}</el-avatar>
            </div>
            <div class="worker-info">
              <h3 class="name">{{ worker.name }}</h3>
              <div class="rating">
                <span>评分：</span>
                <el-rate v-model="worker.rating" disabled :colors="rateColors" />
                <span class="rating-value">{{ worker.rating.toFixed(1) }}</span>
              </div>
              <div class="metrics">
                <div class="metric-item">
                  <div class="label">完成工单</div>
                  <div class="value">{{ worker.completedCount }}</div>
                </div>
                <div class="metric-item">
                  <div class="label">好评数</div>
                  <div class="value">{{ worker.goodReviews }}</div>
                </div>
                <div class="metric-item">
                  <div class="label">平均完成时间</div>
                  <div class="value">{{ worker.avgCompletionTime }}小时</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/手机号" clearable />
        </el-form-item>
        <el-form-item label="技能">
          <el-select v-model="searchForm.skill" placeholder="全部" clearable>
            <el-option v-for="item in skillOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-table-column label="技能" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="(skill, index) in row.skills"
              :key="index"
              class="skill-tag"
              type="success"
              effect="plain"
            >
              {{ skill }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled :colors="rateColors" />
            <span class="rating-text">{{ row.rating.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completedOrders" label="已完成" width="100" />
        <el-table-column prop="ongoingOrders" label="进行中" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditWorker(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.status === 1 ? 'danger' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '离职' : '在职' }}
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
        <el-form-item label="技能" prop="skills">
          <el-select
            v-model="workerForm.skills"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或创建技能标签"
            style="width: 100%"
          >
            <el-option
              v-for="item in skillOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="workerForm.status">
            <el-radio :label="1">在职</el-radio>
            <el-radio :label="0">离职</el-radio>
          </el-radio-group>
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
import { getRepairWorkerList, updateWorkerStatus, getWorkerStats } from '@/api/repair'
import type { RepairWorker, WorkerStatusParams, WorkerStatsVO } from '@/api/repair'

// 评分颜色
const rateColors = ['#99A9BF', '#F7BA2A', '#FF9900']

// 技能选项
const skillOptions = [
  { value: '水电维修', label: '水电维修' },
  { value: '家具维修', label: '家具维修' },
  { value: '门窗维修', label: '门窗维修' },
  { value: '墙面维修', label: '墙面维修' },
  { value: '电器维修', label: '电器维修' },
  { value: '管道疏通', label: '管道疏通' },
  { value: '安装服务', label: '安装服务' }
]

// 状态选项
const statusOptions = [
  { value: 1, label: '在职' },
  { value: 0, label: '离职' }
]

// 搜索表单
const searchForm = reactive({
  keyword: '',
  skill: '',
  status: null as number | null
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
const workerForm = reactive({
  id: 0,
  name: '',
  phone: '',
  avatarUrl: '',
  skills: [] as string[],
  status: 1
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
  skills: [
    { required: true, message: '请至少选择一项技能', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一项技能', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载维修工列表
const loadWorkerList = async () => {
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
    // 获取绩效统计，如果需要获取特定维修工的统计，可以传递workerId参数
    // 这里我们获取最近的几个维修工的统计数据
    const res = await getWorkerStats(1) // 这里可以根据实际需求传递不同的workerId，或者在页面上添加选择维修工的功能
    
    if (res.code === 200) {
      workerStats.value = res.data
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
  searchForm.status = null
  handleSearch()
}

// 添加维修工
const handleAddWorker = () => {
  formType.value = 'add'
  workerForm.id = 0
  workerForm.name = ''
  workerForm.phone = ''
  workerForm.avatar = ''
  workerForm.skills = []
  workerForm.status = 1
  avatarUrl.value = ''
  formVisible.value = true
}

// 编辑维修工
const handleEditWorker = (row: RepairWorker) => {
  formType.value = 'edit'
  workerForm.id = row.id
  workerForm.name = row.name
  workerForm.phone = row.phone
  workerForm.avatar = row.avatar
  workerForm.skills = [...row.skills]
  workerForm.status = row.status
  avatarUrl.value = row.avatar
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

// 头像变更
const handleAvatarChange = (file: any) => {
  // 实际项目中应该上传到服务器
  avatarUrl.value = URL.createObjectURL(file.raw)
  workerForm.avatarUrl = avatarUrl.value
}

// 提交表单
const submitWorkerForm = () => {
  if (!workerFormRef.value) return
  
  workerFormRef.value.validate((valid) => {
    if (valid) {
      // 实际项目中应调用API保存数据
      ElMessage.success(formType.value === 'add' ? '添加维修工成功' : '更新维修工信息成功')
      formVisible.value = false
      loadWorkerList()
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
    
    .worker-stats {
      .stats-list {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
      }
      
      .stats-item {
        flex: 0 0 calc(25% - 15px);
        display: flex;
        align-items: center;
        background-color: #f8f9fa;
        border-radius: var(--border-radius);
        padding: 16px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
        
        .worker-avatar {
          margin-right: 16px;
        }
        
        .worker-info {
          flex: 1;
          
          .name {
            font-size: 16px;
            font-weight: 500;
            margin: 0 0 5px;
          }
          
          .rating {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
            
            .rating-value {
              margin-left: 5px;
              font-weight: 500;
            }
          }
          
          .metrics {
            display: flex;
            gap: 10px;
            
            .metric-item {
              flex: 1;
              text-align: center;
              background-color: #fff;
              border-radius: var(--border-radius);
              padding: 8px;
              
              .label {
                font-size: 12px;
                color: var(--text-color-secondary);
                margin-bottom: 5px;
              }
              
              .value {
                font-size: 16px;
                font-weight: 500;
                color: var(--text-color);
              }
            }
          }
        }
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