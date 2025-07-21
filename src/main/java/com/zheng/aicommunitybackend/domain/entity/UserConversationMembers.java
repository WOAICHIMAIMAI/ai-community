package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户会话成员表
 * @TableName user_conversation_members
 */
@TableName(value ="user_conversation_members")
@Data
public class UserConversationMembers {
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 成员用户ID
     */
    private Long userId;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 角色：0-普通成员 1-管理员 2-创建者
     */
    private Integer role;

    /**
     * 在会话中的昵称
     */
    private String nickname;

    /**
     * 是否静音通知(0-否 1-是)
     */
    private Integer muteNotification;

    /**
     * 最后阅读的消息ID
     */
    private Long lastReadMessageId;

    /**
     * 状态：0-已退出 1-正常
     */
    private Integer status;

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
        UserConversationMembers other = (UserConversationMembers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getConversationId() == null ? other.getConversationId() == null : this.getConversationId().equals(other.getConversationId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getRole() == null ? other.getRole() == null : this.getRole().equals(other.getRole()))
            && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
            && (this.getMuteNotification() == null ? other.getMuteNotification() == null : this.getMuteNotification().equals(other.getMuteNotification()))
            && (this.getLastReadMessageId() == null ? other.getLastReadMessageId() == null : this.getLastReadMessageId().equals(other.getLastReadMessageId()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getConversationId() == null) ? 0 : getConversationId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getMuteNotification() == null) ? 0 : getMuteNotification().hashCode());
        result = prime * result + ((getLastReadMessageId() == null) ? 0 : getLastReadMessageId().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", conversationId=").append(conversationId);
        sb.append(", userId=").append(userId);
        sb.append(", joinTime=").append(joinTime);
        sb.append(", role=").append(role);
        sb.append(", nickname=").append(nickname);
        sb.append(", muteNotification=").append(muteNotification);
        sb.append(", lastReadMessageId=").append(lastReadMessageId);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}