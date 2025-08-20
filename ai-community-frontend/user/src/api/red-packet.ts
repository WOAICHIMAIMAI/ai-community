import { get, post } from '@/utils/request'

// 红包活动相关接口

// 红包抢红包DTO
export interface RedPacketGrabDTO {
  activityId: number
}

// 红包记录查询DTO
export interface RedPacketRecordQueryDTO {
  page?: number
  size?: number
  activityId?: number
}

// 红包活动列表VO
export interface RedPacketActivityListVO {
  id: number
  activityName: string
  activityDesc?: string
  totalAmountYuan: number
  totalCount: number
  grabbedCount: number
  startTime: string
  endTime: string
  status: number
  statusName: string
  grabRate: number
  remainingCount: number
  canGrab: boolean
  hasGrabbed: boolean
}

// 红包活动详情VO
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
  canGrab: boolean
  cannotGrabReason?: string
  hasGrabbed: boolean
  userGrabbedAmount?: number
  userGrabbedAmountYuan?: number
  userGrabTime?: string
}

// 红包抢红包结果VO
export interface RedPacketGrabVO {
  success: boolean
  message: string
  amount?: number
  amountYuan?: number
  transactionNo?: string
  grabTime?: string
  packetIndex?: number
  ranking?: number
  isLuckiest?: boolean
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

// 用户端红包API
export const redPacketApi = {
  // 获取进行中的红包活动列表
  getActiveActivities: (): Promise<ApiResponse<RedPacketActivityListVO[]>> => {
    return get('/api/user/red-packet/activities')
  },

  // 获取红包活动详情
  getActivityDetail: (id: number): Promise<ApiResponse<RedPacketActivityVO>> => {
    return get(`/api/user/red-packet/activities/${id}`)
  },

  // 抢红包
  grabRedPacket: (data: RedPacketGrabDTO): Promise<ApiResponse<RedPacketGrabVO>> => {
    return post('/api/user/red-packet/grab', data)
  },

  // 获取用户抢红包记录
  getUserRecords: (data: RedPacketRecordQueryDTO): Promise<ApiResponse<PageResponse<RedPacketRecordVO>>> => {
    return post('/api/user/red-packet/records', data)
  },

  // 获取用户抢红包统计
  getUserStats: (): Promise<ApiResponse<Record<string, any>>> => {
    return get('/api/user/red-packet/stats')
  },

  // 检查用户是否可以抢红包
  checkCanGrab: (activityId: number): Promise<ApiResponse<Record<string, any>>> => {
    return get(`/api/user/red-packet/check/${activityId}`)
  }
}

export default redPacketApi
