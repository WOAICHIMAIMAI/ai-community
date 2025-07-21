package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserChatMessageVO {
    
    private Long id;
    
    private String conversationId;
    
    private Long senderId;
    
    private String senderNickname;
    
    private String senderAvatarUrl;
    
    private String messageType;
    
    private String content;
    
    private String metadata;
    
    private Integer status;
    
    private Integer readCount;
    
    private Boolean isRead; // 当前用户是否已读
    
    private Date createTime;
}