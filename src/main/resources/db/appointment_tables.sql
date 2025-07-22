-- 服务预约模块数据库表设计

-- 服务类型表
CREATE TABLE IF NOT EXISTS appointment_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '服务ID',
    service_type VARCHAR(50) NOT NULL UNIQUE COMMENT '服务类型标识(cleaning/repair/moving等)',
    service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
    description VARCHAR(500) COMMENT '服务描述',
    icon VARCHAR(100) COMMENT '服务图标',
    base_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '基础价格',
    unit VARCHAR(20) DEFAULT '次' COMMENT '计价单位',
    is_hot TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门服务：0-否 1-是',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_type (service_type),
    INDEX idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约服务类型表';

-- 服务人员表
CREATE TABLE IF NOT EXISTS appointment_workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '服务人员ID',
    worker_name VARCHAR(50) NOT NULL COMMENT '服务人员姓名',
    worker_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    age INT COMMENT '年龄',
    experience_years INT DEFAULT 0 COMMENT '工作经验年数',
    service_types VARCHAR(200) COMMENT '服务类型，多个用逗号分隔',
    skill_description TEXT COMMENT '技能描述',
    rating DECIMAL(3,2) DEFAULT 5.0 COMMENT '评分(1-5分)',
    order_count INT NOT NULL DEFAULT 0 COMMENT '接单数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-离职 1-在职 2-休假',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_types (service_types),
    INDEX idx_status_rating (status, rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约服务人员表';

-- 预约订单表
CREATE TABLE IF NOT EXISTS appointment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    service_id BIGINT NOT NULL COMMENT '服务ID',
    service_type VARCHAR(50) NOT NULL COMMENT '服务类型',
    service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
    worker_id BIGINT COMMENT '服务人员ID',
    worker_name VARCHAR(50) COMMENT '服务人员姓名',
    worker_phone VARCHAR(20) COMMENT '服务人员电话',
    appointment_time DATETIME NOT NULL COMMENT '预约服务时间',
    address VARCHAR(500) NOT NULL COMMENT '服务地址',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    requirements TEXT COMMENT '特殊要求',
    estimated_price DECIMAL(10,2) COMMENT '预估价格',
    actual_price DECIMAL(10,2) COMMENT '实际价格',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消',
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    rating INT COMMENT '用户评分(1-5分)',
    comment TEXT COMMENT '用户评价',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    confirm_time DATETIME COMMENT '确认时间',
    start_time DATETIME COMMENT '开始服务时间',
    finish_time DATETIME COMMENT '完成时间',
    cancel_time DATETIME COMMENT '取消时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_service_type (service_type),
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status),
    INDEX idx_appointment_time (appointment_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 服务推荐记录表
CREATE TABLE IF NOT EXISTS appointment_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '推荐记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    service_id BIGINT NOT NULL COMMENT '服务ID',
    service_type VARCHAR(50) NOT NULL COMMENT '服务类型',
    recommend_reason VARCHAR(200) COMMENT '推荐理由',
    recommend_score DECIMAL(5,2) DEFAULT 0 COMMENT '推荐分数',
    is_clicked TINYINT NOT NULL DEFAULT 0 COMMENT '是否点击：0-否 1-是',
    is_booked TINYINT NOT NULL DEFAULT 0 COMMENT '是否预约：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    click_time DATETIME COMMENT '点击时间',
    book_time DATETIME COMMENT '预约时间',
    INDEX idx_user_service (user_id, service_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务推荐记录表';

-- 服务时间段配置表
CREATE TABLE IF NOT EXISTS appointment_time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '时间段ID',
    service_type VARCHAR(50) NOT NULL COMMENT '服务类型',
    slot_date DATE NOT NULL COMMENT '日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    max_appointments INT NOT NULL DEFAULT 1 COMMENT '最大预约数',
    current_appointments INT NOT NULL DEFAULT 0 COMMENT '当前预约数',
    is_available TINYINT NOT NULL DEFAULT 1 COMMENT '是否可用：0-不可用 1-可用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_service_date (service_type, slot_date),
    INDEX idx_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约时间段配置表';

-- 初始化服务类型数据
INSERT INTO appointment_services (service_type, service_name, description, icon, base_price, is_hot, sort_order) VALUES
('cleaning', '家政保洁', '深度清洁，专业保洁团队', 'brush-o', 80.00, 1, 1),
('repair', '维修服务', '水电维修，家具安装', 'setting-o', 50.00, 1, 2),
('appliance', '家电维修', '家电故障，快速维修', 'tv-o', 60.00, 0, 3),
('moving', '搬家服务', '专业搬家，安全可靠', 'logistics', 200.00, 0, 4),
('gardening', '园艺服务', '花草养护，园艺设计', 'flower-o', 100.00, 0, 5),
('pest', '除虫服务', '专业除虫，安全环保', 'delete-o', 120.00, 0, 6);

-- 初始化服务人员数据
INSERT INTO appointment_workers (worker_name, worker_phone, gender, age, experience_years, service_types, skill_description, rating, order_count) VALUES
('李阿姨', '139****9999', 2, 45, 8, 'cleaning', '专业家政保洁，经验丰富，服务细致', 4.8, 156),
('王师傅', '137****7777', 1, 38, 12, 'repair,appliance', '水电维修专家，家电维修经验丰富', 4.9, 203),
('张师傅', '138****6666', 1, 42, 10, 'moving', '专业搬家团队负责人，服务周到', 4.6, 89),
('刘师傅', '136****5555', 1, 35, 6, 'gardening', '园艺设计师，植物养护专家', 4.5, 67),
('陈师傅', '135****4444', 1, 40, 9, 'pest', '专业除虫技师，环保安全', 4.4, 78);
