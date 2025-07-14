package com.zheng.aicommunitybackend.service;

import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;

/**
 * 消息队列生产者服务接口
 */
public interface MQProducerService {

    /**
     * 发送点赞/取消点赞消息
     * @param message 点赞消息实体
     * @return 是否发送成功
     */
    boolean sendLikeMessage(LikeActionMessage message);
} 