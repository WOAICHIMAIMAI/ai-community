package com.zheng.aicommunitybackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 点赞/取消点赞消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeActionMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 目标类型：1-帖子 2-评论
     */
    private Integer targetType;
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 操作类型：0-取消点赞 1-点赞
     */
    private Integer actionType;
} 