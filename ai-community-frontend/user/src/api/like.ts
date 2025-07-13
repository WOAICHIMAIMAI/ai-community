import request from '@/utils/request'

/**
 * 点赞或取消点赞
 * @param data 点赞数据
 */
export const likeOrUnlike = (data: {
  type: number // 1-帖子 2-评论
  targetId: number
}) => {
  return request({
    url: '/user/likes',
    method: 'post',
    data
  })
}

/**
 * 获取点赞状态
 * @param type 点赞类型：1-帖子 2-评论
 * @param targetId 目标ID
 */
export const getLikeStatus = (type: number, targetId: number) => {
  return request({
    url: '/user/likes/status',
    method: 'get',
    params: { type, targetId }
  })
}

/**
 * 批量获取点赞状态
 * @param type 点赞类型：1-帖子 2-评论
 * @param targetIds 目标ID数组
 */
export const batchGetLikeStatus = (type: number, targetIds: number[]) => {
  return request({
    url: '/user/likes/batch-status',
    method: 'post',
    params: { type },
    data: targetIds
  })
}

/**
 * 获取用户点赞列表
 * @param type 点赞类型：1-帖子 2-评论
 */
export const getUserLikedList = (type: number) => {
  return request({
    url: '/user/likes/list',
    method: 'get',
    params: { type }
  })
} 