package com.zheng.aicommunitybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zheng.aicommunitybackend.domain.dto.ChatSettingsUpdateDTO;
import com.zheng.aicommunitybackend.domain.entity.UserChatSettings;
import com.zheng.aicommunitybackend.mapper.UserChatSettingsMapper;
import com.zheng.aicommunitybackend.service.UserChatSettingsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户聊天设置服务实现
 */
@Service
public class UserChatSettingsServiceImpl extends ServiceImpl<UserChatSettingsMapper, UserChatSettings>
        implements UserChatSettingsService {

    @Override
    public UserChatSettings getUserChatSettings(Long userId) {
        // 1. 查询用户设置
        LambdaQueryWrapper<UserChatSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserChatSettings::getUserId, userId);
        UserChatSettings settings = getOne(wrapper);
        
        // 2. 如果不存在则创建默认设置
        if (settings == null) {
            settings = createDefaultSettings(userId);
            save(settings);
        }
        
        return settings;
    }

    @Override
    public void updateChatSettings(ChatSettingsUpdateDTO dto, Long userId) {
        // 1. 获取现有设置
        UserChatSettings settings = getUserChatSettings(userId);
        
        // 2. 更新设置
        if (dto.getAllowStrangerMessage() != null) {
            settings.setAllowStrangerMessage(dto.getAllowStrangerMessage());
        }
        if (dto.getMessageNotification() != null) {
            settings.setMessageNotification(dto.getMessageNotification());
        }
        if (dto.getNotificationSound() != null) {
            settings.setNotificationSound(dto.getNotificationSound());
        }
        if (dto.getVibration() != null) {
            settings.setVibration(dto.getVibration());
        }
        if (dto.getFontSize() != null) {
            settings.setFontSize(dto.getFontSize());
        }
        if (dto.getTheme() != null) {
            settings.setTheme(dto.getTheme());
        }
        
        settings.setUpdateTime(new Date());
        updateById(settings);
    }

    /**
     * 创建默认设置
     */
    private UserChatSettings createDefaultSettings(Long userId) {
        UserChatSettings settings = new UserChatSettings();
        settings.setUserId(userId);
        settings.setAllowStrangerMessage(1); // 默认允许陌生人消息
        settings.setMessageNotification(1); // 默认开启消息通知
        settings.setNotificationSound(1); // 默认开启通知声音
        settings.setVibration(1); // 默认开启震动
        settings.setFontSize(14); // 默认字体大小
        settings.setTheme("light"); // 默认浅色主题
        settings.setCreateTime(new Date());
        settings.setUpdateTime(new Date());
        return settings;
    }
}
