package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.ChatSettingsUpdateDTO;
import com.zheng.aicommunitybackend.domain.entity.UserChatSettings;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author ZhengJJ
* @description 针对表【user_chat_settings(用户聊天设置表)】的数据库操作Service
* @createDate 2025-07-21 09:47:53
*/
public interface UserChatSettingsService extends IService<UserChatSettings> {

    /**
     * 获取用户聊天设置
     */
    UserChatSettings getUserChatSettings(Long userId);

    /**
     * 更新用户聊天设置
     */
    void updateChatSettings(ChatSettingsUpdateDTO dto, Long userId);
}