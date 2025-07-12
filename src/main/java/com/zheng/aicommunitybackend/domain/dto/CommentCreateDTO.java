package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论创建请求
 */
@Data
@Schema(description = "评论创建请求")
public class CommentCreateDTO {

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", required = true)
    private Long postId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度应在1-500字符之间")
    @Schema(description = "评论内容", required = true)
    private String content;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID", example = "回复评论时需填写，顶级评论不需要")
    private Long parentId;
} 