package com.zheng.aicommunitybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    List<String> getChatIds(Long userId);

    List<ChatMessage> list(String chatId, Integer lastK);
}
