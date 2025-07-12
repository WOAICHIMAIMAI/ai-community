package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞状态VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点赞状态VO")
public class LikeStatusVO {
    
    /**
     * 是否已点赞
     */
    @Schema(description = "是否已点赞")
    private Boolean liked;
    
    /**
     * 点赞数量
     */
    @Schema(description = "点赞数量")
    private Integer likeCount;
} 