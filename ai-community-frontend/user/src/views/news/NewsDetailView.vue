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
          
          <!-- 封面图片 -->
          <div class="news-cover" v-if="newsDetail.coverImage">
            <van-image
              :src="newsDetail.coverImage"
              width="100%"
              fit="cover"
              :lazy-load="true"
            />
          </div>
          
          <!-- 新闻内容 -->
          <div class="news-content" v-html="formatContent(newsDetail.content)"></div>
          
          <!-- 新闻图片 -->
          <div class="news-images" v-if="newsDetail.images">
            <van-image
              v-for="(image, index) in parseImages(newsDetail.images)"
              :key="index"
              :src="image"
              width="100%"
              fit="cover"
              :lazy-load="true"
              @click="previewImage(image, parseImages(newsDetail.images))"
            />
          </div>
          
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
import { showFailToast, showSuccessToast, showImagePreview } from 'vant'
import { newsApi, mockNewsData, type NewsItem } from '@/api/news'

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

// 解析图片
const parseImages = (images: string) => {
  return images ? images.split(',').filter(img => img.trim()) : []
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
    
    // 先尝试从真实API获取
    if (newsId.value < 1000) {
      const response = await newsApi.getNewsDetail(newsId.value)
      if (response.code === 200 && response.data) {
        newsDetail.value = response.data
        return
      }
    }
    
    // 如果API没有数据，从虚拟数据中查找
    const allMockData = [
      ...mockNewsData.tech,
      ...mockNewsData.sports,
      ...mockNewsData.entertainment,
      ...mockNewsData.society
    ]
    
    const mockNews = allMockData.find(news => news.id === newsId.value)
    if (mockNews) {
      // 为虚拟数据添加完整的内容
      newsDetail.value = {
        ...mockNews,
        content: generateMockContent(mockNews.title, mockNews.summary),
        images: `https://picsum.photos/400/300?random=${newsId.value},https://picsum.photos/400/300?random=${newsId.value + 1}`,
        tags: getMockTags(mockNews.category),
        sourceUrl: `https://example.com/news/${newsId.value}`
      } as NewsItem
    } else {
      newsDetail.value = null
    }
  } catch (error) {
    console.error('获取新闻详情失败:', error)
    showFailToast('获取新闻详情失败')
    newsDetail.value = null
  } finally {
    loading.value = false
  }
}

// 生成虚拟内容
const generateMockContent = (title: string, summary: string) => {
  return `
    <p>${summary}</p>
    
    <p>据相关报道，${title.substring(0, 10)}相关事件引起了广泛关注。专家表示，这一发展趋势将对行业产生深远影响。</p>
    
    <p>**重要进展**</p>
    <p>在最新的发展中，相关部门采取了一系列措施来推进这一进程。这些措施包括：</p>
    <p>• 加强政策支持和引导</p>
    <p>• 完善相关制度和标准</p>
    <p>• 促进产业协调发展</p>
    
    <p>**专家观点**</p>
    <p>业内专家认为，这一发展将带来新的机遇和挑战。相关企业和机构需要积极应对，抓住发展机遇。</p>
    
    <p>**未来展望**</p>
    <p>展望未来，随着相关政策的进一步完善和技术的不断进步，预计将会有更多积极的变化。</p>
    
    <p>我们将持续关注相关进展，为读者提供最新的资讯和分析。</p>
  `
}

// 获取虚拟标签
const getMockTags = (category: string) => {
  const tagMap: Record<string, string> = {
    '科技': '人工智能,创新,技术',
    '体育': '运动,健康,竞技',
    '娱乐': '文化,艺术,影视',
    '社会': '民生,社区,服务',
    '财经': '经济,金融,投资'
  }
  return tagMap[category] || '热点,资讯'
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

// 预览图片
const previewImage = (current: string, images: string[]) => {
  showImagePreview({
    images,
    startPosition: images.indexOf(current)
  })
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

.news-cover {
  margin-bottom: 20px;
  
  :deep(.van-image) {
    width: 100%;
  }
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

.news-images {
  padding: 0 20px 20px;
  display: grid;
  gap: 12px;
  
  :deep(.van-image) {
    border-radius: 8px;
    cursor: pointer;
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
