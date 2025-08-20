package com.zheng.aicommunitybackend.dto;

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
     * 数量
     */
    private Integer count;

    /**
     * 百分比
     */
    private BigDecimal percentage;
}
