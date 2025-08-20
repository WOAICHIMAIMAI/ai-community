import { get, put, post } from '@/utils/request'

// 用户信息接口
export interface UserInfo {
  id: number
  username: string
  phone: string
  nickname: string
  avatarUrl: string
  gender: number
  birthday: string | null
  registerTime: string
  lastLoginTime: string
  isVerified: number
  role: string
}

// 用户验证信息接口
export interface VerificationInfo {
  id: number
  realName: string
  idCardNumber: string
  verificationStatus: number
  failureReason: string | null
  submitTime: string
  completeTime: string | null
}

// 修改密码参数接口
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
}

/**
 * 获取当前登录用户信息
 * @returns 用户信息
 */
export function getUserInfo(): Promise<{ code: number, data: UserInfo, message: string }> {
  return get<{ code: number, data: UserInfo, message: string }>('/api/user/user/info')
}

/**
 * 更新用户信息
 * @param userInfo 用户信息
 * @returns 更新结果
 */
export function updateUserInfo(userInfo: Partial<UserInfo>): Promise<{ code: number, message: string }> {
  return put<{ code: number, message: string }>('/api/user/user/info', userInfo)
}

/**
 * 修改密码
 * @param params 密码参数
 * @returns 修改结果
 */
export function changePassword(params: ChangePasswordParams): Promise<{ code: number, message: string }> {
  return post<{ code: number, message: string }>('/api/user/user/change-password', null, {
    params
  })
}

/**
 * 获取用户实名认证信息
 * @returns 认证信息
 */
export function getVerificationInfo(): Promise<{ code: number, data: VerificationInfo, message: string }> {
  return get<{ code: number, data: VerificationInfo, message: string }>('/api/user/verification/info')
}

// 获取指定用户信息（通过用户ID）
export const getUserInfoById = (userId: string | number) => {
  // 确保userId是字符串，避免大数精度丢失
  const userIdStr = String(userId)
  return get(`/api/user/user/info/${userIdStr}`)
}

// 获取用户帖子列表
export const getUserPosts = (userId: string | number, params?: {
  page?: number
  pageSize?: number
  category?: string
  keyword?: string
}) => {
  const userIdStr = String(userId)
  return get(`/api/user/posts/user/${userIdStr}`, params || { page: 1, pageSize: 20 })
}
