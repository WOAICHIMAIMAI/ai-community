package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 红包活动查询DTO
 */
@Data
@Schema(description = "红包活动查询DTO")
public class RedPacketActivityQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Schema(description = "页码，从1开始")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private Integer size = 10;

    /**
     * 活动名称（模糊查询）
     */
    @Schema(description = "活动名称（模糊查询）")
    private String activityName;

    /**
     * 活动状态：0-未开始 1-进行中 2-已结束 3-已取消
     */
    @Schema(description = "活动状态：0-未开始 1-进行中 2-已结束 3-已取消")
    private Integer status;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long creatorId;

    /**
     * 开始时间范围-开始
     */
    @Schema(description = "开始时间范围-开始")
    private Date startTimeBegin;

    /**
     * 开始时间范围-结束
     */
    @Schema(description = "开始时间范围-结束")
    private Date startTimeEnd;

    /**
     * 创建时间范围-开始
     */
    @Schema(description = "创建时间范围-开始")
    private Date createdTimeBegin;

    /**
     * 创建时间范围-结束
     */
    @Schema(description = "创建时间范围-结束")
    private Date createdTimeEnd;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段：created_time, start_time, total_amount, grab_rate")
    private String sortField = "created_time";

    /**
     * 排序方向
     */
    @Schema(description = "排序方向：asc, desc")
    private String sortOrder = "desc";

    /**
     * 是否只查询当前用户创建的活动
     */
    @Schema(description = "是否只查询当前用户创建的活动")
    private Boolean onlyMine = false;
}
