package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.ai.chetmemory.MessageConverter;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;
import com.zheng.aicommunitybackend.mapper.ChatMessageMapper;
import com.zheng.aicommunitybackend.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    @Override
    public List<String> getChatIds(Long userId) {
        List<ChatMessage> chatMessages = lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .list();
        return chatMessages == null ? Collections.EMPTY_LIST : chatMessages.stream().map(ChatMessage::getConversationId).distinct().sorted().collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> list(String chatId, Integer lastN) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        // 查询最近的 lastN 条消息
        queryWrapper.eq(ChatMessage::getConversationId, chatId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last(lastN > 0, "LIMIT " + lastN);


        List<ChatMessage> chatMessages = list(queryWrapper);

        // 按照时间顺序返回
        if (!chatMessages.isEmpty()) {
            Collections.reverse(chatMessages);
        }
        return chatMessages;
    }


}
