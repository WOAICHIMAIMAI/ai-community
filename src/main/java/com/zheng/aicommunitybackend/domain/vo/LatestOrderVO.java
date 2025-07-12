package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 最新工单VO
 */
@Data
@Schema(description = "最新工单数据")
public class LatestOrderVO {
    
    /**
     * 工单ID
     */
    @Schema(description = "工单ID")
    private Long id;
    
    /**
     * 工单标题
     */
    @Schema(description = "工单标题")
    private String title;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;
    
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createdAt;
} 