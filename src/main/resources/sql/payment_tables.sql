-- 费用缴纳模块数据库表

-- 1. 费用账单表
CREATE TABLE payment_bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    bill_type TINYINT NOT NULL COMMENT '账单类型：1-物业费 2-水费 3-电费 4-燃气费 5-停车费 6-其他',
    bill_title VARCHAR(100) NOT NULL COMMENT '账单标题',
    bill_description TEXT COMMENT '账单描述',
    amount DECIMAL(10,2) NOT NULL COMMENT '应缴金额',
    billing_period VARCHAR(20) COMMENT '计费周期（如：2024-01）',
    due_date DATETIME COMMENT '缴费截止日期',
    status TINYINT DEFAULT 0 COMMENT '账单状态：0-未缴费 1-已缴费 2-已逾期 3-部分缴费',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_bill_type (bill_type),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '费用账单表';

-- 2. 缴费记录表
CREATE TABLE payment_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缴费记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    payment_no VARCHAR(64) UNIQUE NOT NULL COMMENT '缴费流水号',
    payment_amount DECIMAL(10,2) NOT NULL COMMENT '实际缴费金额',
    payment_method TINYINT NOT NULL COMMENT '支付方式：1-微信支付 2-支付宝支付 3-银行卡支付 4-余额支付',
    payment_channel VARCHAR(50) COMMENT '支付渠道标识',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态：0-待支付 1-支付成功 2-支付失败 3-已退款',
    payment_time DATETIME COMMENT '支付完成时间',
    refund_amount DECIMAL(10,2) DEFAULT 0 COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    remark TEXT COMMENT '备注信息',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_bill_id (bill_id),
    INDEX idx_payment_no (payment_no),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_time (payment_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (bill_id) REFERENCES payment_bills(id) ON DELETE CASCADE
) COMMENT '缴费记录表';

-- 3. 用户账户余额表
CREATE TABLE user_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户ID',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额',
    total_recharge DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计充值金额',
    total_consumption DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额',
    status TINYINT DEFAULT 1 COMMENT '账户状态：0-冻结 1-正常',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户账户余额表';

-- 4. 账户流水表
CREATE TABLE account_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    transaction_no VARCHAR(64) UNIQUE NOT NULL COMMENT '交易流水号',
    transaction_type TINYINT NOT NULL COMMENT '交易类型：1-充值 2-消费 3-退款 4-冻结 5-解冻',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    balance_before DECIMAL(10,2) NOT NULL COMMENT '交易前余额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
    related_id BIGINT COMMENT '关联业务ID（如缴费记录ID）',
    related_type VARCHAR(50) COMMENT '关联业务类型',
    description VARCHAR(200) COMMENT '交易描述',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES user_accounts(id) ON DELETE CASCADE
) COMMENT '账户流水表';

-- 插入测试数据
-- 为用户ID为1的用户创建账户
INSERT INTO user_accounts (user_id, balance, frozen_amount, total_recharge, total_consumption, status) 
VALUES (1, 1000.00, 0.00, 1000.00, 0.00, 1);

-- 插入测试账单数据
INSERT INTO payment_bills (user_id, bill_type, bill_title, bill_description, amount, billing_period, due_date, status) VALUES
(1, 1, '2024年1月物业费', '包含物业管理费、公共设施维护费等', 350.00, '2024-01', '2024-02-15 23:59:59', 0),
(1, 2, '2024年1月水费', '用水量：25立方米，单价：3.5元/立方米', 87.50, '2024-01', '2024-02-20 23:59:59', 0),
(1, 3, '2024年1月电费', '用电量：180度，单价：0.65元/度', 117.00, '2024-01', '2024-02-20 23:59:59', 0),
(1, 4, '2024年1月燃气费', '用气量：15立方米，单价：2.8元/立方米', 42.00, '2024-01', '2024-02-25 23:59:59', 0),
(1, 5, '2024年1月停车费', '地下车位月租费', 200.00, '2024-01', '2024-02-10 23:59:59', 0),
(1, 1, '2023年12月物业费', '包含物业管理费、公共设施维护费等', 350.00, '2023-12', '2024-01-15 23:59:59', 1),
(1, 2, '2023年12月水费', '用水量：22立方米，单价：3.5元/立方米', 77.00, '2023-12', '2024-01-20 23:59:59', 1),
(1, 3, '2023年12月电费', '用电量：165度，单价：0.65元/度', 107.25, '2023-12', '2024-01-20 23:59:59', 1);

-- 插入已缴费记录
INSERT INTO payment_records (user_id, bill_id, payment_no, payment_amount, payment_method, payment_status, payment_time, transaction_id) VALUES
(1, 6, 'PAY202401150001', 350.00, 1, 1, '2024-01-15 10:30:00', 'wx_20240115103000001'),
(1, 7, 'PAY202401200001', 77.00, 2, 1, '2024-01-20 14:20:00', 'ali_20240120142000001'),
(1, 8, 'PAY202401200002', 107.25, 4, 1, '2024-01-20 16:45:00', NULL);
