package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 报修工单统计VO
 */
@Data
@Schema(description = "报修工单统计VO")
public class RepairStatsVO {
    
    /**
     * 总工单数
     */
    @Schema(description = "总工单数")
    private Long totalCount;
    
    /**
     * 待受理工单数
     */
    @Schema(description = "待受理工单数")
    private Long pendingCount;
    
    /**
     * 已分配工单数
     */
    @Schema(description = "已分配工单数")
    private Long assignedCount;
    
    /**
     * 处理中工单数
     */
    @Schema(description = "处理中工单数")
    private Long processingCount;
    
    /**
     * 已完成工单数
     */
    @Schema(description = "已完成工单数")
    private Long completedCount;
    
    /**
     * 已取消工单数
     */
    @Schema(description = "已取消工单数")
    private Long cancelledCount;
    
    /**
     * 今日工单数
     */
    @Schema(description = "今日工单数")
    private Long todayCount;
    
    /**
     * 今日完成数
     */
    @Schema(description = "今日完成数")
    private Long todayCompletedCount;
    
    /**
     * 本周工单数
     */
    @Schema(description = "本周工单数")
    private Long weekCount;
    
    /**
     * 本月工单数
     */
    @Schema(description = "本月工单数")
    private Long monthCount;
    
    /**
     * 报修类型分布
     */
    @Schema(description = "报修类型分布")
    private Map<String, Long> typeCounts;
    
    /**
     * 满意度分布
     */
    @Schema(description = "满意度分布")
    private Map<Integer, Long> satisfactionCounts;
    
    /**
     * 平均满意度评分
     */
    @Schema(description = "平均满意度评分")
    private Double avgSatisfaction;
} 