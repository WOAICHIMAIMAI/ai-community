import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, ApiResult } from '@/types/common'

// 用户数据接口
export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatarUrl: string
  gender: number
  status: number
  createdTime: string
  lastLoginTime: string
}

// 用户状态参数
export interface UserStatusParams {
  userId: number
  status: number
}

// 认证申请数据接口
export interface VerificationInfo {
  id: number
  userId: number
  username: string
  realName: string
  idCardNumber: string
  idCardFront: string
  idCardBack: string
  verificationType: number
  status: number
  applyTime: string
  auditTime: string
  auditReason: string
}

// 认证审核参数
export interface VerificationAuditParams {
  verificationId: number
  status: number
  reason?: string
}

/**
 * 获取用户分页列表
 * @param params 分页参数
 * @returns 用户分页数据
 */
export function getUserList(params: PageParams): Promise<PageResult<UserInfo>> {
  return get<PageResult<UserInfo>>('/api/admin/user/list', params)
}

/**
 * 更新用户状态
 * @param userId 用户ID
 * @param status 状态
 * @returns 操作结果
 */
export function updateUserStatus(userId: number, status: number): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>(`/api/admin/user/${userId}/status?status=${status}`)
}

/**
 * 获取认证申请分页列表
 * @param params 分页参数
 * @returns 认证申请分页数据
 */
export function getVerificationList(params: PageParams): Promise<PageResult<VerificationInfo>> {
  return post<PageResult<VerificationInfo>>('/api/admin/verification/list', params)
}

/**
 * 审核认证申请
 * @param params 认证审核参数
 * @returns 操作结果
 */
export function auditVerification(params: VerificationAuditParams): Promise<ApiResult<boolean>> {
  return post<ApiResult<boolean>>('/api/admin/verification/admin/audit', params)
} 