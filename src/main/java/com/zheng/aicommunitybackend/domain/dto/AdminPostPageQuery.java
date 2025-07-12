package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员帖子分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理员帖子分页查询参数")
public class AdminPostPageQuery extends PostPageQuery {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID，用于筛选指定用户的帖子", example = "1")
    private Long userId;
    
    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶：0-否 1-是", example = "1")
    private Integer isTop;
    
    /**
     * 是否精华
     */
    @Schema(description = "是否精华：0-否 1-是", example = "1")
    private Integer isEssence;
    
    /**
     * 起始创建时间
     */
    @Schema(description = "起始创建时间，格式：yyyy-MM-dd HH:mm:ss", example = "2023-01-01 00:00:00")
    private String startTime;
    
    /**
     * 结束创建时间
     */
    @Schema(description = "结束创建时间，格式：yyyy-MM-dd HH:mm:ss", example = "2023-12-31 23:59:59")
    private String endTime;
} 