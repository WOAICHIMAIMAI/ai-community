package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 服务审核DTO
 */
@Data
@Schema(description = "服务审核数据")
public class ServiceApprovalDTO {
    
    @NotNull(message = "服务ID不能为空")
    @Schema(description = "服务ID", required = true)
    private Long serviceId;
    
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "是否通过：true-通过 false-拒绝", required = true)
    private Boolean approved;
    
    @Schema(description = "拒绝原因（拒绝时必填）")
    private String rejectReason;
}

