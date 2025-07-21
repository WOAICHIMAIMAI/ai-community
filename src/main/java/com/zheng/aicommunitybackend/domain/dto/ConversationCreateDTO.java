package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Data
public class ConversationCreateDTO {
    
    @NotBlank(message = "会话名称不能为空")
    @Size(max = 50, message = "会话名称不能超过50个字符")
    private String conversationName;
    
    @NotNull(message = "会话类型不能为空")
    private Integer conversationType; // 1-单聊 2-群聊
    
    @Size(max = 200, message = "会话描述不能超过200个字符")
    private String description;
    
    private List<Long> memberIds; // 会话成员ID列表
}