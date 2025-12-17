package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 管理端工单分配DTO
 */
@Data
@Schema(description = "管理端工单分配DTO")
public class AdminRepairOrderAssignDTO {
    
    /**
     * 报修单ID
     */
    @NotNull(message = "报修单ID不能为空")
    @Schema(description = "报修单ID", required = true)
    private Long orderId;
    
    /**
     * 维修工ID
     */
    @NotNull(message = "维修工ID不能为空")
    @Schema(description = "维修工ID", required = true)
    private Long workerId;
} 