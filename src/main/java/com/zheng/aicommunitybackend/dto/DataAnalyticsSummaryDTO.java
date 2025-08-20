package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据分析摘要DTO
 */
@Data
public class DataAnalyticsSummaryDTO {

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 已完成订单数
     */
    private Integer completedOrders;

    /**
     * 活跃用户数
     */
    private Integer activeUsers;

    /**
     * 平均满意度
     */
    private BigDecimal avgSatisfaction;
}
