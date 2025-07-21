package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

/**
 * 常见问题查询DTO
 */
@Data
public class CommonProblemQueryDTO {
    /**
     * 问题分类类型
     */
    private Integer type;
    
    /**
     * 是否只查询置顶问题 (priority=1)
     */
    private Boolean onlyPriority;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
