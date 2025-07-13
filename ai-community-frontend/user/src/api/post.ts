import request from '@/utils/request'

/**
 * 分页获取帖子列表
 * @param params 查询参数
 */
export const listPosts = (params: {
  page: number
  pageSize: number
  category?: string
  keyword?: string
  sortBy?: string
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params
  })
}

/**
 * 获取所有帖子分类
 */
export const getCategories = () => {
  return ['全部', '公告', '动态', '讨论', '求助', '分享', '闲置']
}

/**
 * 获取社区公告列表
 * @param params 查询参数
 */
export const getAnnouncements = (params: {
  page?: number
  pageSize?: number
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params: {
      ...params,
      category: '公告',
      sortBy: 'createTime'
    }
  })
}

/**
 * 获取社区动态列表
 * @param params 查询参数
 */
export const getCommunityUpdates = (params: {
  page?: number
  pageSize?: number
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params: {
      ...params,
      category: '动态',
      sortBy: 'createTime'
    }
  })
}

/**
 * 获取社区所有帖子（不限类别）
 * @param params 查询参数
 */
export const getAllPosts = (params: {
  page?: number
  pageSize?: number
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params: {
      ...params,
      sortBy: 'createTime'
    }
  })
}

/**
 * 获取热门帖子列表
 * @param params 查询参数
 */
export const getHotPosts = (params: {
  page?: number
  pageSize?: number
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params: {
      ...params,
      sortBy: 'likeCount'
    }
  })
}

/**
 * 获取精华帖子列表
 * @param params 查询参数
 */
export const getEssencePosts = (params: {
  page?: number
  pageSize?: number
}) => {
  return request({
    url: '/user/posts/list',
    method: 'get',
    params: {
      ...params,
      isEssence: 1
    }
  })
}

/**
 * 获取帖子详情
 * @param postId 帖子ID
 */
export const getPostDetail = (postId: number) => {
  return request({
    url: `/user/posts/${postId}`,
    method: 'get'
  })
}

/**
 * 创建帖子
 * @param data 帖子数据
 */
export const createPost = (data: {
  title: string
  content: string
  category: string
  images?: string[]
}) => {
  return request({
    url: '/user/posts',
    method: 'post',
    data
  })
}

/**
 * 获取帖子评论
 * @param params 查询参数
 */
export const getComments = (params: {
  postId: number
  page: number
  pageSize: number
}) => {
  return request({
    url: '/user/comments/page',
    method: 'get',
    params
  })
}

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
 * 获取当前用户的帖子列表
 * @param params 查询参数
 */
export const getMyPosts = (params: {
  page: number
  pageSize: number
  category?: string
}) => {
  return request({
    url: '/user/posts/my',
    method: 'get',
    params
  })
} 