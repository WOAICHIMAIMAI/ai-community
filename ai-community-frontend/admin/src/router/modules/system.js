import Layout from '@/layout'

const systemRouter = {
  path: '/system',
  component: Layout,
  redirect: '/system/news-deduplication',
  name: 'System',
  meta: {
    title: '系统监控',
    icon: 'monitor'
  },
  children: [
    {
      path: 'news-deduplication',
      component: () => import('@/views/system/NewsDeduplicationMonitor'),
      name: 'NewsDeduplicationMonitor',
      meta: {
        title: '新闻去重监控',
        icon: 'document',
        roles: ['admin'] // 只有管理员可以访问
      }
    }
  ]
}

export default systemRouter
