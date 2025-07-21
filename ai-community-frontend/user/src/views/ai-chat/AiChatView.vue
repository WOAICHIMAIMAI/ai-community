<template>
  <div class="ai-chat-container">
    <van-nav-bar 
      title="AI助手" 
      left-text="返回" 
      left-arrow 
      @click-left="handleBack"
      fixed
    />
    
    <div class="chat-content" ref="messageContainer">
      <div class="message-list">
        <div 
          v-for="(message, index) in messageList" 
          :key="index"
          class="message-item"
          :class="message.role"
        >
          <div v-if="message.role === 'user'" class="user-message">
            <div class="message-content">{{ message.content }}</div>
            <van-image
              :src="userAvatar"
              width="40"
              height="40"
              round
              class="avatar"
            />
          </div>
          <div v-else class="ai-message">
            <van-image
              src="https://img.yzcdn.cn/vant/cat.jpeg"
              width="40"
              height="40"
              round
              class="avatar"
            />
            <div class="message-content markdown-content" v-html="renderMarkdown(message.content)"></div>
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-message">
          <van-image
            src="https://img.yzcdn.cn/vant/cat.jpeg"
            width="40"
            height="40"
            round
            class="avatar"
          />
          <div class="message-content">
            <van-loading size="16" color="#1989fa">AI正在思考中...</van-loading>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <van-field
        v-model="inputContent"
        placeholder="请输入您的问题..."
        type="textarea"
        rows="1"
        autosize
        maxlength="500"
        show-word-limit
        @keyup.enter="handleSend"
      />
      <van-button 
        type="primary" 
        size="small"
        :disabled="!inputContent.trim() || isLoading"
        @click="handleSend"
        class="send-btn"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { Image, NavBar, Field, Button, Loading } from 'vant';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showFailToast } from 'vant';
import { v4 as uuidv4 } from 'uuid';
import { marked } from 'marked';
import { aiChatApi } from '@/api/aiChatApi';
import { useAuthStore } from '@/store/auth';

// 配置marked选项
marked.setOptions({
  breaks: true,
  gfm: true
});

// 渲染Markdown
const renderMarkdown = (content: string) => {
  try {
    return marked(content);
  } catch (error) {
    console.error('Markdown渲染失败:', error);
    return content;
  }
};

// 类型定义
interface Message {
  role: 'user' | 'ai';
  content: string;
}

// 状态管理
const messageList = ref<Message[]>([]);
const inputContent = ref('');
const isLoading = ref(false);
const messageContainer = ref<HTMLElement | null>(null);
const userAvatar = ref('https://img.yzcdn.cn/vant/user.png');
const chatId = ref('');

// 路由和存储
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

// 处理返回
const handleBack = () => {
  // 优先返回到聊天列表页面
  if (route.query.from === 'list' || document.referrer.includes('/ai-chat-list')) {
    router.push('/ai-chat-list');
  } else {
    // 如果是从其他页面进入，返回上一页
    router.back();
  }
};

// 初始化聊天ID
const initChatId = () => {
  // 从路由参数获取chatId，如果没有则生成新的
  const routeChatId = route.query.chatId as string;
  if (routeChatId) {
    chatId.value = routeChatId;
  } else {
    chatId.value = uuidv4();
    // 更新路由，添加chatId参数和新会话标识
    router.replace({
      path: route.path,
      query: { ...route.query, chatId: chatId.value, isNew: 'true' }
    });
  }
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
};

// 加载聊天记录
const loadChatHistory = async () => {
  try {
    const response = await aiChatApi.getChatMessages(chatId.value, 50);
    if (response.code === 200 && response.data && response.data.length > 0) {
      messageList.value = response.data.map((msg: any) => ({
        role: msg.messageType === 'USER' ? 'user' : 'ai',
        content: msg.content
      }));
      scrollToBottom();
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error);
  }
};

// 发送消息
const handleSend = async () => {
  const content = inputContent.value.trim();
  if (!content || isLoading.value) return;

  // 添加用户消息
  messageList.value.push({
    role: 'user',
    content
  });
  inputContent.value = '';
  isLoading.value = true;
  scrollToBottom();

  try {
    // 调用API获取AI回复
    const response = await aiChatApi.sendMessage(content, chatId.value);
    if (response.code === 200) {
      // 添加AI回复
      messageList.value.push({
        role: 'ai',
        content: response.data
      });
    } else {
      messageList.value.push({
        role: 'ai',
        content: '抱歉，获取回复失败，请稍后再试。'
      });
      showFailToast(response.msg || '发送失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    messageList.value.push({
      role: 'ai',
      content: '网络错误，请检查网络连接后重试。'
    });
    showFailToast('网络错误，请重试');
  } finally {
    isLoading.value = false;
    scrollToBottom();
  }
};

// 发送隐式自我介绍请求
const sendIntroductionPrompt = async () => {
  try {
    const introPrompt = "请简单介绍一下你自己，包括你的名字、身份以及你能为社区居民提供哪些服务和帮助。请用温馨友好的语气，控制在100字以内。";
    
    const response = await aiChatApi.sendMessage(introPrompt, chatId.value);
    if (response.code === 200) {
      // 添加AI的自我介绍到消息列表
      messageList.value.push({
        role: 'ai',
        content: response.data
      });
      scrollToBottom();
    }
  } catch (error) {
    console.error('获取AI自我介绍失败:', error);
    // 如果请求失败，显示默认欢迎消息
    messageList.value.push({
      role: 'ai',
      content: '您好！我是小智，您的AI社区管家助手。我可以帮您处理物业报修、查询便民信息、解答社区相关问题等。有什么需要帮助的吗？'
    });
    scrollToBottom();
  }
};

