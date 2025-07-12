import { get } from '@/utils/request'
import type { ApiResult } from '@/types/common'
import type { OrderStatsVO } from './repair'

// 仪表盘数据接口
export interface DashboardDataVO {
  // 用户统计
  userCount: number
  userIncrease: number
  
  // 帖子统计
  postCount: number
  postIncrease: number
  
  // 工单统计
  pendingOrderCount: number
  orderIncrease: number
  
  // 审核内容统计
  reviewCount: number
  reviewIncrease: number
}

// 用户增长趋势数据
export interface UserGrowthVO {
  dates: string[]
  counts: number[]
}

// 报修工单类型分布
export interface RepairTypeDistributionVO {
  types: string[]
  counts: number[]
}

// 最新工单
export interface LatestOrderVO {
  id: number
  title: string
  userName: string
  status: string
  createdAt: string
}

// 待审核内容
export interface PendingReviewVO {
  id: number
  title: string
  type: string
  userName: string
  createdAt: string
}

/**
 * 获取仪表盘概览数据
 * @returns 仪表盘数据
 */
export function getDashboardOverview(): Promise<ApiResult<DashboardDataVO>> {
  return get<ApiResult<DashboardDataVO>>('/api/admin/dashboard/overview')
}

/**
 * 获取用户增长趋势
 * @param timeRange 时间范围：week-本周, month-本月, year-全年
 * @returns 用户增长趋势数据
 */
export function getUserGrowthTrend(timeRange: string): Promise<ApiResult<UserGrowthVO>> {
  return get<ApiResult<UserGrowthVO>>('/api/admin/dashboard/user-growth', { timeRange })
}

/**
 * 获取报修工单类型分布
 * @returns 工单类型分布数据
 */
export function getRepairTypeDistribution(): Promise<ApiResult<RepairTypeDistributionVO>> {
  return get<ApiResult<RepairTypeDistributionVO>>('/api/admin/dashboard/repair-types')
}

/**
 * 获取最新工单
 * @param limit 数量限制
 * @returns 最新工单列表
 */
export function getLatestOrders(limit: number = 5): Promise<ApiResult<LatestOrderVO[]>> {
  return get<ApiResult<LatestOrderVO[]>>('/api/admin/dashboard/latest-orders', { limit })
}

/**
 * 获取待审核内容
 * @param limit 数量限制
 * @returns 待审核内容列表
 */
export function getPendingReviews(limit: number = 5): Promise<ApiResult<PendingReviewVO[]>> {
  return get<ApiResult<PendingReviewVO[]>>('/api/admin/dashboard/pending-reviews', { limit })
} 