package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞记录DTO
 */
@Data
@Schema(description = "点赞记录DTO")
public class LikeRecordDTO {
    
    /**
     * 点赞类型：1-帖子 2-评论
     */
    @NotNull(message = "点赞类型不能为空")
    @Schema(description = "点赞类型：1-帖子 2-评论", required = true)
    private Integer type;
    
    /**
     * 目标ID（帖子ID或评论ID）
     */
    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID（帖子ID或评论ID）", required = true)
    private Long targetId;
} 