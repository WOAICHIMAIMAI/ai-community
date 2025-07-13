-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_community;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户唯一标识ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，用于登录',
    password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    phone VARCHAR(20) UNIQUE COMMENT '手机号，用于登录和联系',
    nickname VARCHAR(50) COMMENT '用户昵称，显示用',
    avatar_url VARCHAR(255) COMMENT '头像图片URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    birthday DATE COMMENT '出生日期',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用 1-正常 2-未激活',
    is_verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成实名认证',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户 1-管理员'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

-- 用户验证表
CREATE TABLE IF NOT EXISTS user_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '验证记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_card VARCHAR(50) NOT NULL COMMENT '身份证号码',
    verify_time DATETIME COMMENT '验证通过时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '验证状态：0-未验证 1-已验证 2-验证失败',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实名认证信息表';

-- 社区帖子表
CREATE TABLE IF NOT EXISTS community_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '发帖用户ID',
    title VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time DATETIME COMMENT '最后更新时间',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '帖子状态：0-删除 1-正常 2-置顶',
    INDEX idx_user_id (user_id),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- 帖子评论表
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    comment_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，用于回复功能',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '评论状态：0-删除 1-正常',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子评论表';

-- 点赞记录表
CREATE TABLE IF NOT EXISTS like_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '点赞目标ID',
    target_type TINYINT NOT NULL COMMENT '目标类型：1-帖子 2-评论',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-取消 1-有效',
    INDEX idx_user_target (user_id, target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 收藏记录表
CREATE TABLE IF NOT EXISTS favorite_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-取消 1-有效',
    INDEX idx_user_post (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏记录表';

-- 维修工人表
CREATE TABLE IF NOT EXISTS repair_workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '维修工人ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    speciality VARCHAR(100) COMMENT '专长领域',
    rating FLOAT DEFAULT 5.0 COMMENT '评分',
    work_status TINYINT NOT NULL DEFAULT 0 COMMENT '工作状态：0-空闲 1-繁忙',
    service_count INT NOT NULL DEFAULT 0 COMMENT '服务次数',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工人信息表';

-- 维修订单表
CREATE TABLE IF NOT EXISTS repair_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    worker_id BIGINT COMMENT '维修工人ID',
    title VARCHAR(100) NOT NULL COMMENT '维修标题',
    description TEXT NOT NULL COMMENT '问题描述',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    appoint_time DATETIME COMMENT '预约时间',
    finish_time DATETIME COMMENT '完成时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待接单 1-已接单 2-维修中 3-已完成 4-已取消',
    fee DECIMAL(10,2) DEFAULT 0 COMMENT '维修费用',
    rating INT COMMENT '评分：1-5',
    comment TEXT COMMENT '评价内容',
    INDEX idx_user_id (user_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修订单表';

-- 维修进度表
CREATE TABLE IF NOT EXISTS repair_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '进度记录ID',
    order_id BIGINT NOT NULL COMMENT '维修订单ID',
    status TINYINT NOT NULL COMMENT '状态：0-待接单 1-已接单 2-维修中 3-已完成 4-已取消',
    description VARCHAR(255) COMMENT '进度描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    operator_id BIGINT COMMENT '操作人ID',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修进度记录表';

-- 热点新闻表
CREATE TABLE IF NOT EXISTS hot_news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '新闻ID',
    title VARCHAR(200) NOT NULL COMMENT '新闻标题',
    content LONGTEXT NOT NULL COMMENT '新闻内容(可带HTML格式)',
    summary VARCHAR(500) NOT NULL COMMENT '新闻摘要',
    cover_image VARCHAR(255) COMMENT '封面图URL',
    images VARCHAR(1000) COMMENT '新闻图片URL，多个用逗号分隔',
    source VARCHAR(50) COMMENT '新闻来源(如: 人民日报)',
    source_url VARCHAR(255) UNIQUE COMMENT '原文链接',
    author VARCHAR(50) COMMENT '作者',
    category VARCHAR(50) COMMENT '新闻分类(如: 国内/国际/财经/科技/体育/娱乐等)',
    tags VARCHAR(200) COMMENT '新闻标签，多个用逗号分隔',
    publish_time DATETIME COMMENT '发布时间',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    is_hot TINYINT NOT NULL DEFAULT 0 COMMENT '是否热点(0-否 1-是)',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否 1-是)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-草稿 1-已发布 2-已下线)',
    crawl_time DATETIME COMMENT '爬取时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_publish_time (publish_time),
    INDEX idx_category (category),
    INDEX idx_is_hot (is_hot),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热点新闻表';

-- 初始化管理员账号：账号-admin，密码-123456
INSERT INTO users (username, password, nickname, status, is_verified, role)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 1, 1); 