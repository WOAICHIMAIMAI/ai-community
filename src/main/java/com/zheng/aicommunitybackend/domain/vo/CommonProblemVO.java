package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

/**
 * 常见问题VO
 */
@Data
public class CommonProblemVO {
    /**
     * 问题ID
     */
    private Integer id;
    
    /**
     * 问题标题
     */
    private String problem;
    
    /**
     * 问题答案
     */
    private String answer;
    
    /**
     * 问题分类类型
     */
    private Integer type;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 优先级 (0-正常, 1-置顶)
     */
    private Integer priority;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
}
