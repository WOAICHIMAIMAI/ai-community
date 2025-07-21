package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.entity.UserMessageReadRecords;
import com.zheng.aicommunitybackend.service.UserMessageReadRecordsService;
import com.zheng.aicommunitybackend.mapper.UserMessageReadRecordsMapper;
import org.springframework.stereotype.Service;

/**
* @author ZhengJJ
* @description 针对表【user_message_read_records(用户消息已读记录表)】的数据库操作Service实现
* @createDate 2025-07-21 09:47:53
*/
@Service
public class UserMessageReadRecordsServiceImpl extends ServiceImpl<UserMessageReadRecordsMapper, UserMessageReadRecords>
    implements UserMessageReadRecordsService{

}




