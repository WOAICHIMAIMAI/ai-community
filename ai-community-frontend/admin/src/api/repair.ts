import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, ApiResult } from '@/types/common'

// 报修工单数据接口
export interface RepairOrder {
  id: number
  userId: number
  username: string
  userPhone: string
  repairType: number
  repairTypeName: string
  address: string
  description: string
  images: string[]
  status: number
  createdTime: string
  expectedTime: string
  urgencyLevel: number
  workerId: number | null
  workerName: string | null
  workerPhone: string | null
}

// 报修进度数据接口
export interface RepairProgress {
  id: number
  orderId: number
  status: number
  remark: string
  images: string[]
  operatorId: number
  operatorName: string
  operatorType: number
  createdTime: string
}

// 维修工人数据接口
export interface RepairWorker {
  id: number
  name: string
  phone: string
  avatar: string
  skills: string[]
  status: number
  rating: number
  completedOrders: number
  ongoingOrders: number
}

// 工单分配参数
export interface AssignWorkerParams {
  orderId: number
  workerId: number
}

// 工单状态更新参数
export interface UpdateOrderStatusParams {
  orderId: number
  status: number
}

// 添加进度参数
export interface AddProgressParams {
  orderId: number
  status: number
  remark: string
  images?: string[]
}

// 维修工状态参数
export interface WorkerStatusParams {
  workerId: number
  status: number
}

// 工单统计数据
export interface OrderStatsVO {
  // 使用与后端一致的字段名
  totalCount: number
  pendingCount: number
  processingCount: number
  completedCount: number
  cancelledCount: number
  todayCount: number
  todayCompletedCount: number
  weekCount?: number
  monthCount?: number
  
  // 保留原有字段名，用于兼容前端现有代码
  total: number
  pending: number
  processing: number
  completed: number
  cancelled: number
  todayNew: number
  todayCompleted: number
}

// 工人绩效数据
export interface WorkerStatsVO {
  id: number
  name: string
  avatar: string
  completedCount: number
  rating: number
  avgCompletionTime: number
  goodReviews: number
}

/**
 * 获取报修工单分页列表
 * @param params 分页参数
 * @returns 工单分页数据
 */
export function getRepairOrderList(params: PageParams): Promise<PageResult<RepairOrder>> {
  return get<PageResult<RepairOrder>>('/api/admin/repair-orders/page', params)
}

/**
 * 获取工单详情
 * @param orderId 工单ID
 * @returns 工单详情
 */
export function getRepairOrderDetail(orderId: number): Promise<ApiResult<RepairOrder>> {
  return get<ApiResult<RepairOrder>>(`/api/admin/repair-orders/${orderId}`)
}

/**
 * 更新工单状态
 * @param params 状态参数
 * @returns 操作结果
 */
export function updateRepairOrderStatus(params: UpdateOrderStatusParams): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/repair-orders/status', params)
}

/**
 * 分配维修工
 * @param params 分配参数
 * @returns 操作结果
 */
export function assignRepairWorker(params: AssignWorkerParams): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/repair-orders/assign', params)
}

/**
 * 获取工单进度列表
 * @param orderId 工单ID
 * @returns 进度列表
 */
export function getRepairProgressList(orderId: number): Promise<ApiResult<RepairProgress[]>> {
  return get<ApiResult<RepairProgress[]>>(`/api/admin/repair-progress/list/${orderId}`)
}

/**
 * 添加工单进度
 * @param params 进度参数
 * @returns 操作结果
 */
export function addRepairProgress(params: AddProgressParams): Promise<ApiResult<boolean>> {
  return post<ApiResult<boolean>>('/api/admin/repair-progress/progress', params)
}

/**
 * 获取维修工人分页列表
 * @param params 分页参数
 * @returns 维修工分页数据
 */
export function getRepairWorkerList(params: PageParams): Promise<PageResult<RepairWorker>> {
  return get<PageResult<RepairWorker>>('/api/admin/workers/page', params)
}

/**
 * 更新维修工状态
 * @param params 状态参数
 * @returns 操作结果
 */
export function updateWorkerStatus(params: WorkerStatusParams): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/workers/status', params)
}

/**
 * 获取工单统计数据
 * @returns 统计数据
 */
export function getOrderStats(): Promise<ApiResult<OrderStatsVO>> {
  return get<ApiResult<OrderStatsVO>>('/api/admin/repair-orders/stats')
}

/**
 * 获取维修工绩效列表
 * @param workerId 维修工ID，如果不提供则获取所有维修工的统计
 * @returns 绩效列表
 */
export function getWorkerStats(workerId?: number): Promise<ApiResult<WorkerStatsVO[]>> {
  if (workerId) {
    // 获取指定维修工的统计数据
    return get<ApiResult<WorkerStatsVO[]>>(`/api/admin/workers/${workerId}/stats`)
  } else {
    // 在没有指定workerId时，可能需要一个默认值或采用其他逻辑
    // 这里暂时使用ID为1的维修工，或者根据实际需求修改
    return get<ApiResult<WorkerStatsVO[]>>('/api/admin/workers/1/stats')
  }
} 