package com.zheng.aicommunitybackend.controller.user;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserAiChatControllerTest {

    @Resource
    UserAiChatController userAiChatController;
    @Test
    void chat() {
        String message = "你好，我叫郑嘉骏";
        String chatId = UUID.randomUUID().toString();
        System.out.println(userAiChatController.chat(message, chatId));
        message = "帮我创建报修工单";
        System.out.println(userAiChatController.chat(message, chatId));
    }
}