<template>
  <div class="chat-list-container">
    <van-nav-bar 
      title="消息" 
      left-text="返回" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    >
      <template #right>
        <van-icon name="plus" @click="showCreateDialog = true" />
      </template>
    </van-nav-bar>
    
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <!-- 会话列表 -->
        <div class="conversation-list">
          <template v-if="loading">
            <van-skeleton title avatar :row="2" style="margin-bottom: 16px;" />
            <van-skeleton title avatar :row="2" style="margin-bottom: 16px;" />
            <van-skeleton title avatar :row="2" />
          </template>
          
          <template v-else-if="conversations.length === 0">
            <van-empty description="暂无会话" />
          </template>
          
          <template v-else>
            <div 
              v-for="conversation in conversations" 
              :key="conversation.conversationId"
              class="conversation-item"
              @click="enterChat(conversation.conversationId)"
            >
              <div class="conversation-avatar">
                <van-image
                  :src="getConversationAvatar(conversation)"
                  width="50"
                  height="50"
                  round
                  fit="cover"
                />
                <van-badge 
                  v-if="conversation.unreadCount > 0"
                  :content="conversation.unreadCount > 99 ? '99+' : conversation.unreadCount"
                  class="unread-badge"
                />
              </div>
              
              <div class="conversation-content">
                <div class="conversation-header">
                  <span class="conversation-name">{{ conversation.conversationName }}</span>
                  <span class="conversation-time">{{ formatTime(conversation.lastMessageTime) }}</span>
                </div>
                <div class="conversation-message">
                  <span class="last-message">{{ conversation.lastMessage || '暂无消息' }}</span>
                </div>
              </div>
            </div>
          </template>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 创建会话弹窗 -->
    <van-popup v-model:show="showCreateDialog" position="bottom" :style="{ height: '60%' }">
      <div class="create-dialog">
        <div class="dialog-header">
          <span class="dialog-title">创建会话</span>
          <van-icon name="cross" @click="showCreateDialog = false" />
        </div>
        
        <van-form @submit="createConversation">
          <van-field
            v-model="createForm.conversationName"
            label="会话名称"
            placeholder="请输入会话名称"
            required
            :rules="[{ required: true, message: '请输入会话名称' }]"
          />
          
          <van-field label="会话类型">
            <template #input>
              <van-radio-group v-model="createForm.conversationType" direction="horizontal">
                <van-radio :name="1">单聊</van-radio>
                <van-radio :name="2">群聊</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          
          <van-field
            v-model="createForm.description"
            label="会话描述"
            placeholder="请输入会话描述（可选）"
            type="textarea"
            rows="2"
          />
          
          <div class="form-actions">
            <van-button type="primary" native-type="submit" block>创建会话</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showFailToast } from 'vant';
import { chatApi } from '@/api/chatApi';
import { useAuthStore } from '@/store/auth';

// 类型定义
interface Conversation {
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

interface CreateForm {
  conversationName: string;
  conversationType: number;
  description: string;
}

// 状态管理
const conversations = ref<Conversation[]>([]);
const loading = ref(false);
const refreshing = ref(false);
const showCreateDialog = ref(false);
const createForm = ref<CreateForm>({
  conversationName: '',
  conversationType: 1,
  description: ''
});

// 路由和存储
const router = useRouter();
const authStore = useAuthStore();

// 获取会话列表
const fetchConversations = async () => {
  loading.value = true;
  try {
    const response = await chatApi.getConversations();
    if (response.code === 200) {
      conversations.value = response.data || [];
      console.log('获取到的会话列表:', conversations.value);
    } else {
      showFailToast(response.msg || '获取会话列表失败');
    }
  } catch (error) {
    console.error('获取会话列表失败:', error);
    showFailToast('获取会话列表失败');
  } finally {
    loading.value = false;
  }
};

// 创建会话
const createConversation = async () => {
  try {
    const response = await chatApi.createConversation(createForm.value);
    if (response.code === 200) {
      showToast('创建成功');
      showCreateDialog.value = false;
      // 重置表单
      createForm.value = {
        conversationName: '',
        conversationType: 1,
        description: ''
      };
      // 刷新列表
      await fetchConversations();
    } else {
      showFailToast(response.msg || '创建失败');
    }
  } catch (error) {
    console.error('创建会话失败:', error);
    showFailToast('创建会话失败');
  }
};

// 进入聊天
const enterChat = (conversationId: string) => {
  router.push(`/chat/${conversationId}`);
};

// 获取会话头像
const getConversationAvatar = (conversation: Conversation) => {
  console.log('获取会话头像:', {
    conversationId: conversation.conversationId,
    conversationName: conversation.conversationName,
    conversationType: conversation.conversationType,
    avatarUrl: conversation.avatarUrl
  });

  if (conversation.avatarUrl) {
    return conversation.avatarUrl;
  }
  // 根据会话类型返回默认头像
  return conversation.conversationType === 2
    ? 'https://img.yzcdn.cn/vant/user-group.png'
    : 'https://img.yzcdn.cn/vant/user.png';
};

// 格式化时间
const formatTime = (timeString?: string) => {
  if (!timeString) return '';
  const date = new Date(timeString);
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

// 下拉刷新
const onRefresh = async () => {
  try {
    await fetchConversations();
    showToast('刷新成功');
  } catch (error) {
    showFailToast('刷新失败');
  } finally {
    refreshing.value = false;
  }
};

// 组件挂载
onMounted(() => {
  if (!authStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }
  
  fetchConversations();
});
</script>

<style scoped>
.chat-list-container {
  min-height: 100vh;
  padding-top: 46px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-bottom: 1px solid rgba(79, 192, 141, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.content {
  padding: 0;
}

.conversation-list {
  background: #ffffff;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s ease;
  cursor: pointer;
  position: relative;
}

.conversation-item:hover {
  background-color: #f8f9fa;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-avatar {
  position: relative;
  margin-right: 12px;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.conversation-time {
  font-size: 12px;
  color: #adb5bd;
  margin-left: 8px;
  flex-shrink: 0;
}

.conversation-message {
  display: flex;
  align-items: center;
}

.last-message {
  font-size: 14px;
  color: #6c757d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.create-dialog {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
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

.form-actions {
  margin-top: 20px;
}

:deep(.van-field__label) {
  color: #2c3e50;
  font-weight: 500;
}

:deep(.van-button--primary) {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  border: none;
  border-radius: 20px;
  height: 44px;
  font-weight: 500;
}

:deep(.van-empty) {
  padding: 60px 20px;
}
</style>
