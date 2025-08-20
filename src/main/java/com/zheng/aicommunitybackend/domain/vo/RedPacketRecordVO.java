package com.zheng.aicommunitybackend.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢红包记录视图对象
 */
@Data
@Schema(description = "抢红包记录视图对象")
public class RedPacketRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Long id;

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
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 红包详情ID
     */
    @Schema(description = "红包详情ID")
    private Long packetDetailId;

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
     * 账户是否已更新：0-未更新 1-已更新
     */
    @Schema(description = "账户是否已更新：0-未更新 1-已更新")
    private Integer accountUpdated;

    /**
     * 账户更新状态名称
     */
    @Schema(description = "账户更新状态名称")
    private String accountUpdatedName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdTime;

    /**
     * 红包序号
     */
    @Schema(description = "红包序号")
    private Integer packetIndex;

    /**
     * 抢红包排名（在该活动中的排名）
     */
    @Schema(description = "抢红包排名")
    private Integer ranking;

    /**
     * 是否是手气最佳
     */
    @Schema(description = "是否是手气最佳")
    private Boolean isLuckiest;

    // 计算衍生字段的方法
    public Double getAmountYuan() {
        return amount != null ? amount / 100.0 : 0.0;
    }

    public String getAccountUpdatedName() {
        if (accountUpdated == null) {
            return "未知";
        }
        switch (accountUpdated) {
            case 0:
                return "处理中";
            case 1:
                return "已到账";
            default:
                return "未知";
        }
    }
}