// 组件挂载
onMounted(async () => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  // 初始化聊天ID
  initChatId();
  
  // 加载聊天记录
  await loadChatHistory();

  // 判断是否为新会话（没有历史记录或者有isNew标识）
  const isNewSession = messageList.value.length === 0 || route.query.isNew === 'true';
  
  if (isNewSession) {
    // 发送隐式自我介绍请求
    await sendIntroductionPrompt();
    
    // 清除isNew标识，避免刷新页面时重复发送
    if (route.query.isNew) {
      router.replace({
        path: route.path,
        query: { ...route.query, isNew: undefined }
      });
    }
  }
});
</script>

<style scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  position: relative;
}

.ai-chat-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="warm-dots" width="40" height="40" patternUnits="userSpaceOnUse"><circle cx="20" cy="20" r="1.5" fill="rgba(79,192,141,0.06)"/><circle cx="10" cy="30" r="1" fill="rgba(255,193,7,0.04)"/></pattern></defs><rect width="100" height="100" fill="url(%23warm-dots)"/></svg>');
  pointer-events: none;
}

:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(79, 192, 141, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 70px 16px 90px;
  position: relative;
  z-index: 1;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  animation: messageSlideIn 0.4s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
  margin-left: auto;
}

.user-message .message-content {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  color: white;
  max-width: 75%;
  padding: 14px 18px;
  border-radius: 18px 18px 6px 18px;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 15px;
  box-shadow: 0 3px 12px rgba(79, 192, 141, 0.25);
  position: relative;
}

.message-item.user {
  display: flex;
  justify-content: flex-end;
  width: 100%;
}

.ai-message, .loading-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 12px;
}

.message-content {
  max-width: 75%;
  padding: 14px 18px;
  border-radius: 18px;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 15px;
}

.ai-message .message-content,
.loading-message .message-content {
  background: #ffffff;
  color: #2c3e50;
  border-radius: 18px 18px 18px 6px;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  position: relative;
}

.avatar {
  flex-shrink: 0;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 16px;
  border-top: 1px solid rgba(79, 192, 141, 0.1);
  display: flex;
  align-items: flex-end;
  gap: 12px;
  z-index: 100;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.06);
}

.input-area :deep(.van-field) {
  flex: 1;
  background: #f8f9fa;
  border-radius: 20px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  
  &:focus-within {
    border-color: rgba(79, 192, 141, 0.3);
    background: #ffffff;
    box-shadow: 0 0 12px rgba(79, 192, 141, 0.15);
  }
  
  .van-field__control {
    color: #2c3e50;
  }
}

.send-btn {
  border-radius: 20px;
  min-width: 70px;
  height: 44px;
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  border: none;
  font-weight: 500;
  box-shadow: 0 3px 12px rgba(79, 192, 141, 0.25);
  transition: all 0.3s ease;
  color: white;
  
  &:active {
    transform: translateY(1px);
    box-shadow: 0 2px 8px rgba(79, 192, 141, 0.3);
  }
  
  &:disabled {
    background: #dee2e6;
    box-shadow: none;
    color: #6c757d;
  }
}

/* 滚动条样式 */
.chat-content::-webkit-scrollbar {
  width: 4px;
}

.chat-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-content::-webkit-scrollbar-thumb {
  background: rgba(79, 192, 141, 0.3);
  border-radius: 2px;
  transition: background 0.3s ease;
}

.chat-content::-webkit-scrollbar-thumb:hover {
  background: rgba(79, 192, 141, 0.5);
}

/* Markdown样式优化 */
.markdown-content {
  line-height: 1.6;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 14px 0 10px 0;
  font-weight: 600;
  color: #2c3e50;
}

.markdown-content :deep(h1) { 
  font-size: 18px;
  border-bottom: 2px solid rgba(79, 192, 141, 0.2);
  padding-bottom: 6px;
}
.markdown-content :deep(h2) { 
  font-size: 17px;
  color: #495057;
}
.markdown-content :deep(h3) { 
  font-size: 16px;
  color: #495057;
}

.markdown-content :deep(p) {
  margin: 10px 0;
  color: #2c3e50;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
  color: #2c3e50;
}

.markdown-content :deep(ul li::marker) {
  color: #4fc08d;
}

.markdown-content :deep(code) {
  background: #f8f9fa;
  padding: 3px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 13px;
  color: #e74c3c;
  border: 1px solid #e9ecef;
}

.markdown-content :deep(pre) {
  background: #2c3e50;
  border: none;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  overflow-x: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  color: #ecf0f1;
  border: none;
  font-size: 13px;
}

.markdown-content :deep(blockquote) {
  border-left: 3px solid #4fc08d;
  padding: 12px 16px;
  margin: 12px 0;
  color: #495057;
  background: rgba(79, 192, 141, 0.05);
  border-radius: 0 8px 8px 0;
  font-style: italic;
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #e9ecef;
  padding: 10px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  color: white;
  font-weight: 500;
}

.markdown-content :deep(tr:nth-child(even)) {
  background-color: #f8f9fa;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: #2c3e50;
  background: rgba(79, 192, 141, 0.1);
  padding: 1px 3px;
  border-radius: 3px;
}

.markdown-content :deep(em) {
  font-style: italic;
  color: #6c757d;
}

.markdown-content :deep(a) {
  color: #4fc08d;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.markdown-content :deep(a:hover) {
  border-bottom-color: #4fc08d;
  background: rgba(79, 192, 141, 0.08);
  padding: 1px 3px;
  border-radius: 3px;
}
</style>
