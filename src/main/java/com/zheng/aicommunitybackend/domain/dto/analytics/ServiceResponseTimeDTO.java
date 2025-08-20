package com.zheng.aicommunitybackend.domain.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务响应时长DTO
 */
@Data
public class ServiceResponseTimeDTO {

    /**
     * 日期
     */
    private Date date;

    /**
     * 日期字符串
     */
    private String dateStr;

    /**
     * 平均响应时长(分钟)
     */
    private BigDecimal avgResponseTime;

    /**
     * 最短响应时长(分钟)
     */
    private BigDecimal minResponseTime;

    /**
     * 最长响应时长(分钟)
     */
    private BigDecimal maxResponseTime;

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 工单数量
     */
    private Integer orderCount;
}
