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
          <div class="user-info" @click="goToUserProfile(post.userId?.toString() || '')" style="cursor: pointer;">
            <van-image
              :src="post.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
              class="user-avatar"
              round
              fit="cover"
            />
            <div>
              <div class="username">{{ post.nickname || post.username }}</div>
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
                  @click="goToUserProfile(comment.userId?.toString() || '')"
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
                <div class="comment-reply" @click="toggleReplies(comment)">
                  <van-icon name="chat-o" />
                  回复({{ comment.replyCount || 0 }})
                </div>
              </div>
              
              <!-- 回复列表 -->
              <div class="replies-list" v-if="comment.showReplies">
                <template v-if="comment.loadingReplies">
                  <van-skeleton title avatar :row="2" />
                </template>
                <template v-else-if="comment.replies && comment.replies.length">
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
                        @click="goToUserProfile(reply.userId?.toString() || '')"
                      />
                      <div>
                        <div class="comment-username">
                          {{ reply.nickname }}
                          <van-tag v-if="reply.isAuthor" type="danger" size="mini">楼主</van-tag>
                        </div>
                        <div class="comment-time">{{ formatDate(reply.createTime) }}</div>
                      </div>
                    </div>
                    
                    <div class="comment-content">
                      <span v-if="reply.replyToNickname" class="reply-to">
                        回复 <span class="reply-to-name">@{{ reply.replyToNickname }}</span>:
                      </span>
                      {{ reply.content }}
                    </div>
                    
                    <div class="comment-actions">
                      <div class="comment-like" @click="handleCommentLike(reply)">
                        <van-icon :name="reply.hasLiked ? 'like' : 'like-o'" :color="reply.hasLiked ? '#ee0a24' : ''" />
                        {{ reply.likeCount || 0 }}
                      </div>
                      <div class="comment-reply" @click="showReplyInput(reply)">
                        回复
                      </div>
                    </div>
                  </div>
                </template>
                <template v-else>
                  <div class="empty-replies">
                    <van-empty description="暂无回复" :image-size="60" />
                  </div>
                </template>
              </div>
            </div>
            
            <div class="load-more" v-if="commentTotal > loadedCommentCount">
              <van-button size="small" plain type="primary" @click="loadMoreComments">加载更多</van-button>
            </div>
          </template>
          
          <template v-else>
            <div class="empty-comments">
              <van-empty description="暂无评论" />
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
import { showToast, showImagePreview, showFailToast } from 'vant'
import { getPostDetail, getComments, likeOrUnlike, addComment } from '@/api/post'
import { getCommentReplies } from '@/api/comment'
// 暂时移除 getUserPosts 的导入

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
// 已加载的扁平评论数量（用于判断是否还有更多）
const loadedCommentCount = ref(0)

// 评论相关
const commentContent = ref('')
const commentSection = ref(null)
const showReply = ref(false)
const replyContent = ref('')
const currentComment = ref<any>(null)

// 移除测试点击方法
// const testClick = () => {
//   console.log('测试点击被触发了！')
//   alert('点击事件触发了！')
// }

// 跳转到用户个人主页
const goToUserProfile = (userId: string) => {
  console.log('=== goToUserProfile被调用 ===')
  console.log('传入的userId:', userId)
  console.log('userId类型:', typeof userId)
  
  if (!userId || userId === 'undefined' || userId === 'null') {
    console.log('userId为空')
    showToast('无法获取用户信息')
    return
  }
  
  console.log('准备跳转到:', `/user/${userId}`)
  
  try {
    router.push(`/user/${userId}`)
    console.log('路由跳转成功')
  } catch (error) {
    console.error('路由跳转失败:', error)
    showToast('跳转失败')
  }
}

