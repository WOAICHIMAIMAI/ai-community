# 布隆过滤器监控系统升级指南

## 🚀 **监控系统升级概览**

### 升级前后对比

| 功能 | 升级前 | 升级后 |
|------|--------|--------|
| 告警方式 | ❌ 仅控制台日志 | ✅ 钉钉 + 企业微信 + 邮件 |
| 实时性 | ❌ 需要查看日志 | ✅ 即时推送通知 |
| 可视化 | ❌ 无 | ✅ 网页监控面板 |
| API接口 | ❌ 无 | ✅ 完整REST API |
| 配置灵活性 | ❌ 有限 | ✅ 多渠道可配置 |

## 📱 **告警渠道配置**

### 1. 钉钉告警配置

#### 步骤1：创建钉钉机器人
```bash
1. 在钉钉群中点击"群设置" → "智能群助手" → "添加机器人"
2. 选择"自定义机器人"
3. 设置机器人名称：布隆过滤器监控
4. 安全设置选择"加签"
5. 复制Webhook URL
```

#### 步骤2：配置application.yml
```yaml
bloom-filter:
  monitoring:
    dingtalk-alert-enabled: true
    dingtalk-webhook-url: "https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN"
```

#### 步骤3：测试告警
```bash
curl -X POST http://localhost:8200/api/monitor/alert/test
```

### 2. 企业微信告警配置

#### 步骤1：创建企业微信机器人
```bash
1. 在企业微信群中点击"群聊信息" → "群机器人" → "添加机器人"
2. 选择"Webhook机器人"
3. 设置机器人名称：布隆过滤器监控
4. 复制Webhook URL
```

#### 步骤2：配置application.yml
```yaml
bloom-filter:
  monitoring:
    wechat-alert-enabled: true
    wechat-webhook-url: "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=YOUR_KEY"
```

### 3. 邮件告警配置

#### 步骤1：配置邮件服务
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: your-email-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### 步骤2：启用邮件告警
```yaml
bloom-filter:
  monitoring:
    email-alert-enabled: true
```

## 🔧 **API接口使用**

### 1. 获取布隆过滤器状态
```bash
GET /api/monitor/bloom-filter/status

# 响应示例
{
  "success": true,
  "data": {
    "usageRatio": 0.75,
    "usagePercentage": "75.0%",
    "loadedUrlCount": 37500,
    "expectedInsertions": 50000,
    "alertLevel": {
      "name": "警告",
      "level": 2,
      "description": "使用率较高，需要关注"
    },
    "alertRecommendation": "建议在非高峰期重建布隆过滤器"
  }
}
```

### 2. 手动触发状态检查
```bash
POST /api/monitor/bloom-filter/check
```

### 3. 手动重建布隆过滤器
```bash
POST /api/monitor/bloom-filter/rebuild
```

### 4. 发送测试告警
```bash
POST /api/monitor/alert/test
```

### 5. 健康检查
```bash
GET /api/monitor/health
```

## 📊 **监控面板集成**

### 1. 独立监控页面
访问：`http://localhost:8200/bloom-filter-monitor.html`

### 2. Vue组件集成
```vue
<template>
  <BloomFilterMonitor 
    :auto-refresh="true"
    :refresh-interval="30000"
    @alert="handleAlert"
  />
</template>
```

### 3. 状态栏集成
```html
<script src="/js/bloom-filter-status-bar.js"></script>
<script>
  BloomFilterStatusBar.init({
    position: 'top-right',
    autoRefresh: true
  });
</script>
```

## ⚡ **智能告警策略**

### 告警级别与频率
```yaml
告警级别配置:
  正常 (< 50%): 不发送告警
  注意 (50-70%): 每小时最多1次
  警告 (70-90%): 每30分钟最多1次  
  危险 (≥ 90%): 每15分钟最多1次
```

### 动态检查间隔
```javascript
// 系统会根据告警级别自动调整检查频率
switch(alertLevel) {
    case '危险': interval = 1分钟; break;
    case '警告': interval = 2分钟; break;
    case '注意': interval = 5分钟; break;
    default: interval = 15分钟;
}
```

## 🛠️ **部署配置**

### 生产环境配置
```yaml
# application-prod.yml
bloom-filter:
  monitoring:
    enabled: true
    check-interval-minutes: 10
    dingtalk-alert-enabled: true
    dingtalk-webhook-url: "${DINGTALK_WEBHOOK_URL}"
    wechat-alert-enabled: true
    wechat-webhook-url: "${WECHAT_WEBHOOK_URL}"

logging:
  level:
    com.zheng.aicommunitybackend.task.BloomFilterMonitorTask: info
  file:
    name: logs/monitoring.log
```

### 环境变量配置
```bash
# 设置环境变量
export DINGTALK_WEBHOOK_URL="https://oapi.dingtalk.com/robot/send?access_token=xxx"
export WECHAT_WEBHOOK_URL="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx"
```

## 📈 **监控效果验证**

### 1. 功能测试清单
- [ ] 钉钉告警接收正常
- [ ] 企业微信告警接收正常
- [ ] 邮件告警接收正常（如果启用）
- [ ] API接口响应正常
- [ ] 监控面板显示正常
- [ ] 自动检查任务运行正常

### 2. 告警测试命令
```bash
# 发送测试告警
curl -X POST http://localhost:8200/api/monitor/alert/test

# 检查当前状态
curl http://localhost:8200/api/monitor/bloom-filter/status

# 健康检查
curl http://localhost:8200/api/monitor/health
```

## 🔍 **故障排查**

### 常见问题及解决方案

#### 1. 钉钉告警发送失败
```bash
# 检查日志
tail -f logs/ai-community-backend.log | grep "钉钉告警"

# 可能原因：
- Webhook URL配置错误
- 网络连接问题
- 机器人权限不足
```

#### 2. 企业微信告警发送失败
```bash
# 检查配置
curl -X POST "${WECHAT_WEBHOOK_URL}" \
  -H "Content-Type: application/json" \
  -d '{"msgtype":"text","text":{"content":"测试消息"}}'
```

#### 3. 监控任务未运行
```bash
# 检查定时任务状态
curl http://localhost:8200/api/monitor/config
```

## 📋 **最佳实践**

### 1. 告警配置建议
- **开发环境**：只启用日志告警
- **测试环境**：启用钉钉告警
- **生产环境**：启用钉钉 + 企业微信告警

### 2. 安全建议
- 定期更换Webhook URL的access_token
- 使用环境变量存储敏感配置
- 限制机器人的权限范围

### 3. 性能优化
- 合理设置检查间隔，避免过于频繁
- 使用告警静默期，防止告警风暴
- 定期清理历史日志文件

## 🎯 **监控价值**

### 升级后的监控优势
1. **实时性**：问题发生时立即通知
2. **可视化**：直观的监控面板
3. **自动化**：无需人工查看日志
4. **可扩展**：支持多种告警渠道
5. **可维护**：完整的API接口

### 业务价值
- 提前发现性能问题
- 减少系统故障时间
- 提高运维效率
- 保障服务稳定性
