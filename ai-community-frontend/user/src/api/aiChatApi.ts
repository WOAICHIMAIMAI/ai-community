import { get } from '@/utils/request';

/**
 * AI聊天相关API
 */
export const aiChatApi = {
  /**
   * 发送消息给AI
   * @param userContent 用户输入内容
   * @param chatId 聊天会话ID
   * @returns AI回复内容
   */
  sendMessage: async (userContent: string, chatId: string) => {
    return await get('/user/ai/chat', {
      userContent,
      chatId
    });
  },

  /**
   * 获取当前用户的所有聊天会话ID
   * @returns 聊天会话ID列表
   */
  getChatIds: async () => {
    return await get('/user/ai');
  },

  /**
   * 获取指定会话的聊天记录
   * @param chatId 聊天会话ID
   * @param lastK 获取最近的K条记录，默认10条
   * @returns 聊天记录列表
   */
  getChatMessages: async (chatId: string, lastK: number = 10) => {
    return await get(`/user/ai/${chatId}`, {
      lastK
    });
  }
};