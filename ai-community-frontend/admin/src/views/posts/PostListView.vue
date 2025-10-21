<template>
  <div class="post-list-container">
    <h2 class="page-title">帖子管理</h2>
    
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标题/内容关键词" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model.number="searchForm.category" placeholder="全部" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model.number="searchForm.status" placeholder="全部" clearable>
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
        <el-table-column label="数据统计" width="200">
          <template #default="{ row }">
            <div class="post-stats">
              <el-tooltip content="浏览量">
                <span class="stat-item">
                  <el-icon><view /></el-icon> {{ row.viewCount }}
                </span>
              </el-tooltip>
              <el-tooltip content="点赞数">
                <span class="stat-item">
                  <el-icon><star /></el-icon> {{ row.likeCount }}
                </span>
              </el-tooltip>
              <el-tooltip content="评论数">
                <span class="stat-item">
                  <el-icon><chat-dot-round /></el-icon> {{ row.commentCount }}
                </span>
              </el-tooltip>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, RefreshRight, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, getPostDetail, updatePostStatus, deletePost } from '@/api/post'
import type { PostInfo } from '@/api/post'

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
  keyword: '',
  userId: '',
  category: undefined as number | undefined,
  status: undefined as number | undefined
})

// 表格数据
const tableData = ref<PostInfo[]>([])
const tableLoading = ref(false)

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
  searchForm.keyword = ''
  searchForm.userId = ''
  searchForm.category = undefined
  searchForm.status = undefined
  handleSearch()
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
  
  .post-stats {
    display: flex;
    gap: 10px;
    
    .stat-item {
      display: flex;
      align-items: center;
      
      .el-icon {
        margin-right: 4px;
      }
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