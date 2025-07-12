package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理端工单状态更新DTO
 */
@Data
@Schema(description = "管理端工单状态更新DTO")
public class AdminRepairOrderUpdateDTO {
    
    /**
     * 报修单ID
     */
    @NotNull(message = "报修单ID不能为空")
    @Schema(description = "报修单ID", required = true)
    private Long orderId;
    
    /**
     * 工单状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消
     */
    @NotNull(message = "工单状态不能为空")
    @Schema(description = "工单状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消", required = true)
    private Integer status;
    
    /**
     * 备注说明
     */
    @Schema(description = "备注说明")
    private String remark;
} 