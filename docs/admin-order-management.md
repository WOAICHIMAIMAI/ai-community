# 管理端订单管理功能说明文档

## 功能概述

管理端的**预约服务订单管理**功能已经完整实现，位于 `/appointment-orders` 路由。该页面提供了完整的订单管理功能，包括数据统计、订单查询、订单操作等。

## 已实现功能清单

### ✅ 1. 数据统计展示

在页面顶部以卡片形式展示6项关键统计数据：

| 统计项 | 字段 | 图标 | 颜色 |
|-------|------|------|------|
| 总预约数 | totalAppointments | Calendar | 紫色渐变 |
| 待处理 | pendingCount | Clock | 红色渐变 |
| 已确认 | confirmedCount | Check | 蓝色渐变 |
| 进行中 | inProgressCount | Tools | 绿色渐变 |
| 已完成 | completedCount | CircleCheck | 黄色渐变 |
| 已取消 | cancelledCount | CircleClose | 灰色渐变 |

**接口**：`GET /api/admin/appointment/orders/stats`

### ✅ 2. 订单列表查询

完整的订单列表展示，包含以下信息：
- 序号
- 订单编号
- 用户信息（姓名、手机号）
- 服务信息（服务名称、服务类型）
- 预约时间
- 服务地址
- 订单状态
- 工作人员
- 服务费用
- 操作按钮

**接口**：`GET /api/admin/appointment/orders/list`

**请求参数**：
```typescript
{
  serviceType?: string    // 服务类型
  status?: number         // 订单状态 0-待确认 1-已确认 2-服务中 3-已完成 4-已取消
  keyword?: string        // 关键词搜索（订单编号、联系人、手机号、地址）
  page: number           // 页码
  pageSize: number       // 每页数量
}
```

### ✅ 3. 订单筛选功能

提供多维度筛选：
- **服务类型筛选**：全部、家政保洁、维修服务、快递代收
- **状态筛选**：全部、待处理、已确认、进行中、已完成、已取消
- **关键词搜索**：支持搜索用户名、手机号、地址
- **快捷操作**：
  - 查询按钮
  - 重置按钮
  - 导出数据按钮

### ✅ 4. 订单详情查看

点击"查看"按钮或点击表格行，弹出详情对话框展示：

**基本信息**：
- 订单编号
- 订单状态
- 服务名称
- 服务类型
- 联系人
- 联系电话
- 预约时间
- 服务地址
- 特殊要求

**价格信息**：
- 预估价格
- 实际价格

**时间信息**：
- 创建时间
- 确认时间
- 完成时间（已完成时显示）

**其他信息**：
- 服务人员信息（已分配时显示）
- 取消原因（已取消时显示）
- 用户评价（已评价时显示）

**接口**：`GET /api/admin/appointment/orders/{id}`

### ✅ 5. 订单操作功能

根据订单状态显示不同的操作按钮：

#### 5.1 待处理状态（status=0）
- **确认订单**：确认用户的预约请求
  - 接口：`PUT /api/admin/appointment/orders/{id}/confirm`
  - 操作后状态变为"已确认"

- **分配人员**：为订单分配服务人员
  - 接口：`PUT /api/admin/appointment/orders/{id}/assign`
  - 请求体：
    ```json
    {
      "orderId": 123,
      "workerId": 456,
      "workerName": "张师傅"
    }
    ```

- **取消订单**：管理员取消订单
  - 接口：`PUT /api/admin/appointment/orders/{id}/cancel?reason=取消原因`
  - 必须填写取消原因

#### 5.2 已确认状态（status=1）
- **分配人员**：为订单分配或更换服务人员
- **取消订单**：管理员取消订单

#### 5.3 进行中状态（status=2）
- **完成订单**：标记订单为已完成
  - 接口：`PUT /api/admin/appointment/orders/{id}/status`
  - 请求体：
    ```json
    {
      "orderId": 123,
      "status": 3,
      "remark": "服务已完成"
    }
    ```

#### 5.4 已完成/已取消状态（status=3/4）
- 仅显示"查看"按钮

### ✅ 6. 子组件

系统使用了模块化的组件设计：

#### 6.1 AppointmentDetail.vue
订单详情展示组件，负责详细信息的展示。

#### 6.2 WorkerAssign.vue
工作人员分配组件，提供：
- 工作人员选择下拉框
- 显示工作人员信息（姓名、电话）
- 提交分配操作

#### 6.3 AppointmentForm.vue
订单表单组件（用于新增/编辑订单，管理端较少使用）

### ✅ 7. 分页功能

- 支持自定义每页数量：10、20、50、100
- 显示总记录数
- 支持跳转到指定页码
- 响应式分页交互

### ✅ 8. 额外功能

- **行点击**：点击表格行即可查看详情
- **状态标签**：不同状态用不同颜色的标签区分
- **服务类型标签**：服务类型用标签展示
- **价格高亮**：价格用红色醒目显示
- **未分配提示**：未分配工作人员时显示灰色斜体"未分配"
- **响应式设计**：适配不同屏幕尺寸

## 后端接口清单

所有接口都在 `AdminAppointmentOrderController.java` 中实现：

