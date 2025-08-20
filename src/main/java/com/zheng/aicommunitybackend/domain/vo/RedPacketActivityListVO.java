package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 红包活动列表视图对象（简化版）
 */
@Data
@Schema(description = "红包活动列表视图对象")
public class RedPacketActivityListVO implements Serializable {

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
     * 是否可以抢红包
     */
    @Schema(description = "是否可以抢红包")
    private Boolean canGrab;

    /**
     * 用户是否已抢过
     */
    @Schema(description = "用户是否已抢过")
    private Boolean hasGrabbed;

    // 计算衍生字段的方法
    public Integer getRemainingCount() {
        if (totalCount != null && grabbedCount != null) {
            return totalCount - grabbedCount;
        }
        return totalCount != null ? totalCount : 0;
    }
}
