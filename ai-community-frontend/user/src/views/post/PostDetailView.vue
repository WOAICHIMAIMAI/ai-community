<template>
  <div class="post-detail-container">
    <!-- 添加一个明显的测试按钮 -->
    <div style="position: fixed; top: 100px; left: 20px; z-index: 9999; background: red; color: white; padding: 10px; cursor: pointer;" @click="testClick">
      测试点击
    </div>
    
    <van-nav-bar 
      title="帖子详情" 
      left-arrow 
      @click-left="goBack"
      fixed
    />
    
    <div class="post-content">
      <!-- 帖子头部信息 -->
      <div class="post-header">
        <div class="user-info" @click="goToUserProfile(post.userId)">
          <van-image
            round
            width="40"
            height="40"
            :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
            fit="cover"
            class="user-avatar"
          />
          <div class="user-details">
            <span class="username">{{ post.username || post.authorName }}</span>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
          </div>
        </div>
        <van-tag plain type="primary">{{ post.category }}</van-tag>
      </div>
      
      <!-- 帖子内容 -->
      <div class="post-body">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-text">{{ post.content }}</div>
        
        <!-- 帖子图片 -->
        <div v-if="post.images" class="post-images">
          <van-image
            v-for="(image, index) in getImageList(post.images)"
            :key="index"
            :src="image"
            fit="cover"
            class="post-image"
            @click="previewImage(image, getImageList(post.images))"
          />
        </div>
      </div>
      
      <!-- 互动统计 -->
      <div class="post-stats">
        <div class="stat-item" @click="toggleLike">
          <van-icon :name="isLiked ? 'like' : 'like-o'" :color="isLiked ? '#ee0a24' : '#969799'" />
          <span>{{ post.likeCount || 0 }}</span>
        </div>
        <div class="stat-item">
          <van-icon name="chat-o" />
          <span>{{ post.commentCount || 0 }}</span>
        </div>
        <div class="stat-item">
          <van-icon name="eye-o" />
          <span>{{ post.viewCount || 0 }}</span>
        </div>
      </div>
    </div>
    
    <!-- 评论区 -->
    <div class="comments-section">
      <div class="comments-header">
        <span>全部评论 ({{ comments.length }})</span>
      </div>
      
      <div class="comments-list">
        <template v-if="loadingComments">
          <van-skeleton title avatar :row="2" style="margin-bottom: 10px;" />
          <van-skeleton title avatar :row="2" style="margin-bottom: 10px;" />
        </template>
        <template v-else-if="comments.length === 0">
          <van-empty description="暂无评论" />
        </template>
        <template v-else>
          <div 
            v-for="comment in comments" 
            :key="comment.id" 
            class="comment-item"
          >
            <div class="comment-user" @click.stop="goToUserProfile(comment.userId)">
              <van-image
                round
                width="32"
                height="32"
                :src="comment.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                fit="cover"
              />
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-username" @click.stop="goToUserProfile(comment.userId)">{{ comment.username }}</span>
                <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
              </div>
              <div class="comment-text">{{ comment.content }}</div>
            </div>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 评论输入框 -->
    <div class="comment-input-section">
      <van-field
        v-model="commentContent"
        placeholder="写评论..."
        type="textarea"
        rows="1"
        autosize
      />
      <van-button 
        type="primary" 
        size="small"
        @click="submitComment"
        :loading="submittingComment"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showFailToast, showImagePreview } from 'vant'
import { getPostDetail, likePost, getPostComments, addComment } from '@/api/post'

const route = useRoute()
const router = useRouter()

const postId = ref(route.params.id as string)
const post = ref<any>({})
const comments = ref<any[]>([])
const commentContent = ref('')
const isLiked = ref(false)
const loadingComments = ref(true)
const submittingComment = ref(false)

// 最简单的测试点击
const testClick = () => {
  console.log('测试点击被触发了！')
  alert('点击事件触发了！')
}

// 跳转到用户个人主页
const goToUserProfile = (userId: number | string) => {
  console.log('=== goToUserProfile被调用 ===')
  console.log('userId:', userId)
  
  if (!userId) {
    console.log('userId为空')
    return
  }
  
  console.log('准备跳转到:', `/user/${userId}`)
  router.push(`/user/${userId}`)
}

// 获取帖子详情
const fetchPostDetail = async () => {
  try {
    const response = await getPostDetail(postId.value)
    if (response.code === 200) {
      post.value = response.data
    } else {
      showFailToast(response.msg || '获取帖子详情失败')
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    showFailToast('获取帖子详情失败')
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    loadingComments.value = true
    const response = await getPostComments(postId.value)
    if (response.code === 200) {
      comments.value = response.data || []
    } else {
      showFailToast(response.msg || '获取评论失败')
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    showFailToast('获取评论失败')
  } finally {
    loadingComments.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  
  try {
    submittingComment.value = true
    const response = await addComment({
      postId: postId.value,
      content: commentContent.value.trim()
    })
    
    if (response.code === 200) {
      showToast('评论成功')
      commentContent.value = ''
      await fetchComments() // 重新获取评论列表
    } else {
      showFailToast(response.msg || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    showFailToast('评论失败')
  } finally {
    submittingComment.value = false
  }
}

// 点赞/取消点赞
const toggleLike = async () => {
  try {
    const response = await likePost(postId.value)
    if (response.code === 200) {
      isLiked.value = !isLiked.value
      post.value.likeCount = response.data.likeCount
    } else {
      showFailToast(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    showFailToast('操作失败')
  }
}

// 获取图片列表
const getImageList = (images: string | string[]) => {
  if (!images) return []
  if (typeof images === 'string') {
    return images.split(',').filter(img => img.trim())
  }
  return Array.isArray(images) ? images : []
}

// 预览图片
const previewImage = (current: string, images: string[]) => {
  showImagePreview({
    images,
    startPosition: images.indexOf(current)
  })
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 容器点击测试
const containerClick = (event: Event) => {
  console.log('容器被点击了:', event.target)
}

const userInfoRef = ref<HTMLElement>()

onMounted(async () => {
  await fetchPostDetail()
  await fetchComments()
  
  // 使用ref绑定事件
  if (userInfoRef.value) {
    userInfoRef.value.onclick = () => {
      console.log('ref点击事件被触发了！')
      alert('ref点击事件触发了！')
      goToUserProfile(post.value.userId)
    }
  }
})
</script>

<style scoped lang="scss">
.post-detail-container {
  padding-top: 46px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.post-content {
  padding: 15px;
  margin-bottom: 60px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
}

.user-info-wrapper {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  background-color: rgba(25, 137, 250, 0.1);
  border: 1px solid rgba(25, 137, 250, 0.3);
  transition: all 0.2s;
  
  &:hover {
    background-color: rgba(25, 137, 250, 0.2);
  }
  
  &:active {
    background-color: rgba(25, 137, 250, 0.3);
    transform: scale(0.98);
  }
}

.user-info {
  display: flex;
  align-items: center;
  pointer-events: none; /* 防止子元素阻止事件 */
}

.user-avatar {
  margin-right: 10px;
  pointer-events: none;
}

.user-details {
  display: flex;
  flex-direction: column;
  pointer-events: none;
}

.username {
  font-weight: 500;
  font-size: 14px;
  color: #1989fa;
  pointer-events: none;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  pointer-events: none;
}

.comment-user {
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  
  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
  }
}

.comment-username {
  color: #1989fa;
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
