package com.zheng.aicommunitybackend.tool;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class DateTimeTools {
    @Tool(description = "查询当前时间")
    String getTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.toString();
    }
}