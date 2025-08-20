# 🚀 预约服务测试数据生成器运行指南

## ✅ **已修复的问题**

1. **增加了事务支持** - 使用 `@Transactional` 确保数据一致性
2. **完善的错误处理** - 详细的异常捕获和错误信息输出
3. **数据验证检查** - 插入前检查重复数据，避免冲突
4. **详细的执行日志** - 每步操作都有清晰的成功/失败提示
5. **数据统计功能** - 实时显示数据量统计

## 🎯 **推荐运行步骤**

### 第一步：验证数据库连接
```java
// 运行这个测试方法来验证数据库连接和表结构
@Test
public void validateDatabaseConnection()
```

### 第二步：按顺序生成数据
```java
// 推荐按以下顺序运行：

1. generateTestUsers()        // 生成10个测试用户
2. generateServiceTypes()     // 生成10种服务类型  
3. generateServiceWorkers()   // 生成20个服务人员
4. generateAppointmentOrders() // 生成50个预约订单
```

### 第三步：一键生成所有数据
```java
// 或者直接运行这个方法，会自动按顺序生成所有数据
@Test
public void generateAllTestData()
```

## 🔧 **主要改进**

### **1. 数据库验证方法**
- 检查表连接状态
- 验证表结构完整性
- 显示当前数据统计

### **2. 增强的错误处理**
```java
try {
    // 数据插入逻辑
    int result = mapper.insert(entity);
    if (result > 0) {
        System.out.println("✅ 创建成功: " + entity.getName());
    } else {
        System.out.println("❌ 创建失败: " + entity.getName());
    }
} catch (Exception e) {
    System.err.println("❌ 异常: " + e.getMessage());
    e.printStackTrace();
}
```

### **3. 重复数据检查**
- 用户：检查用户名是否存在
- 服务类型：检查服务类型标识是否存在
- 服务人员：检查姓名是否存在
- 自动跳过已存在的数据

### **4. 数据质量保证**
- BigDecimal 精度控制（价格保留2位小数，评分保留1位小数）
- 订单状态逻辑完整（评分评价只有已完成订单才有）
- 服务人员技能匹配（订单优先分配对应技能的服务人员）

## 📊 **预期生成数据**

| 数据类型 | 数量 | 特点说明 |
|----------|------|----------|
| 👥 测试用户 | 10个 | test_user01~05, zhang_san等，真实昵称 |
| 🛠️ 服务类型 | 10种 | 家政保洁、维修服务等，含5个热门服务 |
| 👷 服务人员 | 20个 | 按服务类型分布，真实姓名和技能描述 |
| 📋 预约订单 | 50个 | 5种状态分布，智能匹配服务人员 |

## 🎮 **运行方式**

### 方式1：IDE中运行
1. 打开 `AppointmentTestDataGenerator.java`
2. 右键点击要运行的测试方法
3. 选择 "Run 'methodName()'"

### 方式2：Maven命令运行
```bash
# 运行所有数据生成
mvn test -Dtest=AppointmentTestDataGenerator#generateAllTestData

# 运行特定方法
mvn test -Dtest=AppointmentTestDataGenerator#validateDatabaseConnection
mvn test -Dtest=AppointmentTestDataGenerator#generateTestUsers
mvn test -Dtest=AppointmentTestDataGenerator#generateServiceTypes
```

### 方式3：清理测试数据
```bash
mvn test -Dtest=AppointmentTestDataGenerator#clearAllTestData
```

## 🚨 **故障排除**

### 问题1：数据库连接失败
**解决方案**：
1. 检查MySQL服务是否启动
2. 确认数据库配置正确 (`application-dev.yml`)
3. 确认数据库 `ai_community` 已创建

### 问题2：表不存在错误
**解决方案**：
1. 运行数据库初始化脚本
2. 确认预约服务相关表已创建

### 问题3：数据插入失败
**解决方案**：
1. 先运行 `validateDatabaseConnection()` 检查表结构
2. 查看控制台错误日志
3. 检查实体类字段是否与数据库表字段匹配

### 问题4：重复数据问题
**已解决**：新版本会自动检查重复数据并跳过

## 🎉 **成功标志**

运行成功后，您应该看到类似输出：
```
=== 数据生成统计 ===
✅ 用户表: 10 条
✅ 服务类型表: 10 条  
✅ 服务人员表: 20 条
✅ 预约订单表: 50 条
=== 统计完成 ===
```

现在可以尝试运行测试数据生成器，如果遇到任何问题，请查看控制台的详细错误信息！🚀

