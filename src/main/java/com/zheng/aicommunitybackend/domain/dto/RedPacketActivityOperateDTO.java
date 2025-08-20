package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 红包活动操作DTO（开始、结束、取消等）
 */
@Data
@Schema(description = "红包活动操作DTO")
public class RedPacketActivityOperateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    @Schema(description = "活动ID", required = true)
    private Long activityId;

    /**
     * 操作类型：start-开始, end-结束, cancel-取消
     */
    @Schema(description = "操作类型：start-开始, end-结束, cancel-取消")
    private String operation;
}
