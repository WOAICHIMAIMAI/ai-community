import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, ApiResult } from '@/types/common'

// 报修工单数据接口（对应后端 AdminRepairOrderVO）
export interface RepairOrder {
  id: number
  orderNumber: string
  userId: number
  userName: string | null
  userNickname: string | null
  addressId: number | null
  addressDetail: string
  repairType: string
  title: string | null
  description: string
  images: string
  contactPhone: string
  expectedTime: string | null
  urgencyLevel: number | null
  status: number
  statusDesc: string
  workerId: number | null
  workerName: string | null
  workerPhone: string | null
  workerAvatar: string | null
  appointmentTime: string | null
  completionTime: string | null
  satisfactionLevel: number | null
  feedback: string | null
  createTime: string
  updateTime: string
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
  avatarUrl: string
  serviceType: string  // 服务类型，多个用逗号分隔
  serviceTypeList?: string[]  // 服务类型列表
  idCardNumber?: string  // 身份证号
  skills: string[]
  status: number
  workStatus: number  // 工作状态：0-休息 1-可接单 2-忙碌
  rating: number
  completedOrders: number
  ongoingOrders: number
  introduction?: string
  createTime?: string  // 创建时间
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
  monthlyServiceCount?: number  // 本月服务次数
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
 * 添加维修工
 * @param data 维修工信息
 * @returns 操作结果
 */
export function addWorker(data: any): Promise<ApiResult<number>> {
  return post<ApiResult<number>>('/api/admin/workers', data)
}

/**
 * 更新维修工
 * @param data 维修工信息
 * @returns 操作结果
 */
export function updateWorker(data: any): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/workers', data)
}

/**
 * 删除维修工
 * @param id 维修工ID
 * @returns 操作结果
 */
export function deleteWorker(id: number): Promise<ApiResult<boolean>> {
  return del<ApiResult<boolean>>(`/api/admin/workers/${id}`)
}

/**
 * 获取工单统计数据
 * @returns 统计数据
 */
export function getOrderStats(): Promise<ApiResult<OrderStatsVO>> {
  return get<ApiResult<OrderStatsVO>>('/api/admin/repair-orders/stats')
}

/**
 * 获取所有维修工绩效统计列表
 * @param limit 限制返回数量，默认10
 * @returns 绩效列表
 */
export function getAllWorkerStats(limit: number = 10): Promise<ApiResult<WorkerStatsVO[]>> {
  return get<ApiResult<WorkerStatsVO[]>>('/api/admin/workers/stats', { limit })
}

/**
 * 获取单个维修工绩效统计
 * @param workerId 维修工ID
 * @returns 绩效统计
 */
export function getWorkerStats(workerId: number): Promise<ApiResult<any>> {
  return get<ApiResult<any>>(`/api/admin/workers/${workerId}/stats`)
} 