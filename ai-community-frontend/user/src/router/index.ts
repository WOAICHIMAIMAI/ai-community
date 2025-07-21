import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('@/views/community/CommunityView.vue')
  },
  {
    path: '/post-detail/:id',
    name: 'PostDetail',
    component: () => import('@/views/posts/PostDetailView.vue')
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: () => import('@/views/user/UserProfileView.vue')
  },
  {
    path: '/post-publish',
    name: 'PostPublish',
    component: () => import('@/views/posts/PostPublishView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { title: '注册' }
  },
  // 修复AI聊天相关路由
  {
    path: '/ai-chat-list',
    name: 'AiChatList',
    component: () => import('@/views/ai-chat/AiChatListView.vue'),
    meta: { title: 'AI聊天记录' }
  },
  {
    path: '/ai-chat',
    name: 'AiChatDetail',
    component: () => import('@/views/ai-chat/AiChatView.vue'),
    meta: { title: 'AI对话' }
  },
  // 修复聊天列表路由
  {
    path: '/chat-list',
    name: 'ChatList',
    component: () => import('@/views/chat/ChatListView.vue'),
    meta: { title: '聊天列表' }
  },
  {
    path: '/chat/:id',
    name: 'ChatDetail',
    component: () => import('@/views/chat/ChatDetailView.vue'),
    meta: { title: '聊天详情' }
  },
  // 修复个人中心路由 - 使用正确的文件路径
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { title: '个人中心' },
    children: [
      {
        path: '',
        name: 'ProfileIndex',
        component: () => import('@/views/profile/ProfileIndexView.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'edit',
        name: 'ProfileEdit',
        component: () => import('@/views/profile/ProfileEditView.vue'),
        meta: { title: '编辑资料' }
      },
      {
        path: 'verification',
        name: 'ProfileVerification',
        component: () => import('@/views/profile/VerificationView.vue'),
        meta: { title: '实名认证' }
      },
      {
        path: 'password',
        name: 'ProfilePassword',
        component: () => import('@/views/profile/ChangePasswordView.vue'),
        meta: { title: '修改密码' }
      }
    ]
  },
  // 添加我的帖子路由
  {
    path: '/my-posts',
    name: 'MyPosts',
    component: () => import('@/views/posts/MyPostsView.vue'),
    meta: { title: '我的帖子' }
  },
  {
    path: '/repair',
    name: 'Repair',
    component: () => import('@/views/repair/RepairView.vue'),
    meta: { title: '在线报修' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 社区百事通`
  }
  
  next()
})

export default router
