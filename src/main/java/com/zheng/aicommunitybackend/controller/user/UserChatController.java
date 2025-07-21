package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.MessageSendDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.UserChatMessageVO;
import com.zheng.aicommunitybackend.service.UserChatMessagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户聊天消息控制器
 */
@RestController
@RequestMapping("/user/conversations")
@Tag(name = "用户聊天消息接口", description = "用户聊天消息相关接口")
public class UserChatController {

    @Resource
    private UserChatMessagesService userChatMessagesService;

    /**
     * 发送消息
     */
    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "发送消息", description = "在指定会话中发送消息")
    public Result<Long> sendMessage(@PathVariable String conversationId, 
                                   @RequestBody @Validated MessageSendDTO dto) {
        Long userId = UserContext.getUserId();
        dto.setConversationId(conversationId);
        Long messageId = userChatMessagesService.sendMessage(dto, userId);
        return Result.success(messageId);
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "获取会话消息", description = "获取指定会话的消息列表")
    public Result<List<UserChatMessageVO>> getMessages(@PathVariable String conversationId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        List<UserChatMessageVO> messages = userChatMessagesService.getConversationMessages(conversationId, userId, page, size);
        return Result.success(messages);
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/messages/{messageId}/read")
    @Operation(summary = "标记消息已读", description = "标记指定消息为已读状态")
    public Result<Void> markMessageRead(@PathVariable Long messageId) {
        Long userId = UserContext.getUserId();
        userChatMessagesService.markMessageRead(messageId, userId);
        return Result.success();
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "删除消息", description = "删除指定消息")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = UserContext.getUserId();
        userChatMessagesService.deleteMessage(messageId, userId);
        return Result.success();
    }
}