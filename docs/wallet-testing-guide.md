# 钱包功能快速测试指南

## 测试前准备

1. 确保后端服务已启动
2. 确保前端服务已启动
3. 确保数据库中 `account_transactions` 表已创建
4. 准备一个已登录的测试账号

## 测试步骤

### 1. 测试账户余额查询（已存在功能）

**接口**: `GET /api/user/payment/account`

**测试方法**:
```bash
curl -X GET "http://localhost:8080/api/user/payment/account" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "userId": 123,
    "balance": 100.00,
    "availableBalance": 100.00,
    "frozenAmount": 0.00,
    "totalRecharge": 500.00,
    "totalConsumption": 400.00,
    "status": 1,
    "statusName": "正常"
  }
}
```

### 2. 测试账户流水查询（新功能）

#### 2.1 查询全部流水

**接口**: `GET /api/user/payment/account/transactions?page=1&pageSize=10`

**测试方法**:
```bash
curl -X GET "http://localhost:8080/api/user/payment/account/transactions?page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期结果**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "accountId": 1,
        "transactionNo": "TXN1703000000000ABC123",
        "transactionType": 1,
        "transactionTypeName": "充值",
        "amount": 100.00,
        "balanceBefore": 0.00,
        "balanceAfter": 100.00,
        "relatedId": null,
        "relatedType": null,
        "description": "账户充值",
        "createdTime": "2025-12-20 10:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 2.2 按交易类型查询

**查询充值记录**:
```bash
curl -X GET "http://localhost:8080/api/user/payment/account/transactions?transactionType=1&page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**查询消费记录**:
```bash
curl -X GET "http://localhost:8080/api/user/payment/account/transactions?transactionType=2&page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 2.3 按日期范围查询

```bash
curl -X GET "http://localhost:8080/api/user/payment/account/transactions?startDate=2025-12-01&endDate=2025-12-31&page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 前端页面测试

#### 3.1 访问费用缴纳页面

1. 打开浏览器访问: `http://localhost:5173/payment`
2. 确认页面顶部显示账户余额卡片
3. 卡片应显示"点击查看钱包详情"提示

#### 3.2 测试钱包入口

1. 点击账户余额卡片
2. 应该跳转到钱包页面 (`/wallet`)

#### 3.3 测试钱包页面功能

**测试项目**:
- [ ] 页面正常加载
- [ ] 顶部显示账户余额、可用余额
- [ ] 显示累计充值和累计消费统计
- [ ] 显示交易类型筛选按钮（全部、充值、消费、退款）
- [ ] 流水列表正常显示
- [ ] 流水记录显示正确的图标、类型、金额、时间
- [ ] 收入金额显示绿色，支出金额显示红色
- [ ] 点击不同类型筛选按钮，列表正确更新

#### 3.4 测试交互功能

- [ ] 下拉刷新功能正常
- [ ] 上拉加载更多功能正常
- [ ] 点击充值按钮显示"充值功能开发中"提示
- [ ] 点击返回按钮正常返回上一页

### 4. 边界情况测试

#### 4.1 无流水记录

1. 使用一个新账户（无流水记录）
2. 访问钱包页面
3. 应显示"暂无流水记录"的空状态

#### 4.2 数据分页

1. 确保账户有超过10条流水记录
2. 测试上拉加载更多功能
3. 验证数据不重复

#### 4.3 筛选功能

1. 切换不同的交易类型
2. 验证列表正确过滤
3. 验证页码重置为1

## 测试数据准备（可选）

如果数据库中没有流水数据，可以执行以下SQL插入测试数据：

```sql
-- 假设用户ID为1，账户ID为1

-- 充值记录
INSERT INTO account_transactions (user_id, account_id, transaction_no, transaction_type, amount, balance_before, balance_after, description, created_time)
VALUES 
(1, 1, 'TXN1703000000001ABC', 1, 100.00, 0.00, 100.00, '账户充值', '2025-12-20 10:00:00'),
(1, 1, 'TXN1703000000002ABC', 1, 200.00, 100.00, 300.00, '账户充值', '2025-12-20 11:00:00');

-- 消费记录
INSERT INTO account_transactions (user_id, account_id, transaction_no, transaction_type, amount, balance_before, balance_after, related_id, related_type, description, created_time)
VALUES 
(1, 1, 'TXN1703000000003ABC', 2, 50.00, 300.00, 250.00, 1, 'PAYMENT', '缴纳物业费', '2025-12-20 12:00:00'),
(1, 1, 'TXN1703000000004ABC', 2, 30.00, 250.00, 220.00, 2, 'APPOINTMENT', '预约服务支付', '2025-12-20 13:00:00');

-- 退款记录
INSERT INTO account_transactions (user_id, account_id, transaction_no, transaction_type, amount, balance_before, balance_after, related_id, related_type, description, created_time)
VALUES 
(1, 1, 'TXN1703000000005ABC', 3, 30.00, 220.00, 250.00, 2, 'APPOINTMENT', '预约服务退款', '2025-12-20 14:00:00');
```

## 常见问题排查

### 问题1: 接口返回401未授权

**原因**: 用户未登录或token过期

**解决**:
1. 确认已登录
2. 检查token是否有效
3. 重新登录获取新token

### 问题2: 流水列表为空

**原因**: 
1. 数据库中没有该用户的流水记录
2. 查询条件过滤掉了所有记录

**解决**:
1. 插入测试数据
2. 检查查询条件是否正确
3. 检查用户ID是否匹配

### 问题3: 页面显示异常

**原因**: 
1. API返回数据格式不符合预期
2. 前端代码错误

**解决**:
1. 打开浏览器开发者工具查看Console错误
2. 检查Network中API返回的数据
3. 确认后端接口返回格式正确

### 问题4: 金额显示错误

**原因**: 
1. 后端返回的金额格式不正确
2. 前端格式化处理有误

**解决**:
1. 检查数据库中金额字段类型（应为DECIMAL(10,2)）
2. 确认后端使用BigDecimal处理金额
3. 前端确保使用toFixed(2)格式化显示

## 性能测试建议

1. **压力测试**: 测试在1000+条流水记录时的分页性能
2. **并发测试**: 多用户同时查询流水
3. **响应时间**: 确保接口响应时间在200ms以内

## 测试检查清单

- [ ] 后端接口正常返回数据
- [ ] 前端页面正常显示
- [ ] 账户余额正确显示
- [ ] 流水列表正确显示
- [ ] 交易类型筛选功能正常
- [ ] 分页功能正常
- [ ] 刷新功能正常
- [ ] 加载更多功能正常
- [ ] 空状态显示正常
- [ ] 错误提示正常
- [ ] 返回功能正常
- [ ] 跳转钱包功能正常

## 测试完成后

1. 确认所有功能正常
2. 记录测试结果
3. 如有问题，记录bug详情
4. 提交测试报告

---

**测试版本**: v1.0.0
**测试日期**: 2025-12-20

