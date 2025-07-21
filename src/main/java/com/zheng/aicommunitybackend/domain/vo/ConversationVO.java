package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ConversationVO {
    
    private String conversationId;
    
    private String conversationName;
    
    private Integer conversationType;
    
    private String description;
    
    private String avatarUrl;
    
    private Integer memberCount;
    
    private String lastMessage;
    
    private Date lastMessageTime;
    
    private Integer unreadCount;
    
    private Date createTime;
    
    private List<ConversationMemberVO> members;
}