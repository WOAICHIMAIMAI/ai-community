package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.ChatSettingsUpdateDTO;
import com.zheng.aicommunitybackend.domain.entity.UserChatSettings;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.UserChatSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户聊天设置控制器
 */
@RestController
@RequestMapping("/user/chat")
@Tag(name = "用户聊天设置接口", description = "用户聊天设置相关接口")
public class UserChatSettingsController {

    @Resource
    private UserChatSettingsService userChatSettingsService;

    /**
     * 获取聊天设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取聊天设置", description = "获取当前用户的聊天设置")
    public Result<UserChatSettings> getChatSettings() {
        Long userId = UserContext.getUserId();
        UserChatSettings settings = userChatSettingsService.getUserChatSettings(userId);
        return Result.success(settings);
    }

    /**
     * 更新聊天设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新聊天设置", description = "更新当前用户的聊天设置")
    public Result<Void> updateChatSettings(@RequestBody @Validated ChatSettingsUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        userChatSettingsService.updateChatSettings(dto, userId);
        return Result.success();
    }
}