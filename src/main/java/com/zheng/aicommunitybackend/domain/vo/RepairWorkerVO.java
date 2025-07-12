package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 维修工VO
 */
@Data
@Schema(description = "维修工VO")
public class RepairWorkerVO {

    /**
     * 维修工ID
     */
    @Schema(description = "维修工ID")
    private Long id;

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
     * 服务类型，多个用逗号分隔
     */
    @Schema(description = "服务类型，多个用逗号分隔")
    private String serviceType;

    /**
     * 服务类型列表
     */
    @Schema(description = "服务类型列表")
    private String[] serviceTypeList;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatarUrl;

    /**
     * 工作状态：0-休息 1-可接单 2-忙碌
     */
    @Schema(description = "工作状态：0-休息 1-可接单 2-忙碌")
    private Integer workStatus;

    /**
     * 工作状态描述
     */
    @Schema(description = "工作状态描述")
    private String workStatusDesc;

    /**
     * 评分，1-5分
     */
    @Schema(description = "评分，1-5分")
    private BigDecimal rating;

    /**
     * 服务次数
     */
    @Schema(description = "服务次数")
    private Integer serviceCount;

    /**
     * 个人介绍
     */
    @Schema(description = "个人介绍")
    private String introduction;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
} 