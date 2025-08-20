package com.zheng.aicommunitybackend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 满意度评分统计DTO
 */
@Data
public class SatisfactionScoreDTO {

    /**
     * 服务类型
     */
    private String serviceType;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 总评分数
     */
    private Integer totalRatings;

    /**
     * 平均评分
     */
    private BigDecimal avgRating;

    /**
     * 高评分数量(4-5分)
     */
    private Integer highRatings;

    /**
     * 中等评分数量(3分)
     */
    private Integer mediumRatings;

    /**
     * 低评分数量(1-2分)
     */
    private Integer lowRatings;
}
