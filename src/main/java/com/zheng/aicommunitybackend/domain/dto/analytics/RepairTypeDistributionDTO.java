package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 保修类型分布DTO
 */
@Data
public class RepairTypeDistributionDTO {

    /**
     * 保修类型
     */
    private String repairType;

    /**
     * 保修类型名称
     */
    private String repairTypeName;

    /**
     * 工单数量
     */
    private Integer orderCount;

    /**
     * 占比百分数
     */
    private BigDecimal percentage;

    /**
     * 平均处理时长(小时)
     */
    private BigDecimal avgProcessTime;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgSatisfactionScore;
}
