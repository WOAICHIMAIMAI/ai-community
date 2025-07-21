package com.zheng.aicommunitybackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.ai.chetmemory.MessageConverter;
import com.zheng.aicommunitybackend.constants.RedisKeys;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;
import com.zheng.aicommunitybackend.mapper.ChatMessageMapper;
import com.zheng.aicommunitybackend.service.ChatMessageService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<String> getChatIds(Long userId) {
        List<ChatMessage> chatMessages = lambdaQuery()
                .eq(ChatMessage::getUserId, userId)
                .list();
        return chatMessages == null ? Collections.EMPTY_LIST : chatMessages.stream().map(ChatMessage::getConversationId).distinct().sorted().collect(Collectors.toList());
    }

    @Override
    public List<ChatMessage> list(String chatId, Integer lastN) {
        // 先查询缓存中是否存在数据
        String chatMessagesRedis = (String) redisTemplate.opsForValue().get(String.format(RedisKeys.AI_CHAT_CONVERSATION, chatId));
        if(StrUtil.isNotEmpty(chatMessagesRedis)){
            return JSONUtil.toList(chatMessagesRedis, ChatMessage.class);
        }
        // 如果不存在数据 查询数据库中数据 并且缓存
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
        if(!lastN.equals(1)){
            redisTemplate.opsForValue().set(String.format(RedisKeys.AI_CHAT_CONVERSATION, chatId), JSONUtil.toJsonStr(chatMessages));
        }
        return chatMessages;
    }


}
