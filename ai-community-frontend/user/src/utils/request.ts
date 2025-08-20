import axios from 'axios'
import { showToast, showFailToast } from 'vant'
import router from '@/router'
import { useAuthStore } from '@/store/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '', // 移除API前缀，避免与后端context-path重复
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
    showFailToast('请求发送失败，请检查网络连接')
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
    
    // 处理大整数精度丢失问题
    const processLargeIntegers = (obj: any): any => {
      if (typeof obj === 'string') {
        // 检查是否是大整数字符串
        if (/^\d{16,}$/.test(obj)) {
          return obj // 保持字符串格式
        }
      } else if (typeof obj === 'number') {
        // 如果是超出安全范围的数字，转换为字符串
        if (obj > Number.MAX_SAFE_INTEGER || obj < Number.MIN_SAFE_INTEGER) {
          return obj.toString()
        }
      } else if (Array.isArray(obj)) {
        return obj.map(processLargeIntegers)
      } else if (obj && typeof obj === 'object') {
        const processed: any = {}
        for (const key in obj) {
          // 特别处理可能是大整数的字段
          if (['userId', 'id', 'authorId', 'createBy', 'targetId'].includes(key)) {
            processed[key] = obj[key]?.toString() || obj[key]
          } else {
            processed[key] = processLargeIntegers(obj[key])
          }
        }
        return processed
      }
      return obj
    }
    
    // 处理响应数据
    let res = response.data
    
    // 处理大整数
    res = processLargeIntegers(res)
    
    // 处理mock数据或直接返回成功
    if (import.meta.env.DEV) {
      if (res.code === undefined) {
        return res
      }
    }
    
    // 根据接口规范，判断响应是否成功
    if (res.code === 200) {
      return res
    } else {
      const errorMsg = res.msg || res.message || '操作失败'
      
      if (res.code === 401) {
        handleAuthError(errorMsg)
      } else {
        showFailToast({
          message: errorMsg,
          duration: 3000,
          forbidClick: true
        })
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
          showFailToast({
            message: '没有权限访问该资源',
            duration: 3000,
            forbidClick: true
          })
          break
        case 404:
          showFailToast({
            message: '请求的资源不存在',
            duration: 3000,
            forbidClick: true
          })
          break
        case 500:
          showFailToast({
            message: '服务器错误，请联系管理员',
            duration: 3000,
            forbidClick: true
          })
          break
        default:
          showFailToast({
            message: `请求失败: ${error.response.statusText || '未知错误'}`,
            duration: 3000,
            forbidClick: true
          })
      }
    } else if (error.request) {
      showFailToast({
        message: '服务器未响应，请稍后重试',
        duration: 3000,
        forbidClick: true
      })
    } else {
      showFailToast({
        message: `请求错误: ${error.message}`,
        duration: 3000,
        forbidClick: true
      })
    }
    
    return Promise.reject(error)
  }
)

// 处理认证错误
const handleAuthError = (message: string) => {
  showFailToast({
    message: message,
    duration: 3000,
    forbidClick: true
  })
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