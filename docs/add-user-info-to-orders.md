# 订单管理页面添加用户信息功能

## 需求说明

在管理端的订单管理页面中添加用户信息展示，包括：
- 用户ID
- 用户名/昵称
- 用户手机号（脱敏显示）

## 实现方案

### 1. 后端修改

#### 1.1 修改 VO 类

**文件**: `src/main/java/com/zheng/aicommunitybackend/domain/vo/AppointmentOrderVO.java`

添加用户信息字段：

```java
/**
 * 用户ID
 */
@Schema(description = "用户ID")
private Long userId;

/**
 * 用户名/昵称
 */
@Schema(description = "用户名", example = "张三")
private String username;

/**
 * 用户手机号
 */
@Schema(description = "用户手机号", example = "138****8888")
private String userPhone;
```

#### 1.2 修改 Service 实现

**文件**: `src/main/java/com/zheng/aicommunitybackend/service/impl/AppointmentServiceImpl.java`

在 `convertToOrderVO` 方法中添加用户信息查询逻辑：

```java
private AppointmentOrderVO convertToOrderVO(AppointmentOrders order) {
    AppointmentOrderVO vo = new AppointmentOrderVO();
    BeanUtils.copyProperties(order, vo);
    vo.setStatusDesc(getStatusDesc(order.getStatus()));
    
    // 设置用户信息
    if (order.getUserId() != null) {
        try {
            com.zheng.aicommunitybackend.domain.entity.Users user = usersMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername() != null ? user.getUsername() : user.getNickname());
                // 脱敏手机号
                if (user.getPhone() != null && user.getPhone().length() >= 11) {
                    vo.setUserPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
                } else {
                    vo.setUserPhone(user.getPhone());
                }
            }
        } catch (Exception e) {
            log.error("查询用户信息失败 userId: {}", order.getUserId(), e);
            vo.setUsername("未知用户");
        }
    }
    
    // ... 其他逻辑 ...
    return vo;
}
```

**实现特点**：
- 根据订单的 `userId` 查询用户表获取用户信息
- 优先使用 `username`，如果为空则使用 `nickname`
- 手机号脱敏处理：保留前3位和后4位，中间显示 `****`
- 异常处理：如果查询失败，用户名显示为"未知用户"

### 2. 前端对接

#### 2.1 接口定义

**文件**: `ai-community-frontend/admin/src/api/appointment.ts`

接口定义中已包含用户信息字段（第37-39行）：

```typescript
export interface AppointmentRecord {
  id: number
  userId?: number
  username?: string
  userPhone?: string
  // ... 其他字段
}
```

#### 2.2 列表页面显示

**文件**: `ai-community-frontend/admin/src/views/appointments/AppointmentListView.vue`

在表格中添加用户信息列（第167-174行）：

```vue
<el-table-column prop="username" label="用户信息" min-width="150">
  <template #default="{ row }">
    <div class="user-info">
      <div class="username">{{ row.username }}</div>
      <div class="phone">{{ row.userPhone }}</div>
    </div>
  </template>
</el-table-column>
```

#### 2.3 详情页面显示

**文件**: `ai-community-frontend/admin/src/views/appointments/components/AppointmentDetail.vue`

在详情页面添加用户信息区块（第53-70行）：

```vue
<!-- 用户信息 -->
<div class="info-section">
  <h4>用户信息</h4>
  <el-descriptions :column="2" border>
    <el-descriptions-item label="用户名">
      {{ appointment.username }}
    </el-descriptions-item>
    <el-descriptions-item label="用户手机">
      {{ appointment.userPhone }}
    </el-descriptions-item>
    <el-descriptions-item label="联系电话">
      {{ appointment.contactPhone }}
    </el-descriptions-item>
    <el-descriptions-item label="服务地址" :span="2">
      {{ appointment.address }}
    </el-descriptions-item>
  </el-descriptions>
</div>
```

## 功能亮点

### 1. 数据安全
- **手机号脱敏**：后端自动将手机号脱敏为 `138****8888` 格式
- **异常处理**：用户信息查询失败时有友好提示

### 2. 用户体验
- **列表展示**：用户信息单独成列，清晰展示用户名和手机号
- **详情展示**：详情页面中用户信息独立区块，包含完整的联系方式
- **信息区分**：区分用户手机号和联系电话（可能是其他人的电话）

### 3. 代码质量
- **关注点分离**：VO转换逻辑统一在Service层处理
- **错误处理**：有完善的异常处理机制
- **日志记录**：查询失败时记录错误日志便于排查

## 测试要点

### 1. 后端测试
1. **正常流程**：
   - 创建订单并查询，验证用户信息是否正确返回
   - 检查手机号是否正确脱敏

2. **异常流程**：
   - 用户被删除后的订单查询
   - 用户信息不完整的情况

### 2. 前端测试
1. **列表页面**：
   - 验证用户信息列是否正确显示
   - 验证脱敏手机号格式

2. **详情页面**：
   - 验证用户信息区块是否完整
   - 验证用户名和手机号是否正确显示

3. **搜索功能**：
   - 验证是否可以通过用户名搜索订单
   - 验证是否可以通过手机号搜索订单

## 数据流转

```
订单实体 (AppointmentOrders)
    ↓ [包含 userId]
Service层 (AppointmentServiceImpl)
    ↓ [查询用户表]
用户实体 (Users)
    ↓ [提取用户名和手机号]
    ↓ [手机号脱敏]
订单VO (AppointmentOrderVO)
    ↓ [返回给前端]
前端页面
    ↓ [展示用户信息]
```

## 相关文件清单

### 后端文件
- `src/main/java/com/zheng/aicommunitybackend/domain/vo/AppointmentOrderVO.java` - VO类定义
- `src/main/java/com/zheng/aicommunitybackend/service/impl/AppointmentServiceImpl.java` - Service实现

### 前端文件
- `ai-community-frontend/admin/src/api/appointment.ts` - API接口定义
- `ai-community-frontend/admin/src/views/appointments/AppointmentListView.vue` - 列表页面
- `ai-community-frontend/admin/src/views/appointments/components/AppointmentDetail.vue` - 详情页面

## 注意事项

1. **数据库关联**：
   - 订单表通过 `user_id` 字段关联用户表
   - 确保用户表数据完整性

2. **性能考虑**：
   - 当前实现是在VO转换时逐个查询用户信息
   - 如果订单量大，建议优化为批量查询或使用左连接

3. **安全性**：
   - 手机号已做脱敏处理
   - 如需完整手机号，建议通过单独接口获取并记录操作日志

4. **扩展性**：
   - 如需显示更多用户信息（如头像、认证状态），可在VO中添加相应字段
   - 前端已预留展示空间，易于扩展

## 更新日期

2025-12-20

