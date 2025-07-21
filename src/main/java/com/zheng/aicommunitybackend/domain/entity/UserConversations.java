package com.zheng.aicommunitybackend.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户会话表
 * @TableName user_conversations
 */
@TableName(value ="user_conversations")
@Data
public class UserConversations {
    /**
     * 会话ID
     */
    @TableId
    private String id;

    /**
     * 会话类型：1-私聊 2-群聊
     */
    private Integer conversationType;

    /**
     * 会话名称
     */
    private String conversationName;

    /**
     * 会话标题(群聊时使用)
     */
    private String title;

    /**
     * 会话头像
     */
    private String avatarUrl;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息时间
     */
    private Date lastMessageTime;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 状态：0-已解散 1-正常
     */
    private Integer status;

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
        UserConversations other = (UserConversations) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getConversationType() == null ? other.getConversationType() == null : this.getConversationType().equals(other.getConversationType()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getAvatarUrl() == null ? other.getAvatarUrl() == null : this.getAvatarUrl().equals(other.getAvatarUrl()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getLastMessageId() == null ? other.getLastMessageId() == null : this.getLastMessageId().equals(other.getLastMessageId()))
            && (this.getLastMessageTime() == null ? other.getLastMessageTime() == null : this.getLastMessageTime().equals(other.getLastMessageTime()))
            && (this.getMemberCount() == null ? other.getMemberCount() == null : this.getMemberCount().equals(other.getMemberCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getConversationType() == null) ? 0 : getConversationType().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getAvatarUrl() == null) ? 0 : getAvatarUrl().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
        result = prime * result + ((getLastMessageId() == null) ? 0 : getLastMessageId().hashCode());
        result = prime * result + ((getLastMessageTime() == null) ? 0 : getLastMessageTime().hashCode());
        result = prime * result + ((getMemberCount() == null) ? 0 : getMemberCount().hashCode());
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
        sb.append(", conversationType=").append(conversationType);
        sb.append(", title=").append(title);
        sb.append(", avatarUrl=").append(avatarUrl);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", lastMessageId=").append(lastMessageId);
        sb.append(", lastMessageTime=").append(lastMessageTime);
        sb.append(", memberCount=").append(memberCount);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}