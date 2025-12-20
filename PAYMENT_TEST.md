# 预约订单支付功能测试说明

## 功能概述

已实现预约订单创建时的账户余额扣款功能，包括：
1. 余额充足性检查
2. 账户余额扣除
3. 账户流水记录
4. 事务保证数据一致性

## 后端实现

### 1. 支付服务 (`UserAccountsService.payForAppointment`)
- **位置**: `src/main/java/com/zheng/aicommunitybackend/service/impl/UserAccountsServiceImpl.java`
- **功能**:
  - 检查账户是否存在
  - 检查账户状态（是否被冻结）
  - 验证余额是否充足
  - 扣除账户余额
  - 更新累计消费金额
  - 生成交易流水号（格式: TXN + 时间戳 + 4位随机数）
  - 记录账户流水（交易类型：2-消费）
- **事务处理**: 使用 `@Transactional(rollbackFor = Exception.class)` 保证原子性

### 2. 订单创建流程 (`AppointmentServiceImpl.createAppointment`)
- **位置**: `src/main/java/com/zheng/aicommunitybackend/service/impl/AppointmentServiceImpl.java`
- **修改内容**:
  - 创建订单前检查余额
  - 先插入订单记录
  - 再调用支付服务扣款
  - 任何步骤失败都会回滚整个事务

### 3. DTO 更新 (`AppointmentCreateDTO`)
- **位置**: `src/main/java/com/zheng/aicommunitybackend/domain/dto/AppointmentCreateDTO.java`
- **新增字段**: `estimatedPrice` (BigDecimal) - 预估价格

## 前端实现

### 1. 订单创建页面 (`OrderCreateView.vue`)
- **位置**: `ai-community-frontend/user/src/views/appointment/OrderCreateView.vue`
- **功能**:
  - 实时计算订单总价（基础价格 × 数量 + 附加费用）
  - 显示费用明细：
    - 服务费用
    - 加急费用（+¥30）
    - 保险费用（+¥20）
    - 预估总费用
  - 提交订单时传递 `estimatedPrice`
  - 余额不足时弹出友好提示对话框

### 2. API 接口更新
- **位置**: `ai-community-frontend/user/src/api/appointment.ts`
- **修改**: `AppointmentCreateData` 接口新增 `estimatedPrice?: number` 字段

## 测试步骤

### 准备工作
1. 确保后端服务已启动
2. 确保用户端前端已启动
3. 使用测试账号登录

### 测试场景 1: 余额充足的情况
1. 登录用户端
2. 查看当前账户余额（可通过数据库或个人中心查看）
3. 进入预约服务页面，选择一个服务
4. 填写订单信息：
   - 选择服务日期和时间
   - 填写/选择服务地址
   - 填写联系信息
   - 选择数量（可选）
   - 勾选特殊要求（可选）
5. 查看费用预估，确认总价
6. 点击"确认预约"按钮
7. **预期结果**:
   - 订单创建成功
   - 显示成功提示弹窗
   - 账户余额减少（减少金额 = 订单预估价格）
   - 数据库 `user_accounts` 表的 `balance` 和 `total_consumption` 字段更新
   - 数据库 `account_transactions` 表新增一条消费记录（`transaction_type=2`）
   - 数据库 `appointment_orders` 表新增订单记录

### 测试场景 2: 余额不足的情况
1. 登录用户端
2. 确保当前账户余额 < 订单预估价格
   - 可以通过数据库手动修改 `user_accounts.balance` 为较小值（如 10）
3. 进入预约服务页面，选择一个服务
4. 填写订单信息并选择较高价格的服务或增加数量
5. 点击"确认预约"按钮
6. **预期结果**:
   - 订单创建失败
   - 弹出"余额不足"对话框，显示当前订单金额和充值提示
   - 账户余额不变
   - 数据库无新增订单和流水记录

### 测试场景 3: 账户被冻结的情况
1. 在数据库中将测试账户的 `user_accounts.status` 设置为 0（冻结）
2. 尝试创建订单
3. **预期结果**:
   - 订单创建失败
   - 显示"账户已被冻结，无法支付"错误提示

### 测试场景 4: 事务回滚测试
1. 暂时修改代码，在支付成功后手动抛出异常
2. 尝试创建订单
3. **预期结果**:
   - 订单创建失败
   - 账户余额不变（事务回滚）
   - 数据库无新增订单和流水记录

## 数据库验证

### 检查订单记录
```sql
SELECT * FROM appointment_orders 
WHERE order_no = '订单编号' 
ORDER BY create_time DESC;
```

### 检查账户余额
```sql
SELECT id, user_id, balance, frozen_amount, total_consumption, updated_time
FROM user_accounts 
WHERE user_id = 用户ID;
```

### 检查账户流水
```sql
SELECT * FROM account_transactions 
WHERE user_id = 用户ID 
  AND related_type = 'appointment_order'
ORDER BY created_time DESC;
```

验证字段：
- `transaction_type` = 2（消费）
- `amount` = 订单预估价格
- `balance_before` 和 `balance_after` 的差值 = `amount`
- `related_id` = 订单ID
- `description` 包含订单编号

## 常见问题

### Q1: 提示"用户账户不存在"
**原因**: 用户首次使用，系统未自动创建账户
**解决**: 
1. 检查 `UserAccountsServiceImpl.getAccountByUserId()` 方法
2. 该方法会在账户不存在时自动创建
3. 或手动在数据库中插入账户记录

### Q2: 余额不足但页面没有弹出对话框
**原因**: 前端错误处理可能没有正确识别错误类型
**解决**: 
1. 打开浏览器控制台查看错误信息
2. 确认后端返回的错误消息包含"余额不足"或"账户余额不足"关键词

### Q3: 订单创建成功但余额未扣除
**原因**: 支付服务未被调用或事务提交失败
**解决**: 
1. 查看后端日志，确认是否执行到 `payForAppointment` 方法
2. 检查数据库事务隔离级别和提交状态

## 充值功能

目前充值接口已存在但前端未完全实现：
- **后端接口**: `POST /api/user/payment/account/recharge`
- **Controller**: `src/main/java/com/zheng/aicommunitybackend/controller/user/PaymentController.java`
- **建议**: 可以先通过数据库手动修改 `user_accounts.balance` 进行测试

## 下一步优化建议

1. 实现完整的充值功能页面
2. 添加订单支付成功后的余额提醒
3. 在个人中心显示账户余额和流水记录
4. 添加退款功能（订单取消时退回余额）
5. 支持多种支付方式（微信、支付宝等）
6. 添加支付密码验证
7. 优化错误提示和用户引导

