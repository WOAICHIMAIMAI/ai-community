package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约订单VO
 */
@Data
@Schema(description = "预约订单VO")
public class AppointmentOrderVO {

    /**
     * 预约订单ID
     */
    @Schema(description = "预约订单ID")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "APT202401250001")
    private String orderNo;

    /**
     * 服务名称
     */
    @Schema(description = "服务名称", example = "家政保洁")
    private String serviceName;

    /**
     * 服务类型
     */
    @Schema(description = "服务类型", example = "cleaning")
    private String serviceType;

    /**
     * 预约服务时间
     */
    @Schema(description = "预约服务时间")
    private Date appointmentTime;

    /**
     * 服务地址
     */
    @Schema(description = "服务地址", example = "阳光小区1号楼1单元101室")
    private String address;

    /**
     * 联系人姓名
     */
    @Schema(description = "联系人姓名", example = "张先生")
    private String contactName;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "138****8888")
    private String contactPhone;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求")
    private String requirements;

    /**
     * 预估价格
     */
    @Schema(description = "预估价格")
    private BigDecimal estimatedPrice;

    /**
     * 实际价格
     */
    @Schema(description = "实际价格")
    private BigDecimal actualPrice;

    /**
     * 订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消
     */
    @Schema(description = "订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "待确认")
    private String statusDesc;

    /**
     * 取消原因
     */
    @Schema(description = "取消原因")
    private String cancelReason;

    /**
     * 用户评分(1-5分)
     */
    @Schema(description = "用户评分")
    private Integer rating;

    /**
     * 用户评价
     */
    @Schema(description = "用户评价")
    private String comment;

    /**
     * 是否已评价
     */
    @Schema(description = "是否已评价")
    private Boolean rated;

    /**
     * 服务人员信息
     */
    @Schema(description = "服务人员信息")
    private WorkerInfo worker;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 确认时间
     */
    @Schema(description = "确认时间")
    private Date confirmTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private Date finishTime;

    /**
     * 服务人员信息内部类
     */
    @Data
    @Schema(description = "服务人员信息")
    public static class WorkerInfo {
        /**
         * 服务人员姓名
         */
        @Schema(description = "服务人员姓名", example = "李阿姨")
        private String name;

        /**
         * 服务人员电话
         */
        @Schema(description = "服务人员电话", example = "139****9999")
        private String phone;

        /**
         * 服务人员头像
         */
        @Schema(description = "服务人员头像")
        private String avatar;

        /**
         * 服务人员评分
         */
        @Schema(description = "服务人员评分", example = "4.8")
        private BigDecimal rating;
    }
}
