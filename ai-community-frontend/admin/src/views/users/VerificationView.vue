<template>
  <div class="verification-container">
    <h2 class="page-title">认证管理</h2>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/真实姓名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证类型">
          <el-select v-model="searchForm.verificationType" placeholder="全部" clearable>
            <el-option label="个人认证" :value="1" />
            <el-option label="企业认证" :value="2" />
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
          <span>认证申请列表</span>
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
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="idCardNumber" label="身份证号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="verificationType" label="认证类型" width="120">
          <template #default="{ row }">
            {{ row.verificationType === 1 ? '个人认证' : row.verificationType === 2 ? '企业认证' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="auditTime" label="审核时间" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleAudit(row.id, 1)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="danger" 
              size="small" 
              @click="handleAudit(row.id, 2)"
            >
              拒绝
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
    
    <!-- 认证详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="认证申请详情"
      width="60%"
      destroy-on-close
    >
      <div class="verification-detail" v-if="currentItem">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID" :span="2">{{ currentItem.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentItem.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentItem.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentItem.realName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentItem.idCardNumber }}</el-descriptions-item>
          <el-descriptions-item label="认证类型">
            {{ currentItem.verificationType === 1 ? '个人认证' : currentItem.verificationType === 2 ? '企业认证' : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentItem.applyTime }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentItem.status)">
              {{ getStatusText(currentItem.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="currentItem.auditTime">{{ currentItem.auditTime }}</el-descriptions-item>
          <el-descriptions-item label="审核理由" v-if="currentItem.auditReason" :span="2">{{ currentItem.auditReason }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="id-card-images">
          <div class="image-item">
            <h4>身份证正面</h4>
            <el-image
              :src="currentItem.idCardFront"
              :preview-src-list="[currentItem.idCardFront]"
              fit="cover"
              style="width: 100%; max-height: 200px"
            />
          </div>
          <div class="image-item">
            <h4>身份证背面</h4>
            <el-image
              :src="currentItem.idCardBack"
              :preview-src-list="[currentItem.idCardBack]"
              fit="cover"
              style="width: 100%; max-height: 200px"
            />
          </div>
        </div>
        
        <div class="audit-actions" v-if="currentItem.status === 0">
          <el-button type="success" @click="handleAudit(currentItem.id, 1)">通过审核</el-button>
          <el-button type="danger" @click="handleAudit(currentItem.id, 2)">拒绝申请</el-button>
        </div>
      </div>
    </el-dialog>
    
    <!-- 拒绝理由表单 -->
    <el-dialog
      v-model="rejectFormVisible"
      title="拒绝原因"
      width="40%"
      destroy-on-close
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectRules"
        label-width="80px"
      >
        <el-form-item prop="reason" label="拒绝原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectFormVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getVerificationList, auditVerification } from '@/api/user'
import type { VerificationInfo } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null as number | null,
  verificationType: null as number | null
})

// 表格数据
const tableData = ref<VerificationInfo[]>([])
const tableLoading = ref(false)

// 分页参数
const pageParams = reactive({
  page: 1,
  pageSize: 10
})
const total = ref(0)

// 认证详情
const detailVisible = ref(false)
const currentItem = ref<VerificationInfo | null>(null)

// 拒绝表单
const rejectFormRef = ref<FormInstance>()
const rejectFormVisible = ref(false)
const rejectForm = reactive({
  verificationId: 0,
  status: 2,
  reason: ''
})

// 拒绝表单验证规则
const rejectRules: FormRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 获取状态标签类型
const getStatusType = (status: number): string => {
  switch (status) {
    case 0:
      return 'info'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: number): string => {
  switch (status) {
    case 0:
      return '待审核'
    case 1:
      return '已通过'
    case 2:
      return '已拒绝'
    default:
      return '未知'
  }
}

// 加载认证列表
const loadVerificationList = async () => {
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
    
    const res = await getVerificationList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取认证申请列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证申请列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.page = 1
  loadVerificationList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = null
  searchForm.verificationType = null
  handleSearch()
}

// 查看认证详情
const handleViewDetail = (row: VerificationInfo) => {
  currentItem.value = row
  detailVisible.value = true
}

// 审核认证
const handleAudit = (verificationId: number, status: number) => {
  if (status === 2) {
    // 拒绝需要填写原因
    rejectForm.verificationId = verificationId
    rejectFormVisible.value = true
  } else {
    // 通过直接确认
    ElMessageBox.confirm(
      '确定要通过该认证申请吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    ).then(async () => {
      try {
        const res = await auditVerification({
          verificationId,
          status
        })
        
        if (res.code === 200 && res.data) {
          ElMessage.success('审核通过成功')
          loadVerificationList()
          if (detailVisible.value) {
            detailVisible.value = false
          }
        } else {
          ElMessage.error(res.message || '审核失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '审核失败')
      }
    }).catch(() => {
      // 取消操作
    })
  }
}

// 确认拒绝
const confirmReject = () => {
  if (!rejectFormRef.value) return
  
  rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await auditVerification({
          verificationId: rejectForm.verificationId,
          status: rejectForm.status,
          reason: rejectForm.reason
        })
        
        if (res.code === 200 && res.data) {
          ElMessage.success('已拒绝该认证申请')
          rejectFormVisible.value = false
          loadVerificationList()
          if (detailVisible.value) {
            detailVisible.value = false
          }
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageParams.pageSize = size
  loadVerificationList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.page = page
  loadVerificationList()
}

// 组件挂载后加载数据
onMounted(() => {
  loadVerificationList()
})
</script>

<style scoped lang="scss">
.verification-container {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 500;
    color: var(--text-color);
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
  
  .verification-detail {
    .id-card-images {
      margin-top: 20px;
      display: flex;
      gap: 20px;
      
      .image-item {
        flex: 1;
        
        h4 {
          margin-bottom: 10px;
          font-weight: normal;
          color: var(--text-color-secondary);
        }
      }
    }
    
    .audit-actions {
      margin-top: 30px;
      display: flex;
      justify-content: center;
      gap: 20px;
    }
  }
}
</style> 