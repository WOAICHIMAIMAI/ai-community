<template>
  <div class="post-detail-container">
    <van-nav-bar
      title="帖子详情"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <template v-if="loading">
        <van-skeleton title :row="10" />
      </template>
      
      <template v-else-if="post">
        <div class="post-header">
          <div class="user-info">
            <van-image
              :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
              class="user-avatar"
              round
              fit="cover"
            />
            <div>
              <div class="username">{{ post.username }}</div>
              <div class="post-time">{{ formatDate(post.createdTime) }}</div>
            </div>
          </div>
          
          <van-tag plain type="primary">{{ post.category }}</van-tag>
        </div>
        
        <div class="post-title">{{ post.title }}</div>
        
        <div class="post-content">{{ post.content }}</div>
        
        <div class="post-images" v-if="post.images && post.images.length">
          <van-image
            v-for="(img, index) in post.images"
            :key="index"
            :src="img"
            fit="cover"
            class="content-image"
            @click="previewImage(index)"
          />
        </div>
        
        <div class="post-actions">
          <div class="action-item">
            <van-icon name="eye-o" /> {{ post.viewCount || 0 }} 浏览
          </div>
          <div class="action-item" @click="handleLike">
            <van-icon :name="post.hasLiked ? 'like' : 'like-o'" :color="post.hasLiked ? '#ee0a24' : ''" />
            {{ post.likeCount || 0 }} 点赞
          </div>
          <div class="action-item" @click="scrollToComments">
            <van-icon name="chat-o" /> {{ post.commentCount || 0 }} 评论
          </div>
        </div>
      </template>
      
      <div class="comment-section" ref="commentSection">
        <div class="section-title">
          <van-divider>全部评论 ({{ commentTotal }})</van-divider>
        </div>
        
        <div class="comments-list">
          <template v-if="commentsLoading">
            <van-skeleton title avatar :row="3" />
            <van-skeleton title avatar :row="3" />
          </template>
          
          <template v-else-if="comments.length > 0">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-user">
                <van-image
                  :src="comment.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                  class="comment-avatar"
                  round
                  fit="cover"
                />
                <div>
                  <div class="comment-username">
                    {{ comment.nickname }}
                    <van-tag v-if="comment.isAuthor" type="danger" size="mini">楼主</van-tag>
                  </div>
                  <div class="comment-time">{{ formatDate(comment.createTime) }}</div>
                </div>
              </div>
              
              <div class="comment-content">{{ comment.content }}</div>
              
              <div class="comment-actions">
                <div class="comment-like" @click="handleCommentLike(comment)">
                  <van-icon :name="comment.hasLiked ? 'like' : 'like-o'" :color="comment.hasLiked ? '#ee0a24' : ''" />
                  {{ comment.likeCount || 0 }}
                </div>
                <div class="comment-reply" @click="showReplyInput(comment)">
                  回复({{ comment.replyCount || 0 }})
                </div>
              </div>
              
              <!-- 回复列表 -->
              <div class="replies-list" v-if="comment.replies && comment.replies.length">
                <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
                >
                  <div class="comment-user">
                    <van-image
                      :src="reply.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
                      class="comment-avatar"
                      round
                      fit="cover"
                    />
                    <div>
                      <div class="comment-username">
                        {{ reply.nickname }}
                        <van-tag v-if="reply.isAuthor" type="danger" size="mini">楼主</van-tag>
                      </div>
                      <div class="comment-time">{{ formatDate(reply.createTime) }}</div>
                    </div>
                  </div>
                  
                  <div class="comment-content">{{ reply.content }}</div>
                </div>
                
                <div class="view-more" v-if="comment.replyCount > comment.replies.length" @click="loadMoreReplies(comment.id)">
                  查看更多回复
                </div>
              </div>
            </div>
            
            <div class="load-more" v-if="commentTotal > comments.length">
              <van-button size="small" plain type="primary" @click="loadMoreComments">加载更多</van-button>
            </div>
          </template>
          
          <template v-else>
            <div class="empty-comments">
              <van-empty image="comment" description="暂无评论" />
            </div>
          </template>
        </div>
      </div>
    </div>
    
    <!-- 底部评论栏 -->
    <div class="comment-bar">
      <van-field
        v-model="commentContent"
        placeholder="写评论..."
        :border="false"
        class="comment-input"
        @focus="onCommentFocus"
      />
      <van-button
        type="primary"
        size="small"
        :disabled="!commentContent.trim()"
        @click="submitComment"
      >
        发送
      </van-button>
    </div>
    
    <!-- 回复弹出层 -->
    <van-popup
      v-model:show="showReply"
      position="bottom"
      :style="{ height: '40%' }"
      round
      closeable
    >
      <div class="reply-popup">
        <div class="reply-header">
          回复 {{ currentComment?.nickname }}
        </div>
        <div class="reply-content">
          <van-field
            v-model="replyContent"
            rows="4"
            autosize
            type="textarea"
            placeholder="写回复..."
            class="reply-textarea"
          />
        </div>
        <div class="reply-footer">
          <van-button
            type="primary"
            block
            :disabled="!replyContent.trim()"
            @click="submitReply"
          >
            发送
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import { getPostDetail, getComments, likeOrUnlike } from '@/api/post'

