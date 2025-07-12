package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "帖子分页查询参数")
public class PostPageQuery extends PageQuery {
    
    /**
     * 分类
     */
    @Schema(description = "帖子分类", example = "讨论")
    private String category;
    
    /**
     * 关键词
     */
    @Schema(description = "搜索关键词，搜索标题和内容", example = "智能家居")
    private String keyword;
    
    /**
     * 状态（用于我的帖子查询）
     */
    @Schema(description = "帖子状态：0-草稿，1-已发布", example = "1")
    private Integer status;
} 