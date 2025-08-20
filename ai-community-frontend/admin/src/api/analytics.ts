import request from '@/utils/request'

/**
 * 数据分析API - 管理端
 */
export const analyticsApi = {
  /**
   * 获取保修类型分布统计
   * @param startDate 开始日期 YYYY-MM-DD
   * @param endDate 结束日期 YYYY-MM-DD
   */
  getRepairTypeDistribution(startDate: string, endDate: string) {
    return request({
      url: '/api/analytics/repair-type-distribution',
      method: 'get',
      params: { startDate, endDate }
    })
  },

  /**
   * 获取服务响应时长统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param serviceType 服务类型（可选）
   */
  getServiceResponseTime(startDate: string, endDate: string, serviceType?: string) {
    const params: any = { startDate, endDate }
    if (serviceType) {
      params.serviceType = serviceType
    }
    return request({
      url: '/api/analytics/service-response-time',
      method: 'get',
      params
    })
  },

  /**
   * 获取用户活跃度统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   */
  getUserActivityStatistics(startDate: string, endDate: string) {
    return request({
      url: '/api/analytics/user-activity',
      method: 'get',
      params: { startDate, endDate }
    })
  },

  /**
   * 获取服务完成量统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param statisticsType 统计类型：1-日 2-周 3-月 4-季度 5-年
   */
  getServiceCompletionStatistics(startDate: string, endDate: string, statisticsType: number = 3) {
    return request({
      url: '/api/analytics/service-completion',
      method: 'get',
      params: { startDate, endDate, statisticsType }
    })
  },

  /**
   * 获取满意度评分统计
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param serviceType 服务类型（可选）
   */
  getSatisfactionScoreStatistics(startDate: string, endDate: string, serviceType?: string) {
    const params: any = { startDate, endDate }
    if (serviceType) {
      params.serviceType = serviceType
    }
    return request({
      url: '/api/analytics/satisfaction-score',
      method: 'get',
      params
    })
  },

  /**
   * 获取历史对比数据
   * @param startDate 当前期间开始日期
   * @param endDate 当前期间结束日期
   * @param compareStartDate 对比期间开始日期
   * @param compareEndDate 对比期间结束日期
   */
  getHistoricalComparisonData(startDate: string, endDate: string, compareStartDate: string, compareEndDate: string) {
    return request({
      url: '/api/analytics/historical-comparison',
      method: 'get',
      params: { startDate, endDate, compareStartDate, compareEndDate }
    })
  },

  /**
   * 生成数据分析报告
   * @param queryDTO 查询条件
   */
  generateAnalyticsReport(queryDTO: any) {
    return request({
      url: '/api/analytics/report',
      method: 'post',
      data: queryDTO
    })
  },

  /**
   * 获取数据分析摘要
   * @param startDate 开始日期
   * @param endDate 结束日期
   */
  getDataAnalyticsSummary(startDate: string, endDate: string) {
    return request({
      url: '/api/analytics/summary',
      method: 'get',
      params: { startDate, endDate }
    })
  },

  /**
   * 导出数据分析报告
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param format 导出格式 excel/pdf
   */
  exportAnalyticsReport(startDate: string, endDate: string, format: string = 'excel') {
    return request({
      url: '/api/analytics/export',
      method: 'get',
      params: { startDate, endDate, format },
      responseType: 'blob'
    })
  }
}

export default analyticsApi
