import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/store/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 避免与API路径中的/api重复
  timeout: 15000, // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 从localStorage获取token，这样无需在每次请求前通过pinia获取
    const token = localStorage.getItem('token')
    
    // 如果有token，添加到请求头
    if (token) {
      // 确保headers对象存在
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
      console.log('请求添加token:', config.url)
    }
    
    // 设置loading状态 - 仅在需要显示loading的情况下
    if (config.showLoading !== false) {
      const authStore = useAuthStore()
      authStore.setLoading(true)
    }
    
    return config
  },
  (error) => {
    // 请求错误处理
    const authStore = useAuthStore()
    authStore.setLoading(false)
    ElMessage.error('请求发送失败，请检查网络连接')
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 关闭loading状态
    if (response.config.showLoading !== false) {
      const authStore = useAuthStore()
      authStore.setLoading(false)
    }
    
    // 处理响应数据
    const res = response.data
    
    // 处理mock数据或直接返回成功
    if (import.meta.env.DEV) {
      // 开发环境，如果没有code字段，则视为mock数据，直接返回
      if (res.code === undefined) {
        return res
      }
    }
    
    // 根据接口规范，判断响应是否成功
    if (res.code === 200 || res.code === 0) {
      return res
    } else {
      // 统一处理业务错误
      const errorMsg = res.message || '操作失败'
      
      // 处理特殊错误码
      if (res.code === 401) {
        // token失效，需要重新登录
        handleAuthError(errorMsg)
      } else {
        ElMessage.error(errorMsg)
      }
      
      return Promise.reject(new Error(errorMsg))
    }
  },
  (error) => {
    // 关闭loading状态
    const authStore = useAuthStore()
    authStore.setLoading(false)
    
    // 特殊处理请求取消的情况
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    
    // 处理HTTP错误
    if (error.response) {
      switch (error.response.status) {
        case 401:
          handleAuthError('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请联系管理员')
          break
        default:
          ElMessage.error(`请求失败: ${error.response.statusText || '未知错误'}`)
      }
    } else if (error.request) {
      ElMessage.error('服务器未响应，请稍后重试')
    } else {
      ElMessage.error(`请求错误: ${error.message}`)
    }
    
    return Promise.reject(error)
  }
)

// 处理认证错误
const handleAuthError = (message: string) => {
  ElMessage.error(message)
  const authStore = useAuthStore()
  authStore.logout()
  
  // 获取当前路径，用于登录后重定向
  const currentPath = router.currentRoute.value.fullPath
  router.push({
    path: '/login',
    query: currentPath !== '/login' ? { redirect: currentPath } : {}
  })
}

// 封装GET请求
export function get<T>(url: string, params?: any, config?: any): Promise<T> {
  return service.get(url, { params, ...config })
}

// 封装POST请求
export function post<T>(url: string, data?: any, config?: any): Promise<T> {
  return service.post(url, data, config)
}

// 封装PUT请求
export function put<T>(url: string, data?: any, config?: any): Promise<T> {
  return service.put(url, data, config)
}

// 封装DELETE请求
export function del<T>(url: string, config?: any): Promise<T> {
  return service.delete(url, config)
}

// 导出axios实例
export default service 