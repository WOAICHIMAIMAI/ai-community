import request from '@/utils/request'

// 预约服务数据接口
export interface AppointmentService {
  type: string
  name: string
  icon: string
  description: string
  price: string
  rating: string
  isHot?: boolean
  gradient?: string
  color?: string
}

// 预约记录数据接口
export interface AppointmentRecord {
  id: number
  orderNo: string
  serviceName: string
  serviceType: string
  appointmentTime: string
  address: string
  contactName: string
  contactPhone: string
  requirements?: string
  status: 'pending' | 'confirmed' | 'completed' | 'cancelled'
  worker?: {
    name: string
    phone: string
    avatar: string
  }
  rated?: boolean
}

// 预约创建数据接口
export interface AppointmentCreateData {
  serviceType: string
  appointmentTime: string
  address: string
  contactName: string
  contactPhone: string
  requirements?: string
}

// 分页查询参数
export interface AppointmentPageQuery {
  page: number
  pageSize: number
  status?: string
  serviceType?: string
}

/**
 * 获取服务类型列表
 */
export const getServiceTypes = () => {
  return request({
    url: '/api/user/appointment/services',
    method: 'get'
  })
}

/**
 * 获取热门服务
 */
export const getHotServices = () => {
  return request({
    url: '/api/user/appointment/services/hot',
    method: 'get'
  })
}

/**
 * 获取推荐服务
 */
export const getRecommendServices = () => {
  return request({
    url: '/api/user/appointment/services/recommend',
    method: 'get'
  })
}

/**
 * 创建预约
 */
export const createAppointment = (data: AppointmentCreateData) => {
  return request({
    url: '/api/user/appointment',
    method: 'post',
    data
  })
}

/**
 * 分页查询用户预约记录
 */
export const getAppointmentList = (params: AppointmentPageQuery) => {
  return request({
    url: '/api/user/appointment/list',
    method: 'get',
    params
  })
}

/**
 * 获取预约详情
 */
export const getAppointmentDetail = (id: number) => {
  return request({
    url: `/api/user/appointment/${id}`,
    method: 'get'
  })
}

/**
 * 取消预约
 */
export const cancelAppointment = (id: number) => {
  return request({
    url: `/api/user/appointment/${id}/cancel`,
    method: 'put'
  })
}

/**
 * 改期预约
 */
export const rescheduleAppointment = (id: number, appointmentTime: string) => {
  return request({
    url: `/api/user/appointment/${id}/reschedule`,
    method: 'put',
    data: { appointmentTime }
  })
}

/**
 * 评价服务
 */
export const rateAppointment = (data: {
  appointmentId: number
  rating: number
  comment?: string
}) => {
  return request({
    url: '/api/user/appointment/rate',
    method: 'post',
    data
  })
}

/**
 * 获取可用时间段
 */
export const getAvailableTimeSlots = (params: {
  serviceType: string
  date: string
}) => {
  return request({
    url: '/api/user/appointment/time-slots',
    method: 'get',
    params
  })
}

/**
 * 获取服务人员列表
 */
export const getServiceWorkers = (serviceType: string) => {
  return request({
    url: '/api/user/appointment/workers',
    method: 'get',
    params: { serviceType }
  })
}

/**
 * 获取最近预约记录
 */
export const getRecentAppointments = (limit: number = 3) => {
  return request({
    url: '/api/user/appointment/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取预约统计信息
 */
export const getAppointmentStats = () => {
  return request({
    url: '/api/user/appointment/stats',
    method: 'get'
  })
}

// 导出默认的API对象
export default {
  getServiceTypes,
  getHotServices,
  getRecommendServices,
  createAppointment,
  getAppointmentList,
  getAppointmentDetail,
  cancelAppointment,
  rescheduleAppointment,
  rateAppointment,
  getAvailableTimeSlots,
  getServiceWorkers,
  getRecentAppointments,
  getAppointmentStats
}
