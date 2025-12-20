package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单状态更新DTO
 */
@Data
@Schema(description = "订单状态更新数据")
public class OrderStatusUpdateDTO {
    
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID", required = true)
    private Long orderId;
    
    @NotNull(message = "状态不能为空")
    @Schema(description = "订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消", required = true)
    private Integer status;
    
    @Schema(description = "备注")
    private String remark;
}

