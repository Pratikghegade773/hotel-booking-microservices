import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, CreditCard, AlertCircle, Ban, CheckCircle2, ChevronRight, RefreshCw, Sparkles } from 'lucide-react';
import { bookingApi } from '../api/bookingApi';
import { inventoryApi } from '../api/inventoryApi';
import { useAuth } from '../context/AuthContext';

export default function MyBookingsPage() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [hotelsMap, setHotelsMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [actionError, setActionError] = useState(null);

  const fetchBookingsAndHotels = async () => {
    setLoading(true);
    setActionError(null);
    try {
      const [bookingsData, hotelsData] = await Promise.all([
        bookingApi.getMyBookings(),
        inventoryApi.getHotels().catch(() => []),
      ]);

      setBookings(bookingsData || []);

      const hMap = {};
      if (Array.isArray(hotelsData)) {
        hotelsData.forEach((h) => {
          hMap[h.id] = h;
        });
      }
      setHotelsMap(hMap);
    } catch (err) {
      console.error(err);
      setActionError('Failed to load your bookings.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookingsAndHotels();
  }, []);

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    setCancellingId(bookingId);
    setActionError(null);
    try {
      await bookingApi.cancelBooking(bookingId);
      // Refresh list
      await fetchBookingsAndHotels();
    } catch (err) {
      console.error(err);
      setActionError(err.response?.data?.message || 'Failed to cancel booking.');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle2 className="w-3.5 h-3.5" /> Confirmed
          </span>
        );
      case 'CHECKED_IN':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
            <Clock className="w-3.5 h-3.5" /> Checked In
          </span>
        );
      case 'CHECKED_OUT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-700 border border-slate-200">
            Checked Out
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200">
            Cancelled
          </span>
        );
      case 'PENDING_PAYMENT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200">
            Pending Payment
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-700">
            {status}
          </span>
        );
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-900">My Bookings</h1>
            <p className="text-xs text-slate-500 mt-1">
              View your hotel reservations, stay schedules, and payment receipts
            </p>
          </div>
          <div className="flex items-center gap-3">
            <button
              onClick={fetchBookingsAndHotels}
              className="p-2 text-slate-500 hover:text-indigo-600 hover:bg-white rounded-xl border border-slate-200 transition-colors"
              title="Refresh"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
            <Link
              to="/"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm shadow-indigo-200 transition-all flex items-center gap-1"
            >
              Book New Stay <ChevronRight className="w-4 h-4" />
            </Link>
          </div>
        </div>

        {/* Action Error Alert */}
        {actionError && (
          <div className="mt-4 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{actionError}</span>
          </div>
        )}

        {/* Bookings List */}
        <div className="mt-6">
          {loading ? (
            <div className="space-y-4">
              {[1, 2].map((i) => (
                <div key={i} className="bg-white rounded-2xl p-6 border border-slate-200 animate-pulse">
                  <div className="h-5 bg-slate-200 rounded w-1/4 mb-3"></div>
                  <div className="h-4 bg-slate-100 rounded w-1/2 mb-4"></div>
                  <div className="h-4 bg-slate-100 rounded w-1/3"></div>
                </div>
              ))}
            </div>
          ) : bookings.length === 0 ? (
            <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
              <div className="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-3">
                <Calendar className="w-6 h-6" />
              </div>
              <h3 className="text-base font-bold text-slate-900">No bookings yet</h3>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                You haven't reserved any hotel rooms yet. Explore our featured destinations and book your next trip!
              </p>
              <Link
                to="/"
                className="mt-5 inline-block px-5 py-2.5 bg-indigo-600 text-white font-bold text-xs rounded-xl hover:bg-indigo-700 transition-colors shadow-sm"
              >
                Browse Hotels
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {bookings.map((b) => {
                const hotel = hotelsMap[b.hotelId];
                const canCancel = b.status === 'CONFIRMED' || b.status === 'PENDING_PAYMENT';

                return (
                  <div
                    key={b.id}
                    className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm hover:shadow-md transition-shadow"
                  >
                    <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-lg font-bold text-slate-900">
                            {hotel ? hotel.name : `Hotel #${b.hotelId}`}
                          </h3>
                          {getStatusBadge(b.status)}
                        </div>
                        <p className="text-xs text-slate-500 mt-0.5">
                          {hotel ? `${hotel.city} • Room #${b.roomId}` : `Room #${b.roomId}`}
                        </p>
                      </div>

                      <div className="text-left sm:text-right">
                        <span className="text-[10px] text-slate-400 font-semibold block">Total Paid</span>
                        <span className="text-lg font-extrabold text-slate-900">₹{b.finalAmount}</span>
                        {b.discountAmount > 0 && (
                          <span className="block text-[10px] text-emerald-600 font-semibold">
                            Saved ₹{b.discountAmount}
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Schedule Grid */}
                    <div className="mt-5 pt-4 border-t border-slate-100 grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs">
                      <div className="bg-slate-50 p-3 rounded-xl">
                        <span className="text-slate-400 block text-[10px] uppercase font-bold">Check-in</span>
                        <span className="font-bold text-slate-800 text-sm mt-0.5 block">{b.checkInDate}</span>
                      </div>
                      <div className="bg-slate-50 p-3 rounded-xl">
                        <span className="text-slate-400 block text-[10px] uppercase font-bold">Check-out</span>
                        <span className="font-bold text-slate-800 text-sm mt-0.5 block">{b.checkOutDate}</span>
                      </div>
                      <div className="bg-slate-50 p-3 rounded-xl col-span-2 sm:col-span-1">
                        <span className="text-slate-400 block text-[10px] uppercase font-bold">Booking Reference</span>
                        <span className="font-mono font-bold text-indigo-600 text-xs mt-0.5 block">#BOOK-{b.id}</span>
                      </div>
                    </div>

                    {/* Actions */}
                    {canCancel && (
                      <div className="mt-4 pt-3 flex justify-end">
                        <button
                          onClick={() => handleCancel(b.id)}
                          disabled={cancellingId === b.id}
                          className="px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50 border border-rose-200 rounded-xl transition-colors flex items-center gap-1.5 disabled:opacity-50"
                        >
                          <Ban className="w-3.5 h-3.5" />
                          {cancellingId === b.id ? 'Cancelling...' : 'Cancel Reservation'}
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
