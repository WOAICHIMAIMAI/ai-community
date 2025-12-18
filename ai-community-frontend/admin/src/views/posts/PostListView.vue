<template>
  <div class="post-list-container">
    <h2 class="page-title">帖子管理</h2>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="searchForm.content" placeholder="请输入内容" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable readonly style="width: 200px">
            <template #append>
              <el-button :icon="Search" @click="showUserSelectDialog" />
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
          <span>帖子列表</span>
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
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="nickname" label="发布者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.avatar || row.userAvatar">{{ row.nickname?.substr(0, 1) }}</el-avatar>
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="浏览量" width="100" align="center">
          <template #default="{ row }">
            <div class="stat-cell">
              <el-icon><view /></el-icon>
              <span>{{ row.viewCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="点赞数" width="100" align="center">
          <template #default="{ row }">
            <div class="stat-cell">
              <el-icon><promotion /></el-icon>
              <span>{{ row.likeCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论数" width="100" align="center">
          <template #default="{ row }">
            <div class="stat-cell">
              <el-icon><chat-dot-round /></el-icon>
              <span>{{ row.commentCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdTime || row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row.id)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 0" 
              type="success" 
              size="small" 
              @click="handlePublishPost(row.id)"
            >
              发布
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDeletePost(row.id)"
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
    
    <!-- 帖子详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="帖子详情"
      width="70%"
      destroy-on-close
    >
      <div class="post-detail" v-if="currentPost">
        <h2>{{ currentPost.title }}</h2>
        
        <div class="post-meta">
          <el-avatar :size="32" :src="currentPost.avatar || currentPost.userAvatar">
            {{ currentPost.nickname?.substr(0, 1) }}
          </el-avatar>
          <div class="meta-info">
            <div class="author">{{ currentPost.nickname }}</div>
            <div class="time">
              发布于：{{ formatTime(currentPost.createdTime || currentPost.createTime) }}
              <el-tag size="small" class="category-tag">{{ currentPost.category || currentPost.categoryName }}</el-tag>
            </div>
          </div>
        </div>
        
        <div class="post-content" v-html="highlightSensitiveWords(currentPost.content)"></div>
        
        <div class="post-images" v-if="currentPost.images && currentPost.images.length > 0">
          <h4>附件图片</h4>
          <div class="image-list">
            <div v-for="(image, index) in currentPost.images" :key="index" class="image-item">
              <el-image
                :src="image"
                :preview-src-list="currentPost.images"
                fit="cover"
              />
            </div>
          </div>
        </div>
        
        <div class="post-actions" v-if="currentPost.status === 0">
          <el-button type="success" @click="handlePublishPost(currentPost.id)">发布帖子</el-button>
          <el-button type="danger" @click="handleDeletePost(currentPost.id)">删除帖子</el-button>
        </div>
      </div>
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
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, View, Promotion, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, getPostDetail, updatePostStatus, deletePost } from '@/api/post'
import type { PostInfo } from '@/api/post'
import { getUserList } from '@/api/user'
import type { UserInfo } from '@/api/user'

// 敏感词列表（实际项目中可从后端获取）
const sensitiveWords = ['违禁', '广告', '敏感', '投诉']

// 分类选项
const categoryOptions = [
  { value: 1, label: '社区公告' },
  { value: 2, label: '邻里互助' },
  { value: 3, label: '闲置交易' },
  { value: 4, label: '生活服务' },
  { value: 5, label: '意见建议' }
]

// 状态选项
const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已删除' }
]

// 搜索表单
const searchForm = reactive({
  title: '',
  content: '',
  userId: ''
})

// 表格数据
const tableData = ref<PostInfo[]>([])
const tableLoading = ref(false)

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

// 分页参数
const pageParams = reactive({
  page: 1,
  pageSize: 10
})
const total = ref(0)

// 帖子详情
const detailVisible = ref(false)
const currentPost = ref<PostInfo | null>(null)

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
      return '草稿'
    case 1:
      return '已发布'
    case 2:
      return '已删除'
    default:
      return '未知'
  }
}

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return '-'
  
  try {
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return time
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

// 加载帖子列表
const loadPostList = async () => {
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
    
    const res = await getPostList(params)
    
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取帖子列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取帖子列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.page = 1
  loadPostList()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.title = ''
  searchForm.content = ''
  searchForm.userId = ''
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

// 查看帖子详情
const handleViewDetail = async (postId: number) => {
  try {
    const res = await getPostDetail(postId)
    
    if (res.code === 200) {
      currentPost.value = res.data
      detailVisible.value = true
    } else {
      ElMessage.error(res.message || '获取帖子详情失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取帖子详情失败')
  }
}

// 发布帖子（将草稿状态改为已发布）
const handlePublishPost = (postId: number) => {
  ElMessageBox.confirm(
    '确定要发布该帖子吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(async () => {
    try {
      const res = await updatePostStatus({
        postId,
        status: 1 // 1表示已发布
      })
      
      if (res.code === 200 && res.data) {
        ElMessage.success('发布成功')
        loadPostList()
        if (detailVisible.value) {
          detailVisible.value = false
        }
      } else {
        ElMessage.error(res.message || '发布失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '发布失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 删除帖子
const handleDeletePost = (postId: number) => {
  ElMessageBox.confirm(
    '确定要删除该帖子吗？此操作不可恢复！',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deletePost(postId)
      
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadPostList()
        if (detailVisible.value) {
          detailVisible.value = false
        }
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
  loadPostList()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  pageParams.page = page
  loadPostList()
}

// 组件挂载后加载数据
onMounted(() => {
  loadPostList()
})
</script>

<style scoped lang="scss">
.post-list-container {
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
  
  .stat-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    
    .el-icon {
      font-size: 16px;
      color: var(--el-color-primary);
    }
    
    span {
      font-weight: 500;
    }
  }
  
  .post-detail {
    h2 {
      margin-bottom: 16px;
      font-size: 20px;
      font-weight: 500;
    }
    
    .post-meta {
      display: flex;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border-color);
      
      .meta-info {
        margin-left: 12px;
        
        .author {
          font-weight: 500;
          margin-bottom: 4px;
        }
        
        .time {
          font-size: 12px;
          color: var(--text-color-secondary);
        }
        
        .category-tag {
          margin-left: 8px;
        }
      }
    }
    
    .post-content {
      margin-bottom: 24px;
      line-height: 1.8;
      white-space: pre-wrap;
    }
    
    .post-images {
      h4 {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 12px;
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
    
    .post-actions {
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