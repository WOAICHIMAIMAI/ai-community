package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 预约评价DTO
 */
@Data
@Schema(description = "预约评价DTO")
public class AppointmentRateDTO {

    /**
     * 预约订单ID
     */
    @NotNull(message = "预约订单ID不能为空")
    @Schema(description = "预约订单ID")
    private Long appointmentId;

    /**
     * 评分(1-5分)
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    @Schema(description = "评分(1-5分)", example = "5")
    private Integer rating;

    /**
     * 评价内容
     */
    @Schema(description = "评价内容", example = "服务很好，师傅很专业")
    private String comment;
}
