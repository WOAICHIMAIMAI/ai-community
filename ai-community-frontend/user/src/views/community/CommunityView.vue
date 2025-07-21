<template>
  <div class="community-container">
    <van-nav-bar
      title="社区"
      fixed
    >
      <template #right>
        <van-icon name="search" size="18" @click="onSearch" />
      </template>
    </van-nav-bar>
    
    <!-- 分类选择 -->
    <div class="category-tabs">
      <van-tabs v-model:active="activeCategory" sticky swipeable animated>
        <van-tab 
          v-for="category in categories" 
          :key="category" 
          :title="category"
          :name="category"
        ></van-tab>
      </van-tabs>
    </div>
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <!-- 热门/精华选择 -->
          <div class="filter-bar" v-if="activeCategory === '全部'">
            <div 
              :class="['filter-item', { active: activeFilter === 'latest' }]"
              @click="setFilter('latest')"
            >
              最新发布
            </div>
            <div 
              :class="['filter-item', { active: activeFilter === 'hot' }]"
              @click="setFilter('hot')"
            >
              热门
            </div>
            <div 
              :class="['filter-item', { active: activeFilter === 'essence' }]"
              @click="setFilter('essence')"
            >
              精华
            </div>
          </div>

          <!-- 帖子列表 -->
          <template v-if="posts.length > 0">
            <div class="post-list">
              <div 
                v-for="post in posts" 
                :key="post.id" 
                class="post-item"
                @click="goToDetail(post.id)"
              >
                <div class="post-header">
                  <div class="user-info">
                    <van-image
                      round
                      width="40"
                      height="40"
                      :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                      fit="cover"
                    />
                    <div class="user-name-time">
                      <span class="username">{{ post.nickname || post.username || '匿名用户' }}</span>
                      <span class="time">{{ formatDate(post.createdTime) }}</span>
                    </div>
                  </div>
                  <van-tag plain type="primary">{{ post.category }}</van-tag>
                </div>
                
                <div class="post-title">
                  <template v-if="post.isTop === 1">
                    <van-tag type="danger" style="margin-right: 4px;">置顶</van-tag>
                  </template>
                  <template v-if="post.isEssence === 1">
                    <van-tag type="success" style="margin-right: 4px;">精华</van-tag>
                  </template>
                  {{ post.title }}
                </div>
                
                <div class="post-content">{{ truncateContent(post.content) }}</div>
                
                <div class="post-images" v-if="post.images && post.images.length > 0">
                  <div class="image-grid">
                    <van-image
                      v-for="(img, index) in post.images.slice(0, 3)"
                      :key="index"
                      :src="img"
                      fit="cover"
                      class="grid-image"
                    />
                    <div v-if="post.images.length > 3" class="more-images">
                      +{{ post.images.length - 3 }}
                    </div>
                  </div>
                </div>
                
                <div class="post-stats">
                  <div class="stat-item">
                    <van-icon name="eye-o" /> {{ post.viewCount || 0 }}
                  </div>
                  <div class="stat-item">
                    <van-icon 
                      :name="post.hasLiked ? 'like' : 'like-o'" 
                      :color="post.hasLiked ? '#ee0a24' : ''" 
                    /> {{ post.likeCount || 0 }}
                  </div>
                  <div class="stat-item">
                    <van-icon name="chat-o" /> {{ post.commentCount || 0 }}
                  </div>
                </div>
              </div>
            </div>
          </template>
          
          <template v-else-if="!loading && finished">
            <van-empty description="暂无内容" />
          </template>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 发布按钮 -->
    <div class="publish-btn">
      <van-button round type="primary" icon="edit" @click="onPublish" />
    </div>

    <!-- 底部导航 -->
    <BottomTabbar />
    
    <!-- 搜索弹窗 -->
    <van-popup v-model:show="showSearch" position="top" :style="{ height: '100%' }">
      <div class="search-container">
        <van-search
          v-model="searchKeyword"
          show-action
          placeholder="搜索帖子"
          @search="onSearchSubmit"
          @cancel="showSearch = false"
        />
        
        <div class="search-history" v-if="searchHistory.length > 0 && !searchResults.length">
          <div class="history-header">
            <span>搜索历史</span>
            <van-icon name="delete" @click="clearSearchHistory" />
          </div>
          <div class="history-list">
            <van-tag 
              v-for="(item, index) in searchHistory" 
              :key="index" 
              plain 
              type="primary"
              closeable
              @click="searchKeyword = item; onSearchSubmit()"
              @close="removeSearchHistory(index)"
            >
              {{ item }}
            </van-tag>
          </div>
        </div>
        
        <div class="search-results" v-if="searchResults.length > 0">
          <div 
            v-for="post in searchResults" 
            :key="post.id" 
            class="search-result-item"
            @click="goToDetail(post.id)"
          >
            <div class="result-title">{{ post.title }}</div>
            <div class="result-content">{{ truncateContent(post.content) }}</div>
            <div class="result-meta">
              <span>{{ post.nickname || post.username || '匿名用户' }}</span>
              <span>{{ formatDate(post.createdTime) }}</span>
            </div>
          </div>
        </div>
        
        <template v-if="searchSubmitted && searchResults.length === 0">
          <van-empty description="没有找到相关内容" />
        </template>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeMount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { listPosts, getCategories, getHotPosts, getEssencePosts } from '@/api/post'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 分类
