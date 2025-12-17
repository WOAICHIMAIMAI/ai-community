package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户端工单状态更新DTO
 */
@Data
@Schema(description = "用户端工单状态更新DTO")
public class UserRepairOrderStatusDTO {
    
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
    @Min(value = 0, message = "工单状态值不正确")
    @Max(value = 4, message = "工单状态值不正确")
    @Schema(description = "工单状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消", required = true)
    private Integer status;
    
    /**
     * 取消原因或备注说明
     */
    @Schema(description = "取消原因或备注说明")
    private String remark;
}

