package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据分析总结DTO
 */
@Data
public class AnalyticsSummaryDTO {

    /**
     * 总工单数
     */
    private Integer totalOrders;

    /**
     * 已完成工单数
     */
    private Integer completedOrders;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 用户活跃率
     */
    private BigDecimal userActiveRate;

    /**
     * 平均响应时长(分钟)
     */
    private BigDecimal avgResponseTime;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgSatisfactionScore;

    /**
     * 总营收
     */
    private BigDecimal totalRevenue;

    /**
     * 同比增长率
     */
    private BigDecimal growthRate;
}
