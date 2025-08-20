package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import java.io.Serializable;

/**
 * 抢红包记录查询DTO
 */
@Data
@Schema(description = "抢红包记录查询DTO")
public class RedPacketRecordQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    /**
     * 活动ID（可选，用于查询特定活动的记录）
     */
    @Schema(description = "活动ID，可选")
    private Long activityId;
}
