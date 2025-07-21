<template>
  <div class="ai-chat-list-container">
    <van-nav-bar 
      title="AI聊天记录" 
      left-text="返回" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />
    
    <div class="content">
      <!-- 新建聊天按钮 -->
      <div class="new-chat-section">
        <van-button 
          type="primary" 
          size="large" 
          block 
          @click="createNewChat"
          icon="plus"
        >
          新建聊天
        </van-button>
      </div>

      <!-- 聊天记录列表 -->
      <div class="chat-list">
        <template v-if="loading">
          <van-skeleton title :row="2" style="margin-bottom: 16px;" />
          <van-skeleton title :row="2" style="margin-bottom: 16px;" />
          <van-skeleton title :row="2" />
        </template>
        
        <template v-else-if="chatList.length === 0">
          <van-empty description="暂无聊天记录" />
        </template>
        
        <template v-else>
          <van-cell-group inset>
            <van-cell
              v-for="chat in chatList"
              :key="chat.chatId"
              :title="chat.title"
              :label="chat.lastMessage"
              :value="chat.updateTime"
              is-link
              @click="enterChat(chat.chatId)"
            >
              <template #icon>
                <van-image
                  src="https://img.yzcdn.cn/vant/cat.jpeg"
                  width="40"
                  height="40"
                  round
                  class="chat-avatar"
                />
              </template>
            </van-cell>
          </van-cell-group>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showFailToast } from 'vant';
import { v4 as uuidv4 } from 'uuid';
import { aiChatApi } from '@/api/aiChatApi';
import { useAuthStore } from '@/store/auth';

// 类型定义
interface ChatItem {
  chatId: string;
  title: string;
  lastMessage: string;
  updateTime: string;
  lastMessageTimestamp: number;
}

// 状态管理
const chatList = ref<ChatItem[]>([]);
const loading = ref(false);

// 路由和存储
const router = useRouter();
const authStore = useAuthStore();

// 获取聊天记录列表
const fetchChatList = async () => {
  loading.value = true;
  try {
    const response = await aiChatApi.getChatIds();
    if (response.code === 200 && response.data) {
      // 为每个chatId获取最后一条消息作为预览
      const chatPromises = response.data.map(async (chatId: string) => {
        try {
          const messagesRes = await aiChatApi.getChatMessages(chatId, 1);
          const lastMessage = messagesRes.code === 200 && messagesRes.data.length > 0 
            ? messagesRes.data[0].content 
            : '暂无消息';
          
          // 获取最后一条消息的时间戳用于排序
          const lastMessageTime = messagesRes.code === 200 && messagesRes.data.length > 0 
            ? new Date(messagesRes.data[0].createTime).getTime()
            : 0;
          
          return {
            chatId,
            title: `对话 ${chatId.substring(0, 8)}`,
            lastMessage: lastMessage.length > 30 ? lastMessage.substring(0, 30) + '...' : lastMessage,
            updateTime: messagesRes.code === 200 && messagesRes.data.length > 0 
              ? formatDate(messagesRes.data[0].createTime) 
              : '',
            lastMessageTimestamp: lastMessageTime // 用于排序的时间戳
          };
        } catch (error) {
          return {
            chatId,
            title: `对话 ${chatId.substring(0, 8)}`,
            lastMessage: '加载失败',
            updateTime: '',
            lastMessageTimestamp: 0
          };
        }
      });
      
      const chatResults = await Promise.all(chatPromises);
      
      // 按最后消息时间倒序排列（最新的在前面）
      chatList.value = chatResults.sort((a, b) => b.lastMessageTimestamp - a.lastMessageTimestamp);
    }
  } catch (error) {
    console.error('获取聊天记录失败:', error);
    showFailToast('获取聊天记录失败');
  } finally {
    loading.value = false;
  }
};

// 创建新聊天
const createNewChat = () => {
  const newChatId = uuidv4();
  router.push(`/ai-chat?chatId=${newChatId}&from=list&isNew=true`);
};

// 进入聊天
const enterChat = (chatId: string) => {
  router.push(`/ai-chat?chatId=${chatId}&from=list`);
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (days === 0) {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  } else if (days === 1) {
    return '昨天';
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return `${date.getMonth() + 1}-${date.getDate()}`;
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
  
  fetchChatList();
});
</script>

<style scoped>
.ai-chat-list-container {
  min-height: 100vh;
  padding-top: 46px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  position: relative;
}

.ai-chat-list-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="warm-pattern" width="60" height="60" patternUnits="userSpaceOnUse"><circle cx="30" cy="30" r="2" fill="rgba(79,192,141,0.08)"/><circle cx="15" cy="45" r="1" fill="rgba(255,193,7,0.06)"/></pattern></defs><rect width="100" height="100" fill="url(%23warm-pattern)"/></svg>');
  pointer-events: none;
}

:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(79, 192, 141, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.content {
  padding: 20px 16px;
  position: relative;
  z-index: 1;
}

.new-chat-section {
  margin-bottom: 24px;
  
  :deep(.van-button--primary) {
    background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
    border: none;
    border-radius: 20px;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    box-shadow: 0 4px 16px rgba(79, 192, 141, 0.25);
    transition: all 0.3s ease;
    color: white;
    
    &:active {
      transform: translateY(1px);
      box-shadow: 0 2px 8px rgba(79, 192, 141, 0.3);
    }
  }
}

.chat-list {
  :deep(.van-cell-group) {
    background: transparent;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  }
  
  :deep(.van-cell) {
    background: #ffffff;
    border-bottom: 1px solid #f0f0f0;
    padding: 18px 16px;
    transition: all 0.3s ease;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 4px;
      height: 100%;
      background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
      transform: scaleY(0);
      transition: transform 0.3s ease;
    }
    
    &:hover {
      background: #f8fffe;
      transform: translateX(4px);
      box-shadow: 0 2px 12px rgba(79, 192, 141, 0.1);
      
      &::before {
        transform: scaleY(1);
      }
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  :deep(.van-cell__title) {
    font-weight: 600;
    color: #2c3e50;
    font-size: 16px;
  }
  
  :deep(.van-cell__label) {
    color: #6c757d;
    font-size: 14px;
    margin-top: 4px;
    line-height: 1.4;
  }
  
  :deep(.van-cell__value) {
    color: #adb5bd;
    font-size: 12px;
    font-weight: 500;
  }
  
  .chat-avatar {
    margin-right: 12px;
    border: 2px solid rgba(79, 192, 141, 0.2);
    box-shadow: 0 2px 8px rgba(79, 192, 141, 0.15);
  }
}

:deep(.van-empty) {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 20px;
  margin: 20px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  
  .van-empty__description {
    color: #6c757d;
  }
}

:deep(.van-skeleton) {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>