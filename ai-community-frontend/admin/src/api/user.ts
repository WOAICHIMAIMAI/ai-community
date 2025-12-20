import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, ApiResult } from '@/types/common'

// 用户数据接口
export interface UserInfo {
  id: string | number  // 使用 string 避免大数精度丢失
  username: string
  nickname: string
  phone: string
  email: string
  avatarUrl: string
  gender: number
  status: number
  createdTime: string
  lastLoginTime: string
  registerTime?: string | number  // 添加注册时间字段
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
  idCardFront: string  // 前端使用驼峰命名
  idCardFrontUrl: string  // 对应后端
  idCardBack: string  // 前端使用驼峰命名
  idCardBackUrl: string  // 对应后端
  verificationType: number
  verificationStatus: number  // 改为 verificationStatus，与后端一致：0-未认证 1-认证中 2-已认证 3-认证失败
  status: number  // 保留兼容
  applyTime: string
  submitTime: string  // 对应后端
  auditTime: string
  completeTime: string  // 对应后端
  auditReason: string
  failureReason: string  // 对应后端
}

// 认证审核参数
export interface VerificationAuditParams {
  verificationId: number
  approved: boolean
  rejectReason?: string
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
 * @param userId 用户ID（字符串或数字）
 * @param status 状态（0-禁用 1-正常 2-未激活）
 * @returns 操作结果
 */
export function updateUserStatus(userId: string | number, status: number): Promise<ApiResult<void>> {
  // 确保 userId 以字符串形式传递，避免大数精度丢失
  return put<ApiResult<void>>('/api/admin/user/status', null, { 
    params: { 
      userId: String(userId), 
      status 
    } 
  })
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
 * 获取认证详情
 * @param id 认证记录ID
 * @returns 认证详情数据
 */
export function getVerificationDetail(id: number): Promise<ApiResult<VerificationInfo>> {
  return get<ApiResult<VerificationInfo>>(`/api/admin/verification/detail/${id}`)
}

/**
 * 审核认证申请
 * @param params 认证审核参数
 * @returns 操作结果
 */
export function auditVerification(params: VerificationAuditParams): Promise<ApiResult<boolean>> {
  return post<ApiResult<boolean>>('/api/admin/verification/admin/audit', params)
} 