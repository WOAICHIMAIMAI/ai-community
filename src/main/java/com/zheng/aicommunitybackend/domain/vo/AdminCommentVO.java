package com.zheng.aicommunitybackend.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 管理端评论视图对象
 */
@Data
@Schema(description = "管理端评论视图对象")
public class AdminCommentVO {

    /**
     * 评论ID
     */
    @Schema(description = "评论ID")
    private Long id;

    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID")
    private Long postId;
    
    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题")
    private String postTitle;

    /**
     * 父评论ID
     */
    @Schema(description = "父评论ID")
    private Long parentId;

    /**
     * 评论用户ID
     */
    @Schema(description = "评论用户ID")
    private Long userId;

    /**
     * 评论用户昵称
     */
    @Schema(description = "评论用户昵称")
    private String nickname;

    /**
     * 评论用户头像
     */
    @Schema(description = "评论用户头像")
    private String avatar;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 回复数量
     */
    @Schema(description = "回复数量")
    private Integer replyCount;
    
    /**
     * 状态：0-隐藏 1-显示
     */
    @Schema(description = "状态：0-隐藏 1-显示")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
} 