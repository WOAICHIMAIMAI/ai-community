package com.zheng.aicommunitybackend.controller.user;

import com.zheng.aicommunitybackend.ai.chetmemory.DatabaseChatMemory;
import com.zheng.aicommunitybackend.common.PromptConstants;
import com.zheng.aicommunitybackend.common.UserContext;
import com.zheng.aicommunitybackend.constants.RedisKeys;
import com.zheng.aicommunitybackend.domain.entity.ChatMessage;
import com.zheng.aicommunitybackend.domain.result.Result;
import com.zheng.aicommunitybackend.service.ChatMessageService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * AI聊天相关接口
 */
@RestController
@RequestMapping("/user/ai")
public class UserAiChatController {

    private final ChatClient chatClient;

    @Resource
    ToolCallbackProvider toolCallbackProvider;

    @Resource
    ToolCallback[] allTools;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private VectorStore communityAppVectorStore;

    @Resource
    private RedisTemplate redisTemplate;



    public UserAiChatController(ChatClient.Builder builder, DatabaseChatMemory databaseChatMemory) {
        this.chatClient = builder
                .defaultSystem(PromptConstants.XIAO_ZHI_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(databaseChatMemory))
                .build();
    }

    /**
     * 与AI对话
     * @param userContent 用户消息
     * @param chatId 聊天序号
     * @return AI回复
     */
    @GetMapping("/chat")
    public Result<String> chat(String userContent, String chatId){
        // 使缓存失效
        redisTemplate.delete(String.format(RedisKeys.AI_CHAT_CONVERSATION, chatId));
        return Result.success(chatClient.prompt()
                .user(userContent)
                .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .tools(allTools)
                .tools(toolCallbackProvider)
                .advisors(new QuestionAnswerAdvisor(communityAppVectorStore))
                .call()
                .content());
    }

    /**
     * 返回当前用户下所有的AI聊天序号
     */
    @GetMapping
    public Result<List<String>> getChatIds(){
        return Result.success(chatMessageService.getChatIds(UserContext.getUserId()));
    }

    /**
     * 返回某一次聊天窗口记录，默认返回组后10条
     * @param chatId 会话ID
     * @param lastK 最后K条
     * @return 返回聊天记录
     */
    @GetMapping("/{chatId}")
    public Result<List<ChatMessage>> getChatMessages(@PathVariable String chatId, @RequestParam(defaultValue = "10", required = false) Integer lastK){
        return Result.success(chatMessageService.list(chatId, lastK));
    }

}
