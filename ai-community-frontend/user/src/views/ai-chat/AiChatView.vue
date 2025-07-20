<template>
  <div class="ai-chat-container">
    <van-nav-bar 
      title="AI助手" 
      left-text="返回" 
      left-arrow 
      @click-left="$router.back()"
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
            <div class="message-content">{{ message.content }}</div>
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
import { aiChatApi } from '@/api/aiChatApi';
import { useAuthStore } from '@/store/auth';

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

// 初始化聊天ID
const initChatId = () => {
  // 从路由参数获取chatId，如果没有则生成新的
  const routeChatId = route.query.chatId as string;
  if (routeChatId) {
    chatId.value = routeChatId;
  } else {
    chatId.value = uuidv4();
    // 更新路由，添加chatId参数
    router.replace({
      path: route.path,
      query: { ...route.query, chatId: chatId.value }
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
    if (response.success && response.data && response.data.length > 0) {
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

// 组件挂载
onMounted(() => {
  // 检查登录状态
  if (!authStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  // 初始化聊天ID
  initChatId();
  
  // 加载聊天记录
  loadChatHistory();

  // 添加欢迎消息（如果是新会话）
  if (messageList.value.length === 0) {
    messageList.value.push({
      role: 'ai',
      content: '您好！我是AI助手，有什么可以帮助您的吗？'
    });
  }
});
</script>

<style scoped>
.ai-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 60px 16px 80px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  margin-left: auto;
}

.user-message .message-content {
  background-color: #1989fa;
  color: white;
  border-bottom-right-radius: 4px;
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.4;
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
  gap: 8px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.4;
}

.ai-message .message-content,
.loading-message .message-content {
  background-color: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.avatar {
  flex-shrink: 0;
}

.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  z-index: 100;
}

.input-area .van-field {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 20px;
  border: none;
}

.send-btn {
  border-radius: 20px;
  min-width: 60px;
}

/* 滚动条样式 */
.chat-content::-webkit-scrollbar {
  width: 4px;
}

.chat-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}
</style>