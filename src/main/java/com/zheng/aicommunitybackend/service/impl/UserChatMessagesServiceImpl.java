package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.MessageSendDTO;
import com.zheng.aicommunitybackend.domain.entity.UserChatMessages;
import com.zheng.aicommunitybackend.domain.entity.UserMessageReadRecords;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.vo.UserChatMessageVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.UserChatMessagesMapper;
import com.zheng.aicommunitybackend.mapper.UserMessageReadRecordsMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.UserChatMessagesService;
import com.zheng.aicommunitybackend.service.UserConversationsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户聊天消息服务实现
 */
@Service
public class UserChatMessagesServiceImpl extends ServiceImpl<UserChatMessagesMapper, UserChatMessages>
        implements UserChatMessagesService {

    @Resource
    private UserConversationsService userConversationsService;
    
    @Resource
    private UserMessageReadRecordsMapper userMessageReadRecordsMapper;
    
    @Resource
    private UsersMapper usersMapper;

    @Override
    @Transactional
    public Long sendMessage(MessageSendDTO dto, Long userId) {
        // 1. 验证用户是否为会话成员
        if (!userConversationsService.isConversationMember(dto.getConversationId(), userId)) {
            throw new BaseException("您不是该会话的成员");
        }
        
        // 2. 创建消息记录
        UserChatMessages message = new UserChatMessages();
        message.setConversationId(dto.getConversationId());
        message.setSenderId(userId);
        message.setMessageType(dto.getMessageType());
        message.setContent(dto.getContent());
        message.setMetadata(dto.getMetadata());
        message.setStatus(1); // 1-正常
        message.setReadCount(0);
        message.setCreateTime(new Date());
        
        save(message);
        
        // 3. 创建发送者的已读记录
        UserMessageReadRecords readRecord = new UserMessageReadRecords();
        readRecord.setMessageId(message.getId());
        readRecord.setUserId(userId);
        readRecord.setReadTime(new Date());
        userMessageReadRecordsMapper.insert(readRecord);
        
        return message.getId();
    }

    @Override
    public List<UserChatMessageVO> getConversationMessages(String conversationId, Long userId, Integer page, Integer size) {
        // 1. 验证用户是否为会话成员
        if (!userConversationsService.isConversationMember(conversationId, userId)) {
            throw new BaseException("您不是该会话的成员");
        }
        
        // 2. 分页查询消息
        LambdaQueryWrapper<UserChatMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserChatMessages::getConversationId, conversationId)
               .eq(UserChatMessages::getStatus, 1)
               .orderByDesc(UserChatMessages::getCreateTime);
        
        // 计算分页参数
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        
        List<UserChatMessages> messages = list(wrapper);
        
        // 3. 转换为VO
        List<UserChatMessageVO> result = new ArrayList<>();
        if (!messages.isEmpty()) {
            // 批量查询发送者信息
            List<Long> senderIds = messages.stream()
                    .map(UserChatMessages::getSenderId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<Users> senders = usersMapper.selectBatchIds(senderIds);
            Map<Long, Users> senderMap = senders.stream()
                    .collect(Collectors.toMap(Users::getId, user -> user));
            
            // 批量查询已读状态
            List<Long> messageIds = messages.stream()
                    .map(UserChatMessages::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Boolean> readStatusMap = getReadStatusMap(messageIds, userId);
            
            // 转换为VO
            for (UserChatMessages message : messages) {
                UserChatMessageVO vo = new UserChatMessageVO();
                BeanUtils.copyProperties(message, vo);
                
                // 填充发送者信息
                Users sender = senderMap.get(message.getSenderId());
                if (sender != null) {
                    vo.setSenderNickname(sender.getNickname());
                    vo.setSenderAvatarUrl(sender.getAvatarUrl());
                }
                
                // 填充已读状态
                vo.setIsRead(readStatusMap.getOrDefault(message.getId(), false));
                
                result.add(vo);
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public void markMessageRead(Long messageId, Long userId) {
        // 1. 检查消息是否存在
        UserChatMessages message = getById(messageId);
        if (message == null || message.getStatus() != 1) {
            throw new BaseException("消息不存在");
        }
        
        // 2. 验证用户是否为会话成员
        if (!userConversationsService.isConversationMember(message.getConversationId(), userId)) {
            throw new BaseException("您不是该会话的成员");
        }
        
        // 3. 检查是否已经标记为已读
        LambdaQueryWrapper<UserMessageReadRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessageReadRecords::getMessageId, messageId)
               .eq(UserMessageReadRecords::getUserId, userId);
        
        if (userMessageReadRecordsMapper.selectCount(wrapper) == 0) {
            // 4. 创建已读记录
            UserMessageReadRecords readRecord = new UserMessageReadRecords();
            readRecord.setMessageId(messageId);
            readRecord.setUserId(userId);
            readRecord.setReadTime(new Date());
            userMessageReadRecordsMapper.insert(readRecord);
            
            // 5. 更新消息的已读数量
            message.setReadCount(message.getReadCount() + 1);
            updateById(message);
        }
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        // 1. 检查消息是否存在
        UserChatMessages message = getById(messageId);
        if (message == null || message.getStatus() != 1) {
            throw new BaseException("消息不存在");
        }
        
        // 2. 验证权限（只有发送者可以删除消息）
        if (!message.getSenderId().equals(userId)) {
            throw new BaseException("只能删除自己发送的消息");
        }
        
        // 3. 软删除消息
        message.setStatus(0);
        updateById(message);
    }

    /**
     * 批量获取消息的已读状态
     */
    private Map<Long, Boolean> getReadStatusMap(List<Long> messageIds, Long userId) {
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        
        LambdaQueryWrapper<UserMessageReadRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserMessageReadRecords::getMessageId, messageIds)
               .eq(UserMessageReadRecords::getUserId, userId);
        
        List<UserMessageReadRecords> readRecords = userMessageReadRecordsMapper.selectList(wrapper);
        
        return readRecords.stream()
                .collect(Collectors.toMap(
                        UserMessageReadRecords::getMessageId,
                        record -> true,
                        (existing, replacement) -> existing
                ));
    }
}