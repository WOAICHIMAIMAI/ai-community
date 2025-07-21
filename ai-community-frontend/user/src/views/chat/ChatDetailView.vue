<template>
  <div class="chat-detail-container">
    <van-nav-bar 
      :title="conversationInfo?.conversationName || '聊天'"
      left-text="返回" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    >
      <template #right>
        <van-icon name="setting-o" @click="showSettings = true" />
      </template>
    </van-nav-bar>
    
    <div class="chat-content" ref="messageContainer">
      <div class="message-list">
        <div
          v-for="(message, index) in messageList"
          :key="message.id"
          class="message-item"
          :class="{ 'own-message': isOwnMessage(message) }"
          :data-is-own="isOwnMessage(message)"
          :data-sender-id="message.senderId"
          :data-current-user-id="currentUserId"
        >
          <div v-if="!isOwnMessage(message)" class="other-message">
            <van-image
              :src="message.senderAvatarUrl || 'https://img.yzcdn.cn/vant/user.png'"
              width="40"
              height="40"
              round
              class="avatar"
            />
            <div class="message-bubble">
              <div class="sender-name">{{ getSenderDisplayName(message) }}</div>
              <div class="message-content" v-html="renderMessage(message)"></div>
              <div class="message-time">{{ formatMessageTime(message.createTime) }}</div>
            </div>
          </div>
          
          <div v-else class="own-message-content">
            <div class="message-bubble own">
              <div class="message-content" v-html="renderMessage(message)"></div>
              <div class="message-time">{{ formatMessageTime(message.createTime) }}</div>
            </div>
            <van-image
              :src="userAvatar"
              width="40"
              height="40"
              round
              class="avatar"
            />
          </div>
        </div>
        
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-message">
          <van-loading size="16" color="#4fc08d">发送中...</van-loading>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <van-field
        v-model="inputContent"
        placeholder="请输入消息..."
        type="textarea"
        rows="1"
        autosize
        maxlength="2000"
        show-word-limit
        @keyup.enter="handleSend"
      />
      <div class="input-actions">
        <van-button 
          icon="photograph" 
          size="small" 
          @click="selectImage"
          class="action-btn"
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

    <!-- 设置弹窗 -->
    <van-popup v-model:show="showSettings" position="bottom" :style="{ height: '40%' }">
      <div class="settings-dialog">
        <div class="dialog-header">
          <span class="dialog-title">会话设置</span>
          <van-icon name="cross" @click="showSettings = false" />
        </div>
        
        <van-cell-group>
          <van-cell title="会话成员" :value="`${conversationInfo?.memberCount || 0}人`" is-link />
          <van-cell title="查找聊天记录" is-link />
          <van-cell title="清空聊天记录" is-link @click="clearMessages" />
          <van-cell 
            title="删除会话" 
            is-link 
            @click="deleteConversation"
            class="danger-cell"
          />
        </van-cell-group>
      </div>
    </van-popup>

    <!-- 图片选择器 -->
    <input 
      ref="imageInput" 
      type="file" 
      accept="image/*" 
      style="display: none" 
      @change="handleImageSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showFailToast, showConfirmDialog } from 'vant';
import { chatApi } from '@/api/chatApi';
import { useAuthStore } from '@/store/auth';

// 类型定义
interface Message {
  id: number;
  conversationId: string;
  senderId: number;
  senderNickname: string;
  senderAvatarUrl?: string;
  messageType: string;
  content: string;
  metadata?: string;
  status: number;
  readCount: number;
  isRead: boolean;
  createTime: string;
}

interface ConversationInfo {
  conversationId: string;
  conversationName: string;
  conversationType: number;
  description?: string;
  avatarUrl?: string;
  memberCount: number;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
  createTime: string;
}

// 状态管理
const messageList = ref<Message[]>([]);
const inputContent = ref('');
const isLoading = ref(false);
const showSettings = ref(false);
const conversationInfo = ref<ConversationInfo | null>(null);
const messageContainer = ref<HTMLElement | null>(null);
const imageInput = ref<HTMLInputElement | null>(null);
// 使用计算属性获取当前用户头像
const userAvatar = computed(() => {
  return authStore.userInfo?.avatarUrl || 'https://img.yzcdn.cn/vant/user.png';
});

// 路由和存储
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 计算属性
const conversationId = computed(() => route.params.id as string);
const currentUserId = computed(() => {
  let userId = authStore.userInfo?.id;

  // 如果 authStore 中没有用户信息，尝试从 localStorage 获取
  if (!userId) {
    try {
      const localUserInfo = localStorage.getItem('userInfo');
      if (localUserInfo) {
        const userInfo = JSON.parse(localUserInfo);
        userId = userInfo.id;
        console.log('从 localStorage 获取用户ID:', userId);
        // 如果 authStore 为空但 localStorage 有数据，更新 authStore
        if (!authStore.userInfo && userInfo) {
          authStore.setUserInfo(userInfo);
        }
      }
    } catch (error) {
      console.error('解析 localStorage 用户信息失败:', error);
    }
  }

  console.log('当前用户ID计算:', {
    userId: userId,
    type: typeof userId,
    authStore: authStore.userInfo,
    isLoggedIn: authStore.isLoggedIn,
    token: authStore.token ? '有token' : '无token'
  });
  return userId;
});

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
};

