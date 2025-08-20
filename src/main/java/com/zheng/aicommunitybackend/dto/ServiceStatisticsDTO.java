package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务统计DTO
 */
@Data
public class ServiceStatisticsDTO {

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 已完成数量
     */
    private Integer completedCount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;
}
