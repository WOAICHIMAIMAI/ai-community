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
    path: '/news',
    name: 'NewsList',
    component: () => import('@/views/news/NewsListView.vue'),
    meta: { title: '新闻资讯' }
  },
  {
    path: '/news/:id',
    name: 'NewsDetail',
    component: () => import('@/views/news/NewsDetailView.vue'),
    meta: { title: '新闻详情' }
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
    path: '/repair/create',
    name: 'RepairCreate',
    component: () => import('@/views/repair/RepairCreateView.vue'),
    meta: { title: '新增报修' }
  },
  {
    path: '/repair/list',
    name: 'RepairList',
    component: () => import('@/views/repair/RepairListView.vue'),
    meta: { title: '我的报修' }
  },
  {
    path: '/repair/detail/:id',
    name: 'RepairDetail',
    component: () => import('@/views/repair/RepairDetailView.vue'),
    meta: { title: '报修详情' }
  },
  // 地址簿路由
  {
    path: '/address-book',
    name: 'AddressBook',
    component: () => import('@/views/address/AddressBookListView.vue'),
    meta: { title: '地址簿' }
  },
  {
    path: '/common-problems',
    name: 'CommonProblems',
    component: () => import('@/views/common-problems/CommonProblemsView.vue'),
    meta: { title: '常见问题' }
  },
  {
    path: '/common-problems/detail/:id',
    name: 'CommonProblemDetail',
    component: () => import('@/views/common-problems/ProblemDetailView.vue'),
    meta: { title: '问题详情' }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/payment/PaymentViewSimple.vue'),
    meta: { title: '费用缴纳' }
  },
  {
    path: '/payment/detail/:id',
    name: 'PaymentDetail',
    component: () => import('@/views/payment/PaymentDetailView.vue'),
    meta: { title: '账单详情' }
  },
  {
    path: '/payment/history',
    name: 'PaymentHistory',
    component: () => import('@/views/payment/PaymentHistoryView.vue'),
    meta: { title: '缴费历史' }
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: () => import('@/views/appointment/AppointmentHomeView.vue'),
    meta: { title: '预约服务' }
  },
  {
    path: '/appointment/services',
    name: 'AppointmentServices',
    component: () => import('@/views/appointment/AppointmentView.vue'),
    meta: { title: '服务分类' }
  },
  {
    path: '/appointment/booking/:serviceType',
    name: 'AppointmentBooking',
    component: () => import('@/views/appointment/BookingView.vue'),
    meta: { title: '预约服务' }
  },
  {
    path: '/appointment/list',
    name: 'AppointmentList',
    component: () => import('@/views/appointment/AppointmentListView.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/appointment/detail/:id',
    name: 'AppointmentDetail',
    component: () => import('@/views/appointment/AppointmentDetailView.vue'),
    meta: { title: '预约详情' }
  },
  // 红包相关路由
  {
    path: '/red-packet',
    name: 'RedPacketList',
    component: () => import('@/views/red-packet/RedPacketListView.vue'),
    meta: { title: '红包活动' }
  },
  {
    path: '/red-packet/:id',
    name: 'RedPacketDetail',
    component: () => import('@/views/red-packet/RedPacketDetailView.vue'),
    meta: { title: '红包详情' }
  },
  {
    path: '/red-packet/records',
    name: 'RedPacketRecords',
    component: () => import('@/views/red-packet/RedPacketRecordsView.vue'),
    meta: { title: '我的红包' }
  },

  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import('@/views/analytics/AnalyticsHomeView.vue'),
    meta: { title: '数据分析' }
  },
  {
    path: '/analytics/detail/:type',
    name: 'AnalyticsDetail',
    component: () => import('@/views/analytics/AnalyticsDetailView.vue'),
    meta: { title: '详细分析' }
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
