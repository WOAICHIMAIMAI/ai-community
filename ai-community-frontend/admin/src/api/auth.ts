import { get, post } from '@/utils/request'

// 登录请求参数接口
export interface LoginParams {
  username: string
  password: string
}

// 登录响应数据接口
export interface LoginResult {
  code: number
  message: string
  data: {
    token: string
    userId: number
    username: string
    nickname: string
    avatarUrl: string
    role: string
    permissions: string[]
  }
}

// 注册请求参数接口
export interface RegisterParams {
  username: string
  password: string
  phone: string
  nickname: string
}

// 注册响应数据接口
export interface RegisterResult {
  code: number
  message: string
  data: {
    userId: number
  }
}

// 用户信息响应数据接口
export interface UserInfoResult {
  code: number
  message: string
  data: {
    id: number
    username: string
    nickname: string
    avatarUrl: string
    gender: number
    email: string
    phone: string
    role: string
    permissions: string[]
    createdTime: string
    lastLoginTime: string
  }
}

/**
 * 用户登录
 * @param params 登录参数
 * @returns 登录结果
 */
export function login(params: LoginParams): Promise<LoginResult> {
  return post<LoginResult>('/api/auth/login', params)
}

/**
 * 用户注册
 * @param params 注册参数
 * @returns 注册结果
 */
export function register(params: RegisterParams): Promise<RegisterResult> {
  return post<RegisterResult>('/api/auth/register', params)
}

/**
 * 获取当前登录用户信息
 * @returns 用户信息
 */
export function getUserInfo(): Promise<UserInfoResult> {
  return get<UserInfoResult>('/api/user/info')
}

/**
 * 退出登录
 * @returns 退出结果
 */
export function logout(): Promise<any> {
  return post<any>('/api/auth/logout')
} 