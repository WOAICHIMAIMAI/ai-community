<template>
  <div class="user-profile-container">
    <van-nav-bar 
      title="用户资料" 
      left-arrow 
      @click-left="goBack"
      fixed
    >
      <template #right>
        <van-icon name="ellipsis" @click="showMoreActions = true" />
      </template>
    </van-nav-bar>
    
    <div class="profile-content">
      <!-- 用户基本信息 -->
      <div class="user-info-section">
        <template v-if="loading">
          <van-skeleton title avatar :row="3" />
        </template>
        <template v-else-if="userInfo">
          <div class="user-header">
            <van-image
              :src="userInfo.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
              width="80"
              height="80"
              round
              class="user-avatar"
            />
            <div class="user-details">
              <div class="user-name">{{ userInfo.nickname || userInfo.username || '加载中...' }}</div>
              <div class="user-id">ID: {{ userInfo.id || '' }}</div>
              <div class="verification-tag">
                <template v-if="userInfo.isVerified === 1">
                  <van-tag type="success" size="small">已认证</van-tag>
                </template>
                <template v-else>
                  <van-tag plain type="primary" size="small" @click="goToVerification">去认证</van-tag>
                </template>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 功能菜单 -->
      <van-cell-group inset class="menu-section">
        <van-cell title="朋友资料" is-link @click="showUserDetail" />
        <van-cell title="朋友权限" is-link />
      </van-cell-group>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <van-button 
          type="default" 
          size="large" 
          icon="chat-o"
          @click="startChat"
          class="action-btn"
        >
          发消息
        </van-button>
        <van-button 
          type="default" 
          size="large" 
          icon="video-o"
          class="action-btn"
        >
          音视频通话
        </van-button>
      </div>

      <!-- 用户帖子列表 -->
      <div class="user-posts-section">
        <div class="section-header">
          <span class="section-title">TA的帖子</span>
          <span class="post-count" v-if="!loadingPosts">({{ userPosts.length }})</span>
        </div>
        
        <van-list
          v-model:loading="loadingPosts"
          :finished="postsFinished"
          finished-text="没有更多了"
          @load="onLoadMorePosts"
        >
          <template v-if="userPosts.length === 0 && !loadingPosts">
            <van-empty description="暂无帖子" />
          </template>
          <template v-else>
            <div class="posts-list">
              <van-card
                v-for="post in userPosts"
                :key="post.id"
                :desc="truncateContent(post.content)"
                :title="post.title"
                :thumb="getPostImage(post)"
                @click="goToPostDetail(post.id)"
                class="post-card"
              >
                <template #tags>
                  <van-tag plain type="primary" size="mini">{{ post.category || '动态' }}</van-tag>
                  <van-tag plain type="success" size="mini">{{ post.likeCount || 0 }}点赞</van-tag>
                  <van-tag plain size="mini">{{ post.viewCount || 0 }}浏览</van-tag>
                </template>
                <template #footer>
                  <div class="post-footer">
                    <span class="post-date">{{ formatDate(post.createTime || post.createdTime) }}</span>
                  </div>
                </template>
              </van-card>
            </div>
          </template>
        </van-list>
      </div>
    </div>

    <!-- 更多操作弹窗 -->
    <van-action-sheet
      v-model:show="showMoreActions"
      :actions="moreActions"
      @select="onSelectAction"
    />

    <!-- 底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showFailToast, showLoadingToast } from 'vant'
import { getUserInfo, getUserInfoById, getUserPosts } from '@/api/user'
import { chatApi } from '@/api/chatApi'
import { useAuthStore } from '@/store/auth'
import BottomTabbar from '@/components/BottomTabbar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 确保userId是字符串，直接从路由参数获取字符串
const userId = ref(route.params.id as string)
const userInfo = ref<any>(null)
const userPosts = ref<any[]>([])
const loading = ref(true)
const loadingPosts = ref(false)
const showMoreActions = ref(false)
const postsPage = ref(1)
const postsPageSize = ref(10)
const postsFinished = ref(false)

