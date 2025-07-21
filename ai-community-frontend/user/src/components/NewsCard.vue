<template>
  <div class="news-card" @click="$emit('click')">
    <div class="news-content">
      <div class="news-header">
        <h3 class="news-title">{{ news.title }}</h3>
        <div class="news-badges">
          <van-tag v-if="news.isHot" type="danger" size="small">热点</van-tag>
          <van-tag v-if="news.isTop" type="primary" size="small">置顶</van-tag>
        </div>
      </div>
      <p class="news-summary" v-if="news.summary">{{ news.summary }}</p>
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
</template>

<script setup lang="ts">
import type { NewsItem } from '@/api/news'

interface Props {
  news: NewsItem
}

defineProps<Props>()
defineEmits<{
  click: []
}>()

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
</script>

<style scoped lang="scss">
.news-card {
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

.news-badges {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
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
</style>
