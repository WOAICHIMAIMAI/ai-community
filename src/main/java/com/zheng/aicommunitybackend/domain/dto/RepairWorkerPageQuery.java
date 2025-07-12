package com.zheng.aicommunitybackend.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 维修工分页查询DTO
 */
@Data
@Schema(description = "维修工分页查询DTO")
public class RepairWorkerPageQuery extends PageQuery {

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 服务类型
     */
    @Schema(description = "服务类型")
    private String serviceType;

    /**
     * 工作状态：0-休息 1-可接单 2-忙碌
     */
    @Schema(description = "工作状态：0-休息 1-可接单 2-忙碌")
    private Integer workStatus;

    /**
     * 最低评分
     */
    @Schema(description = "最低评分")
    private Double minRating;

    /**
     * 最高评分
     */
    @Schema(description = "最高评分")
    private Double maxRating;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private Date startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private Date endTime;
} 