const route = useRoute()
const router = useRouter()

// 帖子数据
const loading = ref(true)
const post = ref<any>(null)
const postId = ref(Number(route.params.id))

// 评论数据
const commentsLoading = ref(true)
const comments = ref<any[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(10)

// 评论相关
const commentContent = ref('')
const commentSection = ref(null)
const showReply = ref(false)
const replyContent = ref('')
const currentComment = ref<any>(null)

// 获取帖子详情
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(postId.value)
    console.log('Post detail response:', res)
    
    if (res.code === 200 && res.data) {
      post.value = res.data
      
      // 处理图片数组
      if (post.value.images && typeof post.value.images === 'string') {
        post.value.images = post.value.images.split(',').filter((img: string) => img)
      } else {
        post.value.images = []
      }
    } else {
      // 使用模拟数据
      console.warn('使用模拟数据显示帖子详情')
      post.value = {
        id: postId.value,
        title: '模拟帖子标题',
        content: '这是一个模拟的帖子内容，用于测试显示效果。当API未返回正确数据时，将显示此模拟数据。',
        username: '测试用户',
        avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
        createdTime: new Date().toISOString(),
        category: '讨论',
        viewCount: 100,
        likeCount: 20,
        commentCount: 5,
        hasLiked: false,
        images: []
      }
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    // 使用模拟数据
    console.warn('使用模拟数据显示帖子详情')
    post.value = {
      id: postId.value,
      title: '模拟帖子标题',
      content: '这是一个模拟的帖子内容，用于测试显示效果。当API未返回正确数据时，将显示此模拟数据。',
      username: '测试用户',
      avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
      createdTime: new Date().toISOString(),
      category: '讨论',
      viewCount: 100,
      likeCount: 20,
      commentCount: 5,
      hasLiked: false,
      images: []
    }
  } finally {
    loading.value = false
  }
}