// 获取会话信息
const fetchConversationInfo = async () => {
  try {
    if (!conversationId.value || conversationId.value === 'null') {
      console.error('会话ID无效:', conversationId.value);
      showFailToast('会话ID无效');
      router.back();
      return;
    }

    const response = await chatApi.getConversationDetail(conversationId.value);
    if (response.code === 200) {
      conversationInfo.value = response.data;
    } else {
      showFailToast(response.msg || '获取会话信息失败');
    }
  } catch (error) {
    console.error('获取会话信息失败:', error);
    showFailToast('获取会话信息失败');
  }
};

// 获取消息列表
const fetchMessages = async (page = 1) => {
  try {
    if (!conversationId.value || conversationId.value === 'null') {
      console.error('会话ID无效:', conversationId.value);
      return;
    }

    const response = await chatApi.getMessages(conversationId.value, page, 50);
    if (response.code === 200) {
      // 确保 senderId 为数字类型
      const messages = (response.data || []).map((msg: any) => ({
        ...msg,
        senderId: Number(msg.senderId)
      }));
      messageList.value = messages;

      console.log('获取到的消息列表:', messages);
      console.log('当前用户ID:', currentUserId.value);
      messages.forEach(msg => {
        console.log(`消息 ${msg.id}: senderId=${msg.senderId}, currentUserId=${currentUserId.value}, isOwn=${msg.senderId === currentUserId.value}`);
      });
      scrollToBottom();
    } else {
      showFailToast(response.msg || '获取消息失败');
    }
  } catch (error) {
    console.error('获取消息失败:', error);
    showFailToast('获取消息失败');
  }
};

// 发送消息
const handleSend = async () => {
  const content = inputContent.value.trim();
  if (!content || isLoading.value) return;

  const tempMessage: Message = {
    id: Date.now(),
    conversationId: conversationId.value,
    senderId: Number(currentUserId.value!),
    senderNickname: authStore.userInfo?.nickname || '我',
    senderAvatarUrl: authStore.userInfo?.avatarUrl,
    messageType: 'text',
    content,
    status: 1,
    readCount: 0,
    isRead: true,
    createTime: new Date().toISOString()
  };

  console.log('发送临时消息:', tempMessage);
  console.log('当前用户ID:', currentUserId.value);
  console.log('是否为自己的消息:', isOwnMessage(tempMessage));

  messageList.value.push(tempMessage);
  inputContent.value = '';
  isLoading.value = true;
  scrollToBottom();

  try {
    const response = await chatApi.sendMessage(conversationId.value, {
      conversationId: conversationId.value,
      content,
      messageType: 'text'
    });
    
    if (response.code === 200) {
      // 更新临时消息的ID
      const index = messageList.value.findIndex(msg => msg.id === tempMessage.id);
      if (index !== -1) {
        messageList.value[index].id = response.data;
      }
    } else {
      // 发送失败，移除临时消息
      const index = messageList.value.findIndex(msg => msg.id === tempMessage.id);
      if (index !== -1) {
        messageList.value.splice(index, 1);
      }
      showFailToast(response.msg || '发送失败');
    }
  } catch (error) {
    console.error('发送消息失败:', error);
    // 发送失败，移除临时消息
    const index = messageList.value.findIndex(msg => msg.id === tempMessage.id);
    if (index !== -1) {
      messageList.value.splice(index, 1);
    }
    showFailToast('发送失败');
  } finally {
    isLoading.value = false;
  }
};

// 选择图片
const selectImage = () => {
  imageInput.value?.click();
};

// 处理图片选择
const handleImageSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 这里应该上传图片到服务器，然后发送图片消息
  // 暂时显示提示
  showToast('图片功能开发中...');
};

// 渲染消息内容
const renderMessage = (message: Message) => {
  if (message.messageType === 'text') {
    return message.content.replace(/\n/g, '<br>');
  }
  // 其他类型消息的渲染逻辑
  return message.content;
};

