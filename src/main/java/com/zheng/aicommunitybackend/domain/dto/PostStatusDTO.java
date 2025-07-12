package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子状态更新DTO
 */
@Data
@Schema(description = "帖子状态更新DTO")
public class PostStatusDTO {
    
    /**
     * 帖子ID
     */
    @Schema(description = "帖子ID", required = true)
    private Long postId;
    
    /**
     * 帖子状态：0-草稿 1-已发布 2-已删除
     */
    @Schema(description = "帖子状态：0-草稿 1-已发布 2-已删除")
    private Integer status;
    
    /**
     * 是否置顶：0-否 1-是
     */
    @Schema(description = "是否置顶：0-否 1-是")
    private Integer isTop;
    
    /**
     * 是否精华：0-否 1-是
     */
    @Schema(description = "是否精华：0-否 1-是")
    private Integer isEssence;
} 