const moreActions = [
  { name: '举报', value: 'report' },
  { name: '拉黑', value: 'block' }
]

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    console.log('正在获取用户信息，userId:', userId.value)
    
    const res = await getUserInfoById(userId.value)
    console.log('User info response:', res)
    
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
      console.log('获取到的用户信息:', userInfo.value)
    } else {
      console.error('获取用户信息失败:', res.message || '未知错误')
      showFailToast('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    showFailToast('获取用户信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取用户帖子
const fetchUserPosts = async (isRefresh = false) => {
  if (isRefresh) {
    postsPage.value = 1
    userPosts.value = []
    postsFinished.value = false
  }
  
  if (postsFinished.value) return
  
  loadingPosts.value = true
  try {
    console.log('正在获取用户帖子，userId:', userId.value, 'page:', postsPage.value)
    
    const res = await getUserPosts(userId.value, {
      page: postsPage.value,
      pageSize: postsPageSize.value
    })
    
    console.log('User posts response:', res)
    
    if (res.code === 200 && res.data) {
      const newPosts = res.data.records || []
      
      // 确保图片数据格式正确 - 后端返回的已经是数组格式
      newPosts.forEach((post: any) => {
        if (!post.images) {
          post.images = []
        }
        
        // 确保userId是字符串
        post.userId = String(post.userId)
      })
      
      if (isRefresh) {
        userPosts.value = newPosts
      } else {
        userPosts.value.push(...newPosts)
      }
      
      // 检查是否还有更多数据
      if (newPosts.length < postsPageSize.value) {
        postsFinished.value = true
      } else {
        postsPage.value++
      }
      
      console.log('处理后的用户帖子:', userPosts.value)
    } else {
      console.error('获取用户帖子失败:', res.message || '未知错误')
      postsFinished.value = true
    }
  } catch (error) {
    console.error('获取用户帖子失败:', error)
    postsFinished.value = true
  } finally {
    loadingPosts.value = false
  }
}

// 加载更多帖子
const onLoadMorePosts = () => {
  fetchUserPosts(false)
}

// 开始聊天
const startChat = async () => {
  try {
    // 显示加载状态
    const loadingToast = showLoadingToast({
      message: '正在查找会话...',
      forbidClick: true,
    });

    // 查找或创建与该用户的私聊会话
    const response = await chatApi.findOrCreatePrivateConversation(parseInt(userId.value))

    loadingToast.close();

    if (response.code === 200) {
      console.log('获取会话成功，会话ID:', response.data)
      if (response.data) {
        // 直接跳转到聊天页面，聊天页面会自动加载历史消息
        router.push(`/chat/${response.data}`)
      } else {
        console.error('获取会话成功但返回的会话ID为空')
        showFailToast('获取会话失败：会话ID为空')
      }
    } else {
      showFailToast(response.msg || '获取会话失败')
    }
  } catch (error) {
    console.error('获取会话失败:', error)
    showFailToast('获取会话失败')
  }
}

// 显示用户详情
const showUserDetail = () => {
  showToast('功能开发中...')
}

// 处理更多操作
const onSelectAction = (action: any) => {
  switch (action.value) {
    case 'report':
      showToast('举报功能开发中...')
      break
    case 'block':
      showToast('拉黑功能开发中...')
      break
  }
}

// 跳转到帖子详情
const goToPostDetail = (postId: string | number) => {
  router.push(`/post-detail/${postId}`)
}

// 截取内容
const truncateContent = (content: string, maxLength = 100) => {
  if (!content) return ''
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content
}

// 获取帖子图片
const getPostImage = (post: any) => {
  if (post.images && post.images.length > 0) {
    return post.images[0]
  }
  return ''
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到认证页面
const goToVerification = () => {
  showToast('认证功能开发中...')
}

// 发送消息
const sendMessage = async () => {
  try {
    // 检查是否已登录
    if (!authStore.isLoggedIn) {
      router.push('/login')
      return
    }
    
    // 发送消息逻辑
    showToast('发送消息功能开发中...')
  } catch (error) {
    showFailToast('发送消息失败')
  }
}

// 音视频通话
const startCall = () => {
  try {
    // 检查是否已登录
    if (!authStore.isLoggedIn) {
      router.push('/login')
      return
    }
    
    showToast('音视频通话功能开发中...')
  } catch (error) {
    showFailToast('启动通话失败')
  }
}

onMounted(() => {
  if (userId.value) {
    fetchUserInfo()
    fetchUserPosts(true)
  } else {
    showFailToast('用户ID无效')
    router.back()
  }
})
</script>

<style scoped lang="scss">
.user-profile-container {
  min-height: 100vh;
  padding-top: 46px;
  background-color: #f7f8fa;
}

.profile-content {
  padding-bottom: 20px;
}

.user-info-section {
  background-color: #fff;
  padding: 20px;
  margin-bottom: 10px;
  
  .user-header {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .user-details {
      flex: 1;
      
      .user-name {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .user-id, .user-location {
        font-size: 14px;
        color: #666;
        margin-bottom: 4px;
      }
    }
  }
}

.menu-section {
  margin-bottom: 10px;
}

.action-buttons {
  padding: 0 16px;
  margin-bottom: 20px;
  
  .action-btn {
    width: 100%;
    margin-bottom: 10px;
  }
}

.user-posts-section {
  .section-header {
    padding: 10px 16px;
    background-color: #fff;
    
    .section-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
  
  .posts-list {
    padding: 0 10px;
    
    .van-card {
      margin-bottom: 10px;
      border-radius: 8px;
    }
    
    .post-footer {
      display: flex;
      justify-content: flex-end;
      font-size: 12px;
      color: #666;
    }
  }
}
</style>
