package com.zheng.aicommunitybackend.service.impl;

import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;
import com.zheng.aicommunitybackend.service.MQProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

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
    
    @Value("${rocketmq.producer.send-message-timeout}")
    private long sendTimeout;
    
    @Value("${rocketmq.producer.retry-times-when-send-failed}")
    private int retryTimesWhenSendFailed;
    
    @Value("${rocketmq.producer.retry-times-when-send-async-failed}")
    private int retryTimesWhenSendAsyncFailed;
    
    @Override
    public boolean sendLikeMessage(LikeActionMessage message) {
        int retryTimes = 0;
        boolean success = false;
        Exception lastException = null;
        
        // 重试机制，最多重试retryTimesWhenSendFailed次
        while (retryTimes <= retryTimesWhenSendFailed && !success) {
            try {
                if (retryTimes > 0) {
                    log.info("点赞消息重发第{}次尝试, userId={}, targetId={}", 
                            retryTimes, message.getUserId(), message.getTargetId());
                }
                
                // 发送消息
                SendResult sendResult = rocketMQTemplate.syncSend(
                        likeTopic, 
                        MessageBuilder.withPayload(message).build(), 
                        sendTimeout);
                
                // 判断发送结果
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    log.info("点赞消息发送成功: msgId={}, userId={}, targetId={}", 
                            sendResult.getMsgId(), message.getUserId(), message.getTargetId());
                    success = true;
                    break;
                } else {
                    log.warn("点赞消息发送状态异常: {}, userId={}, targetId={}, 状态: {}, 重试次数: {}/{}", 
                            message, message.getUserId(), message.getTargetId(), 
                            sendResult.getSendStatus(), retryTimes, retryTimesWhenSendFailed);
                    retryTimes++;
                    // 简单退避策略，每次重试延迟增加
                    if (retryTimes <= retryTimesWhenSendFailed) {
                        Thread.sleep(100 * retryTimes);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                retryTimes++;
                log.error("点赞消息发送异常: {}, 错误: {}, 重试次数: {}/{}", 
                        message, e.getMessage(), retryTimes, retryTimesWhenSendFailed);
                // 简单退避策略，每次重试延迟增加
                try {
                    if (retryTimes <= retryTimesWhenSendFailed) {
                        Thread.sleep(100 * retryTimes);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("消息重发等待被中断", ie);
                    break;
                }
            }
        }
        
        // 所有重试都失败
        if (!success && retryTimes > retryTimesWhenSendFailed) {
            log.error("点赞消息发送失败，已重试{}次，userId={}, targetId={}, 最后错误: {}", 
                    retryTimesWhenSendFailed, message.getUserId(), message.getTargetId(), 
                    lastException != null ? lastException.getMessage() : "未知错误");
        }
        
        return success;
    }
    
    @Override
    public void sendLikeMessageAsync(LikeActionMessage message) {
        // 使用AtomicInteger跟踪异步重试次数
        AtomicInteger retryCount = new AtomicInteger(0);
        
        // 创建包含重试逻辑的异步回调
        SendCallback callbackWithRetry = new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    log.info("点赞消息异步发送成功: msgId={}, userId={}, targetId={}", 
                            sendResult.getMsgId(), message.getUserId(), message.getTargetId());
                } else {
                    log.warn("点赞消息异步发送状态异常: userId={}, targetId={}, 结果状态: {}", 
                            message.getUserId(), message.getTargetId(), sendResult.getSendStatus());
                    // 状态不是SEND_OK也进行重试
                    retryAsyncSend(message, retryCount);
                }
            }
            
            @Override
            public void onException(Throwable throwable) {
                log.error("点赞消息异步发送失败: userId={}, targetId={}, 错误: {}", 
                        message.getUserId(), message.getTargetId(), throwable.getMessage());
                // 发送异常时进行重试
                retryAsyncSend(message, retryCount);
            }
        };
        
        try {
            // 首次异步发送
            Message<LikeActionMessage> rocketMessage = MessageBuilder.withPayload(message).build();
            rocketMQTemplate.asyncSend(likeTopic, rocketMessage, callbackWithRetry, sendTimeout);
        } catch (Exception e) {
            log.error("点赞消息首次异步发送异常: {}, 错误: {}", message, e.getMessage());
            // 发送出现异常时进行重试
            retryAsyncSend(message, retryCount);
        }
    }
    
    /**
     * 异步消息重发逻辑
     * @param message 消息体
     * @param retryCount 当前重试次数
     */
    private void retryAsyncSend(LikeActionMessage message, AtomicInteger retryCount) {
        int currentRetry = retryCount.incrementAndGet();
        if (currentRetry <= retryTimesWhenSendAsyncFailed) {
            try {
                // 简单退避策略，每次重试延迟增加
                Thread.sleep(100 * currentRetry);
                
                log.info("点赞消息异步重发第{}次尝试, userId={}, targetId={}", 
                        currentRetry, message.getUserId(), message.getTargetId());
                
                // 创建简单回调，不再包含重试逻辑，避免递归重试
                SendCallback simpleCallback = new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                            log.info("点赞消息异步重发成功: msgId={}, userId={}, targetId={}, 重试次数: {}", 
                                    sendResult.getMsgId(), message.getUserId(), message.getTargetId(), currentRetry);
                        } else {
                            log.warn("点赞消息异步重发结果异常: userId={}, targetId={}, 结果状态: {}, 重试次数: {}", 
                                    message.getUserId(), message.getTargetId(), sendResult.getSendStatus(), currentRetry);
                        }
                    }
                    
                    @Override
                    public void onException(Throwable throwable) {
                        log.error("点赞消息异步重发失败: userId={}, targetId={}, 错误: {}, 重试次数: {}", 
                                message.getUserId(), message.getTargetId(), throwable.getMessage(), currentRetry);
                    }
                };
                
                // 重新发送消息
                Message<LikeActionMessage> rocketMessage = MessageBuilder.withPayload(message).build();
                rocketMQTemplate.asyncSend(likeTopic, rocketMessage, simpleCallback, sendTimeout);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("点赞消息异步重发等待被中断, 重试次数: {}/{}", 
                        currentRetry, retryTimesWhenSendAsyncFailed);
            } catch (Exception e) {
                log.error("点赞消息异步重发异常: {}, 错误: {}, 重试次数: {}/{}", 
                        message, e.getMessage(), currentRetry, retryTimesWhenSendAsyncFailed);
            }
        } else {
            log.error("点赞消息异步发送失败，已重试{}次，userId={}, targetId={}", 
                    retryTimesWhenSendAsyncFailed, message.getUserId(), message.getTargetId());
        }
    }
    
    /**
     * 发送带有延时等级的消息（带生产者确认）
     * 
     * @param message 点赞消息实体
     * @param delayLevel 延时等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h)
     * @return 是否发送成功
     */
    @Override
    public boolean sendLikeMessageWithDelay(LikeActionMessage message, int delayLevel) {
        Message<LikeActionMessage> rocketMessage = MessageBuilder.withPayload(message).build();
        
        try {
            // 同步发送延时消息并获取发送结果
            SendResult sendResult = rocketMQTemplate.syncSend(
                likeTopic, 
                rocketMessage, 
                sendTimeout, 
                delayLevel
            );
            
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                log.info("延时点赞消息发送成功: msgId={}, userId={}, targetId={}, delayLevel={}", 
                        sendResult.getMsgId(), message.getUserId(), message.getTargetId(), delayLevel);
                return true;
            } else {
                log.warn("延时点赞消息发送状态异常: {}, userId={}, targetId={}, 结果状态: {}", 
                        message, message.getUserId(), message.getTargetId(), sendResult.getSendStatus());
                return false;
            }
        } catch (Exception e) {
            log.error("延时点赞消息发送异常: {}, 错误: {}", message, e.getMessage());
            return false;
        }
    }
} 