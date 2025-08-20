<template>
  <div class="home-container">
    <van-nav-bar title="社区百事通" fixed />
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <!-- 轮播图 -->
        <div class="banner-section">
          <van-swipe class="banner-swipe" :autoplay="3000" indicator-color="white">
            <van-swipe-item v-for="(item, index) in bannerList" :key="index">
              <div class="banner-item">
                <img :src="item.imageUrl" alt="banner" />
              </div>
            </van-swipe-item>
          </van-swipe>
        </div>

        <!-- 功能入口 -->
        <div class="feature-section">
          <van-grid :column-num="4" :border="false">
            <van-grid-item icon="service-o" text="在线报修" to="/repair" />
            <van-grid-item icon="comment-o" text="社区互动" to="/community" />
            <van-grid-item icon="gold-coin-o" text="费用缴纳" to="/payment" />
            <van-grid-item icon="calendar-o" text="预约服务" to="/appointment" />
            <van-grid-item icon="gift-o" text="红包活动" to="/red-packet" />
            <van-grid-item icon="chat-o" text="聊天消息" @click="goToChat" />
            <van-grid-item icon="newspaper-o" text="新闻资讯" to="/news" />
            <van-grid-item icon="question-o" text="常见问题" to="/common-problems" />
            <van-grid-item icon="bullhorn-o" text="通知公告" @click="goToAnnouncementList" />
            <van-grid-item icon="user-o" text="个人中心" to="/profile" />
            <van-grid-item icon="wechat-o" text="AI聊天" @click="goToAiChat" />
          </van-grid>
        </div>

        <!-- 社区公告 -->
        <div class="notice-section">
          <div class="section-header">
            <span class="section-title">社区公告</span>
            <MoreButton text="更多" @click="goToAnnouncementList" />
          </div>
          <van-cell-group inset>
            <template v-if="loadingAnnouncements">
              <van-cell>
                <template #title>
                  <van-skeleton title :row="1" />
                </template>
              </van-cell>
              <van-cell>
                <template #title>
                  <van-skeleton title :row="1" />
                </template>
              </van-cell>
              <van-cell>
                <template #title>
                  <van-skeleton title :row="1" />
                </template>
              </van-cell>
            </template>
            <template v-else-if="announcements.length === 0">
              <van-empty description="暂无公告" />
            </template>
            <template v-else>
              <van-cell 
                v-for="notice in announcements" 
                :key="notice.id" 
                :title="notice.title" 
                is-link
                @click="goToPostDetail(notice.id)"
              >
                <template #label>
                  <span class="notice-date">{{ formatDate(notice.createTime) }}</span>
                </template>
              </van-cell>
            </template>
          </van-cell-group>
        </div>

        <!-- 热点新闻 -->
        <div class="news-section">
          <div class="section-header">
            <span class="section-title">热点新闻</span>
            <MoreButton text="更多" @click="goToNewsList" />
          </div>
          <van-cell-group inset>
            <template v-if="loadingNews">
              <van-cell>
                <template #title>
                  <van-skeleton title :row="1" />
                </template>
              </van-cell>
              <van-cell>
                <template #title>
                  <van-skeleton title :row="1" />
                </template>
              </van-cell>
            </template>
            <template v-else-if="hotNews.length === 0">
              <van-empty description="暂无新闻" />
            </template>
            <template v-else>
              <van-cell
                v-for="news in hotNews"
                :key="news.id"
                :title="news.title"
                is-link
                @click="goToNewsDetail(news.id)"
              >
                <template #label>
                  <div class="news-meta">
                    <span class="news-source">{{ news.source }}</span>
                    <span class="news-date">{{ formatDate(news.publishTime) }}</span>
                  </div>
                </template>
                <template #right-icon>
                  <van-tag v-if="news.isHot" type="danger" size="small">热</van-tag>
                </template>
              </van-cell>
            </template>
          </van-cell-group>
        </div>

        <!-- 社区动态 -->
        <div class="posts-section">
          <div class="section-header">
            <span class="section-title">社区动态</span>
            <MoreButton text="更多" @click="goToCommunityUpdates" />
          </div>
          <div class="posts-list">
            <template v-if="loadingUpdates">
              <van-skeleton title avatar :row="3" style="margin-bottom: 10px;" />
              <van-skeleton title avatar :row="3" style="margin-bottom: 10px;" />
              <van-skeleton title avatar :row="3" />
            </template>
            <template v-else-if="communityUpdates.length === 0">
              <van-empty description="暂无动态" />
            </template>
            <template v-else>
              <van-card
                v-for="post in communityUpdates"
                :key="post.id"
                :desc="truncateContent(post.content)"
                :title="post.title"
                :thumb="getPostImage(post)"
                @click="goToPostDetail(post.id)"
              >
                <template #tags>
                  <van-tag plain type="primary" style="margin-right: 5px;">{{ post.category || '动态' }}</van-tag>
                  <van-tag plain type="success">{{ post.likeCount || 0 }}点赞</van-tag>
                </template>
                <template #footer>
                  <div class="post-footer">
                    <div class="post-author">
                      <van-image
                        :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                        round
                        width="32"
                        height="32"
                        class="author-avatar"
                        @click.stop="goToUserProfile(post.userId || post.authorId || post.createBy)"
                        style="cursor: pointer;"
                      />
                      <div class="author-info">
                        <span class="author-name">{{ post.nickname || post.username || '匿名用户' }}</span>
                        <span class="post-time">{{ formatDate(post.createTime || post.createdTime) }}</span>
                      </div>
                    </div>
                  </div>
                </template>
              </van-card>
            </template>
          </div>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { useAuthStore } from '@/store/auth'
