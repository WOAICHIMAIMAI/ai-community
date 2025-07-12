package com.zheng.aicommunitybackend.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminLikeVO {
    private Long id;              // 点赞记录ID
    private Long userId;          // 用户ID
    private String username;      // 用户名
    private String nickname;      // 用户昵称
    private Integer targetType;   // 点赞目标类型
    private Long targetId;        // 点赞目标ID
    private String targetTitle;   // 点赞目标标题/内容预览
    private LocalDateTime createTime; // 点赞时间
}
