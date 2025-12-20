-- ======================================================
-- 预约订单支付功能测试数据初始化脚本
-- ======================================================

-- 1. 确保测试用户存在（假设用户ID为1，请根据实际情况修改）
-- 注意：如果用户不存在，请先在应用中注册或手动创建用户

-- 2. 为测试用户创建账户（如果不存在）
INSERT INTO user_accounts (user_id, balance, frozen_amount, total_recharge, total_consumption, status, created_time, updated_time)
SELECT 1, 1000.00, 0.00, 1000.00, 0.00, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM user_accounts WHERE user_id = 1);

-- 3. 为测试用户充值（如果账户已存在）
UPDATE user_accounts 
SET balance = balance + 1000.00,
    total_recharge = total_recharge + 1000.00,
    updated_time = NOW()
WHERE user_id = 1;

-- 4. 添加充值流水记录
INSERT INTO account_transactions (
    user_id, 
    account_id, 
    transaction_no, 
    transaction_type, 
    amount, 
    balance_before, 
    balance_after, 
    related_id, 
    related_type, 
    description, 
    created_time
)
SELECT 
    1,
    a.id,
    CONCAT('TXN', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    1, -- 1=充值
    1000.00,
    a.balance - 1000.00,
    a.balance,
    NULL,
    'manual_recharge',
    '测试充值',
    NOW()
FROM user_accounts a
WHERE a.user_id = 1;

-- ======================================================
-- 验证数据
-- ======================================================

-- 查看用户账户信息
SELECT 
    id,
    user_id,
    balance as 当前余额,
    frozen_amount as 冻结金额,
    (balance - frozen_amount) as 可用余额,
    total_recharge as 累计充值,
    total_consumption as 累计消费,
    CASE status WHEN 1 THEN '正常' ELSE '冻结' END as 账户状态,
    created_time as 创建时间,
    updated_time as 更新时间
FROM user_accounts 
WHERE user_id = 1;

-- 查看最近的账户流水
SELECT 
    id,
    transaction_no as 流水号,
    CASE transaction_type 
        WHEN 1 THEN '充值'
        WHEN 2 THEN '消费'
        WHEN 3 THEN '退款'
        WHEN 4 THEN '冻结'
        WHEN 5 THEN '解冻'
        ELSE '未知'
    END as 交易类型,
    amount as 交易金额,
    balance_before as 交易前余额,
    balance_after as 交易后余额,
    related_type as 关联业务类型,
    description as 描述,
    created_time as 创建时间
FROM account_transactions
WHERE user_id = 1
ORDER BY created_time DESC
LIMIT 10;

-- ======================================================
-- 测试场景准备
-- ======================================================

-- 场景1：模拟余额不足（将余额设置为10元）
-- UPDATE user_accounts SET balance = 10.00, updated_time = NOW() WHERE user_id = 1;

-- 场景2：模拟账户冻结
-- UPDATE user_accounts SET status = 0, updated_time = NOW() WHERE user_id = 1;

-- 场景3：恢复账户正常状态
-- UPDATE user_accounts SET status = 1, balance = 1000.00, updated_time = NOW() WHERE user_id = 1;

-- ======================================================
-- 清理测试数据（谨慎使用）
-- ======================================================

-- 删除测试订单（保留最近10条）
-- DELETE FROM appointment_orders 
-- WHERE user_id = 1 
-- AND id NOT IN (
--     SELECT id FROM (
--         SELECT id FROM appointment_orders WHERE user_id = 1 ORDER BY create_time DESC LIMIT 10
--     ) tmp
-- );

-- 删除测试流水（保留最近10条）
-- DELETE FROM account_transactions 
-- WHERE user_id = 1 
-- AND id NOT IN (
--     SELECT id FROM (
--         SELECT id FROM account_transactions WHERE user_id = 1 ORDER BY created_time DESC LIMIT 10
--     ) tmp
-- );

