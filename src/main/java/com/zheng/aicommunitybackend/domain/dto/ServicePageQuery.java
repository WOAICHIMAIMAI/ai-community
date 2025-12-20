package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务分页查询DTO
 */
@Data
@Schema(description = "服务分页查询条件")
public class ServicePageQuery {
    
    @Schema(description = "页码", example = "1")
    private Integer page = 1;
    
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
    
    @Schema(description = "服务类型")
    private String serviceType;
    
    @Schema(description = "服务名称（模糊查询）")
    private String serviceName;
    
    @Schema(description = "关键词（服务名称或描述模糊查询）")
    private String keyword;
    
    @Schema(description = "状态：0-禁用 1-启用 2-待审核 3-拒绝")
    private Integer status;
    
    @Schema(description = "审核状态：0-待审核 1-已通过 2-已拒绝")
    private Integer approvalStatus;
    
    @Schema(description = "是否热门：0-否 1-是")
    private Integer isHot;
    
    @Schema(description = "创建用户ID")
    private Long userId;
    
    @Schema(description = "排序字段")
    private String sortField;
    
    @Schema(description = "排序方式：asc-升序 desc-降序")
    private String sortOrder = "desc";
}