// 格式化消息时间
const formatMessageTime = (timeString: string) => {
  const date = new Date(timeString);
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 判断消息是否为当前用户发送
const isOwnMessage = (message: Message) => {
  const userId = currentUserId.value;
  const senderId = message.senderId;

  if (!userId || !senderId) {
    console.warn('用户ID或发送者ID为空:', { userId, senderId, authStore: authStore.userInfo });
    return false;
  }

  // 确保类型一致的比较
  const isOwn = Number(senderId) === Number(userId);

  // 简化的调试信息
  console.log(`消息 ${message.id}: senderId=${senderId}, currentUserId=${userId}, isOwn=${isOwn}`);

  return isOwn;
};

// 获取发送者显示名称
const getSenderDisplayName = (message: Message) => {
  if (isOwnMessage(message)) {
    return '我';
  }
  return message.senderNickname || '未知用户';
};

// 强制刷新用户信息
const refreshUserInfo = async () => {
  try {
    const { getUserInfo } = await import('@/api/user');
    const response = await getUserInfo();
    if (response.code === 200 && response.data) {
      authStore.setUserInfo(response.data);
      console.log('用户信息已刷新:', authStore.userInfo);
      return true;
    }
  } catch (error) {
    console.error('刷新用户信息失败:', error);
  }
  return false;
};

// 清空聊天记录
const clearMessages = async () => {
  try {
    await showConfirmDialog({
      title: '确认清空',
      message: '确定要清空所有聊天记录吗？此操作不可恢复。'
    });
    
    // 这里应该调用清空消息的API
    messageList.value = [];
    showToast('聊天记录已清空');
  } catch (error) {
    // 用户取消
  }
};

// 删除会话
const deleteConversation = async () => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这个会话吗？此操作不可恢复。'
    });
    
    const response = await chatApi.deleteConversation(conversationId.value);
    if (response.code === 200) {
      showToast('会话已删除');
      router.back();
    } else {
      showFailToast(response.msg || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error);
      showFailToast('删除失败');
    }
  }
};

// 组件挂载
onMounted(async () => {
  console.log('ChatDetailView mounted, route params:', route.params);
  console.log('conversationId:', conversationId.value);
  console.log('authStore.userInfo:', authStore.userInfo);
  console.log('currentUserId:', currentUserId.value);

  if (!authStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }

  // 如果用户信息为空，尝试重新获取
  if (!authStore.userInfo || !authStore.userInfo.id) {
    console.log('用户信息为空，尝试重新获取...');
    const success = await refreshUserInfo();
    if (!success) {
      showFailToast('获取用户信息失败');
      router.push('/login');
      return;
    }
  }

  // 最终检查用户ID
  if (!currentUserId.value) {
    console.error('无法获取当前用户ID');
    showFailToast('用户信息异常，请重新登录');
    router.push('/login');
    return;
  }

  console.log('最终用户ID:', currentUserId.value);

  if (!conversationId.value || conversationId.value === 'null') {
    console.error('会话ID无效，无法加载聊天详情');
    showFailToast('会话ID无效');
    router.back();
    return;
  }

  await fetchConversationInfo();
  await fetchMessages();
});
</script>

<style scoped>
.chat-detail-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
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
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  animation: messageSlideIn 0.3s ease-out;
  width: 100%;
  margin-bottom: 16px;
}

/* 对方的消息 - 左对齐 */
.message-item:not(.own-message) {
  display: flex;
  justify-content: flex-start;
}

/* 自己的消息 - 右对齐 */
.message-item.own-message {
  display: flex !important;
  justify-content: flex-end !important;
}

/* 确保自己的消息不显示对方消息的样式 */
.message-item.own-message .other-message {
  display: none !important;
}

/* 确保自己的消息内容容器显示 */
.message-item.own-message .own-message-content {
  display: flex !important;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.other-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

/* 自己消息的内容容器 */
.own-message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 80%;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.other-message .message-bubble {
  background: #ffffff;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-bubble.own {
  background: linear-gradient(135deg, #07c160 0%, #4fc08d 100%) !important;
  color: white !important;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(7, 193, 96, 0.4);
}

/* 强制确保自己消息的文字颜色 */
.message-bubble.own .message-content {
  color: white !important;
}

.message-bubble.own .message-time {
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 强制自己的消息样式 */
.message-item.own-message .own-message-content {
  display: flex !important;
  justify-content: flex-end !important;
  align-items: flex-start !important;
  gap: 12px !important;
  max-width: 80% !important;
}

.message-item.own-message .other-message {
  display: none !important;
}

/* 确保对方消息的样式 */
.message-item:not(.own-message) .own-message-content {
  display: none !important;
}

.message-item:not(.own-message) .other-message {
  display: flex !important;
}



.sender-name {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
}

.message-content {
  font-size: 15px;
  line-height: 1.4;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
  text-align: right;
}

.other-message .message-time {
  color: #adb5bd;
  text-align: left;
}

.avatar {
  flex-shrink: 0;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-message {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  gap: 12px;
}

.action-btn {
  background: #f8f9fa;
  border: none;
  color: #6c757d;
}

.send-btn {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  border: none;
  border-radius: 16px;
  min-width: 70px;
}

:deep(.van-field) {
  background: #f8f9fa;
  border-radius: 20px;
  border: 1px solid #e9ecef;
}

.settings-dialog {
  padding: 20px;
  height: 100%;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.danger-cell {
  color: #e74c3c;
}

.chat-content::-webkit-scrollbar {
  width: 4px;
}

.chat-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-content::-webkit-scrollbar-thumb {
  background: rgba(79, 192, 141, 0.3);
  border-radius: 2px;
}
</style>
