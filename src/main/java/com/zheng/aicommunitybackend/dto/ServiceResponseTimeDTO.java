package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务响应时间DTO
 */
@Data
public class ServiceResponseTimeDTO {

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 平均响应时间(小时)
     */
    private BigDecimal avgResponseHours;

    /**
     * 最短响应时间(小时)
     */
    private BigDecimal minResponseHours;

    /**
     * 最长响应时间(小时)
     */
    private BigDecimal maxResponseHours;
}
