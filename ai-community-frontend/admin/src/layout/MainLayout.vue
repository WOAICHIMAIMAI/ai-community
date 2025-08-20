<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <!-- 移除图片引用，改为使用文字 -->
        <span class="logo-icon">社区</span>
        <span v-if="!isCollapse" class="logo-text">百事通</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapse"
        router
        unique-opened
      >
        <template v-for="item in menuItems" :key="item.index">
          <!-- 带子菜单的菜单项 -->
          <el-sub-menu v-if="item.children" :index="item.index">
            <template #title>
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item 
              v-for="child in item.children" 
              :key="child.index" 
              :index="child.index"
            >
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 单独的菜单项 -->
          <el-menu-item v-else :index="item.index">
            <el-icon v-if="item.icon">
              <component :is="item.icon" />
            </el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-button 
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon :size="20">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
          
          <breadcrumb />
        </div>
        
        <div class="header-right">
          <div class="user-info" @click="toggleUserMenu">
            <el-avatar :size="30" :src="authStore.userInfo?.avatarUrl">
              {{ authStore.nickname?.substr(0, 1) }}
            </el-avatar>
            <span class="nickname">{{ authStore.nickname }}</span>
            <el-icon><arrow-down /></el-icon>
            
            <!-- 用户下拉菜单 -->
            <div v-if="userMenuVisible" class="user-dropdown">
              <div class="dropdown-item" @click="handleLogout">
                <el-icon><switch-button /></el-icon>
                退出登录
              </div>
            </div>
          </div>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
      
      <!-- 页脚 -->
      <el-footer class="footer">
        社区百事通管理系统 &copy; {{ new Date().getFullYear() }}
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import {
  HomeFilled, UserFilled, Check, Document, ChatDotRound,
  Tools, Avatar, Setting, ArrowDown, SwitchButton, QuestionFilled, Monitor, Money
} from '@element-plus/icons-vue'
import Breadcrumb from '@/components/common/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 菜单配置
const menuItems = [
  {
    title: '首页',
    icon: HomeFilled,
    index: '/',
  },
  {
    title: '用户管理',
    icon: UserFilled,
    index: '/users'
  },
  {
    title: '认证管理',
    icon: Check,
    index: '/verification'
  },
  {
    title: '内容管理',
    icon: Document,
    index: 'content',
    children: [
      {
        title: '帖子管理',
        index: '/posts'
      },
      {
        title: '评论管理',
        index: '/comments'
      }
    ]
  },
  {
    title: '报修管理',
    icon: Tools,
    index: 'repair',
    children: [
      {
        title: '工单管理',
        index: '/repair-orders'
      },
      {
        title: '维修工管理',
        index: '/repair-workers'
      }
    ]
  },
  {
    title: '红包管理',
    icon: Money,
    index: '/red-packet'
  },
  {
    title: '常见问题',
    icon: QuestionFilled,
    index: '/common-problems'
  },
  {
    title: '系统监控',
    icon: Monitor,
    index: '/news-deduplication'
  }
]

// 是否折叠侧边栏
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 用户下拉菜单
const userMenuVisible = ref(false)
const toggleUserMenu = () => {
  userMenuVisible.value = !userMenuVisible.value
}

// 登出
const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// 获取当前页面标题
const pageTitle = computed(() => {
  return route.meta.title as string || ''
})

// 初始化
onMounted(() => {
  // 可以在这里添加初始化逻辑
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.sidebar {
  height: 100%;
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 10;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background-color: #002140;
    overflow: hidden;
    
    .logo-icon {
      height: 32px;
      width: 32px;
      background-color: #1890ff;
      color: white;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 14px;
    }
    
    .logo-text {
      margin-left: 12px;
      color: white;
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    border-right: none;
    height: calc(100% - 60px);
    overflow-y: auto;
    background-color: #001529;
    
    :deep(.el-menu-item.is-active) {
      background-color: #1890ff;
      color: #fff;
    }
    
    :deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
      background-color: #001f3d;
    }
    
    :deep(.el-sub-menu.is-active .el-sub-menu__title) {
      color: #1890ff;
    }
    
    // 增加菜单文字亮度和对比度
    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.85);
      font-weight: 500;
      
      .el-icon {
        color: rgba(255, 255, 255, 0.85);
      }
    }
    
    // 未选中菜单项悬浮效果增强
    :deep(.el-menu-item:not(.is-active):hover) {
      color: #fff;
      background-color: rgba(24, 144, 255, 0.2);
    }
    
    // 子菜单项样式增强
    :deep(.el-menu--inline .el-menu-item) {
      background-color: #000c17;
      
      &:hover {
        background-color: rgba(24, 144, 255, 0.1);
      }
      
      &.is-active {
        background-color: rgba(24, 144, 255, 0.8);
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

.header {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  z-index: 9;
  height: 60px; /* 确保高度一致 */
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      margin-right: 16px;
      padding: 0;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    padding: 0 8px;
    cursor: pointer;
    position: relative;
    
    .nickname {
      margin: 0 8px;
      font-size: 14px;
      color: #606266;
    }
    
    &:hover {
      background-color: #f6f6f6;
    }
    
    .user-dropdown {
      position: absolute;
      top: 100%;
      right: 0;
      width: 120px;
      background-color: white;
      border-radius: 4px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      z-index: 100;
      padding: 4px 0;
      
      .dropdown-item {
        padding: 8px 16px;
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 8px;
        }
        
        &:hover {
          background-color: #f6f6f6;
        }
      }
    }
  }
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 16px;
  background-color: white;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.08);
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 