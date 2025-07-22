package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预约服务人员表
 * @TableName appointment_workers
 */
@TableName(value = "appointment_workers")
@Data
public class AppointmentWorkers implements Serializable {
    /**
     * 服务人员ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务人员姓名
     */
    private String workerName;

    /**
     * 联系电话
     */
    private String workerPhone;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 工作经验年数
     */
    private Integer experienceYears;

    /**
     * 服务类型，多个用逗号分隔
     */
    private String serviceTypes;

    /**
     * 技能描述
     */
    private String skillDescription;

    /**
     * 评分(1-5分)
     */
    private BigDecimal rating;

    /**
     * 接单数量
     */
    private Integer orderCount;

    /**
     * 状态：0-离职 1-在职 2-休假
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

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
        AppointmentWorkers other = (AppointmentWorkers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWorkerName() == null ? other.getWorkerName() == null : this.getWorkerName().equals(other.getWorkerName()))
            && (this.getWorkerPhone() == null ? other.getWorkerPhone() == null : this.getWorkerPhone().equals(other.getWorkerPhone()))
            && (this.getAvatarUrl() == null ? other.getAvatarUrl() == null : this.getAvatarUrl().equals(other.getAvatarUrl()))
            && (this.getGender() == null ? other.getGender() == null : this.getGender().equals(other.getGender()))
            && (this.getAge() == null ? other.getAge() == null : this.getAge().equals(other.getAge()))
            && (this.getExperienceYears() == null ? other.getExperienceYears() == null : this.getExperienceYears().equals(other.getExperienceYears()))
            && (this.getServiceTypes() == null ? other.getServiceTypes() == null : this.getServiceTypes().equals(other.getServiceTypes()))
            && (this.getSkillDescription() == null ? other.getSkillDescription() == null : this.getSkillDescription().equals(other.getSkillDescription()))
            && (this.getRating() == null ? other.getRating() == null : this.getRating().equals(other.getRating()))
            && (this.getOrderCount() == null ? other.getOrderCount() == null : this.getOrderCount().equals(other.getOrderCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWorkerName() == null) ? 0 : getWorkerName().hashCode());
        result = prime * result + ((getWorkerPhone() == null) ? 0 : getWorkerPhone().hashCode());
        result = prime * result + ((getAvatarUrl() == null) ? 0 : getAvatarUrl().hashCode());
        result = prime * result + ((getGender() == null) ? 0 : getGender().hashCode());
        result = prime * result + ((getAge() == null) ? 0 : getAge().hashCode());
        result = prime * result + ((getExperienceYears() == null) ? 0 : getExperienceYears().hashCode());
        result = prime * result + ((getServiceTypes() == null) ? 0 : getServiceTypes().hashCode());
        result = prime * result + ((getSkillDescription() == null) ? 0 : getSkillDescription().hashCode());
        result = prime * result + ((getRating() == null) ? 0 : getRating().hashCode());
        result = prime * result + ((getOrderCount() == null) ? 0 : getOrderCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", workerName=").append(workerName);
        sb.append(", workerPhone=").append(workerPhone);
        sb.append(", avatarUrl=").append(avatarUrl);
        sb.append(", gender=").append(gender);
        sb.append(", age=").append(age);
        sb.append(", experienceYears=").append(experienceYears);
        sb.append(", serviceTypes=").append(serviceTypes);
        sb.append(", skillDescription=").append(skillDescription);
        sb.append(", rating=").append(rating);
        sb.append(", orderCount=").append(orderCount);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
