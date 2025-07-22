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

// 问题表单接口
export interface ProblemForm {
  id?: number
  problem: string
  answer: string
  type: number
  priority: number
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
  return request.get<ProblemCategory[]>('/admin/common-problems/categories')
}

/**
 * 根据条件查询问题列表
 */
export const getProblemsPage = (params: ProblemQueryParams) => {
  return request.get<PageResponse<ProblemDetail>>('/admin/common-problems/list', { params })
}

/**
 * 根据问题ID查询问题详情
 */
export const getProblemDetail = (id: number) => {
  return request.get<ProblemDetail>(`/admin/common-problems/${id}`)
}

/**
 * 新增问题
 */
export const createProblem = (data: ProblemForm) => {
  return request.post<boolean>('/admin/common-problems', data)
}

/**
 * 更新问题
 */
export const updateProblem = (id: number, data: ProblemForm) => {
  return request.put<boolean>(`/admin/common-problems/${id}`, data)
}

/**
 * 删除问题
 */
export const deleteProblem = (id: number) => {
  return request.delete<boolean>(`/admin/common-problems/${id}`)
}

/**
 * 批量删除问题
 */
export const batchDeleteProblems = (ids: number[]) => {
  return request.delete<boolean>('/admin/common-problems/batch', { data: { ids } })
}

/**
 * 设置问题置顶状态
 */
export const setProblemPriority = (id: number, priority: number) => {
  return request.put<boolean>(`/admin/common-problems/${id}/priority`, { priority })
}
