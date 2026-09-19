import axiosClient from './axiosClient';

export const inventoryApi = {
  getHotels: async (city) => {
    const params = city ? { city } : {};
    const res = await axiosClient.get('/api/hotels', { params });
    return res.data;
  },

  getHotelById: async (id) => {
    const res = await axiosClient.get(`/api/hotels/${id}`);
    return res.data;
  },

  getRooms: async (hotelId, offerCode) => {
    const params = {};
    if (hotelId) params.hotelId = hotelId;
    if (offerCode) params.offerCode = offerCode;
    const res = await axiosClient.get('/api/rooms', { params });
    return res.data;
  },

  createHotel: async (hotelData) => {
    const res = await axiosClient.post('/api/inventory/hotels', hotelData);
    return res.data;
  },

  getMyHotels: async () => {
    const res = await axiosClient.get('/api/inventory/hotels/my');
    return res.data;
  },

  createRoom: async (roomData) => {
    const res = await axiosClient.post('/api/inventory/rooms', roomData);
    return res.data;
  },

  updateRoomAvailability: async (roomId, isAvailable) => {
    const res = await axiosClient.patch(`/api/inventory/rooms/${roomId}/availability`, null, {
      params: { isAvailable }
    });
    return res.data;
  }
};
