import { defineStore } from 'pinia'

// 定义用户信息类型
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatarUrl: string
  role: string
  isVerified: number
}

// 定义认证状态接口
interface AuthState {
  token: string
  userInfo: UserInfo | null
  isLoading: boolean
}

// 定义并导出认证状态store
export const useAuthStore = defineStore('auth', {
  // 定义状态
  state: (): AuthState => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    isLoading: false
  }),

  // 定义getters
  getters: {
    // 获取用户昵称
    nickname: (state): string => state.userInfo?.nickname || '用户',
    // 是否已登录
    isLoggedIn: (state): boolean => !!state.token && !!state.userInfo
  },

  // 定义actions
  actions: {
    // 设置token
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },

    // 设置用户信息
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
      // 将用户基本信息存储到localStorage，方便刷新页面时快速显示
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    // 设置loading状态
    setLoading(isLoading: boolean) {
      this.isLoading = isLoading
    },

    // 登录操作
    login(token: string, userInfo: UserInfo) {
      this.setToken(token)
      this.setUserInfo(userInfo)
    },

    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
}) 