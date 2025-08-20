import { get, post, put, del } from '@/utils/request';

/**
 * 聊天相关API
 */
export const chatApi = {
  /**
   * 获取会话列表
   */
  getConversations: async () => {
    return await get('/api/user/conversations');
  },

  /**
   * 创建会话
   */
  createConversation: async (data: {
    conversationName: string;
    conversationType: number;
    description?: string;
    memberIds?: number[];
  }) => {
    return await post('/api/user/conversations', data);
  },

  /**
   * 获取会话详情
   */
  getConversationDetail: async (conversationId: string) => {
    return await get(`/api/user/conversations/${conversationId}`);
  },

  /**
   * 查找或创建私聊会话
   */
  findOrCreatePrivateConversation: async (targetUserId: number) => {
    return await post(`/api/user/conversations/private/${targetUserId}`);
  },

  /**
   * 删除会话
   */
  deleteConversation: async (conversationId: string) => {
    return await del(`/api/user/conversations/${conversationId}`);
  },

  /**
   * 发送消息
   */
  sendMessage: async (conversationId: string, data: {
    content: string;
    messageType: string;
    metadata?: string;
  }) => {
    return await post(`/api/user/conversations/${conversationId}/messages`, data);
  },

  /**
   * 获取会话消息
   */
  getMessages: async (conversationId: string, page = 1, size = 20) => {
    return await get(`/api/user/conversations/${conversationId}/messages`, {
      page,
      size
    });
  },

  /**
   * 标记消息已读
   */
  markMessageRead: async (messageId: number) => {
    return await put(`/api/user/messages/${messageId}/read`);
  },

  /**
   * 删除消息
   */
  deleteMessage: async (messageId: number) => {
    return await del(`/api/user/messages/${messageId}`);
  },

  /**
   * 获取聊天设置
   */
  getChatSettings: async () => {
    return await get('/api/user/chat/settings');
  },

  /**
   * 更新聊天设置
   */
  updateChatSettings: async (data: {
    allowStrangerMessage?: number;
    messageNotification?: number;
    notificationSound?: number;
    vibration?: number;
    fontSize?: number;
    theme?: string;
  }) => {
    return await put('/api/user/chat/settings', data);
  }
};