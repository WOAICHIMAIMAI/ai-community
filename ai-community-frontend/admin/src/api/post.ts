import { get, post, put, del } from '@/utils/request'
import type { PageParams, PageResult, ApiResult } from '@/types/common'

// 帖子数据接口
export interface PostInfo {
  id: number
  userId: number
  username: string
  nickname: string
  userAvatar: string
  title: string
  content: string
  category: number
  categoryName: string
  images: string[]
  viewCount: number
  likeCount: number
  commentCount: number
  status: number
  createdTime: string
  updatedTime: string
}

// 帖子状态变更参数
export interface PostStatusParams {
  postId: number
  status: number
  reason?: string
}

// 评论数据接口
export interface CommentInfo {
  id: number
  postId: number
  userId: number
  username: string
  nickname: string
  userAvatar: string
  content: string
  parentId: number | null
  likeCount: number
  status: number
  createdTime: string
}

// 评论状态变更参数
export interface CommentStatusParams {
  commentId: number
  status: number
  reason?: string
}

/**
 * 获取帖子分页列表
 * @param params 分页参数
 * @returns 帖子分页数据
 */
export function getPostList(params: PageParams): Promise<PageResult<PostInfo>> {
  return get<PageResult<PostInfo>>('/api/admin/posts/list', params)
}

/**
 * 获取帖子详情
 * @param postId 帖子ID
 * @returns 帖子详情
 */
export function getPostDetail(postId: number): Promise<ApiResult<PostInfo>> {
  return get<ApiResult<PostInfo>>(`/api/admin/posts/${postId}`)
}

/**
 * 更新帖子状态
 * @param params 帖子状态参数
 * @returns 操作结果
 */
export function updatePostStatus(params: PostStatusParams): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/posts/status', params)
}

/**
 * 删除帖子
 * @param postId 帖子ID
 * @returns 操作结果
 */
export function deletePost(postId: number): Promise<ApiResult<boolean>> {
  return del<ApiResult<boolean>>(`/api/admin/posts/${postId}`)
}

/**
 * 获取评论分页列表
 * @param params 分页参数
 * @returns 评论分页数据
 */
export function getCommentList(params: PageParams): Promise<PageResult<CommentInfo>> {
  // 检查响应
  console.log('请求评论列表API:', '/api/admin/comments/page')
  return get<PageResult<CommentInfo>>('/api/admin/comments/page', params)
}

/**
 * 更新评论状态
 * @param params 评论状态参数
 * @returns 操作结果
 */
export function updateCommentStatus(params: CommentStatusParams): Promise<ApiResult<boolean>> {
  return put<ApiResult<boolean>>('/api/admin/comments/status', params)
}

/**
 * 删除评论
 * @param commentId 评论ID
 * @returns 操作结果
 */
export function deleteComment(commentId: number): Promise<ApiResult<boolean>> {
  return del<ApiResult<boolean>>(`/api/admin/comments/${commentId}`)
} 