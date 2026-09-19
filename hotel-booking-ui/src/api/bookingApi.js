import axiosClient from './axiosClient';

export const bookingApi = {
  createBookingOrder: async (bookingData) => {
    const res = await axiosClient.post('/api/bookings/create-order', bookingData);
    return res.data;
  },

  getMyBookings: async () => {
    const res = await axiosClient.get('/api/bookings/my-bookings');
    return res.data;
  },

  getBookingById: async (id) => {
    const res = await axiosClient.get(`/api/bookings/${id}`);
    return res.data;
  },

  cancelBooking: async (id) => {
    const res = await axiosClient.post(`/api/bookings/${id}/cancel`);
    return res.data;
  },

  verifyPayment: async (paymentData) => {
    const res = await axiosClient.post('/api/payments/verify', paymentData);
    return res.data;
  },

  getHotelBookings: async (hotelId) => {
    const params = hotelId ? { hotelId } : {};
    const res = await axiosClient.get('/api/manager/bookings', { params });
    return res.data;
  },

  checkIn: async (bookingId) => {
    const res = await axiosClient.post(`/api/manager/bookings/${bookingId}/check-in`);
    return res.data;
  },

  checkOut: async (bookingId) => {
    const res = await axiosClient.post(`/api/manager/bookings/${bookingId}/check-out`);
    return res.data;
  },

  getOwnerReports: async () => {
    const res = await axiosClient.get('/api/owner/reports');
    return res.data;
  }
};
