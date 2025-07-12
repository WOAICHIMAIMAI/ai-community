package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 仪表盘概览数据VO
 */
@Data
@Schema(description = "仪表盘概览数据")
public class DashboardVO {
    
    /**
     * 用户总数
     */
    @Schema(description = "用户总数")
    private Long userCount;
    
    /**
     * 用户增长率
     */
    @Schema(description = "用户增长率")
    private Double userIncrease;
    
    /**
     * 帖子总数
     */
    @Schema(description = "帖子总数")
    private Long postCount;
    
    /**
     * 帖子增长率
     */
    @Schema(description = "帖子增长率")
    private Double postIncrease;
    
    /**
     * 待处理工单数
     */
    @Schema(description = "待处理工单数")
    private Long pendingOrderCount;
    
    /**
     * 工单增长率
     */
    @Schema(description = "工单增长率")
    private Double orderIncrease;
    
    /**
     * 待审核内容数
     */
    @Schema(description = "待审核内容数")
    private Long reviewCount;
    
    /**
     * 审核内容增长率
     */
    @Schema(description = "审核内容增长率")
    private Double reviewIncrease;
} 