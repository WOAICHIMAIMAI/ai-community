import request from '@/utils/request'

/**
 * 分页获取帖子评论
 * @param params 查询参数
 */
export const getPostComments = (params: {
  postId: number
  page: number
  pageSize: number
}) => {
  return request({
    url: '/api/user/comments/page',
    method: 'get',
    params
  })
}

/**
 * 获取评论回复
 * @param commentId 评论ID
 */
export const getCommentReplies = (commentId: number) => {
  return request({
    url: `/api/user/comments/replies/${commentId}`,
    method: 'get'
  })
}

/**
 * 创建评论
 * @param data 评论数据
 */
export const createComment = (data: {
  postId: number
  content: string
  parentId?: number
  replyToId?: number
}) => {
  return request({
    url: '/api/user/comments',
    method: 'post',
    data
  })
}

/**
 * 删除评论
 * @param commentId 评论ID
 */
export const deleteComment = (commentId: number) => {
  return request({
    url: `/api/user/comments/${commentId}`,
    method: 'delete'
  })
}

/**
 * 获取帖子评论数量
 * @param postId 帖子ID
 */
export const getCommentCount = (postId: number) => {
  return request({
    url: `/api/user/comments/count/${postId}`,
    method: 'get'
  })
} 