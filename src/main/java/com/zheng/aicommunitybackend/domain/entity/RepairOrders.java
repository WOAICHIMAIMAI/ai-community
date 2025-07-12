package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 物业报修工单表
 * @TableName repair_orders
 */
@TableName(value ="repair_orders")
@Data
public class RepairOrders implements Serializable {
    /**
     * 报修单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报修单编号
     */
    private String orderNumber;

    /**
     * 报修用户ID
     */
    private Long userId;

    /**
     * 报修地址ID
     */
    private Long addressId;

    /**
     * 报修类型：水电/门窗/家电等
     */
    private String repairType;

    /**
     * 报修标题
     */
    private String title;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 现场照片URL，多个用逗号分隔
     */
    private String images;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 期望上门时间
     */
    private Date expectedTime;

    /**
     * 状态：0-待受理 1-已分配 2-处理中 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 维修工ID
     */
    private Long workerId;

    /**
     * 维修工姓名
     */
    private String workerName;

    /**
     * 维修工电话
     */
    private String workerPhone;

    /**
     * 预约上门时间
     */
    private Date appointmentTime;

    /**
     * 完成时间
     */
    private Date completionTime;

    /**
     * 满意度评价：1-5星
     */
    private Integer satisfactionLevel;

    /**
     * 用户反馈
     */
    private String feedback;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
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
        RepairOrders other = (RepairOrders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrderNumber() == null ? other.getOrderNumber() == null : this.getOrderNumber().equals(other.getOrderNumber()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAddressId() == null ? other.getAddressId() == null : this.getAddressId().equals(other.getAddressId()))
            && (this.getRepairType() == null ? other.getRepairType() == null : this.getRepairType().equals(other.getRepairType()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getImages() == null ? other.getImages() == null : this.getImages().equals(other.getImages()))
            && (this.getContactPhone() == null ? other.getContactPhone() == null : this.getContactPhone().equals(other.getContactPhone()))
            && (this.getExpectedTime() == null ? other.getExpectedTime() == null : this.getExpectedTime().equals(other.getExpectedTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getWorkerId() == null ? other.getWorkerId() == null : this.getWorkerId().equals(other.getWorkerId()))
            && (this.getWorkerName() == null ? other.getWorkerName() == null : this.getWorkerName().equals(other.getWorkerName()))
            && (this.getWorkerPhone() == null ? other.getWorkerPhone() == null : this.getWorkerPhone().equals(other.getWorkerPhone()))
            && (this.getAppointmentTime() == null ? other.getAppointmentTime() == null : this.getAppointmentTime().equals(other.getAppointmentTime()))
            && (this.getCompletionTime() == null ? other.getCompletionTime() == null : this.getCompletionTime().equals(other.getCompletionTime()))
            && (this.getSatisfactionLevel() == null ? other.getSatisfactionLevel() == null : this.getSatisfactionLevel().equals(other.getSatisfactionLevel()))
            && (this.getFeedback() == null ? other.getFeedback() == null : this.getFeedback().equals(other.getFeedback()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrderNumber() == null) ? 0 : getOrderNumber().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAddressId() == null) ? 0 : getAddressId().hashCode());
        result = prime * result + ((getRepairType() == null) ? 0 : getRepairType().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getImages() == null) ? 0 : getImages().hashCode());
        result = prime * result + ((getContactPhone() == null) ? 0 : getContactPhone().hashCode());
        result = prime * result + ((getExpectedTime() == null) ? 0 : getExpectedTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getWorkerId() == null) ? 0 : getWorkerId().hashCode());
        result = prime * result + ((getWorkerName() == null) ? 0 : getWorkerName().hashCode());
        result = prime * result + ((getWorkerPhone() == null) ? 0 : getWorkerPhone().hashCode());
        result = prime * result + ((getAppointmentTime() == null) ? 0 : getAppointmentTime().hashCode());
        result = prime * result + ((getCompletionTime() == null) ? 0 : getCompletionTime().hashCode());
        result = prime * result + ((getSatisfactionLevel() == null) ? 0 : getSatisfactionLevel().hashCode());
        result = prime * result + ((getFeedback() == null) ? 0 : getFeedback().hashCode());
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
        sb.append(", orderNumber=").append(orderNumber);
        sb.append(", userId=").append(userId);
        sb.append(", addressId=").append(addressId);
        sb.append(", repairType=").append(repairType);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", images=").append(images);
        sb.append(", contactPhone=").append(contactPhone);
        sb.append(", expectedTime=").append(expectedTime);
        sb.append(", status=").append(status);
        sb.append(", workerId=").append(workerId);
        sb.append(", workerName=").append(workerName);
        sb.append(", workerPhone=").append(workerPhone);
        sb.append(", appointmentTime=").append(appointmentTime);
        sb.append(", completionTime=").append(completionTime);
        sb.append(", satisfactionLevel=").append(satisfactionLevel);
        sb.append(", feedback=").append(feedback);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}