| 序号 | 接口路径 | 方法 | 功能 | 状态 |
|-----|---------|------|------|------|
| 1 | `/api/admin/appointment/orders/stats` | GET | 获取订单统计数据 | ✅ |
| 2 | `/api/admin/appointment/orders/list` | GET | 分页查询订单列表 | ✅ |
| 3 | `/api/admin/appointment/orders/{id}` | GET | 获取订单详情 | ✅ |
| 4 | `/api/admin/appointment/orders/{id}/confirm` | PUT | 确认订单 | ✅ |
| 5 | `/api/admin/appointment/orders/{id}/assign` | PUT | 分配服务人员 | ✅ |
| 6 | `/api/admin/appointment/orders/{id}/status` | PUT | 修改订单状态 | ✅ |
| 7 | `/api/admin/appointment/orders/{id}/cancel` | PUT | 取消订单 | ✅ |
| 8 | `/api/admin/appointment/orders/{id}` | DELETE | 删除订单 | ✅ |

## 前端文件结构

```
ai-community-frontend/admin/src/views/appointments/
├── AppointmentListView.vue          # 主页面（订单管理）
├── ServiceManageView.vue            # 服务管理页面
├── ServiceListView.vue              # 服务列表页面
└── components/
    ├── AppointmentDetail.vue        # 订单详情组件
    ├── AppointmentForm.vue          # 订单表单组件
    ├── ServiceDetail.vue            # 服务详情组件
    ├── ServiceForm.vue              # 服务表单组件
    └── WorkerAssign.vue             # 工作人员分配组件
```

## 路由配置

```typescript
{
  path: 'appointment-orders',
  name: 'appointment-orders',
  component: () => import('@/views/appointments/AppointmentListView.vue'),
  meta: {
    title: '订单管理',
    requiresAuth: true
  }
}
```

**访问地址**：`http://localhost:5174/appointment-orders`（端口根据实际情况）

## 使用说明

### 1. 查看订单统计
- 页面加载时自动显示统计卡片
- 统计数据实时更新

### 2. 查询订单
- **快速查询**：使用服务类型和状态筛选
- **关键词搜索**：输入用户名、手机号或地址关键词
- **重置筛选**：点击"重置"按钮清空所有筛选条件

### 3. 查看订单详情
- 方式1：点击"查看"按钮
- 方式2：直接点击表格行

### 4. 确认订单
1. 找到状态为"待处理"的订单
2. 点击"确认"按钮
3. 在弹出的确认框中点击"确定"

### 5. 分配服务人员
1. 找到状态为"待处理"或"已确认"的订单
2. 点击"分配"按钮
3. 在弹出框中选择服务人员
4. 点击"确定分配"

### 6. 取消订单
1. 找到未完成的订单
2. 点击"取消"按钮
3. 在弹出框中输入取消原因
4. 点击"确认取消"

### 7. 完成订单
1. 找到状态为"进行中"的订单
2. 点击"完成"按钮
3. 确认完成操作

## 订单状态流转

```
待处理(0) → 已确认(1) → 进行中(2) → 已完成(3)
    ↓           ↓           ↓
  已取消(4)   已取消(4)   [无法取消]
```

**状态说明**：
- **待处理(0)**：用户刚提交预约，等待管理员确认
- **已确认(1)**：管理员已确认，等待分配服务人员
- **进行中(2)**：服务人员已分配，服务正在进行
- **已完成(3)**：服务已完成
- **已取消(4)**：订单已被取消

## 数据格式示例

### 订单列表响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "APT202401250001",
        "serviceId": 10,
        "serviceName": "家政保洁",
        "serviceType": "cleaning",
        "appointmentTime": "2024-01-25 14:00:00",
        "address": "阳光小区1栋101",
        "contactName": "张三",
        "contactPhone": "138****8888",
        "requirements": "需要深度清洁",
        "estimatedPrice": 200.00,
        "actualPrice": 180.00,
        "status": 1,
        "statusDesc": "已确认",
        "workerId": 5,
        "workerName": "李阿姨",
        "workerPhone": "139****9999",
        "rating": 5,
        "comment": "服务很满意",
        "rated": true,
        "createTime": "2024-01-20 10:00:00",
        "confirmTime": "2024-01-20 11:00:00",
        "finishTime": null,
        "cancelReason": null
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 统计数据响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalOrders": 128,
    "pendingCount": 15,
    "confirmedCount": 32,
    "inProgressCount": 8,
    "completedCount": 65,
    "cancelledCount": 8
  }
}
```

## 注意事项

1. **权限控制**：所有接口需要管理员权限
2. **操作日志**：所有操作都有后台日志记录
3. **数据验证**：
   - 取消订单必须填写原因
   - 分配人员必须选择服务人员
4. **状态限制**：
   - 已完成和已取消的订单不能再次操作
   - 进行中的订单不能取消
5. **实时更新**：操作成功后自动刷新列表和统计数据

## 功能扩展建议

1. **批量操作**：支持批量确认、批量分配、批量导出
2. **高级搜索**：日期范围筛选、价格区间筛选
3. **数据可视化**：订单趋势图表、服务类型分布饼图
4. **导出功能**：导出Excel报表
5. **打印功能**：打印订单详情
6. **消息通知**：订单状态变更时通知用户
7. **工单派发**：智能分配算法，自动匹配服务人员
8. **服务评价**：查看用户评价和评分统计

## 总结

✅ **所有功能已完整实现**，包括：
1. ✅ 数据统计展示
2. ✅ 订单列表查询
3. ✅ 订单筛选功能
4. ✅ 订单详情查看
5. ✅ 订单确认功能
6. ✅ 服务人员分配
7. ✅ 订单取消功能
8. ✅ 订单完成功能
9. ✅ 分页功能
10. ✅ 响应式设计

**后端接口**：8个接口全部实现并可用
**前端页面**：主页面 + 3个子组件
**访问路径**：`/appointment-orders`

系统功能完善，交互流畅，可直接投入使用！🎉

---

**文档版本**: v1.0.0  
**最后更新**: 2025-12-20

