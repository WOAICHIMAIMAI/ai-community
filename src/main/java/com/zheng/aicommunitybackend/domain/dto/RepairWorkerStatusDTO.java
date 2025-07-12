package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 维修工状态更新DTO
 */
@Data
@Schema(description = "维修工状态更新DTO")
public class RepairWorkerStatusDTO {

    /**
     * 维修工ID
     */
    @NotNull(message = "维修工ID不能为空")
    @Schema(description = "维修工ID", required = true)
    private Long workerId;

    /**
     * 工作状态：0-休息 1-可接单 2-忙碌
     */
    @NotNull(message = "工作状态不能为空")
    @Min(value = 0, message = "工作状态值不正确")
    @Max(value = 2, message = "工作状态值不正确")
    @Schema(description = "工作状态：0-休息 1-可接单 2-忙碌", required = true)
    private Integer workStatus;
} 