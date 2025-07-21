package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class MessageSendDTO {
    
    @NotNull(message = "会话ID不能为空")
    private String conversationId;
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String content;
    
    @NotBlank(message = "消息类型不能为空")
    private String messageType; // text/image/voice/video/location/file
    
    private String metadata; // 元数据JSON字符串
}