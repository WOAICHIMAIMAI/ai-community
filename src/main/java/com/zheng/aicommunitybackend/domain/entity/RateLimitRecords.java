package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 限流记录表
 * @TableName rate_limit_records
 */
@TableName(value = "rate_limit_records")
@Data
public class RateLimitRecords implements Serializable {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 限流键
     */
    private String limitKey;

    /**
     * 限流类型：USER/IP/GLOBAL
     */
    private String limitType;

    /**
     * 用户ID（用户级限流时记录）
     */
    private Long userId;

    /**
     * IP地址（IP级限流时记录）
     */
    private String ipAddress;

    /**
     * 请求次数
     */
    private Integer requestCount;

    /**
     * 是否被限流：0-通过 1-被限流
     */
    private Integer isBlocked;

    /**
     * 时间窗口开始时间
     */
    private Date windowStartTime;

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
        RateLimitRecords other = (RateLimitRecords) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLimitKey() == null ? other.getLimitKey() == null : this.getLimitKey().equals(other.getLimitKey()))
            && (this.getLimitType() == null ? other.getLimitType() == null : this.getLimitType().equals(other.getLimitType()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getIpAddress() == null ? other.getIpAddress() == null : this.getIpAddress().equals(other.getIpAddress()))
            && (this.getRequestCount() == null ? other.getRequestCount() == null : this.getRequestCount().equals(other.getRequestCount()))
            && (this.getIsBlocked() == null ? other.getIsBlocked() == null : this.getIsBlocked().equals(other.getIsBlocked()))
            && (this.getWindowStartTime() == null ? other.getWindowStartTime() == null : this.getWindowStartTime().equals(other.getWindowStartTime()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLimitKey() == null) ? 0 : getLimitKey().hashCode());
        result = prime * result + ((getLimitType() == null) ? 0 : getLimitType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getIpAddress() == null) ? 0 : getIpAddress().hashCode());
        result = prime * result + ((getRequestCount() == null) ? 0 : getRequestCount().hashCode());
        result = prime * result + ((getIsBlocked() == null) ? 0 : getIsBlocked().hashCode());
        result = prime * result + ((getWindowStartTime() == null) ? 0 : getWindowStartTime().hashCode());
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
        sb.append(", limitKey=").append(limitKey);
        sb.append(", limitType=").append(limitType);
        sb.append(", userId=").append(userId);
        sb.append(", ipAddress=").append(ipAddress);
        sb.append(", requestCount=").append(requestCount);
        sb.append(", isBlocked=").append(isBlocked);
        sb.append(", windowStartTime=").append(windowStartTime);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
