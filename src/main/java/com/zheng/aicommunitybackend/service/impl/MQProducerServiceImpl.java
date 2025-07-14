package com.zheng.aicommunitybackend.service.impl;

import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;
import com.zheng.aicommunitybackend.service.MQProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * 消息队列生产者服务实现
 */
@Slf4j
@Service
public class MQProducerServiceImpl implements MQProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Value("${mq.like.topic}")
    private String likeTopic;
    
    @Override
    public boolean sendLikeMessage(LikeActionMessage message) {
        try {
            // 异步发送消息
            rocketMQTemplate.asyncSend(likeTopic, MessageBuilder.withPayload(message).build(), new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("点赞消息发送成功: {}, 结果: {}", message, sendResult);
                }
                
                @Override
                public void onException(Throwable throwable) {
                    log.error("点赞消息发送失败: {}, 错误: {}", message, throwable.getMessage());
                }
            });
            return true;
        } catch (Exception e) {
            log.error("点赞消息发送异常: {}, 错误: {}", message, e.getMessage());
            return false;
        }
    }
} 