package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢红包记录表
 * @TableName red_packet_records
 */
@TableName(value = "red_packet_records")
@Data
public class RedPacketRecords implements Serializable {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 红包详情ID
     */
    private Long packetDetailId;

    /**
     * 抢到的金额（分）
     */
    private Long amount;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 抢红包时间
     */
    private Date grabTime;

    /**
     * 账户是否已更新：0-未更新 1-已更新
     */
    private Integer accountUpdated;

    /**
     * 创建时间
     */
    private Date createdTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        RedPacketRecords other = (RedPacketRecords) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getActivityId() == null ? other.getActivityId() == null : this.getActivityId().equals(other.getActivityId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getPacketDetailId() == null ? other.getPacketDetailId() == null : this.getPacketDetailId().equals(other.getPacketDetailId()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getTransactionNo() == null ? other.getTransactionNo() == null : this.getTransactionNo().equals(other.getTransactionNo()))
            && (this.getGrabTime() == null ? other.getGrabTime() == null : this.getGrabTime().equals(other.getGrabTime()))
            && (this.getAccountUpdated() == null ? other.getAccountUpdated() == null : this.getAccountUpdated().equals(other.getAccountUpdated()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPacketDetailId() == null) ? 0 : getPacketDetailId().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getTransactionNo() == null) ? 0 : getTransactionNo().hashCode());
        result = prime * result + ((getGrabTime() == null) ? 0 : getGrabTime().hashCode());
        result = prime * result + ((getAccountUpdated() == null) ? 0 : getAccountUpdated().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", activityId=").append(activityId);
        sb.append(", userId=").append(userId);
        sb.append(", packetDetailId=").append(packetDetailId);
        sb.append(", amount=").append(amount);
        sb.append(", transactionNo=").append(transactionNo);
        sb.append(", grabTime=").append(grabTime);
        sb.append(", accountUpdated=").append(accountUpdated);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