// 获取评论列表
const fetchComments = async (append = false) => {
  commentsLoading.value = !append
  try {
    const res = await getComments({
      postId: postId.value,
      page: commentPage.value,
      pageSize: commentPageSize.value
    })
    
    console.log('Comments response:', res)
    
    if (res.code === 200 && res.data) {
      if (append) {
        comments.value = [...comments.value, ...res.data.records]
      } else {
        comments.value = res.data.records || []
      }
      commentTotal.value = res.data.total || 0
    } else {
      // 使用模拟数据
      console.warn('使用模拟数据显示评论')
      if (!append) {
        comments.value = [
          {
            id: 1,
            content: '这是一条模拟评论，用于测试显示效果。',
            nickname: '评论用户1',
            avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
            createTime: new Date().toISOString(),
            likeCount: 5,
            hasLiked: false,
            isAuthor: true,
            replyCount: 2,
            replies: [
              {
                id: 101,
                content: '这是一条回复评论',
                nickname: '回复用户1',
                avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
                createTime: new Date().toISOString(),
                isAuthor: false
              }
            ]
          },
          {
            id: 2,
            content: '这是另一条模拟评论。',
            nickname: '评论用户2',
            avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
            createTime: new Date().toISOString(),
            likeCount: 3,
            hasLiked: false,
            isAuthor: false,
            replyCount: 0,
            replies: []
          }
        ]
        commentTotal.value = 2
      }
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    // 使用模拟数据
    console.warn('使用模拟数据显示评论')
    if (!append) {
      comments.value = [
        {
          id: 1,
          content: '这是一条模拟评论，用于测试显示效果。',
          nickname: '评论用户1',
          avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
          createTime: new Date().toISOString(),
          likeCount: 5,
          hasLiked: false,
          isAuthor: true,
          replyCount: 2,
          replies: [
            {
              id: 101,
              content: '这是一条回复评论',
              nickname: '回复用户1',
              avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
              createTime: new Date().toISOString(),
              isAuthor: false
            }
          ]
        },
        {
          id: 2,
          content: '这是另一条模拟评论。',
          nickname: '评论用户2',
          avatar: 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg',
          createTime: new Date().toISOString(),
          likeCount: 3,
          hasLiked: false,
          isAuthor: false,
          replyCount: 0,
          replies: []
        }
      ]
      commentTotal.value = 2
    }
  } finally {
    commentsLoading.value = false
  }
}

// 加载更多评论
const loadMoreComments = () => {
  commentPage.value += 1
  fetchComments(true)
}

// 加载更多回复
const loadMoreReplies = async (commentId: number) => {
  // 实现获取更多回复的逻辑
  showToast('加载更多回复功能开发中')
}

// 点赞帖子
const handleLike = async () => {
  if (!post.value) return
  
  try {
    const res = await likeOrUnlike({
      type: 1, // 1-帖子
      targetId: post.value.id
    })
    
    console.log('Like response:', res)
    
    if (res.code === 200 && res.data) {
      post.value.hasLiked = res.data.liked
      post.value.likeCount = res.data.likeCount
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    showToast('点赞失败，请稍后重试')
  }
}

// 点赞评论
const handleCommentLike = async (comment: any) => {
  try {
    const res = await likeOrUnlike({
      type: 2, // 2-评论
      targetId: comment.id
    })
    
    console.log('Comment like response:', res)
    
    if (res.code === 200 && res.data) {
      comment.hasLiked = res.data.liked
      comment.likeCount = res.data.likeCount
    }
  } catch (error) {
    console.error('点赞评论失败:', error)
    showToast('点赞失败，请稍后重试')
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) return
  
  // 实现提交评论的逻辑
  showToast('评论功能开发中')
  commentContent.value = ''
}

// 显示回复输入框
const showReplyInput = (comment: any) => {
  currentComment.value = comment
  showReply.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim() || !currentComment.value) return
  
  // 实现提交回复的逻辑
  showToast('回复功能开发中')
  replyContent.value = ''
  showReply.value = false
}

// 滚动到评论区
const scrollToComments = () => {
  if (commentSection.value) {
    commentSection.value.scrollIntoView({ behavior: 'smooth' })
  }
}

// 图片预览
const previewImage = (index: number) => {
  if (!post.value || !post.value.images) return
  
  showImagePreview({
    images: post.value.images,
    startPosition: index
  })
}

// 日期格式化
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffSecs = Math.floor(diffMs / 1000)
  const diffMins = Math.floor(diffSecs / 60)
  const diffHours = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHours / 24)
  
  if (diffDays > 30) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } else if (diffDays > 0) {
    return `${diffDays}天前`
  } else if (diffHours > 0) {
    return `${diffHours}小时前`
  } else if (diffMins > 0) {
    return `${diffMins}分钟前`
  } else {
    return '刚刚'
  }
}

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 评论输入框聚焦
const onCommentFocus = () => {
  // 聚焦时的处理
}

onMounted(async () => {
  await fetchPostDetail()
  await fetchComments()
})
</script>

<style scoped>
.post-detail-container {
  padding-top: 46px;
  padding-bottom: 56px;
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 15px;
  margin-bottom: 60px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  margin-right: 10px;
}

.username {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 2px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.post-content {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 15px;
  color: #333;
}

.post-images {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.content-image {
  width: calc(33.33% - 5px);
  height: 100px;
  margin-right: 5px;
  margin-bottom: 5px;
  border-radius: 5px;
  overflow: hidden;
}

.post-actions {
  display: flex;
  border-bottom: 1px solid #f2f2f2;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.action-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  font-size: 13px;
  color: #646566;
  cursor: pointer;
}

.action-item .van-icon {
  margin-right: 4px;
  font-size: 16px;
}

.section-title {
  text-align: center;
  margin: 10px 0;
}

.comments-list {
  margin-top: 10px;
}

.comment-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
}

.comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.comment-username {
  font-weight: 500;
  font-size: 13px;
  margin-bottom: 2px;
}

.comment-time {
  font-size: 11px;
  color: #999;
}

.comment-content {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  font-size: 12px;
  color: #666;
}

.comment-like, .comment-reply {
  display: flex;
  align-items: center;
  margin-right: 15px;
  cursor: pointer;
}

.comment-like .van-icon {
  margin-right: 2px;
}

.replies-list {
  margin-top: 10px;
  margin-left: 20px;
  padding: 8px;
  border-left: 2px solid #eee;
}

.reply-item {
  margin-bottom: 10px;
}

.view-more {
  text-align: center;
  color: #1989fa;
  font-size: 13px;
  padding: 5px 0;
  cursor: pointer;
}

.load-more {
  text-align: center;
  margin: 15px 0;
}

.empty-comments {
  padding: 30px 0;
}

.comment-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  background-color: #fff;
  border-top: 1px solid #eee;
  z-index: 10;
}

.comment-input {
  flex: 1;
  margin-right: 10px;
}

.reply-popup {
  padding: 15px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.reply-header {
  font-weight: 500;
  margin-bottom: 15px;
  text-align: center;
}

.reply-content {
  flex: 1;
}

.reply-footer {
  padding: 15px 0;
}

.reply-textarea {
  height: 100px;
}
</style> 