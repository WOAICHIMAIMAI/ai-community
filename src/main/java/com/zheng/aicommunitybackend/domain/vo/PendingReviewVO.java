package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待审核内容VO
 */
@Data
@Schema(description = "待审核内容数据")
public class PendingReviewVO {
    
    /**
     * 内容ID
     */
    @Schema(description = "内容ID")
    private Long id;
    
    /**
     * 内容标题
     */
    @Schema(description = "内容标题")
    private String title;
    
    /**
     * 内容类型
     */
    @Schema(description = "内容类型")
    private String type;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createdAt;
} 