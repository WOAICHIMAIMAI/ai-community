package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报修工单评价反馈DTO
 */
@Data
@Schema(description = "报修工单评价反馈DTO")
public class RepairFeedbackDTO {
    
    /**
     * 报修单ID
     */
    @NotNull(message = "报修单ID不能为空")
    @Schema(description = "报修单ID", required = true)
    private Long orderId;
    
    /**
     * 满意度评价：1-5星
     */
    @NotNull(message = "满意度评价不能为空")
    @Min(value = 1, message = "满意度评价最低为1星")
    @Max(value = 5, message = "满意度评价最高为5星")
    @Schema(description = "满意度评价：1-5星", required = true)
    private Integer satisfactionLevel;
    
    /**
     * 用户反馈
     */
    @Schema(description = "用户反馈")
    private String feedback;
} 