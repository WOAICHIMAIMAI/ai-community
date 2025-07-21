package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.UserConversationMembers;
import com.zheng.aicommunitybackend.service.UserConversationMembersService;
import com.zheng.aicommunitybackend.mapper.UserConversationMembersMapper;
import org.springframework.stereotype.Service;

/**
* @author ZhengJJ
* @description 针对表【user_conversation_members(用户会话成员表)】的数据库操作Service实现
* @createDate 2025-07-21 09:47:53
*/
@Service
public class UserConversationMembersServiceImpl extends ServiceImpl<UserConversationMembersMapper, UserConversationMembers>
    implements UserConversationMembersService{

}




