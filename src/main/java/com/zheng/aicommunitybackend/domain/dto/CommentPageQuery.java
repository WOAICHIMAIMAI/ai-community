package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论分页查询参数（仅用于查询帖子的一级评论）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论分页查询参数")
public class CommentPageQuery extends PageQuery {

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", required = true)
    private Long postId;
} 