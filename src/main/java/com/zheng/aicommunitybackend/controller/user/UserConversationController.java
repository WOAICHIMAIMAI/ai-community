package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.domain.dto.ConversationCreateDTO;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.domain.vo.ConversationVO;
import com.zheng.aicommunitybackend.service.UserConversationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户会话控制器
 */
@RestController
@RequestMapping("/user/conversations")
@Tag(name = "用户会话接口", description = "用户会话管理相关接口")
public class UserConversationController {

    @Resource
    private UserConversationsService userConversationsService;

    /**
     * 创建会话
     */
    @PostMapping
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    public Result<String> createConversation(@RequestBody @Validated ConversationCreateDTO dto) {
        Long userId = UserContext.getUserId();
        String conversationId = userConversationsService.createConversation(dto, userId);
        return Result.success(conversationId);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话")
    public Result<List<ConversationVO>> getConversations() {
        Long userId = UserContext.getUserId();
        List<ConversationVO> conversations = userConversationsService.getUserConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{conversationId}")
    @Operation(summary = "获取会话详情", description = "获取指定会话的详细信息")
    public Result<ConversationVO> getConversationDetail(@PathVariable String conversationId) {
        Long userId = UserContext.getUserId();
        ConversationVO conversation = userConversationsService.getConversationDetail(conversationId, userId);
        return Result.success(conversation);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{conversationId}")
    @Operation(summary = "删除会话", description = "删除指定会话")
    public Result<Void> deleteConversation(@PathVariable String conversationId) {
        Long userId = UserContext.getUserId();
        userConversationsService.deleteConversation(conversationId, userId);
        return Result.success();
    }
}