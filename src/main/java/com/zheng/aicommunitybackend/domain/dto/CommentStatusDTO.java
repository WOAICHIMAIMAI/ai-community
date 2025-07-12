package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论状态更新DTO
 */
@Data
@Schema(description = "评论状态更新DTO")
public class CommentStatusDTO {
    
    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    @Schema(description = "评论ID", required = true)
    private Long commentId;
    
    /**
     * 状态：0-隐藏 1-显示
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-隐藏 1-显示", required = true)
    private Integer status;
} 