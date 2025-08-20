package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务统计DTO
 */
@Data
public class ServiceStatisticsDTO {

    /**
     * 统计期间
     */
    private String period;

    /**
     * 日期
     */
    private Date date;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 已完成数量
     */
    private Integer completedCount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 总营收
     */
    private BigDecimal totalRevenue;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgRating;

    /**
     * 同比增长率
     */
    private BigDecimal growthRate;

    /**
     * 环比增长率
     */
    private BigDecimal monthOverMonthGrowth;
}
