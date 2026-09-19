import axiosClient from './axiosClient';

export const offerApi = {
  getActiveOffers: async () => {
    const res = await axiosClient.get('/api/offers');
    return res.data;
  },

  createOffer: async (offerData) => {
    const res = await axiosClient.post('/api/offers', offerData);
    return res.data;
  },

  calculateDiscount: async (amount, offerCode) => {
    const res = await axiosClient.post('/api/offers/calculate', { amount, offerCode });
    return res.data;
  }
};
