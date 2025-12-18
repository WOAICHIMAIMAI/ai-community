<template>
  <div class="comment-list-container">
    <h2 class="page-title">评论管理</h2>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="评论内容">
          <el-input v-model="searchForm.content" placeholder="请输入评论内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable readonly style="width: 200px">
            <template #append>
              <el-button :icon="Search" @click="showUserSelectDialog" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="帖子ID">
          <el-input v-model="searchForm.postId" placeholder="帖子ID" clearable readonly style="width: 200px">
            <template #append>
              <el-button :icon="Search" @click="showPostSelectDialog" />
            </template>
          </el-input>
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
          <span>评论列表</span>
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
        <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-html="highlightSensitiveWords(row.content)"></div>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.avatar || row.userAvatar">{{ row.nickname?.substr(0, 1) }}</el-avatar>
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="postId" label="帖子ID" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handleUpdateStatus(row.id, 1)"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="warning" 
              size="small" 
              @click="handleUpdateStatus(row.id, 2)"
            >
              拒绝
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row.id)"
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
    
    <!-- 评论详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="评论详情"
      width="50%"
      destroy-on-close
    >
      <div class="comment-detail" v-if="currentComment">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="评论ID">{{ currentComment.id }}</el-descriptions-item>
          <el-descriptions-item label="帖子ID">{{ currentComment.postId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentComment.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">
            <div class="user-info">
              <el-avatar :size="24" :src="currentComment.avatar || currentComment.userAvatar">{{ currentComment.nickname?.substr(0, 1) }}</el-avatar>
              <span>{{ currentComment.nickname }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="评论内容">
            <div v-html="highlightSensitiveWords(currentComment.content)" class="comment-content"></div>
          </el-descriptions-item>
          <el-descriptions-item label="父评论ID">{{ currentComment.parentId || '无（顶级评论）' }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentComment.likeCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentComment.status)">
              {{ getStatusText(currentComment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentComment.createTime || currentComment.createdTime }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="comment-actions" v-if="currentComment.status === 0">
          <el-button type="success" @click="handleUpdateStatus(currentComment.id, 1)">通过审核</el-button>
          <el-button type="danger" @click="handleUpdateStatus(currentComment.id, 2)">拒绝发布</el-button>
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
    
    <!-- 用户选择对话框 -->
    <el-dialog
      v-model="userSelectVisible"
      title="选择用户"
      width="70%"
      destroy-on-close
    >
      <div class="user-select-dialog">
        <!-- 搜索表单 -->
        <el-form :model="userSearchForm" inline class="user-search-form">
          <el-form-item label="用户名">
            <el-input v-model="userSearchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="userSearchForm.nickName" placeholder="请输入昵称" clearable />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="userSearchForm.phone" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUserSearch">
              <el-icon><search /></el-icon>搜索
            </el-button>
            <el-button @click="resetUserSearch">
              <el-icon><refresh-right /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 用户列表 -->
        <el-table
          v-loading="userTableLoading"
          :data="userTableData"
          border
          stripe
          highlight-current-row
          @current-change="handleUserSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="id" label="用户ID" width="180" show-overflow-tooltip />
          <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
          <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
          <el-table-column label="头像" width="80" align="center">
            <template #default="{ row }">
              <el-avatar :size="40" :src="row.avatarUrl">
                {{ row.nickname?.substr(0, 1) }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="120" />
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="userPageParams.pageNum"
            v-model:page-size="userPageParams.pageSize"
            :total="userTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleUserSizeChange"
            @current-change="handleUserCurrentChange"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="userSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUserSelection" :disabled="!selectedUser">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 帖子选择对话框 -->
    <el-dialog
      v-model="postSelectVisible"
      title="选择帖子"
      width="70%"
      destroy-on-close
    >
      <div class="post-select-dialog">
        <!-- 搜索表单 -->
        <el-form :model="postSearchForm" inline class="post-search-form">
          <el-form-item label="标题">
            <el-input v-model="postSearchForm.title" placeholder="请输入标题" clearable />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="postSearchForm.content" placeholder="请输入内容" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePostSearch">
              <el-icon><search /></el-icon>搜索
            </el-button>
            <el-button @click="resetPostSearch">
              <el-icon><refresh-right /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 帖子列表 -->
        <el-table
          v-loading="postTableLoading"
          :data="postTableData"
          border
          stripe
          highlight-current-row
          @current-change="handlePostSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="id" label="帖子ID" width="180" show-overflow-tooltip />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="nickname" label="发布者" width="120" show-overflow-tooltip />
          <el-table-column label="浏览量" width="100" align="center">
            <template #default="{ row }">
              {{ row.viewCount || 0 }}
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="postPageParams.page"
            v-model:page-size="postPageParams.pageSize"
            :total="postTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handlePostSizeChange"
            @current-change="handlePostCurrentChange"
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="postSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPostSelection" :disabled="!selectedPost">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { getCommentList, updateCommentStatus, deleteComment, getPostList } from '@/api/post'
import type { CommentInfo, PostInfo } from '@/api/post'
import { getUserList } from '@/api/user'
import type { UserInfo } from '@/api/user'

// 敏感词列表（实际项目中可从后端获取）
const sensitiveWords = ['违禁', '广告', '敏感', '投诉', '辱骂', '歧视']

// 状态选项
const statusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已拒绝' }
]

// 搜索表单
const searchForm = reactive({
  content: '',
  userId: '',
  postId: ''
})

// 表格数据
const tableData = ref<CommentInfo[]>([])
const tableLoading = ref(false)

// 分页参数
const pageParams = reactive({
  page: 1,
  pageSize: 10
})
const total = ref(0)

// 评论详情
const detailVisible = ref(false)
const currentComment = ref<CommentInfo | null>(null)

// 拒绝表单
const rejectFormRef = ref<FormInstance>()
const rejectFormVisible = ref(false)
const rejectForm = reactive({
  commentId: 0,
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

// 用户选择对话框
const userSelectVisible = ref(false)
const selectedUser = ref<UserInfo | null>(null)

// 用户搜索表单
const userSearchForm = reactive({
  username: '',
  nickName: '',
  phone: ''
})

// 用户表格数据
const userTableData = ref<UserInfo[]>([])
const userTableLoading = ref(false)

// 用户分页参数
const userPageParams = reactive({
  pageNum: 1,
  pageSize: 10
})
const userTotal = ref(0)

// 帖子选择对话框
const postSelectVisible = ref(false)
const selectedPost = ref<PostInfo | null>(null)

// 帖子搜索表单
const postSearchForm = reactive({
  title: '',
  content: ''
})

// 帖子表格数据
const postTableData = ref<PostInfo[]>([])
const postTableLoading = ref(false)

// 帖子分页参数
const postPageParams = reactive({
  page: 1,
  pageSize: 10
})
const postTotal = ref(0)

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

// 高亮敏感词
const highlightSensitiveWords = (text: string): string => {
  if (!text) return ''
  
  let result = text
  sensitiveWords.forEach(word => {
    const regex = new RegExp(word, 'g')
    result = result.replace(regex, `<span class="highlight-sensitive">${word}</span>`)
  })
  
  return result
}

// 加载评论列表
const loadCommentList = async () => {
  try {
    tableLoading.value = true
    const params = {
      ...pageParams,
      ...searchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const res = await getCommentList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取评论列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取评论列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.page = 1
  loadCommentList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.content = ''
  searchForm.userId = ''
  searchForm.postId = ''
  handleSearch()
}

// 显示用户选择对话框
const showUserSelectDialog = () => {
  userSelectVisible.value = true
  loadUserList()
}

// 加载用户列表
const loadUserList = async () => {
  try {
    userTableLoading.value = true
    const params = {
      ...userPageParams,
      ...userSearchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getUserList(params)
    
    if (res.code === 200) {
      userTableData.value = res.data.records
      userTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
  } finally {
    userTableLoading.value = false
  }
}

// 用户搜索
const handleUserSearch = () => {
  userPageParams.pageNum = 1
  loadUserList()
}

// 重置用户搜索条件
const resetUserSearch = () => {
  userSearchForm.username = ''
  userSearchForm.nickName = ''
  userSearchForm.phone = ''
  handleUserSearch()
}

// 用户选择变化
const handleUserSelectionChange = (user: UserInfo | null) => {
  selectedUser.value = user
}

// 确认用户选择
const confirmUserSelection = () => {
  if (selectedUser.value) {
    searchForm.userId = String(selectedUser.value.id)
    userSelectVisible.value = false
    ElMessage.success('已选择用户')
  }
}

// 用户分页大小变化
const handleUserSizeChange = (size: number) => {
  userPageParams.pageSize = size
  loadUserList()
}

// 用户页码变化
const handleUserCurrentChange = (page: number) => {
  userPageParams.pageNum = page
  loadUserList()
}

// 显示帖子选择对话框
const showPostSelectDialog = () => {
  postSelectVisible.value = true
  loadPostList()
}

// 加载帖子列表
const loadPostList = async () => {
  try {
    postTableLoading.value = true
    const params = {
      ...postPageParams,
      ...postSearchForm
    }
    
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const res = await getPostList(params)
    
    if (res.code === 200) {
      postTableData.value = res.data.records
      postTotal.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取帖子列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取帖子列表失败')
  } finally {
    postTableLoading.value = false
  }
}

// 帖子搜索
const handlePostSearch = () => {
  postPageParams.page = 1
  loadPostList()
}

// 重置帖子搜索条件
const resetPostSearch = () => {
  postSearchForm.title = ''
  postSearchForm.content = ''
  handlePostSearch()
}

// 帖子选择变化
const handlePostSelectionChange = (post: PostInfo | null) => {
  selectedPost.value = post
}

// 确认帖子选择
const confirmPostSelection = () => {
  if (selectedPost.value) {
    searchForm.postId = String(selectedPost.value.id)
    postSelectVisible.value = false
    ElMessage.success('已选择帖子')
  }
}

// 帖子分页大小变化
const handlePostSizeChange = (size: number) => {
  postPageParams.pageSize = size
  loadPostList()
}

// 帖子页码变化
const handlePostCurrentChange = (page: number) => {
  postPageParams.page = page
  loadPostList()
}

// 查看评论详情
const handleViewDetail = (comment: CommentInfo) => {
  currentComment.value = comment
  detailVisible.value = true
}

// 更新评论状态
const handleUpdateStatus = (commentId: number, status: number) => {
  if (status === 2) {
    // 拒绝需要填写原因
    rejectForm.commentId = commentId
    rejectFormVisible.value = true
  } else {
    // 通过直接确认
    ElMessageBox.confirm(
      '确定要通过该评论吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    ).then(async () => {
      try {
        const res = await updateCommentStatus({
          commentId,
          status
        })
        
        if (res.code === 200 && res.data) {
          ElMessage.success('审核通过成功')
          loadCommentList()
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
        const res = await updateCommentStatus({
          commentId: rejectForm.commentId,
          status: rejectForm.status,
          reason: rejectForm.reason
        })
        
        if (res.code === 200 && res.data) {
          ElMessage.success('已拒绝该评论')
          rejectFormVisible.value = false
          loadCommentList()
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

// 删除评论
const handleDelete = (commentId: number) => {
  ElMessageBox.confirm(
    '确定要删除该评论吗？删除后不可恢复。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteComment(commentId)
      
      if (res.code === 200 && res.data) {
        ElMessage.success('删除成功')
        loadCommentList()
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

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageParams.pageSize = size
  loadCommentList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.page = page
  loadCommentList()
}

// 组件挂载后加载数据
onMounted(() => {
  loadCommentList()
})
</script>

<style scoped lang="scss">
.comment-list-container {
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
  
  .user-info {
    display: flex;
    align-items: center;
    
    span {
      margin-left: 8px;
    }
  }
  
  .comment-detail {
    .comment-content {
      line-height: 1.8;
      white-space: pre-wrap;
    }
    
    .comment-actions {
      margin-top: 30px;
      display: flex;
      justify-content: center;
      gap: 20px;
    }
  }
}

:deep(.highlight-sensitive) {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
  padding: 0 2px;
  border-radius: 2px;
  font-weight: bold;
}
</style> 