package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;

/**
 * 消息队列生产者服务接口
 */
public interface MQProducerService {

    /**
     * 同步发送点赞/取消点赞消息（带生产者确认）
     * @param message 点赞消息实体
     * @return 是否发送成功
     */
    boolean sendLikeMessage(LikeActionMessage message);
    
    /**
     * 异步发送点赞/取消点赞消息（带生产者确认）
     * @param message 点赞消息实体
     */
    void sendLikeMessageAsync(LikeActionMessage message);
    
    /**
     * 发送带有延时等级的点赞/取消点赞消息（带生产者确认）
     * 
     * @param message 点赞消息实体
     * @param delayLevel 延时等级 (1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h)
     * @return 是否发送成功
     */
    boolean sendLikeMessageWithDelay(LikeActionMessage message, int delayLevel);
} 