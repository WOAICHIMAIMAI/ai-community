-- 抢红包功能相关表结构
-- 创建时间：2025-07-23
-- 说明：支持超高并发抢红包功能，金额最小单位为分

-- 1. 红包活动表
CREATE TABLE red_packet_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    activity_name VARCHAR(100) NOT NULL COMMENT '活动名称',
    activity_desc VARCHAR(500) COMMENT '活动描述',
    total_amount BIGINT NOT NULL COMMENT '红包总金额（分）',
    total_count INT NOT NULL COMMENT '红包总数量',
    grabbed_count INT DEFAULT 0 COMMENT '已抢红包数量',
    grabbed_amount BIGINT DEFAULT 0 COMMENT '已抢红包金额（分）',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    status TINYINT DEFAULT 0 COMMENT '活动状态：0-未开始 1-进行中 2-已结束 3-已取消',
    creator_id BIGINT NOT NULL COMMENT '创建者ID（管理员）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_status_start_time (status, start_time),
    INDEX idx_creator_id (creator_id),
    INDEX idx_start_end_time (start_time, end_time)
) COMMENT '红包活动表';

-- 2. 红包详情表（预分配）
CREATE TABLE red_packet_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '红包详情ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    packet_index INT NOT NULL COMMENT '红包序号（从1开始）',
    amount BIGINT NOT NULL COMMENT '红包金额（分）',
    status TINYINT DEFAULT 0 COMMENT '红包状态：0-未抢 1-已抢',
    user_id BIGINT COMMENT '抢到红包的用户ID',
    grab_time DATETIME COMMENT '抢红包时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_activity_index (activity_id, packet_index),
    INDEX idx_activity_status (activity_id, status),
    INDEX idx_user_id (user_id),
    INDEX idx_grab_time (grab_time),
    FOREIGN KEY (activity_id) REFERENCES red_packet_activities(id) ON DELETE CASCADE
) COMMENT '红包详情表（预分配）';

-- 3. 抢红包记录表
CREATE TABLE red_packet_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    packet_detail_id BIGINT NOT NULL COMMENT '红包详情ID',
    amount BIGINT NOT NULL COMMENT '抢到的金额（分）',
    transaction_no VARCHAR(64) NOT NULL COMMENT '交易流水号',
    grab_time DATETIME NOT NULL COMMENT '抢红包时间',
    account_updated TINYINT DEFAULT 0 COMMENT '账户是否已更新：0-未更新 1-已更新',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_user_activity (user_id, activity_id) COMMENT '用户每个活动只能抢一次',
    UNIQUE KEY uk_transaction_no (transaction_no),
    INDEX idx_activity_id (activity_id),
    INDEX idx_user_id (user_id),
    INDEX idx_grab_time (grab_time),
    INDEX idx_account_updated (account_updated),
    FOREIGN KEY (activity_id) REFERENCES red_packet_activities(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (packet_detail_id) REFERENCES red_packet_details(id) ON DELETE CASCADE
) COMMENT '抢红包记录表';

-- 4. 红包活动配置表（扩展配置）
CREATE TABLE red_packet_activity_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    config_desc VARCHAR(200) COMMENT '配置描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_activity_key (activity_id, config_key),
    FOREIGN KEY (activity_id) REFERENCES red_packet_activities(id) ON DELETE CASCADE
) COMMENT '红包活动配置表';

-- 5. 限流记录表（用于监控和统计）
CREATE TABLE rate_limit_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    limit_key VARCHAR(100) NOT NULL COMMENT '限流键',
    limit_type VARCHAR(20) NOT NULL COMMENT '限流类型：USER/IP/GLOBAL',
    user_id BIGINT COMMENT '用户ID（用户级限流时记录）',
    ip_address VARCHAR(45) COMMENT 'IP地址（IP级限流时记录）',
    request_count INT NOT NULL COMMENT '请求次数',
    is_blocked TINYINT NOT NULL COMMENT '是否被限流：0-通过 1-被限流',
    window_start_time DATETIME NOT NULL COMMENT '时间窗口开始时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_limit_key (limit_key),
    INDEX idx_user_id (user_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_window_start_time (window_start_time),
    INDEX idx_created_time (created_time)
) COMMENT '限流记录表';

-- 插入一些测试数据

-- 插入测试活动
INSERT INTO red_packet_activities (
    activity_name, activity_desc, total_amount, total_count, 
    start_time, end_time, status, creator_id
) VALUES (
    '新年红包雨', 
    '新年特别活动，总金额10000元，共1000个红包', 
    1000000,  -- 10000元 = 1000000分
    1000, 
    '2025-01-01 12:00:00', 
    '2025-01-01 12:30:00', 
    0,  -- 未开始
    1   -- 管理员ID
);

-- 为活动添加配置
INSERT INTO red_packet_activity_config (activity_id, config_key, config_value, config_desc) VALUES
(1, 'min_amount', '100', '最小红包金额（分）'),
(1, 'max_amount', '5000', '最大红包金额（分）'),
(1, 'user_limit', '1', '每用户限抢次数'),
(1, 'algorithm', 'DOUBLE_AVERAGE', '红包分配算法');

-- 注意：red_packet_details 表的数据需要在活动创建时通过程序生成
-- 这里不插入具体数据，因为需要使用红包分配算法生成

-- 创建索引优化查询性能
-- 复合索引：活动状态和时间范围查询
CREATE INDEX idx_activity_status_time ON red_packet_activities(status, start_time, end_time);

-- 复合索引：用户抢红包记录查询
CREATE INDEX idx_user_grab_records ON red_packet_records(user_id, grab_time DESC);

-- 复合索引：活动抢红包统计
CREATE INDEX idx_activity_grab_stats ON red_packet_records(activity_id, grab_time);
