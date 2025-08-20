import request from '@/utils/request'

/**
 * 创建报修工单
 * @param data 报修工单创建数据
 */
export const createRepairOrder = (data: {
  addressId: number
  repairType: string
  title: string
  description: string
  images?: string
  contactPhone: string
  expectedTime?: string
}) => {
  return request({
    url: '/api/user/repair-orders',
    method: 'post',
    data
  })
}

/**
 * 取消报修工单
 * @param orderId 工单ID
 */
export const cancelRepairOrder = (orderId: number) => {
  return request({
    url: `/user/repair-orders/cancel/${orderId}`,
    method: 'put'
  })
}

/**
 * 分页查询用户报修工单
 * @param params 查询参数
 */
export const pageRepairOrders = (params: {
  page: number
  pageSize: number
  repairType?: string
  status?: number
  startTime?: string
  endTime?: string
  keyword?: string
}) => {
  return request({
    url: '/api/user/repair-orders/page',
    method: 'get',
    params
  })
}

/**
 * 获取报修工单详情
 * @param orderId 工单ID
 */
export const getRepairOrderDetail = (orderId: number) => {
  return request({
    url: `/user/repair-orders/${orderId}`,
    method: 'get'
  })
}

/**
 * 提交维修评价
 * @param data 评价数据
 */
export const submitFeedback = (data: {
  orderId: number
  satisfactionLevel: number
  feedback?: string
}) => {
  return request({
    url: '/api/user/repair-orders/feedback',
    method: 'post',
    data
  })
}

/**
 * 获取维修进度列表
 * @param orderId 工单ID
 */
export const getRepairProgressList = (orderId: number) => {
  return request({
    url: `/user/repair-progress/list/${orderId}`,
    method: 'get'
  })
}

/**
 * 添加维修进度
 * @param data 进度数据
 */
export const addRepairProgress = (data: {
  orderId: number
  action: string
  description: string
}) => {
  return request({
    url: '/api/user/repair-progress',
    method: 'post',
    data
  })
}

/**
 * 获取可用维修工列表
 * @param serviceType 服务类型
 */
export const getAvailableWorkers = (serviceType?: string) => {
  return request({
    url: '/api/user/workers/available',
    method: 'get',
    params: serviceType ? { serviceType } : {}
  })
}

/**
 * 获取维修工详情
 * @param workerId 维修工ID
 */
export const getWorkerDetail = (workerId: number) => {
  return request({
    url: `/user/workers/${workerId}`,
    method: 'get'
  })
}

/**
 * 获取维修工历史工单
 * @param workerId 维修工ID
 */
export const getWorkerCompletedOrders = (workerId: number) => {
  return request({
    url: `/user/workers/${workerId}/orders`,
    method: 'get'
  })
}

/**
 * 获取报修类型列表
 */
export const getRepairTypes = () => {
  return [
    '水电维修',
    '门窗维修',
    '家电维修',
    '家具维修',
    '管道疏通',
    '墙面修补',
    '地板维修',
    '其他'
  ]
}

/**
 * 获取工单状态描述
 * @param status 状态码
 */
export const getStatusDesc = (status: number) => {
  switch (status) {
    case 0: return '待受理'
    case 1: return '已分配'
    case 2: return '处理中'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return '未知状态'
  }
}

/**
 * 获取工单状态列表
 */
export const getStatusList = () => {
  return [
    { value: null, text: '全部' },
    { value: 0, text: '待受理' },
    { value: 1, text: '已分配' },
    { value: 2, text: '处理中' },
    { value: 3, text: '已完成' },
    { value: 4, text: '已取消' }
  ]
} 