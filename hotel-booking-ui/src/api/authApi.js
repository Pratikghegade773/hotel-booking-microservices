import axiosClient from './axiosClient';

export const authApi = {
  register: async (data) => {
    const res = await axiosClient.post('/api/auth/register', data);
    return res.data;
  },

  login: async (credentials) => {
    const res = await axiosClient.post('/api/auth/login', credentials);
    return res.data;
  },

  getMe: async () => {
    const res = await axiosClient.get('/api/users/me');
    return res.data;
  },

  createManager: async (managerData) => {
    const res = await axiosClient.post('/api/users/managers', managerData);
    return res.data;
  }
};
