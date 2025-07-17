package com.zheng.aicommunitybackend.mapper;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageRepository extends CrudRepository<ChatMessageMapper, ChatMessage> {
}