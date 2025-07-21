package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户聊天设置表
 * @TableName user_chat_settings
 */
@TableName(value ="user_chat_settings")
@Data
public class UserChatSettings {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否允许陌生人消息(0-否 1-是)
     */
    private Integer allowStrangerMessage;

    /**
     * 是否开启消息通知(0-否 1-是)
     */
    private Integer messageNotification;

    /**
     * 是否开启通知声音(0-否 1-是)
     */
    private Integer notificationSound;

    /**
     * 是否开启震动(0-否 1-是)
     */
    private Integer vibration;

    /**
     * 聊天字体大小(12-20)
     */
    private Integer fontSize;

    /**
     * 聊天主题(light/dark)
     */
    private String theme;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

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
        UserChatSettings other = (UserChatSettings) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAllowStrangerMessage() == null ? other.getAllowStrangerMessage() == null : this.getAllowStrangerMessage().equals(other.getAllowStrangerMessage()))
            && (this.getMessageNotification() == null ? other.getMessageNotification() == null : this.getMessageNotification().equals(other.getMessageNotification()))
            && (this.getNotificationSound() == null ? other.getNotificationSound() == null : this.getNotificationSound().equals(other.getNotificationSound()))
            && (this.getVibration() == null ? other.getVibration() == null : this.getVibration().equals(other.getVibration()))
            && (this.getFontSize() == null ? other.getFontSize() == null : this.getFontSize().equals(other.getFontSize()))
            && (this.getTheme() == null ? other.getTheme() == null : this.getTheme().equals(other.getTheme()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAllowStrangerMessage() == null) ? 0 : getAllowStrangerMessage().hashCode());
        result = prime * result + ((getMessageNotification() == null) ? 0 : getMessageNotification().hashCode());
        result = prime * result + ((getNotificationSound() == null) ? 0 : getNotificationSound().hashCode());
        result = prime * result + ((getVibration() == null) ? 0 : getVibration().hashCode());
        result = prime * result + ((getFontSize() == null) ? 0 : getFontSize().hashCode());
        result = prime * result + ((getTheme() == null) ? 0 : getTheme().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", allowStrangerMessage=").append(allowStrangerMessage);
        sb.append(", messageNotification=").append(messageNotification);
        sb.append(", notificationSound=").append(notificationSound);
        sb.append(", vibration=").append(vibration);
        sb.append(", fontSize=").append(fontSize);
        sb.append(", theme=").append(theme);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}