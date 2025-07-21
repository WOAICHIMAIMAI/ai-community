# 动态会话头像实现

## 问题分析

### 原有问题
1. **固定头像问题**: 会话头像存储在数据库中，所有用户看到的都是同一个头像
2. **用户体验差**: 私聊会话应该显示对方用户的头像，而不是固定的头像
3. **逻辑不合理**: 发送者和接收者看到的会话头像应该不同

### 解决方案
实现动态头像解析：
- **私聊会话**: 显示对方用户的头像
- **群聊会话**: 使用会话自身的固定头像

## 实现细节

### 后端修改

#### 1. 核心逻辑
```java
private void setConversationAvatarOptimized(ConversationVO vo, UserConversations conversation, 
                                          Long currentUserId, Map<String, Users> otherUsersMap) {
    if (conversation.getConversationType() == 1) {
        // 私聊：使用对方用户头像
        Users otherUser = otherUsersMap.get(conversation.getId());
        if (otherUser != null) {
            vo.setAvatarUrl(otherUser.getAvatarUrl());
            vo.setConversationName(otherUser.getNickname() || otherUser.getUsername());
        }
    } else {
        // 群聊：使用会话固定头像
        vo.setAvatarUrl(conversation.getAvatarUrl());
    }
}
```

#### 2. 性能优化
- **批量查询**: `batchGetOtherUsersForPrivateChats()` 方法
- **减少数据库查询**: 一次性获取所有对方用户信息
- **内存映射**: 使用 Map 缓存用户信息

#### 3. 调试信息
添加了详细的调试日志，便于排查问题：
```java
System.out.println("批量设置私聊头像 - 会话ID: " + conversation.getId() + 
                 ", 当前用户: " + currentUserId + 
                 ", 对方用户: " + otherUser.getId() + 
                 ", 对方头像: " + otherUser.getAvatarUrl() + 
                 ", 对方昵称: " + displayName);
```

### 前端支持

#### 1. 头像获取逻辑
```typescript
const getConversationAvatar = (conversation: Conversation) => {
  console.log('获取会话头像:', {
    conversationId: conversation.conversationId,
    conversationType: conversation.conversationType,
    avatarUrl: conversation.avatarUrl
  });
  
  if (conversation.avatarUrl) {
    return conversation.avatarUrl; // 后端动态设置的头像
  }
  // 降级到默认头像
  return conversation.conversationType === 2 
    ? 'https://img.yzcdn.cn/vant/user-group.png' 
    : 'https://img.yzcdn.cn/vant/user.png';
};
```

#### 2. 调试信息
添加了控制台日志，便于调试：
- 会话列表获取日志
- 头像解析日志

## 测试验证

### 测试场景

#### 场景1：用户A查看会话列表
1. 用户A登录系统
2. 查看会话列表
3. **预期结果**:
   - 与用户B的私聊显示用户B的头像
   - 与用户C的私聊显示用户C的头像
   - 群聊显示群聊默认头像

#### 场景2：用户B查看同一会话
1. 用户B登录系统
2. 查看会话列表
3. **预期结果**:
   - 与用户A的私聊显示用户A的头像
   - 会话名称显示为用户A的昵称

#### 场景3：用户更新头像
1. 用户A更新头像
2. 用户B查看会话列表
3. **预期结果**:
   - 与用户A的私聊头像自动更新为新头像

### 调试步骤

#### 1. 后端调试
查看控制台输出：
```
批量设置私聊头像 - 会话ID: xxx, 当前用户: 123, 对方用户: 456, 对方头像: http://..., 对方昵称: 张三
```

#### 2. 前端调试
查看浏览器控制台：
```javascript
获取到的会话列表: [
  {
    conversationId: "xxx",
    conversationType: 1,
    avatarUrl: "http://...",
    conversationName: "张三"
  }
]

获取会话头像: {
  conversationId: "xxx",
  conversationType: 1,
  avatarUrl: "http://..."
}
```

#### 3. 网络请求检查
检查 `/user/conversations` API 响应：
```json
{
  "code": 200,
  "data": [
    {
      "conversationId": "xxx",
      "conversationType": 1,
      "avatarUrl": "http://对方用户头像URL",
      "conversationName": "对方用户昵称"
    }
  ]
}
```

## 预期效果

### 私聊会话
- ✅ **动态头像**: 每个用户看到的都是对方的头像
- ✅ **动态名称**: 显示对方的昵称
- ✅ **实时更新**: 对方更新头像后自动同步

### 群聊会话
- ✅ **固定头像**: 使用群聊设置的头像
- ✅ **固定名称**: 使用群聊名称
- ✅ **不受影响**: 私聊逻辑不影响群聊

## 性能优势

### 1. 批量查询优化
- 避免 N+1 查询问题
- 一次性获取所有需要的用户信息
- 减少数据库连接开销

### 2. 内存缓存
- 使用 Map 缓存用户信息
- 避免重复查询相同用户
- 提升响应速度

### 3. 前端缓存
- 浏览器自动缓存头像图片
- 减少网络请求
- 提升用户体验

## 注意事项

### 1. 数据一致性
- 确保用户信息更新时会话头像同步
- 处理用户删除头像的情况
- 处理用户不存在的异常情况

### 2. 性能考虑
- 监控批量查询的性能
- 考虑添加 Redis 缓存
- 优化图片加载策略

### 3. 错误处理
- 头像加载失败时的降级策略
- 用户信息缺失时的默认处理
- 网络异常时的用户提示
