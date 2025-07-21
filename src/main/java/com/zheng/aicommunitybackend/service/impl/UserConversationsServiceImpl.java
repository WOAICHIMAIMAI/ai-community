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
        // 1. 如果是单聊，检查是否已存在相同成员的会话
        if (dto.getConversationType() == 1 && dto.getMemberIds() != null && dto.getMemberIds().size() == 1) {
            String existingConversationId = findExistingPrivateConversation(userId, dto.getMemberIds().get(0));
            if (existingConversationId != null) {
                return existingConversationId;
            }
        }

        // 2. 生成会话ID
        String conversationId = UUID.randomUUID().toString().replace("-", "");
        
        // 2. 创建会话记录
        UserConversations conversation = new UserConversations();
        conversation.setId(conversationId);
        conversation.setConversationName(dto.getConversationName()); // 使用 conversationName 字段
        conversation.setTitle(dto.getConversationName()); // 同时设置 title 字段作为备用
        conversation.setConversationType(dto.getConversationType());
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
        
        // 4. 批量获取私聊会话的对方用户信息
        Map<String, Users> otherUsersMap = batchGetOtherUsersForPrivateChats(conversationIds, userId);

        // 5. 转换为VO并填充额外信息
        List<ConversationVO> result = new ArrayList<>();
        for (UserConversations conversation : conversations) {
            ConversationVO vo = new ConversationVO();
            BeanUtils.copyProperties(conversation, vo);

            // 手动设置conversationId，确保字段正确映射
            vo.setConversationId(conversation.getId());

            // 填充成员数量
            vo.setMemberCount(getMemberCount(conversation.getId()));

            // 填充最后一条消息
            fillLastMessage(vo, conversation.getId());

            // 填充未读消息数
            vo.setUnreadCount(getUnreadCount(conversation.getId(), userId));

            // 动态设置会话头像（使用批量查询的结果）
            setConversationAvatarOptimized(vo, conversation, userId, otherUsersMap);

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

        // 手动设置conversationId，确保字段正确映射
        vo.setConversationId(conversation.getId());
        
        // 4. 填充成员信息
        vo.setMembers(getConversationMembers(conversationId));
        vo.setMemberCount(vo.getMembers().size());
        
        // 5. 填充最后一条消息
        fillLastMessage(vo, conversationId);

        // 6. 填充未读消息数
        vo.setUnreadCount(getUnreadCount(conversationId, userId));

        // 7. 动态设置会话头像
        if (conversation.getConversationType() == 1) {
            // 为私聊会话设置对方用户的头像
            Map<String, Users> otherUsersMap = batchGetOtherUsersForPrivateChats(
                Collections.singletonList(conversationId), userId);
            setConversationAvatarOptimized(vo, conversation, userId, otherUsersMap);
        } else {
            // 群聊使用会话自身的头像
            vo.setAvatarUrl(conversation.getAvatarUrl());
        }

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

    @Override
    @Transactional
    public void updateLastMessage(String conversationId, Long messageId, Date messageTime) {
        UserConversations conversation = getById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(messageId);
            conversation.setLastMessageTime(messageTime);
            conversation.setUpdateTime(new Date());
            updateById(conversation);
        }
    }

    @Override
    @Transactional
    public String findOrCreatePrivateConversation(Long userId1, Long userId2) {
        // 1. 检查参数有效性
        if (userId1.equals(userId2)) {
            throw new BaseException("不能与自己创建会话");
        }

        // 2. 查找已存在的私聊会话
        String existingConversationId = findExistingPrivateConversation(userId1, userId2);
        if (existingConversationId != null) {
            return existingConversationId;
        }

        // 3. 创建新的私聊会话
        // 查询目标用户信息用于生成会话名称
        Users targetUser = usersMapper.selectById(userId2);
        if (targetUser == null) {
            throw new BaseException("目标用户不存在");
        }

        Users currentUser = usersMapper.selectById(userId1);
        if (currentUser == null) {
            throw new BaseException("当前用户不存在");
        }

        // 创建会话DTO
        ConversationCreateDTO dto = new ConversationCreateDTO();
        String targetUserName = targetUser.getNickname() != null && !targetUser.getNickname().isEmpty()
                               ? targetUser.getNickname()
                               : targetUser.getUsername();
        dto.setConversationName("与" + targetUserName + "的聊天");
        dto.setConversationType(1); // 单聊
        dto.setMemberIds(List.of(userId2));

        return createConversation(dto, userId1);
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
        // 1. 查询会话中的所有消息总数
        LambdaQueryWrapper<UserChatMessages> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(UserChatMessages::getConversationId, conversationId)
                     .eq(UserChatMessages::getStatus, 1);
        Long totalMessages = userChatMessagesMapper.selectCount(messageWrapper);

        // 2. 查询用户已读的消息数
        String sql = "SELECT COUNT(DISTINCT umrr.message_id) FROM user_message_read_records umrr " +
                    "INNER JOIN user_chat_messages ucm ON umrr.message_id = ucm.id " +
                    "WHERE umrr.user_id = ? AND umrr.conversation_id = ? AND ucm.status = 1";

        // 使用原生SQL查询已读消息数
        Long readMessages = userChatMessagesMapper.selectCount(
            new LambdaQueryWrapper<UserChatMessages>()
                .inSql(UserChatMessages::getId,
                      "SELECT message_id FROM user_message_read_records WHERE user_id = " + userId +
                      " AND conversation_id = '" + conversationId + "'")
                .eq(UserChatMessages::getConversationId, conversationId)
                .eq(UserChatMessages::getStatus, 1)
        );

        return Math.max(0, (int)(totalMessages - readMessages));
    }

    /**
     * 获取会话成员列表
     */
    private List<ConversationMemberVO> getConversationMembers(String conversationId) {
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMembers::getConversationId, conversationId)
               .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> members = userConversationMembersMapper.selectList(wrapper);

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        List<Long> userIds = members.stream()
                .map(UserConversationMembers::getUserId)
                .collect(Collectors.toList());
        List<Users> users = usersMapper.selectBatchIds(userIds);
        Map<Long, Users> userMap = users.stream()
                .collect(Collectors.toMap(Users::getId, user -> user));

        List<ConversationMemberVO> result = new ArrayList<>();
        for (UserConversationMembers member : members) {
            Users user = userMap.get(member.getUserId());
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

    /**
     * 查找已存在的私聊会话
     */
    private String findExistingPrivateConversation(Long userId1, Long userId2) {
        // 1. 查找用户1参与的所有单聊会话
        LambdaQueryWrapper<UserConversationMembers> user1Wrapper = new LambdaQueryWrapper<>();
        user1Wrapper.eq(UserConversationMembers::getUserId, userId1)
                   .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> user1Conversations = userConversationMembersMapper.selectList(user1Wrapper);

        if (user1Conversations.isEmpty()) {
            return null;
        }

        // 2. 查找用户2参与的所有单聊会话
        LambdaQueryWrapper<UserConversationMembers> user2Wrapper = new LambdaQueryWrapper<>();
        user2Wrapper.eq(UserConversationMembers::getUserId, userId2)
                   .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> user2Conversations = userConversationMembersMapper.selectList(user2Wrapper);

        if (user2Conversations.isEmpty()) {
            return null;
        }

        // 3. 找到两个用户共同参与的会话ID
        Set<String> user1ConversationIds = user1Conversations.stream()
                .map(UserConversationMembers::getConversationId)
                .collect(Collectors.toSet());

        Set<String> user2ConversationIds = user2Conversations.stream()
                .map(UserConversationMembers::getConversationId)
                .collect(Collectors.toSet());

        user1ConversationIds.retainAll(user2ConversationIds);

        if (user1ConversationIds.isEmpty()) {
            return null;
        }

        // 4. 检查这些会话中是否有单聊且只有两个成员的
        for (String conversationId : user1ConversationIds) {
            // 检查会话类型和状态
            LambdaQueryWrapper<UserConversations> conversationWrapper = new LambdaQueryWrapper<>();
            conversationWrapper.eq(UserConversations::getId, conversationId)
                              .eq(UserConversations::getConversationType, 1) // 单聊
                              .eq(UserConversations::getStatus, 1); // 正常状态
            UserConversations conversation = getOne(conversationWrapper);

            if (conversation != null) {
                // 检查成员数量是否为2
                LambdaQueryWrapper<UserConversationMembers> memberCountWrapper = new LambdaQueryWrapper<>();
                memberCountWrapper.eq(UserConversationMembers::getConversationId, conversationId)
                                 .eq(UserConversationMembers::getStatus, 1);
                Long memberCount = userConversationMembersMapper.selectCount(memberCountWrapper);

                if (memberCount == 2) {
                    return conversationId;
                }
            }
        }

        return null;
    }

    /**
     * 动态设置会话头像
     * - 私聊会话：显示对方用户的头像
     * - 群聊会话：使用会话自身的头像
     */
    private void setConversationAvatar(ConversationVO vo, UserConversations conversation, Long currentUserId) {
        if (conversation.getConversationType() == 1) {
            // 私聊会话：动态获取对方用户的头像
            setPrivateChatAvatar(vo, conversation.getId(), currentUserId);
        } else {
            // 群聊会话：使用会话自身的头像
            vo.setAvatarUrl(conversation.getAvatarUrl());
        }
    }

    /**
     * 设置私聊会话的头像（使用对方用户的头像）
     */
    private void setPrivateChatAvatar(ConversationVO vo, String conversationId, Long currentUserId) {
        // 1. 获取会话中的所有成员
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMembers::getConversationId, conversationId)
               .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> members = userConversationMembersMapper.selectList(wrapper);

        // 2. 找到对方用户（不是当前用户的那个成员）
        for (UserConversationMembers member : members) {
            if (!member.getUserId().equals(currentUserId)) {
                // 3. 查询对方用户信息
                Users otherUser = usersMapper.selectById(member.getUserId());
                if (otherUser != null) {
                    // 4. 设置会话头像为对方用户的头像
                    vo.setAvatarUrl(otherUser.getAvatarUrl());

                    // 5. 动态设置会话名称为对方的昵称
                    String displayName = otherUser.getNickname() != null && !otherUser.getNickname().isEmpty()
                                       ? otherUser.getNickname()
                                       : otherUser.getUsername();
                    vo.setConversationName(displayName);

                    // 6. 添加调试日志
                    System.out.println("设置私聊头像 - 会话ID: " + conversationId +
                                     ", 当前用户: " + currentUserId +
                                     ", 对方用户: " + otherUser.getId() +
                                     ", 对方头像: " + otherUser.getAvatarUrl() +
                                     ", 对方昵称: " + displayName);
                }
                break; // 私聊只有两个成员，找到对方就退出
            }
        }
    }

    /**
     * 批量获取私聊会话的对方用户信息
     */
    private Map<String, Users> batchGetOtherUsersForPrivateChats(List<String> conversationIds, Long currentUserId) {
        Map<String, Users> result = new HashMap<>();

        if (conversationIds.isEmpty()) {
            return result;
        }

        // 1. 批量查询所有会话的成员
        LambdaQueryWrapper<UserConversationMembers> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserConversationMembers::getConversationId, conversationIds)
               .eq(UserConversationMembers::getStatus, 1);
        List<UserConversationMembers> allMembers = userConversationMembersMapper.selectList(wrapper);

        // 2. 按会话ID分组
        Map<String, List<UserConversationMembers>> membersByConversation = allMembers.stream()
                .collect(Collectors.groupingBy(UserConversationMembers::getConversationId));

        // 3. 收集所有对方用户ID
        Set<Long> otherUserIds = new HashSet<>();
        for (Map.Entry<String, List<UserConversationMembers>> entry : membersByConversation.entrySet()) {
            for (UserConversationMembers member : entry.getValue()) {
                if (!member.getUserId().equals(currentUserId)) {
                    otherUserIds.add(member.getUserId());
                }
            }
        }

        // 4. 批量查询用户信息
        if (!otherUserIds.isEmpty()) {
            List<Users> otherUsers = usersMapper.selectBatchIds(otherUserIds);
            Map<Long, Users> userMap = otherUsers.stream()
                    .collect(Collectors.toMap(Users::getId, user -> user));

            // 5. 建立会话ID到对方用户的映射
            for (Map.Entry<String, List<UserConversationMembers>> entry : membersByConversation.entrySet()) {
                String conversationId = entry.getKey();
                for (UserConversationMembers member : entry.getValue()) {
                    if (!member.getUserId().equals(currentUserId)) {
                        Users otherUser = userMap.get(member.getUserId());
                        if (otherUser != null) {
                            result.put(conversationId, otherUser);
                        }
                        break; // 私聊只有一个对方用户
                    }
                }
            }
        }

        return result;
    }

    /**
     * 优化版本的会话头像设置（使用批量查询结果）
     */
    private void setConversationAvatarOptimized(ConversationVO vo, UserConversations conversation,
                                              Long currentUserId, Map<String, Users> otherUsersMap) {
        if (conversation.getConversationType() == 1) {
            // 私聊会话：使用批量查询的对方用户信息
            Users otherUser = otherUsersMap.get(conversation.getId());
            if (otherUser != null) {
                vo.setAvatarUrl(otherUser.getAvatarUrl());

                // 设置会话名称为对方的昵称
                String displayName = otherUser.getNickname() != null && !otherUser.getNickname().isEmpty()
                                   ? otherUser.getNickname()
                                   : otherUser.getUsername();
                vo.setConversationName(displayName);

                // 添加调试日志
                System.out.println("批量设置私聊头像 - 会话ID: " + conversation.getId() +
                                 ", 当前用户: " + currentUserId +
                                 ", 对方用户: " + otherUser.getId() +
                                 ", 对方头像: " + otherUser.getAvatarUrl() +
                                 ", 对方昵称: " + displayName);
            }
        } else {
            // 群聊会话：使用会话自身的头像
            vo.setAvatarUrl(conversation.getAvatarUrl());
        }
    }
}
