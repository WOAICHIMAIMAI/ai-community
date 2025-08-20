import { get, post } from '@/utils/request'

// 登录请求参数接口
export interface LoginParams {
  username: string
  password: string
}

// 登录响应数据接口
export interface LoginResult {
  code: number
  msg: string
  data: {
    username: string
    nickName: string
    role: number
    token: string
    avatarUrl: string
  }
}

// 注册请求参数接口
export interface RegisterParams {
  username: string
  password: string
  confirmPassword: string
  phone: string
  nickname?: string
}

// 注册响应数据接口
export interface RegisterResult {
  code: number
  msg: string
  data: null
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