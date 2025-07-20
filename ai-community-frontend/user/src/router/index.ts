import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { v4 as uuidv4 } from 'uuid'

// 定义路由
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { requiresAuth: false, title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/login/RegisterView.vue'),
      meta: { requiresAuth: false, title: '注册' }
    },
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true, title: '首页' }
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('@/views/community/CommunityView.vue'),
      meta: { requiresAuth: true, title: '社区' }
    },
    {
      path: '/repair',
      name: 'repair',
      component: () => import('@/views/repair/RepairView.vue'),
      meta: { requiresAuth: true, title: '在线报修' }
    },
    {
      path: '/repair/create',
      name: 'repair-create',
      component: () => import('@/views/repair/RepairCreateView.vue'),
      meta: { requiresAuth: true, title: '创建报修' }
    },
    {
      path: '/repair/detail/:id',
      name: 'repair-detail',
      component: () => import('@/views/repair/RepairDetailView.vue'),
      meta: { requiresAuth: true, title: '报修详情' },
      props: true
    },
    {
      path: '/repair/list',
      name: 'repair-list',
      component: () => import('@/views/repair/RepairListView.vue'),
      meta: { requiresAuth: true, title: '我的报修' }
    },
    {
      path: '/post-detail/:id',
      name: 'post-detail',
      component: () => import('@/views/posts/PostDetailView.vue'),
      meta: { requiresAuth: true, title: '帖子详情' }
    },
    {
      path: '/post-publish',
      name: 'post-publish',
      component: () => import('@/views/posts/PostPublishView.vue'),
      meta: { requiresAuth: true, title: '发布帖子' }
    },
    {
      path: '/my-posts',
      name: 'my-posts',
      component: () => import('@/views/posts/MyPostsView.vue'),
      meta: { requiresAuth: true, title: '我的帖子' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/ProfileView.vue'),
      meta: { requiresAuth: true, title: '个人中心' },
      children: [
        {
          path: '',
          name: 'profile-index',
          component: () => import('@/views/profile/ProfileIndexView.vue'),
          meta: { requiresAuth: true, title: '个人中心' }
        },
        {
          path: 'edit',
          name: 'profile-edit',
          component: () => import('@/views/profile/ProfileEditView.vue'),
          meta: { requiresAuth: true, title: '编辑资料' }
        },
        {
          path: 'password',
          name: 'change-password',
          component: () => import('@/views/profile/ChangePasswordView.vue'),
          meta: { requiresAuth: true, title: '修改密码' }
        },
        {
          path: 'verification',
          name: 'verification',
          component: () => import('@/views/profile/VerificationView.vue'),
          meta: { requiresAuth: true, title: '实名认证' }
        }
      ]
    },
    {
      path: '/ai-chat',
      name: 'ai-chat',
      component: () => import('@/views/ai-chat/AiChatView.vue'),
      meta: { requiresAuth: true, title: 'AI聊天' }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { requiresAuth: false, title: '页面不存在' }
    }
  ]
})

// 导航守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = (to.meta.title as string) || '社区百事通'
  
  // 延迟获取store以确保正确初始化
  setTimeout(() => {
    // 同时检查Pinia store和localStorage确保登录状态判断准确
    const authStore = useAuthStore()
    const token = localStorage.getItem('token')
    const isLoggedIn = authStore.isLoggedIn || !!token
    
    // 添加调试信息
    console.log('路由守卫 - 从:', from.path, '到:', to.path)
    console.log('路由守卫 - 认证状态:', isLoggedIn, 'store:', authStore.isLoggedIn, 'token:', !!token)
    
    // 判断页面是否需要登录权限
    if (to.meta.requiresAuth && !isLoggedIn) {
      // 如果需要登录但用户未登录，跳转到登录页面
      console.log('需要登录权限，但用户未登录，重定向到登录页')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      // 如果已登录或不需要登录权限，直接跳转
      console.log('通过路由守卫检查，允许访问:', to.path)
      next()
    }
  }, 0)
})

// 添加全局后置钩子，帮助调试
router.afterEach((to, from) => {
  console.log('路由跳转完成 - 从:', from.path, '到:', to.path)
})

export default router