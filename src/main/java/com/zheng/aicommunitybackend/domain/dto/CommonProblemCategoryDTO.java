package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

/**
 * 常见问题分类DTO
 */
@Data
public class CommonProblemCategoryDTO {
    /**
     * 分类类型
     */
    private Integer type;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 该分类下的问题数量
     */
    private Long problemCount;
}
