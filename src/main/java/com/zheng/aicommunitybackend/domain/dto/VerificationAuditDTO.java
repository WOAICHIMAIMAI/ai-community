package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 实名认证审核DTO
 */
@Data
@Schema(description = "实名认证审核DTO")
public class VerificationAuditDTO {
    
    /**
     * 认证记录ID
     */
    @NotNull(message = "认证记录ID不能为空")
    @Schema(description = "认证记录ID", required = true)
    private Long verificationId;
    
    /**
     * 审核结果：true-通过，false-拒绝
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：true-通过，false-拒绝", required = true)
    private Boolean approved;
    
    /**
     * 拒绝原因，审核拒绝时必填
     */
    @Schema(description = "拒绝原因，审核拒绝时必填")
    private String rejectReason;
} 