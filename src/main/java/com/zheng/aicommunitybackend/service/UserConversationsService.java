package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.ConversationCreateDTO;
import com.zheng.aicommunitybackend.domain.vo.ConversationVO;

import java.util.List;

/**
 * 用户会话服务接口
 */
public interface UserConversationsService {

    /**
     * 创建会话
     */
    String createConversation(ConversationCreateDTO dto, Long userId);

    /**
     * 获取用户会话列表
     */
    List<ConversationVO> getUserConversations(Long userId);

    /**
     * 获取会话详情
     */
    ConversationVO getConversationDetail(String conversationId, Long userId);

    /**
     * 删除会话
     */
    void deleteConversation(String conversationId, Long userId);

    /**
     * 验证用户是否为会话成员
     */
    boolean isConversationMember(String conversationId, Long userId);
}