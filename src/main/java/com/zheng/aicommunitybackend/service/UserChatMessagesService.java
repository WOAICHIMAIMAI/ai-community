package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.MessageSendDTO;
import com.zheng.aicommunitybackend.domain.entity.UserChatMessages;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zheng.aicommunitybackend.domain.vo.UserChatMessageVO;

import java.util.List;

/**
* @author ZhengJJ
* @description 针对表【user_chat_messages(用户聊天消息表)】的数据库操作Service
* @createDate 2025-07-21 09:47:53
*/
public interface UserChatMessagesService extends IService<UserChatMessages> {

    /**
     * 发送消息
     */
    Long sendMessage(MessageSendDTO dto, Long userId);

    /**
     * 获取会话消息列表
     */
    List<UserChatMessageVO> getConversationMessages(String conversationId, Long userId, Integer page, Integer size);

    /**
     * 标记消息已读
     */
    void markMessageRead(Long messageId, Long userId);

    /**
     * 删除消息
     */
    void deleteMessage(Long messageId, Long userId);
}