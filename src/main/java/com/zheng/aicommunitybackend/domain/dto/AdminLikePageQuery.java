package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminLikePageQuery extends PageQuery{
    private Long id;               // 点赞记录ID，可选
    private Long userId;           // 用户ID，可选
    private Integer targetType;    // 点赞目标类型（1-帖子，2-评论等），可选
    private Long targetId;         // 点赞目标ID，可选
    private LocalDateTime startTime; // 开始时间，可选
    private LocalDateTime endTime;   // 结束时间，可选
}
