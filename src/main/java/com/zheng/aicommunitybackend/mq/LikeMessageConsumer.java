package com.zheng.aicommunitybackend.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zheng.aicommunitybackend.domain.dto.LikeActionMessage;
import com.zheng.aicommunitybackend.domain.entity.LikeRecords;
import com.zheng.aicommunitybackend.service.LikeRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;

/**
 * 点赞消息消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${mq.like.topic}",
        consumerGroup = "${mq.like.consumer-group}",
        nameServer = "${rocketmq.name-server}"
)
public class LikeMessageConsumer implements RocketMQListener<LikeActionMessage> {

    @Autowired
    private LikeRecordsService likeRecordsService;
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${mq.like.topic}")
    private String topic;
    
    @Value("${mq.like.consumer-group}")
    private String consumerGroup;
    
    @PostConstruct
    public void init() {
        log.info("点赞消费者配置: nameServer={}, topic={}, consumerGroup={}", 
                nameServer, topic, consumerGroup);
    }

    @Override
    public void onMessage(LikeActionMessage message) {
        log.info("收到点赞消息: {}", message);
        try {
            // 获取消息内容
            Long userId = message.getUserId();
            Integer targetType = message.getTargetType();
            Long targetId = message.getTargetId();
            Integer actionType = message.getActionType();
            
            // 构建查询条件
            LambdaQueryWrapper<LikeRecords> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecords::getUserId, userId)
                    .eq(LikeRecords::getTargetType, targetType)
                    .eq(LikeRecords::getTargetId, targetId);
            
            // 查询点赞记录
            LikeRecords record = likeRecordsService.getOne(queryWrapper);
            
            // 根据操作类型处理数据库
            if (actionType == 1) {
                // 点赞操作
                if (record == null) {
                    // 创建新的点赞记录
                    record = new LikeRecords();
                    record.setUserId(userId);
                    record.setTargetType(targetType);
                    record.setTargetId(targetId);
                    record.setCreateTime(new Date());
                    likeRecordsService.save(record);
                    
                    // 更新目标点赞数 +1
                    likeRecordsService.updateLikeCount(targetType, targetId, 1);
                    log.info("用户{}点赞成功: 类型={}, 目标ID={}", userId, targetType, targetId);
                } else {
                    log.info("用户{}已经点过赞，不重复处理: 类型={}, 目标ID={}", userId, targetType, targetId);
                }
            } else {
                // 取消点赞操作
                if (record != null) {
                    // 删除点赞记录
                    likeRecordsService.removeById(record.getId());
                    
                    // 更新目标点赞数 -1
                    likeRecordsService.updateLikeCount(targetType, targetId, -1);
                    log.info("用户{}取消点赞成功: 类型={}, 目标ID={}", userId, targetType, targetId);
                } else {
                    log.info("用户{}尚未点赞，无需取消: 类型={}, 目标ID={}", userId, targetType, targetId);
                }
            }
        } catch (Exception e) {
            log.error("处理点赞消息失败: {}, 错误: {}", message, e.getMessage(), e);
        }
    }
} 