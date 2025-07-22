<template>
  <div class="problem-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>常见问题管理</h2>
      <p>管理系统中的常见问题，包括问题的增删改查和分类管理</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增问题
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
          v-model="queryParams.type"
          placeholder="选择分类"
          clearable
          style="width: 150px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option
            v-for="category in categories"
            :key="category.type"
            :label="category.categoryName"
            :value="category.type"
          />
        </el-select>
        
        <el-select
          v-model="queryParams.onlyPriority"
          placeholder="优先级"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="置顶问题" :value="true" />
          <el-option label="普通问题" :value="false" />
        </el-select>
        
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索问题标题或内容"
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
        
        <el-table-column prop="problem" label="问题标题" min-width="200">
          <template #default="{ row }">
            <div class="problem-title">
              <el-tag v-if="row.isTop" type="danger" size="small">置顶</el-tag>
              <span>{{ row.problem }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.type)">
              {{ row.categoryName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="answer" label="答案内容" min-width="250">
          <template #default="{ row }">
            <div class="answer-preview">
              {{ truncateText(row.answer, 100) }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isTop"
              @change="handlePriorityChange(row)"
              active-text="置顶"
              inactive-text="普通"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
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
      width="800px"
      @close="handleDialogClose"
    >
      <problem-form
        ref="problemFormRef"
        :form-data="currentProblem"
        :categories="categories"
        @submit="handleFormSubmit"
      />
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="问题详情"
      width="700px"
    >
      <problem-detail
        v-if="viewDialogVisible"
        :problem="currentProblem"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, Refresh } from '@element-plus/icons-vue'
import {
  getProblemCategories,
  getProblemsPage,
  deleteProblem,
  batchDeleteProblems,
  setProblemPriority,
  type ProblemDetail,
  type ProblemCategory,
  type ProblemQueryParams
} from '@/api/common-problems'
import ProblemForm from './components/ProblemForm.vue'
import ProblemDetail from './components/ProblemDetail.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<ProblemDetail[]>([])
const categories = ref<ProblemCategory[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// 查询参数
const queryParams = reactive<ProblemQueryParams>({
  type: undefined,
  onlyPriority: undefined,
  keyword: '',
  page: 1,
  pageSize: 20
})

// 弹窗相关
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentProblem = ref<Partial<ProblemDetail>>({})
const problemFormRef = ref()

// 计算属性
const dialogTitle = computed(() => {
  return currentProblem.value.id ? '编辑问题' : '新增问题'
})

// 页面初始化
onMounted(() => {
  loadCategories()
  loadProblems()
})

// 加载分类数据
const loadCategories = async () => {
  try {
    // 使用模拟数据
    categories.value = [
      { type: 1, categoryName: '账户相关', problemCount: 4 },
      { type: 2, categoryName: '维修服务', problemCount: 4 },
      { type: 3, categoryName: '社区使用', problemCount: 3 },
      { type: 4, categoryName: '支付账单', problemCount: 3 },
      { type: 5, categoryName: '技术支持', problemCount: 3 },
      { type: 6, categoryName: '其他问题', problemCount: 3 }
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载问题列表
const loadProblems = async () => {
  try {
    loading.value = true

    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 使用模拟数据
    const mockData = [
      {
        id: 1,
        problem: '如何注册账户？',
        answer: '您可以通过手机号或用户名进行注册。点击注册按钮，填写必要信息，验证手机号后即可完成注册。',
        type: 1,
        categoryName: '账户相关',
        priority: 1,
        isTop: true
      },
      {
        id: 2,
        problem: '忘记密码怎么办？',
        answer: '点击登录页面的"忘记密码"链接，输入您的手机号，系统会发送验证码到您的手机，验证后可重置密码。',
        type: 1,
        categoryName: '账户相关',
        priority: 1,
        isTop: true
      },
      {
        id: 3,
        problem: '如何提交维修申请？',
        answer: '进入"在线报修"页面，选择维修类型，填写问题描述、联系方式和地址信息，提交后等待维修师傅接单。',
        type: 2,
        categoryName: '维修服务',
        priority: 1,
        isTop: true
      },
      {
        id: 4,
        problem: '维修师傅多久能上门？',
        answer: '一般情况下，维修师傅会在24小时内联系您确认上门时间。紧急情况可在备注中说明，我们会优先安排。',
        type: 2,
        categoryName: '维修服务',
        priority: 1,
        isTop: true
      },
      {
        id: 5,
        problem: '如何修改个人信息？',
        answer: '登录后进入个人中心，点击"编辑资料"即可修改昵称、头像、性别等个人信息。',
        type: 1,
        categoryName: '账户相关',
        priority: 0,
        isTop: false
      },
      {
        id: 6,
        problem: '如何发布帖子？',
        answer: '进入社区页面，点击"发布"按钮，填写标题和内容，可以添加图片，发布后其他用户可以看到并评论。',
        type: 3,
        categoryName: '社区使用',
        priority: 1,
        isTop: true
      },
      {
        id: 7,
        problem: '支持哪些支付方式？',
        answer: '目前支持微信支付、支付宝支付和银行卡支付。选择您方便的支付方式即可完成付款。',
        type: 4,
        categoryName: '支付账单',
        priority: 1,
        isTop: true
      },
      {
        id: 8,
        problem: 'APP闪退怎么办？',
        answer: '请尝试重启APP或手机，确保APP是最新版本。如果问题持续存在，请联系客服并提供手机型号和系统版本。',
        type: 5,
        categoryName: '技术支持',
        priority: 1,
        isTop: true
      }
    ]

    // 根据查询条件过滤数据
    let filteredData = mockData

    if (queryParams.type) {
      filteredData = filteredData.filter(item => item.type === queryParams.type)
    }

    if (queryParams.onlyPriority !== undefined) {
      filteredData = filteredData.filter(item => item.isTop === queryParams.onlyPriority)
    }

    if (queryParams.keyword) {
      const keyword = queryParams.keyword.toLowerCase()
      filteredData = filteredData.filter(item =>
        item.problem.toLowerCase().includes(keyword) ||
        item.answer.toLowerCase().includes(keyword)
      )
    }

    // 分页处理
    const start = (queryParams.page! - 1) * queryParams.pageSize!
    const end = start + queryParams.pageSize!
    const pageData = filteredData.slice(start, end)

    tableData.value = pageData
    total.value = filteredData.length

  } catch (error) {
    console.error('加载问题列表失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  loadProblems()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    type: undefined,
    onlyPriority: undefined,
    keyword: '',
    page: 1,
    pageSize: 20
  })
  loadProblems()
}

// 新增
const handleCreate = () => {
  currentProblem.value = {}
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: ProblemDetail) => {
  currentProblem.value = { ...row }
  dialogVisible.value = true
}

// 查看
const handleView = (row: ProblemDetail) => {
  currentProblem.value = { ...row }
  viewDialogVisible.value = true
}

// 删除
const handleDelete = async (row: ProblemDetail) => {
  try {
    await ElMessageBox.confirm('确定要删除这个问题吗？', '提示', {
      type: 'warning'
    })

    // 模拟删除操作
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('删除成功')
    loadProblems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个问题吗？`, '提示', {
      type: 'warning'
    })

    // 模拟批量删除操作
    await new Promise(resolve => setTimeout(resolve, 800))
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadProblems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 优先级变更
const handlePriorityChange = async (row: ProblemDetail) => {
  try {
    const priority = row.isTop ? 1 : 0

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))

    ElMessage.success('优先级设置成功')
    row.priority = priority
  } catch (error) {
    ElMessage.error('优先级设置失败')
    // 恢复原状态
    row.isTop = !row.isTop
  }
}

// 表格选择变更
const handleSelectionChange = (selection: ProblemDetail[]) => {
  selectedIds.value = selection.map(item => item.id)
}

// 表单提交
const handleFormSubmit = () => {
  dialogVisible.value = false
  loadProblems()
}

// 弹窗关闭
const handleDialogClose = () => {
  currentProblem.value = {}
}

// 获取分类标签类型
const getCategoryTagType = (type: number) => {
  const typeMap: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'warning',
    5: 'danger',
    6: ''
  }
  return typeMap[type] || ''
}

// 截断文本
const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}
</script>

<style scoped lang="scss">
.problem-list-container {
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

.problem-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answer-preview {
  line-height: 1.5;
  color: #606266;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
