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
 * 获取预约服务列表
 */
export const getServices = (params: ServiceQueryParams) => {
  return request.get<PageResponse<AppointmentService>>('/admin/appointments/services', { params })
}

/**
 * 获取预约记录列表
 */
export const getAppointments = (params: AppointmentQueryParams) => {
  return request.get<PageResponse<AppointmentRecord>>('/admin/appointments/records', { params })
}

/**
 * 根据ID获取服务详情
 */
export const getServiceDetail = (id: number) => {
  return request.get<AppointmentService>(`/admin/appointments/services/${id}`)
}

/**
 * 根据ID获取预约详情
 */
export const getAppointmentDetail = (id: number) => {
  return request.get<AppointmentRecord>(`/admin/appointments/records/${id}`)
}

/**
 * 创建预约服务
 */
export const createService = (data: ServiceForm) => {
  return request.post<boolean>('/admin/appointments/services', data)
}

/**
 * 更新预约服务
 */
export const updateService = (id: number, data: ServiceForm) => {
  return request.put<boolean>(`/admin/appointments/services/${id}`, data)
}

/**
 * 删除预约服务
 */
export const deleteService = (id: number) => {
  return request.delete<boolean>(`/admin/appointments/services/${id}`)
}

/**
 * 批量删除预约服务
 */
export const batchDeleteServices = (ids: number[]) => {
  return request.delete<boolean>('/admin/appointments/services/batch', { data: { ids } })
}

/**
 * 创建预约记录
 */
export const createAppointment = (data: AppointmentForm) => {
  return request.post<boolean>('/admin/appointments/records', data)
}

/**
 * 更新预约状态
 */
export const updateAppointmentStatus = (id: number, status: AppointmentStatus, workerId?: number, reason?: string) => {
  return request.put<boolean>(`/admin/appointments/records/${id}/status`, { 
    status, 
    workerId, 
    reason 
  })
}

/**
 * 分配工作人员
 */
export const assignWorker = (id: number, workerId: number) => {
  return request.put<boolean>(`/admin/appointments/records/${id}/assign`, { workerId })
}

/**
 * 获取预约统计数据
 */
export const getAppointmentStats = () => {
  return request.get<AppointmentStats>('/admin/appointments/stats')
}

/**
 * 导出预约记录
 */
export const exportAppointments = (params: AppointmentQueryParams) => {
  return request.get('/admin/appointments/records/export', { 
    params,
    responseType: 'blob'
  })
}
