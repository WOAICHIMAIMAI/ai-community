package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户实名认证信息表
 * @TableName user_verification
 */
@TableName(value ="user_verification")
@Data
public class UserVerification implements Serializable {
    /**
     * 认证记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 身份证正面照片URL
     */
    private String idCardFrontUrl;

    /**
     * 身份证反面照片URL
     */
    private String idCardBackUrl;

    /**
     * 认证状态：0-未认证 1-认证中 2-已认证 3-认证失败
     */
    private Integer verificationStatus;

    /**
     * 认证失败原因
     */
    private String failureReason;

    /**
     * 提交认证时间
     */
    private Date submitTime;

    /**
     * 认证完成时间
     */
    private Date completeTime;

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
        UserVerification other = (UserVerification) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getIdCardNumber() == null ? other.getIdCardNumber() == null : this.getIdCardNumber().equals(other.getIdCardNumber()))
            && (this.getIdCardFrontUrl() == null ? other.getIdCardFrontUrl() == null : this.getIdCardFrontUrl().equals(other.getIdCardFrontUrl()))
            && (this.getIdCardBackUrl() == null ? other.getIdCardBackUrl() == null : this.getIdCardBackUrl().equals(other.getIdCardBackUrl()))
            && (this.getVerificationStatus() == null ? other.getVerificationStatus() == null : this.getVerificationStatus().equals(other.getVerificationStatus()))
            && (this.getFailureReason() == null ? other.getFailureReason() == null : this.getFailureReason().equals(other.getFailureReason()))
            && (this.getSubmitTime() == null ? other.getSubmitTime() == null : this.getSubmitTime().equals(other.getSubmitTime()))
            && (this.getCompleteTime() == null ? other.getCompleteTime() == null : this.getCompleteTime().equals(other.getCompleteTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getIdCardNumber() == null) ? 0 : getIdCardNumber().hashCode());
        result = prime * result + ((getIdCardFrontUrl() == null) ? 0 : getIdCardFrontUrl().hashCode());
        result = prime * result + ((getIdCardBackUrl() == null) ? 0 : getIdCardBackUrl().hashCode());
        result = prime * result + ((getVerificationStatus() == null) ? 0 : getVerificationStatus().hashCode());
        result = prime * result + ((getFailureReason() == null) ? 0 : getFailureReason().hashCode());
        result = prime * result + ((getSubmitTime() == null) ? 0 : getSubmitTime().hashCode());
        result = prime * result + ((getCompleteTime() == null) ? 0 : getCompleteTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", realName=").append(realName);
        sb.append(", idCardNumber=").append(idCardNumber);
        sb.append(", idCardFrontUrl=").append(idCardFrontUrl);
        sb.append(", idCardBackUrl=").append(idCardBackUrl);
        sb.append(", verificationStatus=").append(verificationStatus);
        sb.append(", failureReason=").append(failureReason);
        sb.append(", submitTime=").append(submitTime);
        sb.append(", completeTime=").append(completeTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}