<template>
  <div class="chat-settings-container">
    <van-nav-bar 
      title="聊天设置" 
      left-text="返回" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />
    
    <div class="content">
      <van-form @submit="saveSettings">
        <!-- 消息设置 -->
        <van-cell-group title="消息设置" inset>
          <van-cell title="允许陌生人消息">
            <template #right-icon>
              <van-switch v-model="settings.allowStrangerMessage" />
            </template>
          </van-cell>
          
          <van-cell title="消息通知">
            <template #right-icon>
              <van-switch v-model="settings.messageNotification" />
            </template>
          </van-cell>
          
          <van-cell title="通知声音">
            <template #right-icon>
              <van-switch v-model="settings.notificationSound" />
            </template>
          </van-cell>
          
          <van-cell title="震动提醒">
            <template #right-icon>
              <van-switch v-model="settings.vibration" />
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 显示设置 -->
        <van-cell-group title="显示设置" inset>
          <van-field label="字体大小">
            <template #input>
              <van-slider 
                v-model="settings.fontSize" 
                :min="12" 
                :max="20" 
                :step="1"
                active-color="#4fc08d"
              />
              <span class="font-size-value">{{ settings.fontSize }}px</span>
            </template>
          </van-field>
          
          <van-field label="主题模式">
            <template #input>
              <van-radio-group v-model="settings.theme" direction="horizontal">
                <van-radio name="light">浅色</van-radio>
                <van-radio name="dark">深色</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>

        <!-- 预览区域 -->
        <van-cell-group title="预览效果" inset>
          <div class="preview-area" :class="{ 'dark-theme': settings.theme === 'dark' }">
            <div class="preview-message">
              <div class="message-bubble other" :style="{ fontSize: settings.fontSize + 'px' }">
                这是一条示例消息，用于预览字体大小和主题效果
              </div>
            </div>
            <div class="preview-message own">
              <div class="message-bubble own-bubble" :style="{ fontSize: settings.fontSize + 'px' }">
                这是您发送的消息预览
              </div>
            </div>
          </div>
        </van-cell-group>

        <!-- 保存按钮 -->
        <div class="save-section">
          <van-button 
            type="primary" 
            block 
            native-type="submit"
            :loading="saving"
          >
            保存设置
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showFailToast } from 'vant';
import { chatApi } from '@/api/chatApi';
import { useAuthStore } from '@/store/auth';

// 类型定义
interface ChatSettings {
  allowStrangerMessage: boolean;
  messageNotification: boolean;
  notificationSound: boolean;
  vibration: boolean;
  fontSize: number;
  theme: string;
}

// 状态管理
const settings = ref<ChatSettings>({
  allowStrangerMessage: true,
  messageNotification: true,
  notificationSound: true,
  vibration: true,
  fontSize: 14,
  theme: 'light'
});
const saving = ref(false);

// 路由和存储
const router = useRouter();
const authStore = useAuthStore();

// 获取聊天设置
const fetchSettings = async () => {
  try {
    const response = await chatApi.getChatSettings();
    if (response.code === 200 && response.data) {
      const data = response.data;
      settings.value = {
        allowStrangerMessage: data.allowStrangerMessage === 1,
        messageNotification: data.messageNotification === 1,
        notificationSound: data.notificationSound === 1,
        vibration: data.vibration === 1,
        fontSize: data.fontSize || 14,
        theme: data.theme || 'light'
      };
    }
  } catch (error) {
    console.error('获取聊天设置失败:', error);
    showFailToast('获取设置失败');
  }
};

// 保存设置
const saveSettings = async () => {
  saving.value = true;
  try {
    const requestData = {
      allowStrangerMessage: settings.value.allowStrangerMessage ? 1 : 0,
      messageNotification: settings.value.messageNotification ? 1 : 0,
      notificationSound: settings.value.notificationSound ? 1 : 0,
      vibration: settings.value.vibration ? 1 : 0,
      fontSize: settings.value.fontSize,
      theme: settings.value.theme
    };

    const response = await chatApi.updateChatSettings(requestData);
    if (response.code === 200) {
      showToast('设置保存成功');
    } else {
      showFailToast(response.msg || '保存失败');
    }
  } catch (error) {
    console.error('保存设置失败:', error);
    showFailToast('保存失败');
  } finally {
    saving.value = false;
  }
};

// 组件挂载
onMounted(() => {
  if (!authStore.isLoggedIn) {
    showToast('请先登录');
    router.push('/login');
    return;
  }
  
  fetchSettings();
});
</script>

<style scoped>
.chat-settings-container {
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
  padding: 20px 16px;
}

:deep(.van-cell-group) {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.van-cell-group__title) {
  color: #2c3e50;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 8px;
}

:deep(.van-cell) {
  background: #ffffff;
  border-bottom: 1px solid #f8f9fa;
}

:deep(.van-cell:last-child) {
  border-bottom: none;
}

:deep(.van-cell__title) {
  color: #2c3e50;
  font-weight: 500;
}

:deep(.van-switch--on) {
  background-color: #4fc08d;
}

.font-size-value {
  margin-left: 12px;
  color: #6c757d;
  font-size: 14px;
  min-width: 40px;
}

:deep(.van-slider) {
  margin: 0 16px 0 0;
}

:deep(.van-radio-group) {
  display: flex;
  gap: 20px;
}

.preview-area {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  margin: 16px;
  transition: all 0.3s ease;
}

.preview-area.dark-theme {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
}

.preview-message {
  margin-bottom: 12px;
  display: flex;
}

.preview-message.own {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.4;
  transition: all 0.3s ease;
}

.message-bubble.other {
  background: #ffffff;
  color: #2c3e50;
  border-bottom-left-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dark-theme .message-bubble.other {
  background: #34495e;
  color: #ecf0f1;
}

.message-bubble.own-bubble {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 2px 8px rgba(79, 192, 141, 0.3);
}

.save-section {
  margin-top: 30px;
  padding: 0 16px;
}

:deep(.van-button--primary) {
  background: linear-gradient(135deg, #4fc08d 0%, #52c41a 100%);
  border: none;
  border-radius: 20px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 16px rgba(79, 192, 141, 0.25);
}
</style>