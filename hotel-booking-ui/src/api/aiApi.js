import axiosClient from './axiosClient';

export const aiApi = {
  chat: async (message, conversationId) => {
    const res = await axiosClient.post('/api/ai/chat', { message, conversationId });
    return res.data;
  },

  search: async (query, topK = 3) => {
    const res = await axiosClient.post('/api/ai/search', { query, topK });
    return res.data;
  },

  indexHotels: async () => {
    const res = await axiosClient.post('/api/ai/index');
    return res.data;
  }
};
