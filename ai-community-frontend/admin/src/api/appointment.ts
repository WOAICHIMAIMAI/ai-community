import request from '@/utils/request'

// 预约服务类型枚举
export enum AppointmentType {
  MAINTENANCE = 1,  // 维修服务
  CLEANING = 2,     // 保洁服务
  SECURITY = 3,     // 安保服务
  DELIVERY = 4,     // 快递代收
  OTHER = 5         // 其他服务
}

// 预约状态枚举
export enum AppointmentStatus {
  PENDING = 0,      // 待处理
  CONFIRMED = 1,    // 已确认
  IN_PROGRESS = 2,  // 进行中
  COMPLETED = 3,    // 已完成
  CANCELLED = 4     // 已取消
}

// 预约服务接口
export interface AppointmentService {
  id: number
  serviceName: string
  serviceType: AppointmentType
  description: string
  price: number
  duration: number  // 服务时长(分钟)
  isActive: boolean
  createdAt: string
  updatedAt: string
}

// 预约记录接口
export interface AppointmentRecord {
  id: number
  userId: number
  username: string
  userPhone: string
  serviceId: number
  serviceName: string
  serviceType: AppointmentType
  appointmentTime: string
  address: string
  contactPhone: string
  description: string
  status: AppointmentStatus
  price: number
  workerId?: number
  workerName?: string
  workerPhone?: string
  createdAt: string
  updatedAt: string
  completedAt?: string
  cancelledAt?: string
  cancelReason?: string
  rating?: number
  feedback?: string
}

// 预约查询参数
export interface AppointmentQueryParams {
  serviceType?: AppointmentType
  status?: AppointmentStatus
  userId?: number
  workerId?: number
  keyword?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

// 服务查询参数
export interface ServiceQueryParams {
  serviceType?: AppointmentType
  isActive?: boolean
  keyword?: string
  page?: number
  pageSize?: number
}

// 预约表单接口
export interface AppointmentForm {
  id?: number
  userId: number
  serviceId: number
  appointmentTime: string
  address: string
  contactPhone: string
  description?: string
}

// 服务表单接口
export interface ServiceForm {
  id?: number
  serviceName: string
  serviceType: AppointmentType
  description: string
  price: number
  duration: number
  isActive: boolean
}

// 分页响应接口
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 统计数据接口
export interface AppointmentStats {
  totalAppointments: number
  pendingCount: number
  confirmedCount: number
  inProgressCount: number
  completedCount: number
  cancelledCount: number
  todayAppointments: number
  weekAppointments: number
  monthAppointments: number
}

/**
 * 获取预约服务列表（管理员）
 */
export const getServiceList = (query: ServiceQueryParams) => {
  return request.post<PageResponse<AppointmentService>>('/api/admin/appointment/services/list', query)
}

/**
 * 获取预约记录列表（管理员）
 */
export const getOrderList = (params: AppointmentQueryParams) => {
  return request.get<PageResponse<AppointmentRecord>>('/api/admin/appointment/orders/list', { params })
}

/**
 * 根据ID获取服务详情（管理员）
 */
export const getServiceDetail = (id: number) => {
  return request.get<AppointmentService>(`/api/admin/appointment/services/${id}`)
}

/**
 * 根据ID获取订单详情（管理员）
 */
export const getOrderDetail = (id: number) => {
  return request.get<AppointmentRecord>(`/api/admin/appointment/orders/${id}`)
}

/**
 * 创建预约服务（弃用，管理员不直接创建）
 */
export const createService = (data: ServiceForm) => {
  return request.post<boolean>('/api/admin/appointment/services', data)
}

/**
 * 审核服务（通过/拒绝）
 */
export const approveService = (serviceId: number, approved: boolean, rejectReason?: string) => {
  return request.post<boolean>('/api/admin/appointment/services/approve', {
    serviceId,
    approved,
    rejectReason
  })
}

/**
 * 更新服务状态（启用/禁用）
 */
export const updateServiceStatus = (id: number, status: number) => {
  return request.put<boolean>(`/api/admin/appointment/services/${id}/status`, null, {
    params: { status }
  })
}

/**
 * 设置热门服务
 */
export const setHotService = (id: number, isHot: number) => {
  return request.put<boolean>(`/api/admin/appointment/services/${id}/hot`, null, {
    params: { isHot }
  })
}

/**
 * 删除预约服务
 */
export const deleteService = (id: number) => {
  return request.delete<boolean>(`/api/admin/appointment/services/${id}`)
}

/**
 * 获取待审核服务数量
 */
export const getPendingServiceCount = () => {
  return request.get<number>('/api/admin/appointment/services/pending/count')
}

/**
 * 创建预约记录（弃用，用户端创建）
 */
export const createAppointment = (data: AppointmentForm) => {
  return request.post<boolean>('/api/admin/appointment/orders', data)
}

/**
 * 确认订单
 */
export const confirmOrder = (id: number) => {
  return request.put<boolean>(`/api/admin/appointment/orders/${id}/confirm`)
}

/**
 * 分配服务人员
 */
export const assignWorker = (orderId: number, workerId: number, workerName: string) => {
  return request.put<boolean>(`/api/admin/appointment/orders/${orderId}/assign`, {
    orderId,
    workerId,
    workerName
  })
}

/**
 * 更新订单状态
 */
export const updateOrderStatus = (orderId: number, status: AppointmentStatus, remark?: string) => {
  return request.put<boolean>(`/api/admin/appointment/orders/${orderId}/status`, { 
    orderId,
    status, 
    remark 
  })
}

/**
 * 取消订单
 */
export const cancelOrder = (id: number, reason: string) => {
  return request.put<boolean>(`/api/admin/appointment/orders/${id}/cancel`, null, {
    params: { reason }
  })
}

/**
 * 删除订单
 */
export const deleteOrder = (id: number) => {
  return request.delete<boolean>(`/api/admin/appointment/orders/${id}`)
}

/**
 * 获取订单统计数据
 */
export const getOrderStats = () => {
  return request.get<Map<string, any>>('/api/admin/appointment/orders/stats')
}
