import { defineStore } from 'pinia'
import { getUserInfo, logout as logoutApi } from '@/api/auth'

// 定义用户信息类型
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: string
  permissions: string[]
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
    userInfo: null,
    isLoading: false
  }),

  // 定义getters
  getters: {
    // 判断是否有特定权限
    hasPermission: (state) => (permission: string): boolean => {
      if (!state.userInfo || !state.userInfo.permissions) return false
      return state.userInfo.permissions.includes(permission)
    },
    // 获取用户角色
    userRole: (state): string => state.userInfo?.role || '',
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
      // 可以将用户基本信息存储到localStorage，方便刷新页面时快速显示
      localStorage.setItem('userInfo', JSON.stringify({
        id: userInfo.id,
        nickname: userInfo.nickname,
        avatar: userInfo.avatar,
        role: userInfo.role
      }))
    },

    // 设置loading状态
    setLoading(isLoading: boolean) {
      this.isLoading = isLoading
    },

    // 登录操作
    login(token: string, userInfo: UserInfo) {
      this.setToken(token)
      this.setUserInfo(userInfo)
      console.log('登录成功，token和用户信息已保存')
    },

    // 退出登录
    logout() {
      // 如果有token，调用退出登录API
      if (this.token) {
        try {
          logoutApi()
        } catch (error) {
          console.error('退出登录API调用失败:', error)
        }
      }
      
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      console.log('已退出登录，清除token和用户信息')
    },

    // 检查登录状态，用于页面刷新后恢复状态
    async checkLoginStatus() {
      // 有token但无用户信息时获取用户信息
      if (this.token && !this.userInfo) {
        try {
          this.setLoading(true)
          console.log('检查登录状态，准备获取用户信息')
          
          // 尝试从localStorage快速恢复基本用户信息
          const cachedUserInfo = localStorage.getItem('userInfo')
          if (cachedUserInfo) {
            this.userInfo = JSON.parse(cachedUserInfo) as UserInfo
          }
          
          // 调用获取用户信息的API
          const { data } = await getUserInfo()
          
          if (data) {
            this.setUserInfo({
              id: data.id,
              username: data.username,
              nickname: data.nickname,
              avatar: data.avatar,
              role: data.role,
              permissions: data.permissions
            })
            console.log('获取用户信息成功')
          } else {
            throw new Error('获取用户信息失败，返回数据为空')
          }
        } catch (error) {
          console.error('获取用户信息失败，可能需要重新登录:', error)
          this.logout()
          return Promise.reject(error)
        } finally {
          this.setLoading(false)
        }
      }
      return Promise.resolve()
    }
  }
}) 