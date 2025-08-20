-- ====================================================
-- 智慧社区数据分析功能 - MySQL建表语句
-- 创建时间: 2024年
-- 说明: 包含数据分析、服务统计相关的所有数据表
-- ====================================================

-- 1. 数据分析汇总表
DROP TABLE IF EXISTS `data_analytics_summary`;
CREATE TABLE `data_analytics_summary` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `statistics_date` DATE NOT NULL COMMENT '统计日期',
  `statistics_type` TINYINT(2) NOT NULL DEFAULT 3 COMMENT '统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计',
  `total_repair_orders` INT(11) DEFAULT 0 COMMENT '保修工单总数',
  `completed_repair_orders` INT(11) DEFAULT 0 COMMENT '已完成保修工单数',
  `total_appointment_orders` INT(11) DEFAULT 0 COMMENT '预约服务总数',
  `completed_appointment_orders` INT(11) DEFAULT 0 COMMENT '已完成预约服务数',
  `active_users` INT(11) DEFAULT 0 COMMENT '活跃用户数',
  `new_users` INT(11) DEFAULT 0 COMMENT '新增用户数',
  `avg_response_time` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平均响应时长(分钟)',
  `avg_satisfaction_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均满意度评分',
  `total_revenue` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总营收',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type` (`statistics_date`, `statistics_type`),
  KEY `idx_statistics_date` (`statistics_date`),
  KEY `idx_statistics_type` (`statistics_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据分析汇总表';

-- 2. 保修类型统计表
DROP TABLE IF EXISTS `repair_type_statistics`;
CREATE TABLE `repair_type_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `statistics_date` DATE NOT NULL COMMENT '统计日期',
  `statistics_type` TINYINT(2) NOT NULL DEFAULT 3 COMMENT '统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计',
  `repair_type` VARCHAR(50) NOT NULL COMMENT '保修类型',
  `order_count` INT(11) DEFAULT 0 COMMENT '工单数量',
  `completed_count` INT(11) DEFAULT 0 COMMENT '已完成数量',
  `avg_process_time` DECIMAL(8,2) DEFAULT 0.00 COMMENT '平均处理时长(小时)',
  `avg_satisfaction_score` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均满意度评分',
  `percentage` DECIMAL(5,2) DEFAULT 0.00 COMMENT '占比百分数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type_repair` (`statistics_date`, `statistics_type`, `repair_type`),
  KEY `idx_statistics_date` (`statistics_date`),
  KEY `idx_repair_type` (`repair_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保修类型统计表';

-- 3. 服务统计表
DROP TABLE IF EXISTS `service_statistics`;
CREATE TABLE `service_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `statistics_date` DATE NOT NULL COMMENT '统计日期',
  `statistics_type` TINYINT(2) NOT NULL DEFAULT 3 COMMENT '统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计',
  `service_type` VARCHAR(50) NOT NULL COMMENT '服务类型',
  `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
  `order_count` INT(11) DEFAULT 0 COMMENT '订单数量',
  `completed_count` INT(11) DEFAULT 0 COMMENT '已完成数量',
  `completion_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成率',
  `total_revenue` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总营收',
  `avg_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均满意度评分',
  `growth_rate` DECIMAL(6,2) DEFAULT 0.00 COMMENT '同比增长率',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type_service` (`statistics_date`, `statistics_type`, `service_type`),
  KEY `idx_statistics_date` (`statistics_date`),
  KEY `idx_service_type` (`service_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务统计表';

-- 4. 用户活跃度统计表
DROP TABLE IF EXISTS `user_activity_statistics`;
CREATE TABLE `user_activity_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `statistics_date` DATE NOT NULL COMMENT '统计日期',
  `statistics_type` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计',
  `daily_active_users` INT(11) DEFAULT 0 COMMENT '日活跃用户数(DAU)',
  `weekly_active_users` INT(11) DEFAULT 0 COMMENT '周活跃用户数(WAU)',
  `monthly_active_users` INT(11) DEFAULT 0 COMMENT '月活跃用户数(MAU)',
  `new_users` INT(11) DEFAULT 0 COMMENT '新增用户数',
  `login_users` INT(11) DEFAULT 0 COMMENT '登录用户数',
  `repair_order_users` INT(11) DEFAULT 0 COMMENT '提交保修工单用户数',
  `appointment_users` INT(11) DEFAULT 0 COMMENT '预约服务用户数',
  `post_users` INT(11) DEFAULT 0 COMMENT '发帖用户数',
  `paying_users` INT(11) DEFAULT 0 COMMENT '付费用户数',
  `retention_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '用户留存率',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type` (`statistics_date`, `statistics_type`),
  KEY `idx_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活跃度统计表';

-- 5. 数据分析任务表 (用于定时统计任务)
DROP TABLE IF EXISTS `analytics_tasks`;
CREATE TABLE `analytics_tasks` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型：repair_statistics/service_statistics/user_activity',
  `statistics_type` TINYINT(2) NOT NULL COMMENT '统计类型：1-日统计 2-周统计 3-月统计 4-季度统计 5-年统计',
  `target_date` DATE NOT NULL COMMENT '目标统计日期',
  `status` TINYINT(2) NOT NULL DEFAULT 0 COMMENT '任务状态：0-待执行 1-执行中 2-执行成功 3-执行失败',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始执行时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '执行结束时间',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `result_data` JSON DEFAULT NULL COMMENT '执行结果数据',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_date` (`task_type`, `statistics_type`, `target_date`),
  KEY `idx_status` (`status`),
  KEY `idx_target_date` (`target_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据分析任务表';

-- 6. 数据分析报告表
DROP TABLE IF EXISTS `analytics_reports`;
CREATE TABLE `analytics_reports` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_title` VARCHAR(200) NOT NULL COMMENT '报告标题',
  `report_type` VARCHAR(50) NOT NULL COMMENT '报告类型：daily/weekly/monthly/quarterly/yearly/custom',
  `start_date` DATE NOT NULL COMMENT '统计开始日期',
  `end_date` DATE NOT NULL COMMENT '统计结束日期',
  `report_data` JSON NOT NULL COMMENT '报告数据(JSON格式)',
  `summary_data` JSON DEFAULT NULL COMMENT '总结数据',
  `file_path` VARCHAR(500) DEFAULT NULL COMMENT '报告文件路径(如PDF)',
  `creator_id` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `status` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '报告状态：0-草稿 1-已生成 2-已发布',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_date_range` (`start_date`, `end_date`),
  KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据分析报告表';

-- ====================================================
-- 索引优化和数据初始化
-- ====================================================

-- 为现有表添加必要的索引(如果不存在)
-- repair_orders表索引优化
ALTER TABLE `repair_orders` 
ADD INDEX `idx_create_time` (`create_time`) IF NOT EXISTS,
ADD INDEX `idx_repair_type` (`repair_type`) IF NOT EXISTS,
ADD INDEX `idx_status` (`status`) IF NOT EXISTS,
ADD INDEX `idx_completion_time` (`completion_time`) IF NOT EXISTS;

-- appointment_orders表索引优化  
ALTER TABLE `appointment_orders`
ADD INDEX `idx_create_time` (`create_time`) IF NOT EXISTS,
ADD INDEX `idx_service_type` (`service_type`) IF NOT EXISTS,
ADD INDEX `idx_status` (`status`) IF NOT EXISTS,
ADD INDEX `idx_finish_time` (`finish_time`) IF NOT EXISTS;

-- users表索引优化
ALTER TABLE `users`
ADD INDEX `idx_register_time` (`register_time`) IF NOT EXISTS,
ADD INDEX `idx_last_login_time` (`last_login_time`) IF NOT EXISTS;

-- community_posts表索引优化
ALTER TABLE `community_posts`
ADD INDEX `idx_create_time` (`create_time`) IF NOT EXISTS,
ADD INDEX `idx_user_id` (`user_id`) IF NOT EXISTS;

-- ====================================================
-- 示例数据插入（可选）
-- ====================================================

-- 插入示例保修类型统计数据
INSERT IGNORE INTO `repair_type_statistics` (
    `statistics_date`, `statistics_type`, `repair_type`, `order_count`, 
    `completed_count`, `avg_process_time`, `avg_satisfaction_score`, `percentage`
) VALUES 
('2024-01-01', 3, '水电', 45, 42, 12.5, 4.2, 35.5),
('2024-01-01', 3, '门窗', 25, 23, 8.2, 4.5, 19.7),
('2024-01-01', 3, '家电', 35, 30, 15.8, 4.0, 27.6),
('2024-01-01', 3, '管道', 15, 14, 18.5, 3.8, 11.8),
('2024-01-01', 3, '其他', 7, 6, 10.2, 4.1, 5.5);

-- 插入示例服务统计数据
INSERT IGNORE INTO `service_statistics` (
    `statistics_date`, `statistics_type`, `service_type`, `service_name`, 
    `order_count`, `completed_count`, `completion_rate`, `total_revenue`, `avg_rating`
) VALUES 
('2024-01-01', 3, 'cleaning', '家政保洁', 120, 115, 95.83, 9600.00, 4.3),
('2024-01-01', 3, 'repair', '维修服务', 80, 75, 93.75, 4000.00, 4.1),
('2024-01-01', 3, 'moving', '搬家服务', 25, 24, 96.00, 4800.00, 4.4),
('2024-01-01', 3, 'appliance', '家电维修', 45, 40, 88.89, 2400.00, 3.9);

-- 插入示例用户活跃度数据
INSERT IGNORE INTO `user_activity_statistics` (
    `statistics_date`, `statistics_type`, `daily_active_users`, `new_users`, 
    `login_users`, `repair_order_users`, `appointment_users`, `post_users`, `retention_rate`
) VALUES 
('2024-01-01', 1, 285, 15, 245, 12, 35, 28, 78.5),
('2024-01-02', 1, 312, 18, 268, 15, 42, 32, 82.1),
('2024-01-03', 1, 298, 12, 255, 10, 38, 25, 79.8);

-- ====================================================
-- 权限设置（根据需要调整）
-- ====================================================

-- 创建数据分析专用用户（可选）
-- CREATE USER 'analytics_user'@'%' IDENTIFIED BY 'AnalyticsPassword123!';
-- GRANT SELECT, INSERT, UPDATE ON ai_community.data_analytics_summary TO 'analytics_user'@'%';
-- GRANT SELECT, INSERT, UPDATE ON ai_community.repair_type_statistics TO 'analytics_user'@'%';
-- GRANT SELECT, INSERT, UPDATE ON ai_community.service_statistics TO 'analytics_user'@'%';
-- GRANT SELECT, INSERT, UPDATE ON ai_community.user_activity_statistics TO 'analytics_user'@'%';
-- GRANT SELECT ON ai_community.repair_orders TO 'analytics_user'@'%';
-- GRANT SELECT ON ai_community.appointment_orders TO 'analytics_user'@'%';
-- GRANT SELECT ON ai_community.users TO 'analytics_user'@'%';
-- GRANT SELECT ON ai_community.community_posts TO 'analytics_user'@'%';
-- FLUSH PRIVILEGES;

-- ====================================================
-- 建表完成提示
-- ====================================================
SELECT '数据分析功能相关表创建完成！' AS message;
SELECT '包含以下表:' AS tables;
SELECT '1. data_analytics_summary - 数据分析汇总表' AS table_info
UNION ALL SELECT '2. repair_type_statistics - 保修类型统计表'
UNION ALL SELECT '3. service_statistics - 服务统计表'  
UNION ALL SELECT '4. user_activity_statistics - 用户活跃度统计表'
UNION ALL SELECT '5. analytics_tasks - 数据分析任务表'
UNION ALL SELECT '6. analytics_reports - 数据分析报告表';
