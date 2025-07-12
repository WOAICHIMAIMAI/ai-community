package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用分页查询参数
 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery {
    
    /**
     * 页码（从1开始）
     */
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;
    
    /**
     * 每页记录数
     */
    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime")
    private String sortField;
    
    /**
     * 排序方式（asc/desc）
     */
    @Schema(description = "排序方式：asc(升序)、desc(降序)", example = "desc")
    private String sortOrder = "desc";
    
    /**
     * 获取起始索引
     * 
     * @return 分页起始索引
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
    
    /**
     * 设置安全的页码值（最小为1）
     */
    public void setPage(Integer page) {
        this.page = (page == null || page < 1) ? 1 : page;
    }
    
    /**
     * 设置安全的每页记录数（最小为1，最大为100）
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
    }
} 