package com.zheng.aicommunitybackend.ai.chetmemory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;
import com.zheng.aicommunitybackend.mapper.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DatabaseChatMemory implements ChatMemory {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Long userId = UserContext.getUserId();
        List<ChatMessage> chatMessages = messages.stream()
                .map(message -> MessageConverter.toChatMessage(message, conversationId, userId))
                .collect(Collectors.toList());
        
        chatMessageRepository.saveBatch(chatMessages, chatMessages.size());
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        // 查询最近的 lastN 条消息
        queryWrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last(lastN > 0, "LIMIT " + lastN);
        
        List<ChatMessage> chatMessages = chatMessageRepository.list(queryWrapper);
        
        // 按照时间顺序返回
        if (!chatMessages.isEmpty()) {
            Collections.reverse(chatMessages);
        }
        
        return chatMessages
                .stream()
                .map(MessageConverter::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatMessage::getConversationId, conversationId);
        chatMessageRepository.remove(queryWrapper);
    }

}
