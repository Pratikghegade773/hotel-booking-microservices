import React, { useState } from 'react';
import { X, Calendar, Tag, ShieldCheck, AlertCircle } from 'lucide-react';
import { bookingApi } from '../api/bookingApi';
import { offerApi } from '../api/offerApi';

export default function BookingModal({ room, onClose, onSuccess }) {
  const today = new Date().toISOString().split('T')[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
  const dayAfter = new Date(Date.now() + 172800000).toISOString().split('T')[0];

  const [checkInDate, setCheckInDate] = useState(tomorrow);
  const [checkOutDate, setCheckOutDate] = useState(dayAfter);
  const [offerCode, setOfferCode] = useState('');
  const [discountInfo, setDiscountInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Calculate nights
  const start = new Date(checkInDate);
  const end = new Date(checkOutDate);
  const nights = Math.max(1, Math.round((end - start) / (1000 * 60 * 60 * 24)));
  const baseTotal = (Number(room.basePrice) * nights).toFixed(2);

  const handleApplyOffer = async () => {
    if (!offerCode.trim()) return;
    try {
      const res = await offerApi.calculateDiscount(Number(baseTotal), offerCode.trim().toUpperCase());
      setDiscountInfo(res);
      setError(null);
    } catch (e) {
      setError('Invalid or expired coupon code.');
      setDiscountInfo(null);
    }
  };

  const finalTotal = discountInfo ? discountInfo.finalAmount : baseTotal;
  const discountAmount = discountInfo ? discountInfo.discountAmount : 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const orderResponse = await bookingApi.createBookingOrder({
        roomId: room.id,
        checkInDate,
        checkOutDate,
        offerCode: offerCode.trim() ? offerCode.trim().toUpperCase() : null,
      });

      onSuccess(orderResponse);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Failed to create booking order.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 relative">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h3 className="text-lg font-bold text-slate-900">Book Room {room.roomNumber}</h3>
            <p className="text-xs text-slate-500 font-medium">{room.type} • ₹{room.basePrice}/night</p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          {/* Dates */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Check-in Date</label>
              <div className="relative">
                <input
                  type="date"
                  min={today}
                  value={checkInDate}
                  onChange={(e) => setCheckInDate(e.target.value)}
                  className="w-full text-xs font-medium px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Check-out Date</label>
              <div className="relative">
                <input
                  type="date"
                  min={checkInDate}
                  value={checkOutDate}
                  onChange={(e) => setCheckOutDate(e.target.value)}
                  className="w-full text-xs font-medium px-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                  required
                />
              </div>
            </div>
          </div>

          {/* Coupon Code */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Promo Coupon</label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Tag className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="text"
                  placeholder="e.g. WELCOME10"
                  value={offerCode}
                  onChange={(e) => setOfferCode(e.target.value)}
                  className="w-full text-xs uppercase font-semibold pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                />
              </div>
              <button
                type="button"
                onClick={handleApplyOffer}
                className="px-3.5 py-2 text-xs font-bold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-xl transition-colors"
              >
                Apply
              </button>
            </div>
            {discountInfo && discountInfo.applied && (
              <p className="mt-1 text-xs text-emerald-600 font-semibold">
                ✓ Coupon '{offerCode}' applied: Saved ₹{discountInfo.discountAmount}
              </p>
            )}
          </div>

          {/* Price Breakdown */}
          <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-100 text-xs space-y-1.5">
            <div className="flex justify-between text-slate-500">
              <span>Duration:</span>
              <span className="font-semibold text-slate-700">{nights} Night{nights > 1 ? 's' : ''}</span>
            </div>
            <div className="flex justify-between text-slate-500">
              <span>Base Rate:</span>
              <span>₹{room.basePrice} × {nights} = ₹{baseTotal}</span>
            </div>
            {discountAmount > 0 && (
              <div className="flex justify-between text-emerald-600 font-semibold">
                <span>Discount:</span>
                <span>- ₹{discountAmount}</span>
              </div>
            )}
            <div className="pt-2 border-t border-slate-200 flex justify-between text-sm font-extrabold text-slate-900">
              <span>Total Payable:</span>
              <span className="text-indigo-600 text-base">₹{finalTotal}</span>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="w-1/3 py-2.5 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="w-2/3 py-2.5 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-md shadow-indigo-100 transition-all flex items-center justify-center gap-1.5 disabled:opacity-50"
            >
              <ShieldCheck className="w-4 h-4" />
              {loading ? 'Creating Order...' : 'Proceed to Payment'}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
}
