import { AppointmentType, AppointmentStatus } from '@/api/appointment'

/**
 * 获取服务类型名称
 * 支持中文名称和枚举值
 */
export function getServiceTypeName(type?: AppointmentType | string): string {
  // 如果已经是中文名称，直接返回
  if (typeof type === 'string' && isNaN(Number(type))) {
    return type
  }
  
  // 否则按枚举值转换
  const typeMap: Record<number, string> = {
    [AppointmentType.MAINTENANCE]: '维修服务',
    [AppointmentType.CLEANING]: '保洁服务',
    [AppointmentType.SECURITY]: '安保服务',
    [AppointmentType.DELIVERY]: '快递代收',
    [AppointmentType.OTHER]: '其他服务'
  }
  
  return typeMap[type as AppointmentType || AppointmentType.OTHER] || type as string || '未知'
}

/**
 * 获取服务类型标签颜色类型
 */
export function getServiceTypeTagType(type?: AppointmentType | string): string {
  // 如果是中文名称，先转换为对应的枚举
  let enumType: AppointmentType
  
  if (typeof type === 'string' && isNaN(Number(type))) {
    const chineseToEnum: Record<string, AppointmentType> = {
      '维修服务': AppointmentType.MAINTENANCE,
      '保洁服务': AppointmentType.CLEANING,
      '家政保洁': AppointmentType.CLEANING,
      '安保服务': AppointmentType.SECURITY,
      '快递代收': AppointmentType.DELIVERY,
      '其他服务': AppointmentType.OTHER
    }
    enumType = chineseToEnum[type] || AppointmentType.OTHER
  } else {
    enumType = type as AppointmentType || AppointmentType.OTHER
  }
  
  const typeMap: Record<number, string> = {
    [AppointmentType.MAINTENANCE]: 'danger',
    [AppointmentType.CLEANING]: 'success',
    [AppointmentType.SECURITY]: 'warning',
    [AppointmentType.DELIVERY]: 'info',
    [AppointmentType.OTHER]: ''
  }
  
  return typeMap[enumType] || ''
}

/**
 * 获取预约状态名称
 */
export function getStatusName(status: AppointmentStatus): string {
  const statusMap: Record<number, string> = {
    [AppointmentStatus.PENDING]: '待处理',
    [AppointmentStatus.CONFIRMED]: '已确认',
    [AppointmentStatus.IN_PROGRESS]: '进行中',
    [AppointmentStatus.COMPLETED]: '已完成',
    [AppointmentStatus.CANCELLED]: '已取消'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取预约状态标签类型
 */
export function getStatusTagType(status: AppointmentStatus): string {
  const statusMap: Record<number, string> = {
    [AppointmentStatus.PENDING]: 'warning',
    [AppointmentStatus.CONFIRMED]: 'primary',
    [AppointmentStatus.IN_PROGRESS]: 'info',
    [AppointmentStatus.COMPLETED]: 'success',
    [AppointmentStatus.CANCELLED]: 'danger'
  }
  return statusMap[status] || ''
}

/**
 * 格式化日期时间
 */
export function formatDateTime(dateTime?: string): string {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 截断文本
 */
export function truncateText(text: string, maxLength: number): string {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