const categories = ref(getCategories())
const activeCategory = ref('全部')

// 筛选
const activeFilter = ref('latest')

// 分页
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

// 数据
const posts = ref<any[]>([])

// 搜索
const showSearch = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const searchSubmitted = ref(false)
const searchHistory = ref<string[]>([])

// 初始化
onBeforeMount(() => {
  // 检查URL参数中是否有指定分类
  const categoryParam = route.query.category as string
  if (categoryParam && categories.value.includes(categoryParam)) {
    activeCategory.value = categoryParam
    console.log('从URL参数设置分类:', categoryParam)
  }
})

onMounted(() => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 加载历史记录
  const history = localStorage.getItem('searchHistory')
  if (history) {
    try {
      searchHistory.value = JSON.parse(history)
    } catch (e) {
      searchHistory.value = []
    }
  }
})

// 监听分类变化
watch(activeCategory, () => {
  resetList()
})

// 监听筛选变化
watch(activeFilter, () => {
  resetList()
})

// 重置列表
const resetList = () => {
  page.value = 1
  posts.value = []
  finished.value = false
  loading.value = true
  onLoad()
}

// 加载数据
const onLoad = async () => {
  try {
    // 构建查询参数
    const params = {
      page: page.value,
      pageSize: pageSize.value,
    }

    // 根据分类筛选
    if (activeCategory.value !== '全部') {
      params['category' as keyof typeof params] = activeCategory.value
    }

    let res: any

    // 根据筛选条件选择API
    if (activeFilter.value === 'hot') {
      // 热门帖子 - 使用通用API并添加排序参数
      res = await listPosts({
        ...params,
        sortBy: 'likeCount'
      })
    } else if (activeFilter.value === 'essence') {
      // 精华帖子 - 使用通用API并添加精华标识
      res = await listPosts({
        ...params,
        isEssence: 1
      })
    } else {
      // 最新发布 - 使用通用API
      res = await listPosts(params)
    }

    if (res && res.code === 200 && res.data) {
      const newPosts = res.data.records || []
      
      // 处理图片数据，将字符串转为数组
      newPosts.forEach((post: any) => {
        if (post.images) {
          post.images = typeof post.images === 'string' 
            ? post.images.split(',') 
            : post.images
        } else {
          post.images = []
        }
      })
      
      posts.value.push(...newPosts)
      
      // 更新分页状态
      page.value++
      if (newPosts.length < pageSize.value) {
        finished.value = true
      }
    } else {
      finished.value = true
      if (res && res.code !== 200) {
        showFailToast('获取帖子列表失败')
      }
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    showFailToast('获取帖子列表失败')
    finished.value = true
  } finally {
    loading.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  resetList()
  refreshing.value = false
}

// 搜索
const onSearch = () => {
  showSearch.value = true
  searchKeyword.value = ''
  searchResults.value = []
  searchSubmitted.value = false
}

// 提交搜索
const onSearchSubmit = async () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  
  try {
    searchSubmitted.value = true
    
    // 保存搜索历史
    if (!searchHistory.value.includes(searchKeyword.value)) {
      searchHistory.value.unshift(searchKeyword.value)
      if (searchHistory.value.length > 10) {
        searchHistory.value.pop()
      }
      localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
    }
    
    const res = await listPosts({
      page: 1,
      pageSize: 20,
      keyword: searchKeyword.value
    })
    
    if (res && res.code === 200 && res.data) {
      searchResults.value = res.data.records || []
      
      // 处理图片数据
      searchResults.value.forEach((post: any) => {
        if (post.images) {
          post.images = typeof post.images === 'string' 
            ? post.images.split(',') 
            : post.images
        } else {
          post.images = []
        }
      })
    } else {
      searchResults.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
    showFailToast('搜索失败')
  }
}

// 清除搜索历史
const clearSearchHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

// 移除某条搜索历史
const removeSearchHistory = (index: number) => {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

// 设置筛选条件
const setFilter = (filter: string) => {
  activeFilter.value = filter
}

// 跳转到帖子详情
const goToDetail = (postId: number) => {
  router.push(`/post-detail/${postId}`)
}

// 发布帖子
const onPublish = () => {
  router.push('/post-publish')
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  
  // 计算时间差（毫秒）
  const diff = now.getTime() - date.getTime()
  
  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  
  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }
  
  // 小于30天
  if (diff < 30 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }
  
  // 大于30天，显示具体日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 截取内容
const truncateContent = (content: string) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}
</script>

<style scoped lang="scss">
.community-container {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 50px;
  background-color: #f7f8fa;
}

.category-tabs {
  position: sticky;
  top: 46px;
  z-index: 2;
  background-color: #fff;
}

.content {
  padding-bottom: 60px;
}

.filter-bar {
  display: flex;
  background-color: #fff;
  padding: 10px 16px;
  border-bottom: 1px solid #f2f2f2;
  
  .filter-item {
    margin-right: 16px;
    font-size: 14px;
    color: #666;
    position: relative;
    padding-bottom: 4px;
    
    &.active {
      color: var(--primary-color);
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 2px;
        background-color: var(--primary-color);
        border-radius: 1px;
      }
    }
  }
}

.post-list {
  .post-item {
    background-color: #fff;
    margin-bottom: 10px;
    padding: 16px;
    
    .post-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .user-info {
        display: flex;
        align-items: center;
        
        .user-name-time {
          margin-left: 10px;
          display: flex;
          flex-direction: column;
          
          .username {
            font-size: 14px;
            color: #323233;
            margin-bottom: 2px;
          }
          
          .time {
            font-size: 12px;
            color: #969799;
          }
        }
      }
    }
    
    .post-title {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 10px;
      color: #323233;
    }
    
    .post-content {
      font-size: 14px;
      color: #646566;
      margin-bottom: 12px;
      line-height: 1.5;
    }
    
    .post-images {
      margin-bottom: 12px;
      
      .image-grid {
        display: flex;
        flex-wrap: wrap;
        
        .grid-image {
          width: calc(33.33% - 5px);
          height: 90px;
          margin-right: 5px;
          margin-bottom: 5px;
          border-radius: 4px;
          overflow: hidden;
          position: relative;
        }
        
        .more-images {
          position: absolute;
          right: 10px;
          bottom: 10px;
          background-color: rgba(0, 0, 0, 0.5);
          color: #fff;
          padding: 2px 6px;
          border-radius: 10px;
          font-size: 12px;
        }
      }
    }
    
    .post-stats {
      display: flex;
      color: #969799;
      font-size: 12px;
      
      .stat-item {
        margin-right: 16px;
        display: flex;
        align-items: center;
        
        .van-icon {
          margin-right: 4px;
          font-size: 16px;
        }
      }
    }
  }
}

.publish-btn {
  position: fixed;
  right: 20px;
  bottom: 70px;
  z-index: 10;
  
  .van-button {
    width: 50px;
    height: 50px;
    
    .van-icon {
      font-size: 20px;
    }
  }
}

.search-container {
  background-color: #f7f8fa;
  min-height: 100%;
  
  .search-history {
    padding: 16px;
    
    .history-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      font-size: 14px;
      color: #323233;
    }
    
    .history-list {
      display: flex;
      flex-wrap: wrap;
      
      .van-tag {
        margin-right: 8px;
        margin-bottom: 8px;
        padding: 4px 8px;
      }
    }
  }
  
  .search-results {
    .search-result-item {
      background-color: #fff;
      padding: 12px 16px;
      margin-bottom: 8px;
      
      .result-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 6px;
        color: #323233;
      }
      
      .result-content {
        font-size: 14px;
        color: #646566;
        margin-bottom: 8px;
      }
      
      .result-meta {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #969799;
      }
    }
  }
}
</style> 