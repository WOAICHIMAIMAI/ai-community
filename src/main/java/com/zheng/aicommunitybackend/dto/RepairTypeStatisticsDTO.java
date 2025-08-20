package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 保修类型统计DTO
 */
@Data
public class RepairTypeStatisticsDTO {

    /**
     * 保修类型
     */
    private String repairType;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 已完成数量
     */
    private Integer completedCount;

    /**
     * 平均完成时间(小时)
     */
    private BigDecimal avgCompletionHours;
}
