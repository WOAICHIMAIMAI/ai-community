// 分页参数接口
export interface PageParams {
  page: number
  pageSize: number
  [key: string]: any
}

// 分页响应数据接口
export interface PageResult<T> {
  code: number
  message: string
  data: {
    total: number
    records: T[]
  }
}

// 通用响应数据接口
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

// 通用列表响应数据接口
export interface ListResult<T> {
  code: number
  message: string
  data: T[]
}

// 通用状态变更参数
export interface StatusChangeParams {
  id: number | string
  status: number
}

// 通用操作结果
export interface OperationResult {
  code: number
  message: string
  data: boolean
} 