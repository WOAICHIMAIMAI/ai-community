package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢红包结果视图对象
 */
@Data
@Schema(description = "抢红包结果视图对象")
public class RedPacketGrabVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否抢到红包
     */
    @Schema(description = "是否抢到红包")
    private Boolean success;

    /**
     * 抢红包结果消息
     */
    @Schema(description = "抢红包结果消息")
    private String message;

    /**
     * 抢到的金额（分）
     */
    @Schema(description = "抢到的金额（分）")
    private Long amount;

    /**
     * 抢到的金额（元，用于显示）
     */
    @Schema(description = "抢到的金额（元）")
    private Double amountYuan;

    /**
     * 交易流水号
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 抢红包时间
     */
    @Schema(description = "抢红包时间")
    private Date grabTime;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long activityId;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 红包序号
     */
    @Schema(description = "红包序号")
    private Integer packetIndex;

    /**
     * 是否是手气最佳
     */
    @Schema(description = "是否是手气最佳")
    private Boolean isLuckiest;

    /**
     * 抢红包排名（在该活动中的排名）
     */
    @Schema(description = "抢红包排名")
    private Integer ranking;

    /**
     * 活动剩余红包数量
     */
    @Schema(description = "活动剩余红包数量")
    private Integer remainingCount;

    /**
     * 活动剩余红包金额（分）
     */
    @Schema(description = "活动剩余红包金额（分）")
    private Long remainingAmount;

    /**
     * 活动剩余红包金额（元，用于显示）
     */
    @Schema(description = "活动剩余红包金额（元）")
    private Double remainingAmountYuan;

    /**
     * 当前账户余额（分）
     */
    @Schema(description = "当前账户余额（分）")
    private Long currentBalance;

    /**
     * 当前账户余额（元，用于显示）
     */
    @Schema(description = "当前账户余额（元）")
    private Double currentBalanceYuan;

    // 计算衍生字段的方法
    public Double getAmountYuan() {
        return amount != null ? amount / 100.0 : 0.0;
    }

    public Double getRemainingAmountYuan() {
        return remainingAmount != null ? remainingAmount / 100.0 : 0.0;
    }

    public Double getCurrentBalanceYuan() {
        return currentBalance != null ? currentBalance / 100.0 : 0.0;
    }

    // 静态工厂方法
    public static RedPacketGrabVO success(Long amount, String transactionNo, Date grabTime) {
        RedPacketGrabVO vo = new RedPacketGrabVO();
        vo.setSuccess(true);
        vo.setMessage("恭喜您抢到红包！");
        vo.setAmount(amount);
        vo.setTransactionNo(transactionNo);
        vo.setGrabTime(grabTime);
        return vo;
    }

    public static RedPacketGrabVO failure(String message) {
        RedPacketGrabVO vo = new RedPacketGrabVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }

    public static RedPacketGrabVO alreadyGrabbed() {
        return failure("您已经抢过这个红包了");
    }

    public static RedPacketGrabVO noPacketLeft() {
        return failure("红包已被抢完");
    }

    public static RedPacketGrabVO activityNotStarted() {
        return failure("活动还未开始");
    }

    public static RedPacketGrabVO activityEnded() {
        return failure("活动已结束");
    }

    public static RedPacketGrabVO rateLimited() {
        return failure("请求过于频繁，请稍后再试");
    }

    public static RedPacketGrabVO systemBusy() {
        return failure("系统繁忙，请稍后再试");
    }
}
