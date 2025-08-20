import { get, post } from '@/utils/request'

// 红包活动相关接口

// 红包活动创建DTO
export interface RedPacketActivityCreateDTO {
  activityName: string
  activityDesc?: string
  totalAmount: number // 元
  totalCount: number
  startTime: string // 格式：yyyy-MM-dd HH:mm:ss
  endTime: string   // 格式：yyyy-MM-dd HH:mm:ss
  algorithm?: string
  minAmount?: number // 分
  maxAmount?: number // 分
  startImmediately?: boolean
}

// 红包活动查询DTO
export interface RedPacketActivityQueryDTO {
  page?: number
  size?: number
  activityName?: string
  status?: number
  creatorId?: number
  startTimeBegin?: string
  startTimeEnd?: string
  createdTimeBegin?: string
  createdTimeEnd?: string
  sortField?: string
  sortOrder?: string
  onlyMine?: boolean
}

// 红包活动操作DTO
export interface RedPacketActivityOperateDTO {
  activityId: number
  operation: 'start' | 'end' | 'cancel'
}

// 红包记录查询DTO
export interface RedPacketRecordQueryDTO {
  page?: number
  size?: number
  activityId?: number
}

// 红包活动VO
export interface RedPacketActivityVO {
  id: number
  activityName: string
  activityDesc?: string
  totalAmount: number
  totalAmountYuan: number
  totalCount: number
  grabbedCount: number
  grabbedAmount: number
  grabbedAmountYuan: number
  startTime: string
  endTime: string
  status: number
  statusName: string
  creatorId: number
  creatorName?: string
  createdTime: string
  updatedTime: string
  grabRate: number
  remainingCount: number
  remainingAmount: number
  remainingAmountYuan: number
  canGrab?: boolean
  cannotGrabReason?: string
  hasGrabbed?: boolean
  userGrabbedAmount?: number
  userGrabbedAmountYuan?: number
  userGrabTime?: string
}

// 红包记录VO
export interface RedPacketRecordVO {
  id: number
  activityId: number
  activityName: string
  userId: number
  nickname: string
  avatar?: string
  packetDetailId: number
  amount: number
  amountYuan: number
  transactionNo: string
  grabTime: string
  accountUpdated: number
  accountUpdatedName: string
  createdTime: string
  packetIndex: number
  ranking?: number
  isLuckiest?: boolean
}

// 分页响应
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 统一响应格式
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 红包管理API
export const redPacketApi = {
  // 创建红包活动
  createActivity: (data: RedPacketActivityCreateDTO): Promise<ApiResponse<number>> => {
    return post('/api/admin/red-packet/activities', data)
  },

  // 分页查询红包活动列表
  getActivityPage: (data: RedPacketActivityQueryDTO): Promise<ApiResponse<PageResponse<RedPacketActivityVO>>> => {
    return post('/api/admin/red-packet/activities/list', data)
  },

  // 获取红包活动详情
  getActivityDetail: (id: number): Promise<ApiResponse<RedPacketActivityVO>> => {
    return get(`/api/admin/red-packet/activities/${id}`)
  },

  // 活动操作（开始、结束、取消）
  operateActivity: (data: RedPacketActivityOperateDTO): Promise<ApiResponse<boolean>> => {
    return post('/api/admin/red-packet/activities/operate', data)
  },

  // 获取活动的抢红包记录
  getActivityRecords: (data: RedPacketRecordQueryDTO): Promise<ApiResponse<PageResponse<RedPacketRecordVO>>> => {
    return post('/api/admin/red-packet/activities/records', data)
  },

  // 获取活动统计信息
  getActivityStats: (id: number): Promise<ApiResponse<Record<string, any>>> => {
    return get(`/api/admin/red-packet/activities/${id}/stats`)
  },

  // 预加载活动到Redis
  preloadActivity: (id: number): Promise<ApiResponse<boolean>> => {
    return post(`/api/admin/red-packet/activities/${id}/preload`)
  },

  // 清理活动Redis数据
  clearActivityCache: (id: number): Promise<ApiResponse<boolean>> => {
    return post(`/api/admin/red-packet/activities/${id}/clear-cache`)
  },

  // 处理未更新账户的记录
  processUnprocessedRecords: (limit: number = 100): Promise<ApiResponse<number>> => {
    return post('/api/admin/red-packet/process-unprocessed', null, { params: { limit } })
  }
}

export default redPacketApi
