import router from '@/router'

/**
 * 跳转到用户个人主页
 * @param userId 用户ID
 */
export const goToUserProfile = (userId: number | string) => {
  if (userId) {
    router.push(`/user/${userId}`)
  }
}

/**
 * 处理用户头像点击事件
 * @param user 用户对象，包含id字段
 */
export const handleUserAvatarClick = (user: any) => {
  if (user && user.id) {
    goToUserProfile(user.id)
  } else if (user && user.userId) {
    goToUserProfile(user.userId)
  }
}

/**
 * 创建一个可以在组件中使用的用户头像点击处理函数
 * @param routerInstance 路由实例
 */
export const createUserAvatarClickHandler = (routerInstance: any) => {
  return (userId: number | string) => {
    if (userId) {
      routerInstance.push(`/user/${userId}`)
    }
  }
}