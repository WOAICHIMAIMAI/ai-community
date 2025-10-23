import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { useAuthStore } from '@/store/auth'
import MainLayout from '@/layout/MainLayout.vue'
import NotFound from '@/views/NotFound.vue'

// 路由配置
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: {
        title: '登录',
        requiresAuth: false
      }
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: {
            title: '首页',
            requiresAuth: true
          }
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/users/UserListView.vue'),
          meta: {
            title: '用户管理',
            requiresAuth: true
          }
        },
        {
          path: 'verification',
          name: 'verification',
          component: () => import('@/views/users/VerificationView.vue'),
          meta: {
            title: '认证管理',
            requiresAuth: true
          }
        },
        {
          path: 'posts',
          name: 'posts',
          component: () => import('@/views/posts/PostListView.vue'),
          meta: {
            title: '帖子管理',
            requiresAuth: true
          }
        },
        {
          path: 'comments',
          name: 'comments',
          component: () => import('@/views/comments/CommentListView.vue'),
          meta: {
            title: '评论管理',
            requiresAuth: true
          }
        },
        {
          path: 'repair-orders',
          name: 'repair-orders',
          component: () => import('@/views/repairs/RepairOrderView.vue'),
          meta: {
            title: '报修工单',
            requiresAuth: true
          }
        },
        {
          path: 'repair-workers',
          name: 'repair-workers',
          component: () => import('@/views/repairs/WorkerManageView.vue'),
          meta: {
            title: '维修工管理',
            requiresAuth: true
          }
        },
        {
          path: 'common-problems',
          name: 'common-problems',
          component: () => import('@/views/common-problems/ProblemListView.vue'),
          meta: {
            title: '常见问题管理',
            requiresAuth: true
          }
        },
        {
          path: 'appointments',
          name: 'appointments',
          component: () => import('@/views/appointments/AppointmentListView.vue'),
          meta: {
            title: '预约服务管理',
            requiresAuth: true
          }
        },
        {
          path: 'news',
          name: 'news',
          component: () => import('@/views/news/NewsListView.vue'),
          meta: {
            title: '新闻管理',
            requiresAuth: true
          }
        },
        {
          path: 'news-deduplication',
          name: 'news-deduplication',
          component: () => import('@/views/system/SimpleMonitor.vue'),
          meta: {
            title: '新闻去重监控',
            requiresAuth: true
          }
        },
        {
          path: 'red-packet',
          name: 'red-packet',
          component: () => import('@/views/red-packet/RedPacketListView.vue'),
          meta: {
            title: '红包管理',
            requiresAuth: true
          }
        },
        {
          path: 'red-packet/create',
          name: 'red-packet-create',
          component: () => import('@/views/red-packet/RedPacketCreateView.vue'),
          meta: {
            title: '创建红包活动',
            requiresAuth: true
          }
        },
        {
          path: 'red-packet/:id',
          name: 'red-packet-detail',
          component: () => import('@/views/red-packet/RedPacketDetailView.vue'),
          meta: {
            title: '红包活动详情',
            requiresAuth: true
          }
        },
        {
          path: 'analytics',
          name: 'Analytics',
          component: () => import('@/views/analytics/AnalyticsView.vue'),
          meta: {
            title: '数据分析',
            requiresAuth: true
          }
        },
        {
          path: 'analytics/detail/:type',
          name: 'AnalyticsDetail',
          component: () => import('@/views/analytics/AnalyticsDetailView.vue'),
          meta: {
            title: '数据分析详情',
            requiresAuth: true
          }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFound,
      meta: {
        title: '页面不存在'
      }
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 开始进度条
  NProgress.start()

  // 设置页面标题
  document.title = `${to.meta.title || '首页'} - 社区百事通管理系统`

  console.log('路由导航:', to.path)

  // 检查是否需要登录权限
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  const authStore = useAuthStore()
  const hasToken = !!localStorage.getItem('token')

  if (requiresAuth) {
    // 需要登录权限的页面
    if (hasToken) {
      // 有token，但没有用户信息，尝试获取用户信息
      if (!authStore.userInfo) {
        authStore.checkLoginStatus().then(() => {
          next()
        }).catch(() => {
          // 获取用户信息失败，可能是token无效
          authStore.logout()
          next({ 
            path: '/login', 
            query: { redirect: to.fullPath } 
          })
        })
      } else {
        // 有token且有用户信息，正常访问
        next()
      }
    } else {
      // 没有token，重定向到登录页
      next({ 
        path: '/login', 
        query: { redirect: to.fullPath }
      })
    }
  } else if (to.path === '/login' && hasToken) {
    // 已登录状态访问登录页，重定向到首页
    next({ path: '/dashboard' })
  } else {
    // 不需要登录权限的页面
    next()
  }
})

// 全局后置守卫
router.afterEach(() => {
  // 结束进度条
  NProgress.done()
})

export default router 