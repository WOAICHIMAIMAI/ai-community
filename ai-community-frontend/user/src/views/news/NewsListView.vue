<template>
  <div class="news-list-container">
    <van-nav-bar 
      title="新闻资讯" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />
    
    <div class="content">
      <!-- 分类标签 -->
      <div class="category-tabs">
        <van-tabs v-model:active="activeCategory" @change="onCategoryChange" sticky>
          <van-tab 
            v-for="category in newsCategories" 
            :key="category.key"
            :title="category.name"
            :name="category.key"
          >
            <!-- 新闻列表 -->
            <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
              <van-list
                v-model:loading="loading"
                :finished="finished"
                finished-text="没有更多了"
                @load="onLoad"
              >
                <div class="news-list">
                  <div 
                    v-for="news in newsList" 
                    :key="news.id"
                    class="news-item"
                    @click="goToDetail(news.id)"
                  >
                    <div class="news-content">
                      <div class="news-header">
                        <h3 class="news-title">{{ news.title }}</h3>
                        <van-tag v-if="news.isHot" type="danger" size="small">热点</van-tag>
                        <van-tag v-if="news.isTop" type="primary" size="small">置顶</van-tag>
                      </div>
                      <p class="news-summary">{{ news.summary }}</p>
                      <div class="news-meta">
                        <span class="news-source">{{ news.source }}</span>
                        <span class="news-time">{{ formatTime(news.publishTime) }}</span>
                        <span class="news-views">{{ news.viewCount }}阅读</span>
                      </div>
                    </div>
                    <div class="news-image" v-if="news.coverImage">
                      <van-image
                        :src="news.coverImage"
                        width="100"
                        height="80"
                        fit="cover"
                        :lazy-load="true"
                      />
                    </div>
                  </div>
                </div>
              </van-list>
            </van-pull-refresh>
          </van-tab>
        </van-tabs>
      </div>
    </div>
    
    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast } from 'vant'
import { newsApi, mockNewsData, newsCategories, type NewsItem } from '@/api/news'
import BottomTabbar from '@/components/BottomTabbar.vue'

const router = useRouter()

// 状态管理
const activeCategory = ref('all')
const newsList = ref<NewsItem[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const currentPage = ref(1)
const pageSize = 10

// 格式化时间
const formatTime = (timeString: string) => {
  const time = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return time.toLocaleDateString()
  }
}

// 获取新闻列表
const fetchNewsList = async (page = 1, isRefresh = false) => {
  try {
    if (isRefresh) {
      currentPage.value = 1
      finished.value = false
    }
    
    let newsData: NewsItem[] = []
    
    if (activeCategory.value === '财经') {
      // 获取真实的财经新闻
      const response = await newsApi.getNewsList({
        page: page,
        pageSize: pageSize,
        category: '财经'
      })
      
      if (response.code === 200 && response.data) {
        newsData = response.data.records || []
        // 检查是否还有更多数据
        if (response.data.records.length < pageSize) {
          finished.value = true
        }
      }
    } else if (activeCategory.value === 'all') {
      // 全部新闻：混合真实财经新闻和虚拟数据
      if (page === 1) {
        // 第一页获取真实财经新闻
        const response = await newsApi.getNewsList({
          page: 1,
          pageSize: 5,
          category: '财经'
        })
        
        if (response.code === 200 && response.data) {
          newsData = response.data.records || []
        }
        
        // 添加虚拟数据
        newsData = [
          ...newsData,
          ...mockNewsData.tech.slice(0, 2),
          ...mockNewsData.sports.slice(0, 2),
          ...mockNewsData.entertainment.slice(0, 1)
        ]
      } else {
        // 后续页面使用虚拟数据
        const allMockData = [
          ...mockNewsData.tech,
          ...mockNewsData.sports,
          ...mockNewsData.entertainment,
          ...mockNewsData.society
        ]
        const startIndex = (page - 2) * pageSize
        newsData = allMockData.slice(startIndex, startIndex + pageSize)
        
        if (newsData.length < pageSize) {
          finished.value = true
        }
      }
    } else {
      // 其他分类使用虚拟数据
      const categoryKey = activeCategory.value as keyof typeof mockNewsData
      const mockData = mockNewsData[categoryKey] || []
      const startIndex = (page - 1) * pageSize
      newsData = mockData.slice(startIndex, startIndex + pageSize)
      
      if (newsData.length < pageSize) {
        finished.value = true
      }
    }
    
    if (isRefresh) {
      newsList.value = newsData
    } else {
      newsList.value.push(...newsData)
    }
    
    currentPage.value = page
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    showFailToast('获取新闻列表失败')
  }
}

// 加载更多
const onLoad = async () => {
  await fetchNewsList(currentPage.value + 1)
  loading.value = false
}

// 下拉刷新
const onRefresh = async () => {
  await fetchNewsList(1, true)
  refreshing.value = false
}

// 分类切换
const onCategoryChange = async () => {
  newsList.value = []
  currentPage.value = 1
  finished.value = false
  await fetchNewsList(1, true)
}

// 跳转到新闻详情
const goToDetail = (newsId: number) => {
  router.push(`/news/${newsId}`)
}

// 组件挂载
onMounted(() => {
  fetchNewsList(1, true)
})
</script>

<style scoped lang="scss">
.news-list-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 70px;
}

.content {
  padding-top: 46px;
}

.category-tabs {
  background: white;
  
  :deep(.van-tabs__wrap) {
    position: sticky;
    top: 46px;
    z-index: 99;
  }
  
  :deep(.van-tab) {
    flex: none;
    padding: 0 16px;
  }
}

.news-list {
  padding: 0 16px;
}

.news-item {
  display: flex;
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.news-content {
  flex: 1;
  margin-right: 12px;
}

.news-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.news-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  color: #333;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.news-source {
  color: var(--primary-color);
  font-weight: 500;
}

.news-image {
  flex-shrink: 0;
  
  :deep(.van-image) {
    border-radius: 6px;
    overflow: hidden;
  }
}

:deep(.van-pull-refresh) {
  min-height: calc(100vh - 140px);
}

:deep(.van-list) {
  padding: 16px 0;
}
</style>
