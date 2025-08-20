<template>
  <div class="service-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>服务类型管理</h2>
      <p>管理预约服务的类型和价格，包括服务的增删改查和状态管理</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增服务
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
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
          v-model="queryParams.isActive"
          placeholder="服务状态"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="启用" :value="true" />
          <el-option label="禁用" :value="false" />
        </el-select>
        
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索服务名称或描述"
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
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="serviceName" label="服务名称" min-width="150">
          <template #default="{ row }">
            <div class="service-name">
              <span>{{ row.serviceName }}</span>
              <el-tag 
                v-if="!row.isActive" 
                type="danger" 
                size="small" 
                style="margin-left: 8px;"
              >
                已禁用
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="serviceType" label="服务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getServiceTypeTagType(row.serviceType)">
              {{ getServiceTypeName(row.serviceType) }}
            </el-tag>
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
        
        <el-table-column prop="duration" label="预计时长" width="100">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        
        <el-table-column prop="isActive" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.isActive"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button-group>
                <el-button type="primary" size="small" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="info" size="small" @click="handleView(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
              </el-button-group>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <service-form
        ref="serviceFormRef"
        :form-data="currentService"
        @submit="handleFormSubmit"
      />
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="服务详情"
      width="600px"
    >
      <service-detail
        v-if="viewDialogVisible"
        :service="currentService"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, Refresh, Edit, View } from '@element-plus/icons-vue'
import {
  type AppointmentService,
  type ServiceQueryParams,
  AppointmentType
} from '@/api/appointment'
import ServiceForm from './components/ServiceForm.vue'
import ServiceDetail from './components/ServiceDetail.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<AppointmentService[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// 查询参数
const queryParams = reactive<ServiceQueryParams>({
  serviceType: undefined,
  isActive: undefined,
  keyword: '',
  page: 1,
  pageSize: 20
})

// 弹窗相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentService = ref<Partial<AppointmentService>>({})
const serviceFormRef = ref()

// 服务类型选项
const serviceTypes = [
  { value: AppointmentType.MAINTENANCE, label: '维修服务' },
  { value: AppointmentType.CLEANING, label: '保洁服务' },
  { value: AppointmentType.SECURITY, label: '安保服务' },
  { value: AppointmentType.DELIVERY, label: '快递代收' },
  { value: AppointmentType.OTHER, label: '其他服务' }
]

// 计算属性
const dialogTitle = computed(() => {
  return currentService.value.id ? '编辑服务' : '新增服务'
})

// 页面初始化
onMounted(() => {
  loadServices()
})

// 加载服务列表
const loadServices = async () => {
  try {
    loading.value = true

    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 使用模拟数据
    const mockData: AppointmentService[] = [
      {
        id: 1,
        serviceName: '水管维修',
        serviceType: AppointmentType.MAINTENANCE,
        description: '专业水管维修服务，包括漏水修补、管道疏通、阀门更换等',
        price: 150,
        duration: 120,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        serviceName: '家庭保洁',
        serviceType: AppointmentType.CLEANING,
        description: '专业家庭清洁服务，包括全屋清洁、深度清洁、定期保洁等',
        price: 200,
        duration: 180,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-15 11:00:00'
      },
      {
        id: 3,
        serviceName: '电器维修',
        serviceType: AppointmentType.MAINTENANCE,
        description: '家电维修服务，包括空调、洗衣机、冰箱、电视等家电维修',
        price: 300,
        duration: 150,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-15 14:20:00'
      },
      {
        id: 4,
        serviceName: '快递代收',
        serviceType: AppointmentType.DELIVERY,
        description: '快递代收服务，安全可靠，支持重要文件和贵重物品代收',
        price: 10,
        duration: 30,
        isActive: true,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-15 09:00:00'
      },
      {
        id: 5,
        serviceName: '安保巡逻',
        serviceType: AppointmentType.SECURITY,
        description: '专业安保巡逻服务，确保小区安全，定期巡查',
        price: 100,
        duration: 60,
        isActive: false,
        createdAt: '2024-01-01 00:00:00',
        updatedAt: '2024-01-15 16:00:00'
      }
    ]

    // 根据查询条件过滤数据
    let filteredData = mockData

    if (queryParams.serviceType !== undefined) {
      filteredData = filteredData.filter(item => item.serviceType === queryParams.serviceType)
    }

    if (queryParams.isActive !== undefined) {
      filteredData = filteredData.filter(item => item.isActive === queryParams.isActive)
    }

    if (queryParams.keyword) {
      const keyword = queryParams.keyword.toLowerCase()
      filteredData = filteredData.filter(item =>
        item.serviceName.toLowerCase().includes(keyword) ||
        item.description.toLowerCase().includes(keyword)
      )
    }

    // 分页处理
    const start = (queryParams.page! - 1) * queryParams.pageSize!
    const end = start + queryParams.pageSize!
    const pageData = filteredData.slice(start, end)

    tableData.value = pageData
    total.value = filteredData.length

  } catch (error) {
    console.error('加载服务列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  loadServices()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    serviceType: undefined,
    isActive: undefined,
    keyword: '',
    page: 1,
    pageSize: 20
  })
  loadServices()
}

// 新增
const handleCreate = () => {
  currentService.value = {}
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: AppointmentService) => {
  currentService.value = { ...row }
  dialogVisible.value = true
}

// 查看
const handleView = (row: AppointmentService) => {
  currentService.value = { ...row }
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (row: AppointmentService) => {
  try {
    await ElMessageBox.confirm('确定要删除这个服务吗？', '提示', {
      type: 'warning'
    })

    // 模拟删除操作
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('删除成功')
    loadServices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个服务吗？`, '提示', {
      type: 'warning'
    })

    // 模拟批量删除操作
    await new Promise(resolve => setTimeout(resolve, 800))
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadServices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row: AppointmentService) => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))

    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.isActive = !row.isActive
  }
}

// 表格选择变更
const handleSelectionChange = (selection: AppointmentService[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 表单提交
const handleFormSubmit = () => {
  dialogVisible.value = false
  loadServices()
}

// 弹窗关闭
const handleDialogClose = () => {
  currentService.value = {}
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
.service-list-container {
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
