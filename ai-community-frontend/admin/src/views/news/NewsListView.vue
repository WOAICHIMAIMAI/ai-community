<template>
  <div class="news-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>新闻管理</h2>
      <p>管理热点新闻，包括爬取、审核、发布和删除</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCrawl" :loading="crawling">
          <el-icon><Refresh /></el-icon>
          爬取新闻
        </el-button>
        <el-button @click="handleClean" :loading="cleaning">
          <el-icon><Delete /></el-icon>
          清理无效
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-select
          v-model="queryParams.category"
          placeholder="分类"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="科技" value="tech" />
          <el-option label="财经" value="finance" />
          <el-option label="娱乐" value="entertainment" />
          <el-option label="体育" value="sports" />
          <el-option label="其他" value="other" />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已隐藏" :value="2" />
        </el-select>
        
        <el-select
          v-model="queryParams.isHot"
          placeholder="是否热点"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleQuery"
        >
          <el-option label="热点" :value="1" />
          <el-option label="普通" :value="0" />
        </el-select>
        
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
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <el-link :href="row.sourceUrl" target="_blank" type="primary">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="source" label="来源" width="120" />
        
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)">
              {{ getCategoryName(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="isHot" label="热点" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isHot"
              :active-value="1"
              :inactive-value="0"
              @change="handleHotChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="isTop" label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isTop"
              :active-value="1"
              :inactive-value="0"
              @change="handleTopChange(row)"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="viewCount" label="浏览量" width="100" align="right" />
        
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 1"
              type="success"
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              size="small"
              @click="handleHide(row)"
            >
              隐藏
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete } from '@element-plus/icons-vue'
import {
  type HotNews,
  type NewsQueryParams,
  getNewsPage,
  crawlNews,
  cleanInvalidNews,
  updateNewsStatus,
  updateNewsHot,
  updateNewsTop,
  deleteNews
} from '@/api/news'

// 响应式数据
const loading = ref(false)
const crawling = ref(false)
const cleaning = ref(false)
const tableData = ref<HotNews[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<NewsQueryParams>({
  page: 1,
  pageSize: 20,
  category: undefined,
  status: undefined,
  isHot: undefined
})

// 页面初始化
onMounted(() => {
  loadNews()
})

// 加载新闻列表
const loadNews = async () => {
  loading.value = true
  try {
    const res = await getNewsPage(queryParams)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('加载新闻列表失败:', error)
    ElMessage.error('加载新闻列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.page = 1
  loadNews()
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.pageSize = 20
  queryParams.category = undefined
  queryParams.status = undefined
  queryParams.isHot = undefined
  loadNews()
}

// 爬取新闻
const handleCrawl = async () => {
  crawling.value = true
  try {
    const res = await crawlNews()
    if (res.code === 200) {
      ElMessage.success(`爬取完成！新增 ${res.data.addedCount} 条，总计 ${res.data.totalCount} 条`)
      loadNews()
    }
  } catch (error) {
    console.error('爬取新闻失败:', error)
    ElMessage.error('爬取新闻失败')
  } finally {
    crawling.value = false
  }
}

// 清理无效新闻
const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确定要清理无效新闻记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    cleaning.value = true
    const res = await cleanInvalidNews()
    if (res.code === 200) {
      ElMessage.success(`清理完成！清理 ${res.data.cleanedCount} 条，剩余 ${res.data.remainingCount} 条`)
      loadNews()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清理失败:', error)
      ElMessage.error('清理失败')
    }
  } finally {
    cleaning.value = false
  }
}

// 修改热点状态
const handleHotChange = async (row: HotNews) => {
  try {
    const res = await updateNewsHot(row.id, row.isHot)
    if (res.code === 200) {
      ElMessage.success('更新成功')
    }
  } catch (error) {
    console.error('更新热点状态失败:', error)
    ElMessage.error('更新热点状态失败')
    // 恢复原值
    row.isHot = row.isHot === 1 ? 0 : 1
  }
}

// 修改置顶状态
const handleTopChange = async (row: HotNews) => {
  try {
    const res = await updateNewsTop(row.id, row.isTop)
    if (res.code === 200) {
      ElMessage.success('更新成功')
    }
  } catch (error) {
    console.error('更新置顶状态失败:', error)
    ElMessage.error('更新置顶状态失败')
    // 恢复原值
    row.isTop = row.isTop === 1 ? 0 : 1
  }
}

// 发布新闻
const handlePublish = async (row: HotNews) => {
  try {
    const res = await updateNewsStatus(row.id, 1)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadNews()
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  }
}

// 隐藏新闻
const handleHide = async (row: HotNews) => {
  try {
    await ElMessageBox.confirm('确定要隐藏这条新闻吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updateNewsStatus(row.id, 2)
    if (res.code === 200) {
      ElMessage.success('隐藏成功')
      loadNews()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('隐藏失败:', error)
      ElMessage.error('隐藏失败')
    }
  }
}

// 删除新闻
const handleDelete = async (row: HotNews) => {
  try {
    await ElMessageBox.confirm('确定要删除这条新闻吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await deleteNews(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadNews()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取分类名称
const getCategoryName = (category: string) => {
  const categoryMap: Record<string, string> = {
    tech: '科技',
    finance: '财经',
    entertainment: '娱乐',
    sports: '体育',
    other: '其他'
  }
  return categoryMap[category] || category
}

// 获取分类标签类型
const getCategoryTagType = (category: string) => {
  const typeMap: Record<string, any> = {
    tech: 'primary',
    finance: 'success',
    entertainment: 'warning',
    sports: 'danger',
    other: 'info'
  }
  return typeMap[category] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '已隐藏'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.news-list-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #303133;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background-color: #fff;
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
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    padding: 16px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}
</style>

