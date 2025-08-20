package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.util.List;

/**
 * 数据分析报告DTO
 */
@Data
public class AnalyticsReportDTO {

    /**
     * 报告标题
     */
    private String title;

    /**
     * 统计时间范围
     */
    private String timeRange;

    /**
     * 保修类型分布
     */
    private List<RepairTypeDistributionDTO> repairTypeDistribution;

    /**
     * 服务响应时长
     */
    private List<ServiceResponseTimeDTO> serviceResponseTime;

    /**
     * 用户活跃度
     */
    private List<UserActivityDTO> userActivity;

    /**
     * 服务完成量统计
     */
    private List<ServiceStatisticsDTO> serviceCompletionStats;

    /**
     * 满意度评分统计
     */
    private List<ServiceStatisticsDTO> satisfactionScoreStats;

    /**
     * 历史对比数据
     */
    private List<ServiceStatisticsDTO> historicalComparison;

    /**
     * 总结数据
     */
    private AnalyticsSummaryDTO summary;
}
