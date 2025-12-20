package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约服务VO
 */
@Data
@Schema(description = "预约服务VO")
public class AppointmentServiceVO {

    /**
     * 服务ID
     */
    @Schema(description = "服务ID")
    private Long id;

    /**
     * 服务类型标识
     */
    @Schema(description = "服务类型标识", example = "cleaning")
    private String type;

    /**
     * 服务名称
     */
    @Schema(description = "服务名称", example = "家政保洁")
    private String name;

    /**
     * 服务名称（管理端使用）
     */
    @Schema(description = "服务名称")
    private String serviceName;

    /**
     * 服务类型（管理端使用，中文）
     */
    @Schema(description = "服务类型")
    private String serviceType;

    /**
     * 服务描述
     */
    @Schema(description = "服务描述", example = "深度清洁，专业保洁团队")
    private String description;

    /**
     * 服务图标
     */
    @Schema(description = "服务图标", example = "brush-o")
    private String icon;

    /**
     * 基础价格
     */
    @Schema(description = "基础价格", example = "80.00")
    private String price;

    /**
     * 基础价格（数字类型）
     */
    @Schema(description = "基础价格")
    private BigDecimal basePrice;

    /**
     * 计价单位
     */
    @Schema(description = "计价单位", example = "次")
    private String unit;

    /**
     * 预计时长（分钟）
     */
    @Schema(description = "预计时长")
    private Integer duration;

    /**
     * 是否热门服务
     */
    @Schema(description = "是否热门服务")
    private Boolean isHot;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean isActive;

    /**
     * 评分
     */
    @Schema(description = "评分", example = "4.8")
    private String rating;

    /**
     * 渐变色配置（前端使用）
     */
    @Schema(description = "渐变色配置")
    private String gradient;

    /**
     * 颜色配置（前端使用）
     */
    @Schema(description = "颜色配置")
    private String color;

    /**
     * 服务状态
     */
    @Schema(description = "服务状态")
    private Integer status;

    /**
     * 审核状态
     */
    @Schema(description = "审核状态")
    private Integer approvalStatus;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createdBy;
}
