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
} 