import { getAnnouncements, getCommunityUpdates, getAllPosts } from '@/api/post'
import { newsApi, type NewsItem } from '@/api/news'
import { handleUserAvatarClick } from '@/utils/userUtils'
import BottomTabbar from '@/components/BottomTabbar.vue'
import MoreButton from '@/components/MoreButton.vue'

const authStore = useAuthStore()
const router = useRouter()
const goToAiChat = () => {
  console.log('[AI聊天导航] 按钮被点击，跳转到AI聊天列表');
  try {
    // 跳转到AI聊天列表页面
    router.push('/ai-chat');
  } catch (error) {
    console.error('[AI聊天导航] 路由跳转异常:', error);
  }
}
const goToChat = () => {
  console.log('[聊天导航] 按钮被点击，跳转到聊天列表');
  try {
    router.push('/chat-list');
  } catch (error) {
    console.error('[聊天导航] 路由跳转异常:', error);
  }
}
const refreshing = ref(false)

// 模拟数据 - 轮播图
const bannerList = ref([
  { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg', link: '' },
  { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg', link: '' },
  { imageUrl: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-3.jpeg', link: '' },
])

// 加载状态
const loadingAnnouncements = ref(true)
const loadingUpdates = ref(true)
const loadingNews = ref(true)

// 社区公告
const announcements = ref<any[]>([])

// 社区动态
const communityUpdates = ref<any[]>([])

// 热点新闻
const hotNews = ref<NewsItem[]>([])

// 模拟数据 - 社区公告
const mockAnnouncements = [
  { 
    id: 1001, 
    title: '关于小区停水通知', 
    content: '因供水管道维修，本小区将于6月20日上午9:00-下午3:00停水，请居民提前做好储水准备。',
    createTime: '2023-06-15T10:00:00',
    category: '公告',
    userId: 1,
    authorName: '物业管理处',
    viewCount: 120,
    likeCount: 0,
    commentCount: 0,
    isTop: 1
  },
  { 
    id: 1002, 
    title: '社区活动中心开放时间调整', 
    content: '自下周起，社区活动中心开放时间调整为上午8:30-下午6:00，请各位居民知悉。',
    createTime: '2023-06-10T14:30:00',
    category: '公告',
    userId: 1,
    authorName: '社区服务中心',
    viewCount: 98,
    likeCount: 5,
    commentCount: 2,
    isTop: 1
  },
  { 
    id: 1003, 
    title: '小区绿化养护工作安排', 
    content: '为维护小区环境，下周将进行绿化养护工作，可能会产生一些噪音，请居民理解配合。',
    createTime: '2023-06-05T09:15:00',
    category: '公告',
    userId: 1,
    authorName: '物业管理处',
    viewCount: 86,
    likeCount: 8,
    commentCount: 0,
    isTop: 0
  }
]

// 模拟数据 - 社区动态
const mockUpdates = [
  { 
    id: 2001, 
    title: '小区环境整治建议', 
    content: '最近发现小区有些地方垃圾较多，希望大家共同维护环境，不要随地丢垃圾。另外建议物业增加垃圾桶数量。',
    createTime: '2023-06-14T15:20:00',
    category: '动态',
    userId: 101,
    authorName: '张三',
    viewCount: 45,
    likeCount: 15,
    commentCount: 8,
    isTop: 0,
    images: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  },
  { 
    id: 2002, 
    title: '分享一个节水小妙招', 
    content: '夏天到了，用水高峰期，分享几个家庭节水的小技巧：1. 洗菜水可以用来浇花；2. 空调冷凝水收集后可以拖地；3. 淘米水可以洗碗。',
    createTime: '2023-06-13T10:45:00',
    category: '动态',
    userId: 102,
    authorName: '李四',
    viewCount: 67,
    likeCount: 28,
    commentCount: 12,
    isTop: 0,
    images: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
  },
  { 
    id: 2003, 
    title: '社区读书会活动回顾', 
    content: '上周末的读书会非常成功，感谢大家的参与和分享。我们交流了《人类简史》的阅读心得，下次读书会将在月底举行，欢迎大家继续参加！',
    createTime: '2023-06-12T18:30:00',
    category: '动态',
    userId: 103,
    authorName: '王五',
    viewCount: 53,
    likeCount: 36,
    commentCount: 15,
    isTop: 0,
    images: 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-3.jpeg'
  }
]

// 获取公告列表
const fetchAnnouncements = async () => {
  loadingAnnouncements.value = true
  try {
    const res = await getAnnouncements({
      page: 1,
      pageSize: 3
    })
    if (res && res.code === 200) {
      // 如果返回的数据为空，使用模拟数据
      announcements.value = (res.data.records && res.data.records.length > 0) 
        ? res.data.records 
        : mockAnnouncements
    } else {
      announcements.value = mockAnnouncements
    }
  } catch (error) {
    console.error('获取公告失败:', error)
    showFailToast('获取公告失败，显示模拟数据')
    announcements.value = mockAnnouncements
  } finally {
    loadingAnnouncements.value = false
  }
}

// 获取社区动态
const fetchCommunityUpdates = async () => {
  loadingUpdates.value = true
  try {
    // 尝试获取动态类别的帖子
    const res = await getCommunityUpdates({
      page: 1,
      pageSize: 3
    })
    console.log('社区动态API返回:', res)
    
    if (res && res.code === 200 && res.data && res.data.records && res.data.records.length > 0) {
      communityUpdates.value = res.data.records
    } else {
      console.warn('动态类别帖子为空，尝试获取所有帖子')
      // 如果动态类别没有帖子，尝试获取所有帖子
      const allPostsRes = await getAllPosts({
        page: 1,
        pageSize: 3
      })
      
      if (allPostsRes && allPostsRes.code === 200 && allPostsRes.data && allPostsRes.data.records && allPostsRes.data.records.length > 0) {
        communityUpdates.value = allPostsRes.data.records
      } else {
        console.warn('所有帖子也为空，使用备用数据')
        communityUpdates.value = mockUpdates
      }
    }
  } catch (error) {
    console.error('获取社区动态失败:', error)
    showFailToast('获取社区动态失败，显示模拟数据')
    communityUpdates.value = mockUpdates
  } finally {
    loadingUpdates.value = false
  }
}

// 获取热点新闻
const fetchHotNews = async () => {
  try {
    loadingNews.value = true
    const response = await newsApi.getHotNews(3)
    if (response.code === 200 && response.data) {
      hotNews.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取热点新闻失败:', error)
    // 使用虚拟数据作为降级
    hotNews.value = [
      {
        id: 1001,
        title: "财经市场迎来新机遇，投资者关注度持续上升",
        source: "财经日报",
        publishTime: "2024-01-20 10:30:00",
        isHot: 1
      },
      {
        id: 1002,
        title: "科技创新推动产业升级，数字化转型加速",
        source: "科技周刊",
        publishTime: "2024-01-20 09:15:00",
        isHot: 1
      },
      {
        id: 1003,
        title: "社区服务质量提升，居民满意度显著改善",
        source: "社区新闻",
        publishTime: "2024-01-20 08:45:00",
        isHot: 0
      }
    ] as NewsItem[]
  } finally {
    loadingNews.value = false
  }
}

// 截取内容
const truncateContent = (content: string) => {
  if (!content) return ''
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 跳转到帖子详情
const goToPostDetail = (postId: number) => {
  router.push(`/post-detail/${postId}`)
}

// 跳转到公告列表
const goToAnnouncementList = () => {
  // 跳转到社区互动页面并选择公告分类
  router.push({
    path: '/community',
    query: { category: '公告' }
  })
}

// 跳转到社区动态列表
const goToCommunityUpdates = () => {
  // 跳转到社区互动页面并选择动态分类
  router.push({
    path: '/community',
    query: { category: '动态' }
  })
}

// 跳转到新闻列表
const goToNewsList = () => {
  router.push('/news')
}

// 跳转到新闻详情
const goToNewsDetail = (newsId: number) => {
  router.push(`/news/${newsId}`)
}

// 获取帖子图片
const getPostImage = (post) => {
  if (!post.images) return 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
  
  if (typeof post.images === 'string') {
    if (post.images.includes(',')) {
      return post.images.split(',')[0]
    }
    return post.images
  }
  
  if (Array.isArray(post.images) && post.images.length > 0) {
    return post.images[0]
  }
  
  return 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
}

// 获取作者名称
const getAuthorName = (post) => {
  return post.authorName || post.username || post.nickName || '匿名用户'
}

// 获取帖子日期
const getPostDate = (post) => {
  return post.createTime || post.createdTime || post.createDate || new Date().toISOString()
}

// 跳转到用户个人主页
const goToUserProfile = (userId: number) => {
  if (userId) {
    router.push(`/user/${userId}`)
  }
}

onMounted(() => {
  // 检查登录状态
  console.log('HomeView - 登录状态:', authStore.isLoggedIn)
  
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 获取数据
  fetchAnnouncements()
  fetchCommunityUpdates()
  fetchHotNews()
})

// 下拉刷新
const onRefresh = async () => {
  try {
    await Promise.all([
      fetchAnnouncements(),
      fetchCommunityUpdates(),
      fetchHotNews()
    ])
    showToast('刷新成功')
  } catch (error) {
    console.error('刷新失败:', error)
    showFailToast('刷新失败')
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped lang="scss">
.home-container {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 50px;
  background-color: #f7f8fa;
}

.content {
  padding-bottom: 20px;
}

.banner-section {
  padding: 10px;
  
  .banner-swipe {
    height: 160px;
    border-radius: 8px;
    overflow: hidden;
    
    .banner-item {
      width: 100%;
      height: 100%;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

.feature-section {
  margin: 10px 0;
  background-color: #fff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  
  .section-title {
    font-size: 16px;
    font-weight: bold;
    position: relative;
    padding-left: 10px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 16px;
      background-color: var(--primary-color);
      border-radius: 3px;
    }
  }
}

.notice-section, .news-section {
  margin-bottom: 10px;

  .notice-date, .news-date {
    font-size: 12px;
    color: var(--text-color-light);
  }
}

.news-section {
  .news-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;

    .news-source {
      color: var(--primary-color);
      font-weight: 500;
    }

    .news-date {
      color: var(--text-color-light);
    }
  }
}

.posts-section {
  .posts-list {
    padding: 0 10px;
    
    .van-card {
      margin-bottom: 10px;
      border-radius: 8px;
    }
    
    .post-footer {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: var(--text-color-light);
      
      .post-author {
        color: var(--primary-color);
      }
    }
  }
}
</style>
