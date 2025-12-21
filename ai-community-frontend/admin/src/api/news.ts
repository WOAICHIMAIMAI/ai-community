import request from '@/utils/request'

/**
 * 热点新闻接口
 */

// 新闻分类枚举
export enum NewsCategory {
  TECH = 'tech',
  FINANCE = 'finance',
  ENTERTAINMENT = 'entertainment',
  SPORTS = 'sports',
  OTHER = 'other'
}

// 新闻状态枚举
export enum NewsStatus {
  DRAFT = 0,      // 草稿
  PUBLISHED = 1,  // 已发布
  HIDDEN = 2      // 已隐藏
}

// 新闻接口
export interface HotNews {
  id: number
  title: string
  content: string
  source: string
  sourceUrl: string
  author?: string
  category: string
  isHot: number        // 0-否 1-是
  isTop: number        // 0-否 1-是
  status: number       // 0-草稿 1-已发布 2-已隐藏
  viewCount: number
  shareCount: number
  publishTime: string
  createTime: string
  updateTime: string
}

// 新闻查询参数
export interface NewsQueryParams {
  page?: number
  pageSize?: number
  category?: string
  isHot?: number
  status?: number
}

// 分页响应接口
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询新闻列表
 */
export const getNewsPage = (params: NewsQueryParams) => {
  return request.get<PageResponse<HotNews>>('/api/admin/news/page', { params })
}

/**
 * 手动触发爬虫任务
 */
export const crawlNews = () => {
  return request.post<{ addedCount: number; totalCount: number }>('/api/admin/news/crawl')
}

/**
 * 清理无效新闻
 */
export const cleanInvalidNews = () => {
  return request.post<{ cleanedCount: number; remainingCount: number }>('/api/admin/news/clean')
}

/**
 * 更新新闻状态
 */
export const updateNewsStatus = (id: number, status: number) => {
  return request.put<string>(`/api/admin/news/${id}/status`, null, {
    params: { status }
  })
}

/**
 * 更新新闻是否热点
 */
export const updateNewsHot = (id: number, isHot: number) => {
  return request.put<string>(`/api/admin/news/${id}/hot`, null, {
    params: { isHot }
  })
}

/**
 * 更新新闻是否置顶
 */
export const updateNewsTop = (id: number, isTop: number) => {
  return request.put<string>(`/api/admin/news/${id}/top`, null, {
    params: { isTop }
  })
}

/**
 * 删除新闻
 */
export const deleteNews = (id: number) => {
  return request.delete<string>(`/api/admin/news/${id}`)
}

/**
 * 获取布隆过滤器统计信息
 */
export const getBloomFilterStats = () => {
  return request.get('/api/admin/news/bloom-filter/stats')
}

/**
 * 重建布隆过滤器
 */
export const rebuildBloomFilter = () => {
  return request.post('/api/admin/news/bloom-filter/rebuild')
}

