<template>
  <div class="profile-index">
    <!-- 个人信息卡片 -->
    <div class="user-info-card">
      <div class="user-info">
        <van-image
          round
          width="64"
          height="64"
          :src="userInfo.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
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
    </div>

    <!-- 菜单卡片 -->
    <van-cell-group inset title="账户与安全" class="menu-group">
      <van-cell title="个人资料" is-link to="/profile/edit">
        <template #icon><van-icon name="contact" class="menu-icon" /></template>
      </van-cell>
      <van-cell title="实名认证" is-link to="/profile/verification">
        <template #icon><van-icon name="shield-o" class="menu-icon" /></template>
        <template #right-icon>
          <div class="verification-status">
            <span :class="['status-text', getVerificationStatusClass(userInfo.isVerified)]">
              {{ getVerificationStatusText(userInfo.isVerified) }}
            </span>
            <van-icon name="arrow" />
          </div>
        </template>
      </van-cell>
      <van-cell title="修改密码" is-link to="/profile/password">
        <template #icon><van-icon name="eye-o" class="menu-icon" /></template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset title="服务" class="menu-group">
      <van-cell title="我的帖子" is-link to="/my-posts">
        <template #icon><van-icon name="notes-o" class="menu-icon" /></template>
      </van-cell>
      <van-cell title="我的收藏" is-link @click="router.push('/')">
        <template #icon><van-icon name="star-o" class="menu-icon" /></template>
      </van-cell>
    </van-cell-group>

    <van-cell-group inset title="其他" class="menu-group">
      <van-cell title="设置" is-link @click="router.push('/')">
        <template #icon><van-icon name="setting-o" class="menu-icon" /></template>
      </van-cell>
      <van-cell title="关于我们" is-link @click="router.push('/')">
        <template #icon><van-icon name="info-o" class="menu-icon" /></template>
      </van-cell>
    </van-cell-group>

    <!-- 退出登录按钮 -->
    <div class="logout-btn">
      <van-button block round type="danger" @click="handleLogout">退出登录</van-button>
    </div>
    
    <!-- 添加底部导航栏 -->
    <BottomTabbar />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showLoadingToast, closeToast, showConfirmDialog } from 'vant'
import { getUserInfo } from '@/api/user'
import { useAuthStore } from '@/store/auth'
import type { UserInfo } from '@/api/user'
import BottomTabbar from '@/components/BottomTabbar.vue'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = ref<Partial<UserInfo>>({
  id: 0,
  username: '',
  nickname: '',
  avatarUrl: '',
  isVerified: 0
})

// 获取用户信息
const fetchUserInfo = async () => {
  const toast = showLoadingToast({
    message: '加载中...',
    forbidClick: true,
  })
  
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  } finally {
    closeToast()
  }
}

// 实名认证状态文本
const getVerificationStatusText = (status: number | undefined): string => {
  if (status === undefined) return '未知'
  switch (status) {
    case 0: return '未认证'
    case 1: return '已认证'
    case 2: return '认证中'
    case 3: return '认证失败'
    default: return '未知'
  }
}

// 实名认证状态样式类
const getVerificationStatusClass = (status: number | undefined): string => {
  if (status === undefined) return 'status-unknown'
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-success'
    case 2: return 'status-processing'
    case 3: return 'status-failed'
    default: return 'status-unknown'
  }
}

// 前往实名认证页面
const goToVerification = () => {
  router.push('/profile/verification')
}

// 退出登录
const handleLogout = () => {
  showConfirmDialog({
    title: '退出登录',
    message: '确定要退出登录吗？',
  }).then(() => {
    authStore.logout()
    showSuccessToast('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 取消操作
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.profile-index {
  padding-bottom: 70px; // 为底部导航栏留出空间
  min-height: 100vh;

  .user-info-card {
    background: linear-gradient(to right, #4fc08d, #42b983);
    padding: 20px;
    margin-bottom: 16px;
    
    .user-info {
      display: flex;
      align-items: center;
      
      .user-avatar {
        margin-right: 16px;
        border: 2px solid #ffffff;
      }
      
      .user-details {
        color: #ffffff;
        
        .user-name {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 4px;
        }
        
        .user-id {
          font-size: 12px;
          margin-bottom: 8px;
          opacity: 0.8;
        }
      }
    }
  }

  .menu-group {
    margin-bottom: 12px;
    
    .menu-icon {
      margin-right: 8px;
      font-size: 20px;
      color: var(--primary-color);
    }
    
    :deep(.van-cell__title) {
      display: flex;
      align-items: center;
    }
    
    .verification-status {
      display: flex;
      align-items: center;
      
      .status-text {
        margin-right: 5px;
        font-size: 12px;
        
        &.status-success {
          color: var(--success-color);
        }
        
        &.status-processing {
          color: #2196f3;
        }
        
        &.status-failed {
          color: var(--danger-color);
        }
        
        &.status-pending {
          color: var(--text-color-lighter);
        }
        
        &.status-unknown {
          color: var(--text-color-lighter);
        }
      }
    }
  }

  .logout-btn {
    padding: 16px;
    margin-top: 20px;
    margin-bottom: 20px; // 增加底部间距
  }
}
</style> 