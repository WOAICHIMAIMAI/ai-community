package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 报修进度DTO
 */
@Data
@Schema(description = "报修进度DTO")
public class RepairProgressDTO {

    /**
     * 关联工单ID
     */
    @NotNull(message = "工单ID不能为空")
    @Schema(description = "工单ID", required = true)
    private Long orderId;

    /**
     * 操作类型
     */
    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型", required = true)
    private String action;

    /**
     * 操作描述
     */
    @NotBlank(message = "操作描述不能为空")
    @Schema(description = "操作描述", required = true)
    private String description;
} 