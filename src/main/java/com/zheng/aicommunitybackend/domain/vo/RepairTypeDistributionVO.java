package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 报修工单类型分布VO
 */
@Data
@Schema(description = "报修工单类型分布数据")
public class RepairTypeDistributionVO {
    
    /**
     * 类型列表
     */
    @Schema(description = "类型列表")
    private List<String> types;
    
    /**
     * 数量列表
     */
    @Schema(description = "数量列表")
    private List<Long> counts;
} 