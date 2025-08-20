package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.analytics.*;

import java.util.Date;
import java.util.List;

/**
 * 数据分析服务接口
 */
public interface DataAnalyticsService {

    /**
     * 获取保修类型分布统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 保修类型分布列表
     */
    List<RepairTypeDistributionDTO> getRepairTypeDistribution(Date startDate, Date endDate);

    /**
     * 获取服务响应时长统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param serviceType 服务类型（可选）
     * @return 响应时长统计列表
     */
    List<ServiceResponseTimeDTO> getServiceResponseTime(Date startDate, Date endDate, String serviceType);

    /**
     * 获取用户活跃度统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户活跃度统计列表
     */
    List<UserActivityDTO> getUserActivityStatistics(Date startDate, Date endDate);

    /**
     * 获取服务完成量统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsType 统计类型：1-日 2-周 3-月 4-季度 5-年
     * @return 服务完成量统计列表
     */
    List<ServiceStatisticsDTO> getServiceCompletionStatistics(Date startDate, Date endDate, Integer statisticsType);

    /**
     * 获取满意度评分统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param serviceType 服务类型（可选）
     * @return 满意度评分统计列表
     */
    List<ServiceStatisticsDTO> getSatisfactionScoreStatistics(Date startDate, Date endDate, String serviceType);

    /**
     * 获取历史对比数据
     * @param startDate 当前期间开始日期
     * @param endDate 当前期间结束日期
     * @param compareStartDate 对比期间开始日期
     * @param compareEndDate 对比期间结束日期
     * @return 对比数据列表
     */
    List<ServiceStatisticsDTO> getHistoricalComparisonData(Date startDate, Date endDate, Date compareStartDate, Date compareEndDate);

    /**
     * 生成数据分析报告
     * @param queryDTO 查询条件
     * @return 分析报告数据
     */
    AnalyticsReportDTO generateAnalyticsReport(AnalyticsQueryDTO queryDTO);
}
