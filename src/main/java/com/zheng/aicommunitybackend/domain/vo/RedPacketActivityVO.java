package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 红包活动视图对象
 */
@Data
@Schema(description = "红包活动视图对象")
public class RedPacketActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 活动描述
     */
    @Schema(description = "活动描述")
    private String activityDesc;

    /**
     * 红包总金额（分）
     */
    @Schema(description = "红包总金额（分）")
    private Long totalAmount;

    /**
     * 红包总金额（元，用于显示）
     */
    @Schema(description = "红包总金额（元）")
    private Double totalAmountYuan;

    /**
     * 红包总数量
     */
    @Schema(description = "红包总数量")
    private Integer totalCount;

    /**
     * 已抢红包数量
     */
    @Schema(description = "已抢红包数量")
    private Integer grabbedCount;

    /**
     * 已抢红包金额（分）
     */
    @Schema(description = "已抢红包金额（分）")
    private Long grabbedAmount;

    /**
     * 已抢红包金额（元，用于显示）
     */
    @Schema(description = "已抢红包金额（元）")
    private Double grabbedAmountYuan;

    /**
     * 活动开始时间
     */
    @Schema(description = "活动开始时间")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @Schema(description = "活动结束时间")
    private Date endTime;

    /**
     * 活动状态：0-未开始 1-进行中 2-已结束 3-已取消
     */
    @Schema(description = "活动状态：0-未开始 1-进行中 2-已结束 3-已取消")
    private Integer status;

    /**
     * 活动状态名称
     */
    @Schema(description = "活动状态名称")
    private String statusName;

    /**
     * 创建者ID
     */
    @Schema(description = "创建者ID")
    private Long creatorId;

    /**
     * 创建者名称
     */
    @Schema(description = "创建者名称")
    private String creatorName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updatedTime;

    /**
     * 抢红包进度（百分比）
     */
    @Schema(description = "抢红包进度（百分比）")
    private Double grabRate;

    /**
     * 剩余红包数量
     */
    @Schema(description = "剩余红包数量")
    private Integer remainingCount;

    /**
     * 剩余红包金额（分）
     */
    @Schema(description = "剩余红包金额（分）")
    private Long remainingAmount;

    /**
     * 剩余红包金额（元，用于显示）
     */
    @Schema(description = "剩余红包金额（元）")
    private Double remainingAmountYuan;

    /**
     * 是否可以抢红包
     */
    @Schema(description = "是否可以抢红包")
    private Boolean canGrab;

    /**
     * 不能抢红包的原因
     */
    @Schema(description = "不能抢红包的原因")
    private String cannotGrabReason;

    /**
     * 用户是否已抢过
     */
    @Schema(description = "用户是否已抢过")
    private Boolean hasGrabbed;

    /**
     * 用户抢到的金额（分）
     */
    @Schema(description = "用户抢到的金额（分）")
    private Long userGrabbedAmount;

    /**
     * 用户抢到的金额（元，用于显示）
     */
    @Schema(description = "用户抢到的金额（元）")
    private Double userGrabbedAmountYuan;

    /**
     * 用户抢红包时间
     */
    @Schema(description = "用户抢红包时间")
    private Date userGrabTime;

    // 计算衍生字段的方法
    public Double getTotalAmountYuan() {
        return totalAmount != null ? totalAmount / 100.0 : 0.0;
    }

    public Double getGrabbedAmountYuan() {
        return grabbedAmount != null ? grabbedAmount / 100.0 : 0.0;
    }

    public Integer getRemainingCount() {
        if (totalCount != null && grabbedCount != null) {
            return totalCount - grabbedCount;
        }
        return totalCount != null ? totalCount : 0;
    }

    public Long getRemainingAmount() {
        if (totalAmount != null && grabbedAmount != null) {
            return totalAmount - grabbedAmount;
        }
        return totalAmount != null ? totalAmount : 0L;
    }

    public Double getRemainingAmountYuan() {
        Long remaining = getRemainingAmount();
        return remaining != null ? remaining / 100.0 : 0.0;
    }

    public Double getUserGrabbedAmountYuan() {
        return userGrabbedAmount != null ? userGrabbedAmount / 100.0 : 0.0;
    }
}
