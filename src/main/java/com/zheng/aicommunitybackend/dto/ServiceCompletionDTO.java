package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务完成量统计DTO
 */
@Data
public class ServiceCompletionDTO {

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 已完成订单数
     */
    private Integer completedOrders;

    /**
     * 完成率
     */
    private BigDecimal completionRate;
}
