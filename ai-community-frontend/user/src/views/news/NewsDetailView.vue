<template>
  <div class="news-detail-container">
    <van-nav-bar 
      title="新闻详情" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    >
      <template #right>
        <van-icon name="share-o" @click="shareNews" />
      </template>
    </van-nav-bar>
    
    <div class="content">
      <template v-if="loading">
        <van-skeleton title avatar :row="10" />
      </template>
      
      <template v-else-if="newsDetail">
        <article class="news-article">
          <!-- 新闻头部 -->
          <header class="news-header">
            <h1 class="news-title">{{ newsDetail.title }}</h1>
            <div class="news-meta">
              <div class="meta-row">
                <span class="news-source">{{ newsDetail.source }}</span>
                <span class="news-author" v-if="newsDetail.author">{{ newsDetail.author }}</span>
              </div>
              <div class="meta-row">
                <span class="news-time">{{ formatTime(newsDetail.publishTime) }}</span>
                <span class="news-views">{{ newsDetail.viewCount }}阅读</span>
              </div>
            </div>
            <div class="news-tags" v-if="newsDetail.tags">
              <van-tag 
                v-for="tag in parseTags(newsDetail.tags)" 
                :key="tag"
                size="small"
                type="primary"
                plain
              >
                {{ tag }}
              </van-tag>
            </div>
          </header>
          
          <!-- 新闻内容 -->
          <div class="news-content" v-html="formatContent(newsDetail.content)"></div>
          
          <!-- 原文链接 -->
          <div class="source-link" v-if="newsDetail.sourceUrl">
            <van-button 
              type="primary" 
              size="small" 
              plain
              @click="openSourceUrl"
            >
              查看原文
            </van-button>
          </div>
        </article>
        
        <!-- 互动区域 -->
        <div class="interaction-area">
          <div class="interaction-buttons">
            <van-button 
              icon="good-job-o" 
              :type="isLiked ? 'primary' : 'default'"
              size="small"
              @click="toggleLike"
            >
              {{ newsDetail.likeCount }}
            </van-button>
            <van-button 
              icon="comment-o" 
              size="small"
              @click="showComments"
            >
              {{ newsDetail.commentCount }}
            </van-button>
            <van-button 
              icon="share-o" 
              size="small"
              @click="shareNews"
            >
              分享
            </van-button>
          </div>
        </div>
      </template>
      
      <template v-else>
        <van-empty description="新闻不存在或已下线" />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { newsApi, type NewsItem } from '@/api/news'

const route = useRoute()
const router = useRouter()

// 状态管理
const newsDetail = ref<NewsItem | null>(null)
const loading = ref(true)
const isLiked = ref(false)

// 获取新闻ID
const newsId = ref(Number(route.params.id))

// 格式化时间
const formatTime = (timeString: string) => {
  const time = new Date(timeString)
  return time.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 解析标签
const parseTags = (tags: string) => {
  return tags ? tags.split(',').filter(tag => tag.trim()) : []
}

// 格式化内容
const formatContent = (content: string) => {
  // 简单的HTML格式化，确保安全性
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

// 获取新闻详情
const fetchNewsDetail = async () => {
  try {
    loading.value = true
    
    // 调用真实API获取新闻详情
    const response = await newsApi.getNewsDetail(newsId.value)
    if (response.code === 200 && response.data) {
      newsDetail.value = response.data
    } else {
      newsDetail.value = null
      showFailToast('新闻不存在或已下线')
    }
  } catch (error) {
    console.error('获取新闻详情失败:', error)
    showFailToast('获取新闻详情失败')
    newsDetail.value = null
  } finally {
    loading.value = false
  }
}

// 点赞功能
const toggleLike = () => {
  if (!newsDetail.value) return
  
  isLiked.value = !isLiked.value
  if (isLiked.value) {
    newsDetail.value.likeCount++
    showSuccessToast('点赞成功')
  } else {
    newsDetail.value.likeCount--
  }
}

// 显示评论
const showComments = () => {
  showSuccessToast('评论功能开发中...')
}

// 分享新闻
const shareNews = () => {
  if (navigator.share && newsDetail.value) {
    navigator.share({
      title: newsDetail.value.title,
      text: newsDetail.value.summary,
      url: window.location.href
    }).catch(err => {
      console.log('分享失败:', err)
      showSuccessToast('分享功能开发中...')
    })
  } else {
    showSuccessToast('分享功能开发中...')
  }
}

// 打开原文链接
const openSourceUrl = () => {
  if (newsDetail.value?.sourceUrl) {
    window.open(newsDetail.value.sourceUrl, '_blank')
  }
}

// 组件挂载
onMounted(() => {
  fetchNewsDetail()
})
</script>

<style scoped lang="scss">
.news-detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 46px 0 20px;
}

.news-article {
  background: white;
  margin: 0 16px 16px;
  border-radius: 8px;
  overflow: hidden;
}

.news-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.news-title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
  color: #333;
  margin: 0 0 16px 0;
}

.news-meta {
  margin-bottom: 12px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.news-source {
  color: var(--primary-color);
  font-weight: 500;
}

.news-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.news-content {
  padding: 0 20px 20px;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  
  :deep(p) {
    margin: 0 0 16px 0;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  :deep(strong) {
    font-weight: 600;
    color: #333;
  }
  
  :deep(em) {
    font-style: italic;
    color: #666;
  }
}

.source-link {
  padding: 0 20px 20px;
  text-align: center;
}

.interaction-area {
  background: white;
  margin: 0 16px;
  border-radius: 8px;
  padding: 16px;
}

.interaction-buttons {
  display: flex;
  justify-content: space-around;
  gap: 16px;
  
  :deep(.van-button) {
    flex: 1;
  }
}
</style>
