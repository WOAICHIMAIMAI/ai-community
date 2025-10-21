<template>
  <div class="my-posts-container">
    <van-nav-bar
      title="我的帖子"
      left-arrow
      @click-left="onClickLeft"
      fixed
    />
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-tabs v-model:active="activeCategory" sticky>
          <van-tab title="全部"></van-tab>
          <van-tab 
            v-for="category in categories.filter(c => c !== '全部')" 
            :key="category" 
            :title="category"
          ></van-tab>
        </van-tabs>
        
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <template v-if="posts.length > 0">
            <div class="post-list">
              <div 
                v-for="post in posts" 
                :key="post.id" 
                class="post-item"
                @click="goToDetail(post.id)"
              >
                <div class="post-header">
                  <div class="post-category">
                    <van-tag plain type="primary">{{ post.category }}</van-tag>
                    <van-tag v-if="post.status === 0" plain type="warning" style="margin-left: 4px;">草稿</van-tag>
                    <span class="post-date">{{ formatDate(post.createTime || post.createdTime) }}</span>
                  </div>
                  <div class="post-status">
                    <van-tag v-if="post.isTop === 1" type="danger" style="margin-right: 4px;">置顶</van-tag>
                    <van-tag v-if="post.isEssence === 1" type="success">精华</van-tag>
                  </div>
                </div>
                
                <div class="post-title">{{ post.title }}</div>
                
                <div class="post-content">{{ truncateContent(post.content) }}</div>
                
                <div class="post-images" v-if="post.images && post.images.length > 0">
                  <div class="image-grid">
                    <van-image
                      v-for="(img, index) in getImageArray(post.images).slice(0, 3)"
                      :key="index"
                      :src="img"
                      fit="cover"
                      class="grid-image"
                      lazy-load
                    >
                      <template v-slot:error>
                        <div class="image-error">加载失败</div>
                      </template>
                    </van-image>
                    <div v-if="getImageArray(post.images).length > 3" class="more-images">
                      +{{ getImageArray(post.images).length - 3 }}
                    </div>
                  </div>
                </div>
                
                <div class="post-stats">
                  <div class="stat-item">
                    <van-icon name="eye-o" /> {{ post.viewCount || 0 }}
                  </div>
                  <div class="stat-item">
                    <van-icon name="like-o" /> {{ post.likeCount || 0 }}
                  </div>
                  <div class="stat-item">
                    <van-icon name="chat-o" /> {{ post.commentCount || 0 }}
                  </div>
                </div>
              </div>
            </div>
          </template>
          
          <template v-else-if="!loading && finished">
            <van-empty description="暂无帖子" />
          </template>
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { getMyPosts, getCategories } from '@/api/post'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

// 分类
const categories = ref<string[]>(['全部'])
const activeCategory = ref<number>(0)

// 加载分类列表
const loadCategories = async () => {
  try {
    const res: any = await getCategories()
    if (res.code === 200 && res.data) {
      categories.value = ['全部', ...res.data]
    } else {
      categories.value = ['全部', '公告', '求助', '分享', '讨论', '动态', '闲置']
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    categories.value = ['全部', '公告', '求助', '分享', '讨论', '动态', '闲置']
  }
}

// 分页
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

// 数据
const posts = ref<any[]>([])

// 监听分类变化
watch(activeCategory, () => {
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
    const params: any = {
      page: page.value,
      pageSize: pageSize.value,
    }

    // 根据分类筛选
    if (activeCategory.value > 0) {
      params.category = categories.value[activeCategory.value]
    }

    const res: any = await getMyPosts(params)

    if (res && res.code === 200 && res.data) {
      const newPosts = res.data.records || []
      
      console.log('=== MyPostsView 帖子数据 ===')
      console.log('获取到的帖子数量:', newPosts.length)
      if (newPosts.length > 0) {
        console.log('第一个帖子的数据:', newPosts[0])
        console.log('第一个帖子的images字段:', {
          value: newPosts[0].images,
          type: typeof newPosts[0].images,
          isArray: Array.isArray(newPosts[0].images)
        })
      }
      
      // 确保图片数据格式正确
      newPosts.forEach((post: any) => {
        // 后端返回的images已经是数组格式，无需处理
        if (!post.images) {
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
      if (res && (res as any).code !== 200) {
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

// 返回
const onClickLeft = () => {
  router.back()
}

// 跳转到帖子详情
const goToDetail = (postId: number) => {
  router.push(`/post-detail/${postId}`)
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

// 获取图片数组
const getImageArray = (images: string | string[]) => {
  if (!images) return []
  // 后端返回的已经是字符串数组，直接使用
  return Array.isArray(images) ? images : []
}

onMounted(() => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 加载分类
  loadCategories()
})
</script>

<style scoped lang="scss">
.my-posts-container {
  min-height: 100vh;
  padding-top: 46px; // 导航栏高度
  background-color: #f7f8fa;
  
  .content {
    padding-bottom: 20px;
  }
  
  .post-list {
    padding: 12px;
    
    .post-item {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      
      .post-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .post-category {
          display: flex;
          align-items: center;
          
          .post-date {
            margin-left: 8px;
            font-size: 12px;
            color: #999;
          }
        }
      }
      
      .post-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 8px;
        color: #333;
      }
      
      .post-content {
        font-size: 14px;
        color: #666;
        margin-bottom: 12px;
        line-height: 1.5;
      }
      
      .post-images {
        margin-bottom: 12px;
        
        .image-grid {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          
          .grid-image {
            width: calc(33.33% - 3px);
            height: 80px;
            border-radius: 4px;
            overflow: hidden;
          }
          
          .image-error {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            background-color: #f7f8fa;
            color: #999;
            font-size: 12px;
          }
          
          .more-images {
            display: flex;
            align-items: center;
            justify-content: center;
            width: calc(33.33% - 3px);
            height: 80px;
            background-color: rgba(0, 0, 0, 0.03);
            border-radius: 4px;
            color: #999;
            font-size: 16px;
          }
        }
      }
      
      .post-stats {
        display: flex;
        font-size: 12px;
        color: #999;
        
        .stat-item {
          display: flex;
          align-items: center;
          margin-right: 16px;
          
          .van-icon {
            margin-right: 4px;
            font-size: 14px;
          }
        }
      }
    }
  }
}
</style> 