// 获取帖子详情 - 使用真实API数据
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const res = await getPostDetail(postId.value)
    console.log('=== 帖子详情API响应 ===')
    console.log('完整响应:', res)
    
    if (res.code === 200 && res.data) {
      post.value = res.data
      console.log('=== 帖子数据分析 ===')
      console.log('帖子ID:', post.value.id)
      console.log('图片数据:', {
        images: post.value.images,
        imagesType: typeof post.value.images,
        isArray: Array.isArray(post.value.images)
      })
      console.log('用户ID字段:', {
        userId: post.value.userId,
        authorId: post.value.authorId,
        createBy: post.value.createBy,
        user: post.value.user
      })
      console.log('用户名字段:', {
        username: post.value.username,
        nickname: post.value.nickname,
        authorName: post.value.authorName
      })
      
      // 确保有userId字段，尝试从不同字段获取
      if (!post.value.userId) {
        const fallbackUserId = post.value.authorId || post.value.user?.id || post.value.createBy
        console.log('userId为空，使用备用字段:', fallbackUserId)
        post.value.userId = fallbackUserId
      }
      
      // 确保图片数据格式正确 - 后端返回的已经是数组格式
      if (!post.value.images) {
        post.value.images = []
      }
    } else {
      console.error('获取帖子详情失败:', res.message || '未知错误')
      showToast('获取帖子详情失败')
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    showToast('获取帖子详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 将扁平评论列表构造成二级树（父评论 -> 回复[]）
const buildTwoLevelComments = (records: any[] = []) => {
  const parents: any[] = []
  const idToParent: Record<string, any> = {}

  for (const item of records) {
    // 判断是否为顶级评论：没有 parentId 或 parentId 为 0/undefined
    const isTopLevel = !item.parentId || Number(item.parentId) === 0
    if (isTopLevel) {
      const parent = { ...item, replies: [] as any[] }
      parents.push(parent)
      idToParent[String(item.id)] = parent
    }
  }

  for (const item of records) {
    const parentId = item.parentId
    if (parentId && Number(parentId) !== 0) {
      const parent = idToParent[String(parentId)]
      if (parent) {
        parent.replies.push(item)
      }
    }
  }

  // 可选：按时间排序（父按时间，子按时间）
  parents.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
  parents.forEach(p => {
    p.replies.sort((a: any, b: any) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())
  })

  return parents
}

// 获取评论列表 - 使用真实API数据（构建二级目录展示）
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
      const flatRecords = res.data.records || []
      const tree = buildTwoLevelComments(flatRecords)
      if (append) {
        comments.value = [...comments.value, ...tree]
        loadedCommentCount.value += flatRecords.length
      } else {
        comments.value = tree
        loadedCommentCount.value = flatRecords.length
      }
      commentTotal.value = res.data.total || 0
    } else {
      console.error('获取评论失败:', res.message || '未知错误')
      if (!append) {
        comments.value = []
        commentTotal.value = 0
        loadedCommentCount.value = 0
      }
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    if (!append) {
      comments.value = []
      commentTotal.value = 0
      loadedCommentCount.value = 0
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

// 切换回复显示状态
const toggleReplies = async (comment: any) => {
  // 如果已经显示,则收起
  if (comment.showReplies) {
    comment.showReplies = false
    return
  }
  
  // 如果没有回复数量,不需要加载
  if (!comment.replyCount || comment.replyCount === 0) {
    showToast('暂无回复')
    return
  }
  
  // 如果已经加载过回复,直接显示
  if (comment.replies && comment.replies.length > 0) {
    comment.showReplies = true
    return
  }
  
  // 加载回复
  comment.loadingReplies = true
  comment.showReplies = true
  
  try {
    const res = await getCommentReplies(comment.id)
    console.log('回复数据响应:', res)
    
    if (res.code === 200 && res.data) {
      comment.replies = res.data
    } else {
      console.error('获取回复失败:', res.message || '未知错误')
      showToast(res.message || '获取回复失败')
      comment.replies = []
    }
  } catch (error) {
    console.error('获取回复失败:', error)
    showToast('获取回复失败,请稍后重试')
    comment.replies = []
  } finally {
    comment.loadingReplies = false
  }
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
  const content = commentContent.value.trim()
  if (!content) return
  try {
    const res = await addComment({
      postId: postId.value,
      content
    })
    if (res && res.code === 200) {
      showToast('评论成功')
      commentContent.value = ''
      // 重新拉取评论，重置到第一页
      commentPage.value = 1
      await fetchComments()
    } else {
      showFailToast(res?.message || '评论失败')
    }
  } catch (error: any) {
    showFailToast(error?.message || '评论失败')
  }
}

// 显示回复输入框
const showReplyInput = (comment: any) => {
  currentComment.value = comment
  showReply.value = true
}

// 提交回复
const submitReply = async () => {
  const content = replyContent.value.trim()
  if (!content || !currentComment.value) return
  try {
    const res = await addComment({
      postId: postId.value,
      content,
      parentId: currentComment.value.parentId ? currentComment.value.parentId : currentComment.value.id,
      replyToId: currentComment.value.id
    })
    if (res && res.code === 200) {
      showToast('回复成功')
      replyContent.value = ''
      showReply.value = false
      
      // 找到父评论并更新其回复列表
      const parentCommentId = currentComment.value.parentId || currentComment.value.id
      const parentComment = comments.value.find(c => c.id === parentCommentId)
      
      if (parentComment) {
        // 更新回复数量
        parentComment.replyCount = (parentComment.replyCount || 0) + 1
        
        // 如果回复列表已展开,重新加载回复
        if (parentComment.showReplies) {
          try {
            const repliesRes = await getCommentReplies(parentComment.id)
            if (repliesRes.code === 200 && repliesRes.data) {
              parentComment.replies = repliesRes.data
            }
          } catch (error) {
            console.error('重新加载回复失败:', error)
          }
        }
      }
      
      // 更新帖子的评论总数
      if (post.value) {
        post.value.commentCount = (post.value.commentCount || 0) + 1
      }
      commentTotal.value = (commentTotal.value || 0) + 1
    } else {
      showFailToast(res?.message || '回复失败')
    }
  } catch (error: any) {
    showFailToast(error?.message || '回复失败')
  }
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
  // 滚动到页面顶部
  window.scrollTo(0, 0)
  
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
  background-color: #f9f9f9;
  border-radius: 4px;
}

.reply-item {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.reply-to {
  color: #999;
  font-size: 13px;
  
  .reply-to-name {
    color: #1989fa;
    font-weight: 500;
  }
}

.empty-replies {
  padding: 20px 0;
  text-align: center;
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
