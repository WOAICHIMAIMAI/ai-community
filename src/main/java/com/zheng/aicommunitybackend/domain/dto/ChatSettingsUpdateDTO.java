package com.zheng.aicommunitybackend.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
public class ChatSettingsUpdateDTO {
    
    private Integer allowStrangerMessage; // 0-否 1-是
    
    private Integer messageNotification; // 0-否 1-是
    
    private Integer notificationSound; // 0-否 1-是
    
    private Integer vibration; // 0-否 1-是
    
    @Min(value = 12, message = "字体大小不能小于12")
    @Max(value = 20, message = "字体大小不能大于20")
    private Integer fontSize;
    
    private String theme; // light/dark
}