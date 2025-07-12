package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 维修工信息表
 * @TableName repair_workers
 */
@TableName(value ="repair_workers")
@Data
public class RepairWorkers implements Serializable {
    /**
     * 维修工ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 服务类型，多个用逗号分隔
     */
    private String serviceType;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 工作状态：0-休息 1-可接单 2-忙碌
     */
    private Integer workStatus;

    /**
     * 评分，1-5分
     */
    private BigDecimal rating;

    /**
     * 服务次数
     */
    private Integer serviceCount;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 创建时间
     */
    private Date createTime;

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
        RepairWorkers other = (RepairWorkers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getServiceType() == null ? other.getServiceType() == null : this.getServiceType().equals(other.getServiceType()))
            && (this.getIdCardNumber() == null ? other.getIdCardNumber() == null : this.getIdCardNumber().equals(other.getIdCardNumber()))
            && (this.getAvatarUrl() == null ? other.getAvatarUrl() == null : this.getAvatarUrl().equals(other.getAvatarUrl()))
            && (this.getWorkStatus() == null ? other.getWorkStatus() == null : this.getWorkStatus().equals(other.getWorkStatus()))
            && (this.getRating() == null ? other.getRating() == null : this.getRating().equals(other.getRating()))
            && (this.getServiceCount() == null ? other.getServiceCount() == null : this.getServiceCount().equals(other.getServiceCount()))
            && (this.getIntroduction() == null ? other.getIntroduction() == null : this.getIntroduction().equals(other.getIntroduction()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getServiceType() == null) ? 0 : getServiceType().hashCode());
        result = prime * result + ((getIdCardNumber() == null) ? 0 : getIdCardNumber().hashCode());
        result = prime * result + ((getAvatarUrl() == null) ? 0 : getAvatarUrl().hashCode());
        result = prime * result + ((getWorkStatus() == null) ? 0 : getWorkStatus().hashCode());
        result = prime * result + ((getRating() == null) ? 0 : getRating().hashCode());
        result = prime * result + ((getServiceCount() == null) ? 0 : getServiceCount().hashCode());
        result = prime * result + ((getIntroduction() == null) ? 0 : getIntroduction().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", phone=").append(phone);
        sb.append(", serviceType=").append(serviceType);
        sb.append(", idCardNumber=").append(idCardNumber);
        sb.append(", avatarUrl=").append(avatarUrl);
        sb.append(", workStatus=").append(workStatus);
        sb.append(", rating=").append(rating);
        sb.append(", serviceCount=").append(serviceCount);
        sb.append(", introduction=").append(introduction);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}