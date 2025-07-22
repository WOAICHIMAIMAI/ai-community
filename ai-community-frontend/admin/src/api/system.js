import request from '@/utils/request'

/**
 * 获取布隆过滤器统计信息
 */
export function getBloomFilterStats() {
  return request({
    url: '/admin/news/bloom-filter/stats',
    method: 'get'
  })
}

/**
 * 手动检查布隆过滤器状态
 */
export function checkBloomFilterStatus() {
  return request({
    url: '/admin/news/bloom-filter/check',
    method: 'get'
  })
}

/**
 * 重建布隆过滤器（系统优化）
 */
export function rebuildBloomFilter() {
  return request({
    url: '/admin/news/bloom-filter/rebuild',
    method: 'post'
  })
}

/**
 * 获取布隆过滤器告警建议
 */
export function getAlertRecommendation() {
  return request({
    url: '/admin/news/bloom-filter/alert-recommendation',
    method: 'get'
  })
}

/**
 * 获取监控配置信息
 */
export function getMonitorConfig() {
  return request({
    url: '/admin/news/bloom-filter/monitor-config',
    method: 'get'
  })
}
