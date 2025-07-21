package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.ConversationCreateDTO;
import com.zheng.aicommunitybackend.domain.entity.UserConversationMembers;
import com.zheng.aicommunitybackend.domain.entity.UserConversations;
import com.zheng.aicommunitybackend.domain.entity.UserChatMessages;
import com.zheng.aicommunitybackend.domain.entity.Users;
import com.zheng.aicommunitybackend.domain.vo.ConversationMemberVO;
import com.zheng.aicommunitybackend.domain.vo.ConversationVO;
import com.zheng.aicommunitybackend.exception.BaseException;
import com.zheng.aicommunitybackend.mapper.UserConversationMembersMapper;
import com.zheng.aicommunitybackend.mapper.UserConversationsMapper;
import com.zheng.aicommunitybackend.mapper.UserChatMessagesMapper;
import com.zheng.aicommunitybackend.mapper.UsersMapper;
import com.zheng.aicommunitybackend.service.UserConversationsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户会话服务实现
 */
@Service
public class UserConversationsServiceImpl extends ServiceImpl<UserConversationsMapper, UserConversations>
        implements UserConversationsService {

    @Resource
    private UserConversationMembersMapper userConversationMembersMapper;
    
    @Resource
    private UserChatMessagesMapper userChatMessagesMapper;
    
    @Resource
    private UsersMapper usersMapper;

    @Override
    @Transactional
    public String createConversation(ConversationCreateDTO dto, Long userId) {
        // 1. 生成会话ID
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        
        // 2. 创建会话记录
        UserConversations conversation = new UserConversations();
        conversation.setId(conversationId);
        conversation.setId(dto.getConversationName());
        conversation.setConversationType(dto.getConversationType());
        conversation.setId(dto.getDescription());
        conversation.setCreatorId(userId);
        conversation.setCreateTime(new Date());
        conversation.setUpdateTime(new Date());
        conversation.setStatus(1); // 1-正常
        
        save(conversation);
        
        // 3. 添加创建者为会话成员
        UserConversationMembers creatorMember = new UserConversationMembers();
        creatorMember.setConversationId(conversationId);
        creatorMember.setUserId(userId);
        creatorMember.setRole(dto.getConversationType() == 2 ? 3 : 1); // 群聊创建者为群主，单聊为普通成员
        creatorMember.setJoinTime(new Date());
        creatorMember.setStatus(1);
        userConversationMembersMapper.insert(creatorMember);
        
        // 4. 添加其他成员
        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            for (Long memberId : dto.getMemberIds()) {
                if (!memberId.equals(userId)) { // 避免重复添加创建者
                    UserConversationMembers member = new UserConversationMembers();
                    member.setConversationId(conversationId);
                    member.setUserId(memberId);
                    member.setRole(1); // 普通成员
                    member.setJoinTime(new Date());
                    member.setStatus(1);
                    userConversationMembersMapper.insert(member);
                }
            }
        }
        
        return conversationId;
    }

    @Override
    public List<ConversationVO> getUserConversations(Long userId) {
        // 1. 查询用户参与的所有会话
        LambdaQueryWrapper<UserConversationMembers> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(UserConversationMembers::getUserId, userId)
                    .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> memberList = userConversationMembersMapper.selectList(memberWrapper);
        
        if (memberList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取会话ID列表
        List<String> conversationIds = memberList.stream()
                .map(UserConversationMembers::getConversationId)
                .collect(Collectors.toList());
        
        // 3. 查询会话详情
        LambdaQueryWrapper<UserConversations> conversationWrapper = new LambdaQueryWrapper<>();
        conversationWrapper.in(UserConversations::getId, conversationIds)
                          .eq(UserConversations::getStatus, 1)
                          .orderByDesc(UserConversations::getUpdateTime);
        List<UserConversations> conversations = list(conversationWrapper);
        
        // 4. 转换为VO并填充额外信息
        List<ConversationVO> result = new ArrayList<>();
        for (UserConversations conversation : conversations) {
            ConversationVO vo = new ConversationVO();
            BeanUtils.copyProperties(conversation, vo);
            
            // 填充成员数量
            vo.setMemberCount(getMemberCount(conversation.getId()));
            
            // 填充最后一条消息
            fillLastMessage(vo, conversation.getId());
            
            // 填充未读消息数
            vo.setUnreadCount(getUnreadCount(conversation.getId(), userId));
            
            result.add(vo);
        }
        
        // 5. 按最后消息时间排序
        result.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        
        return result;
    }

    @Override
    public ConversationVO getConversationDetail(String conversationId, Long userId) {
        // 1. 验证用户是否为会话成员
        if (!isConversationMember(conversationId, userId)) {
            throw new BaseException("您不是该会话的成员");
        }
        
        // 2. 查询会话信息
        LambdaQueryWrapper<UserConversations> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversations::getId, conversationId)
               .eq(UserConversations::getStatus, 1);
        UserConversations conversation = getOne(wrapper);
        
        if (conversation == null) {
            throw new BaseException("会话不存在");
        }
        
        // 3. 转换为VO
        ConversationVO vo = new ConversationVO();
        BeanUtils.copyProperties(conversation, vo);
        
        // 4. 填充成员信息
        vo.setMembers(getConversationMembers(conversationId));
        vo.setMemberCount(vo.getMembers().size());
        
        // 5. 填充最后一条消息
        fillLastMessage(vo, conversationId);
        
        // 6. 填充未读消息数
        vo.setUnreadCount(getUnreadCount(conversationId, userId));
        
        return vo;
    }

    @Override
    @Transactional
    public void deleteConversation(String conversationId, Long userId) {
        // 1. 验证用户是否为会话成员
        if (!isConversationMember(conversationId, userId)) {
            throw new BaseException("您不是该会话的成员");
        }
        
        // 2. 查询会话信息
        LambdaQueryWrapper<UserConversations> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversations::getId, conversationId);
        UserConversations conversation = getOne(wrapper);
        
        if (conversation == null) {
            throw new BaseException("会话不存在");
        }
        
        // 3. 检查权限（只有创建者可以删除会话）
        if (!conversation.getCreatorId().equals(userId)) {
            throw new BaseException("只有会话创建者可以删除会话");
        }
        
        // 4. 软删除会话
        conversation.setStatus(0);
        conversation.setUpdateTime(new Date());
        updateById(conversation);
        
        // 5. 删除所有成员关系
        LambdaQueryWrapper<UserConversationMembers> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(UserConversationMembers::getConversationId, conversationId);
        List<UserConversationMembers> members = userConversationMembersMapper.selectList(memberWrapper);
        
        for (UserConversationMembers member : members) {
            member.setStatus(0);
            userConversationMembersMapper.updateById(member);
        }
    }

    @Override
    public boolean isConversationMember(String conversationId, Long userId) {
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMembers::getConversationId, conversationId)
               .eq(UserConversationMembers::getUserId, userId)
               .eq(UserConversationMembers::getStatus, 1);
        return userConversationMembersMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取会话成员数量
     */
    private Integer getMemberCount(String conversationId) {
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMembers::getConversationId, conversationId)
               .eq(UserConversationMembers::getStatus, 1);
        return userConversationMembersMapper.selectCount(wrapper).intValue();
    }

    /**
     * 填充最后一条消息信息
     */
    private void fillLastMessage(ConversationVO vo, String conversationId) {
        LambdaQueryWrapper<UserChatMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserChatMessages::getConversationId, conversationId)
               .eq(UserChatMessages::getStatus, 1)
               .orderByDesc(UserChatMessages::getCreateTime)
               .last("LIMIT 1");
        
        UserChatMessages lastMessage = userChatMessagesMapper.selectOne(wrapper);
        if (lastMessage != null) {
            vo.setLastMessage(lastMessage.getContent());
            vo.setLastMessageTime(lastMessage.getCreateTime());
        }
    }

    /**
     * 获取未读消息数
     */
    private Integer getUnreadCount(String conversationId, Long userId) {
        // 这里需要根据消息已读记录表来计算，暂时返回0
        // TODO: 实现未读消息数统计逻辑
        return 0;
    }

    /**
     * 获取会话成员列表
     */
    private List<ConversationMemberVO> getConversationMembers(String conversationId) {
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMembers::getConversationId, conversationId)
               .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> members = userConversationMembersMapper.selectList(wrapper);
        
        List<ConversationMemberVO> result = new ArrayList<>();
        for (UserConversationMembers member : members) {
            // 查询用户信息
            Users user = usersMapper.selectById(member.getUserId());
            if (user != null) {
                ConversationMemberVO memberVO = new ConversationMemberVO();
                memberVO.setUserId(user.getId());
                memberVO.setNickname(user.getNickname());
                memberVO.setAvatarUrl(user.getAvatarUrl());
                memberVO.setRole(member.getRole());
                memberVO.setJoinTime(member.getJoinTime());
                result.add(memberVO);
            }
        }
        
        return result;
    }
}
