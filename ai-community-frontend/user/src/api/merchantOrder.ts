import request from '@/utils/request'

/**
 * 商家服务订单API
 */

// 分页查询商家服务订单
export interface MerchantOrderPageQuery {
  page: number
  pageSize: number
  status?: number  // 0-待确认 1-已确认 2-服务中 3-已完成 4-已取消
  serviceType?: string
  keyword?: string
}

// 商家订单VO
export interface MerchantOrderVO {
  id: number
  orderNo: string
  userId: number
  serviceId: number
  serviceType: string
  serviceName: string
  workerId?: number
  workerName?: string
  workerPhone?: string
  appointmentTime: string
  address: string
  contactName: string
  contactPhone: string
  requirements?: string
  estimatedPrice: string
  actualPrice?: string
  status: number
  statusDesc: string
  cancelReason?: string
  rating?: number
  comment?: string
  createTime: string
  updateTime?: string
  confirmTime?: string
  startTime?: string
  finishTime?: string
  cancelTime?: string
  rated: boolean
  worker?: {
    name: string
    phone: string
    avatar?: string
  }
}

// 商家订单统计
export interface MerchantOrderStats {
  total: number
  pending: number
  processing: number
  completed: number
}

/**
 * 分页查询商家服务订单
 */
export function getMerchantOrderPage(params: MerchantOrderPageQuery) {
  return request({
    url: '/api/user/merchant/orders/page',
    method: 'get',
    params
  })
}

/**
 * 获取商家订单详情
 */
export function getMerchantOrderDetail(orderId: number) {
  return request({
    url: `/api/user/merchant/orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 获取商家订单统计
 */
export function getMerchantOrderStats() {
  return request({
    url: '/api/user/merchant/orders/stats',
    method: 'get'
  })
}

/**
 * 商家确认接单
 */
export function confirmMerchantOrder(orderId: number) {
  return request({
    url: `/api/user/merchant/orders/${orderId}/confirm`,
    method: 'put'
  })
}

/**
 * 商家开始服务
 */
export function startMerchantService(orderId: number) {
  return request({
    url: `/api/user/merchant/orders/${orderId}/start`,
    method: 'put'
  })
}

/**
 * 商家完成服务
 */
export function finishMerchantService(orderId: number) {
  return request({
    url: `/api/user/merchant/orders/${orderId}/finish`,
    method: 'put'
  })
}

/**
 * 商家拒绝订单
 */
export function rejectMerchantOrder(orderId: number, reason?: string) {
  return request({
    url: `/api/user/merchant/orders/${orderId}/reject`,
    method: 'put',
    params: { reason }
  })
}

