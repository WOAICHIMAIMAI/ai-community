package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端评论分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理端评论分页查询参数")
public class AdminCommentPageQuery extends PageQuery {

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    private Long postId;
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;
    
    /**
     * 评论内容关键词
     */
    @Schema(description = "评论内容关键词")
    private String keyword;
    
    /**
     * 评论状态：0-隐藏 1-显示
     */
    @Schema(description = "评论状态：0-隐藏 1-显示")
    private Integer status;
} 