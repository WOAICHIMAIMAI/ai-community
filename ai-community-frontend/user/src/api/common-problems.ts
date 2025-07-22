import request from '@/utils/request'

// 问题分类接口
export interface ProblemCategory {
  type: number
  categoryName: string
  problemCount: number
}

// 问题查询参数
export interface ProblemQueryParams {
  type?: number
  onlyPriority?: boolean
  keyword?: string
  page?: number
  pageSize?: number
}

// 问题详情接口
export interface ProblemDetail {
  id: number
  problem: string
  answer: string
  type: number
  categoryName: string
  priority: number
  isTop: boolean
}

// 分页响应接口
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 获取问题分类列表
 */
export const getProblemCategories = () => {
  return request.get<ProblemCategory[]>('/user/common-problems/categories')
}

/**
 * 根据条件查询问题列表
 */
export const getProblemsPage = (params: ProblemQueryParams) => {
  return request.get<PageResponse<ProblemDetail>>('/user/common-problems/list', { params })
}

/**
 * 根据问题ID查询问题详情
 */
export const getProblemDetail = (id: number) => {
  return request.get<ProblemDetail>(`/user/common-problems/${id}`)
}
