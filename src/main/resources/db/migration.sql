-- 数据库迁移脚本
-- 为现有的 user_conversations 表添加缺失的字段

USE ai_community;

-- 为 repair_orders 表添加 urgency_level 字段
ALTER TABLE repair_orders 
ADD COLUMN IF NOT EXISTS urgency_level TINYINT COMMENT '紧急程度：1-一般 2-紧急 3-非常紧急' AFTER expected_time;

-- 检查并添加 conversation_name 字段
ALTER TABLE user_conversations 
ADD COLUMN IF NOT EXISTS conversation_name VARCHAR(50) COMMENT '会话名称';

-- 将现有的 title 数据复制到 conversation_name 字段
UPDATE user_conversations 
SET conversation_name = title 
WHERE conversation_name IS NULL AND title IS NOT NULL;

-- 检查并添加 description 字段
ALTER TABLE user_conversations 
ADD COLUMN IF NOT EXISTS description VARCHAR(200) COMMENT '会话描述';

-- 确保所有聊天相关表都存在
CREATE TABLE IF NOT EXISTS user_conversation_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成员记录ID',
    conversation_id VARCHAR(32) NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1-普通成员 2-管理员 3-群主',
    nickname VARCHAR(50) COMMENT '群内昵称',
    mute_notification TINYINT NOT NULL DEFAULT 0 COMMENT '是否静音通知：0-否 1-是',
    last_read_message_id BIGINT COMMENT '最后已读消息ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已退出 1-正常',
    INDEX idx_conversation_user (conversation_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话成员表';

CREATE TABLE IF NOT EXISTS user_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    conversation_id VARCHAR(32) NOT NULL COMMENT '会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型(text/image/voice/video/location/file等)',
    content TEXT NOT NULL COMMENT '消息内容',
    metadata VARCHAR(1000) COMMENT '元数据(如图片大小、语音时长、文件信息等)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已撤回 1-正常 2-已删除',
    read_count INTEGER NOT NULL DEFAULT 0 COMMENT '已读人数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户聊天消息表';

CREATE TABLE IF NOT EXISTS user_message_read_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(32) NOT NULL COMMENT '会话ID',
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_conversation_user (conversation_id, user_id),
    INDEX idx_read_time (read_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息已读记录表';

CREATE TABLE IF NOT EXISTS user_chat_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设置ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    allow_stranger_message TINYINT NOT NULL DEFAULT 1 COMMENT '允许陌生人消息：0-否 1-是',
    message_notification TINYINT NOT NULL DEFAULT 1 COMMENT '消息通知：0-否 1-是',
    notification_sound TINYINT NOT NULL DEFAULT 1 COMMENT '通知声音：0-否 1-是',
    vibration TINYINT NOT NULL DEFAULT 1 COMMENT '震动：0-否 1-是',
    font_size INTEGER NOT NULL DEFAULT 16 COMMENT '字体大小',
    theme VARCHAR(20) NOT NULL DEFAULT 'light' COMMENT '主题：light/dark',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户聊天设置表';
