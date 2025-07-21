package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ConversationMemberVO {
    
    private Long userId;
    
    private String nickname;
    
    private String avatarUrl;
    
    private Integer role; // 1-普通成员 2-管理员 3-群主
    
    private Date joinTime;
}