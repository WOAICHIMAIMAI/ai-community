package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 服务人员分配DTO
 */
@Data
@Schema(description = "服务人员分配数据")
public class WorkerAssignDTO {
    
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", required = true)
    private Long orderId;
    
    @NotNull(message = "服务人员ID不能为空")
    @Schema(description = "服务人员ID", required = true)
    private Long workerId;
    
    @Schema(description = "服务人员姓名")
    private String workerName;
}

