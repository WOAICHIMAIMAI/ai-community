package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 维修工绩效统计VO
 */
@Data
@Schema(description = "维修工绩效统计VO")
public class WorkerStatsVO {

    /**
     * 维修工ID
     */
    @Schema(description = "维修工ID")
    private Long workerId;

    /**
     * 维修工姓名
     */
    @Schema(description = "维修工姓名")
    private String workerName;

    /**
     * 总服务次数
     */
    @Schema(description = "总服务次数")
    private Integer totalServiceCount;

    /**
     * 平均评分
     */
    @Schema(description = "平均评分")
    private BigDecimal averageRating;

    /**
     * 本月服务次数
     */
    @Schema(description = "本月服务次数")
    private Integer monthlyServiceCount;

    /**
     * 各评分统计（1-5分及对应的数量）
     */
    @Schema(description = "各评分统计（1-5分及对应的数量）")
    private Map<Integer, Long> ratingCounts;

    /**
     * 各工单状态统计
     */
    @Schema(description = "各工单状态统计")
    private Map<Integer, Long> statusCounts;

    /**
     * 各服务类型统计
     */
    @Schema(description = "各服务类型统计")
    private Map<String, Long> serviceTypeCounts;

    /**
     * 月度服务统计（近6个月）
     */
    @Schema(description = "月度服务统计（近6个月）")
    private Map<String, Long> monthlyStats;
    
    /**
     * 维修工头像URL（用于列表展示）
     */
    @Schema(description = "维修工头像URL")
    private String avatar;
    
    /**
     * 维修工姓名（用于列表展示）
     */
    @Schema(description = "维修工姓名")
    private String name;
    
    /**
     * 评分（用于列表展示）
     */
    @Schema(description = "评分")
    private BigDecimal rating;
    
    /**
     * 完成工单数（用于列表展示）
     */
    @Schema(description = "完成工单数")
    private Integer completedCount;
    
    /**
     * 好评数（用于列表展示）
     */
    @Schema(description = "好评数")
    private Long goodReviews;
    
    /**
     * 平均完成时间（小时，用于列表展示）
     */
    @Schema(description = "平均完成时间（小时）")
    private Long avgCompletionTime;
} 