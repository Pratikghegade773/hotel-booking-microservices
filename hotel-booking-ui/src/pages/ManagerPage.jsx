import React, { useState, useEffect } from 'react';
import { ShieldCheck, UserCheck, LogOut, CheckCircle2, Clock, AlertCircle, RefreshCw, Hotel, Search } from 'lucide-react';
import { bookingApi } from '../api/bookingApi';
import { inventoryApi } from '../api/inventoryApi';
import { useAuth } from '../context/AuthContext';

export default function ManagerPage() {
  const { user } = useAuth();

  const [hotels, setHotels] = useState([]);
  const [selectedHotelId, setSelectedHotelId] = useState(user?.hotelId || '');
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [error, setError] = useState(null);
  const [filterStatus, setFilterStatus] = useState('ALL');

  // Load available hotels for selection (for Owners or Managers)
  useEffect(() => {
    const loadHotels = async () => {
      try {
        const hotelList = await inventoryApi.getHotels();
        setHotels(hotelList || []);
        if (!selectedHotelId && hotelList && hotelList.length > 0) {
          setSelectedHotelId(user?.hotelId || hotelList[0].id);
        }
      } catch (err) {
        console.error(err);
      }
    };
    loadHotels();
  }, [user]);

  // Fetch bookings whenever selectedHotelId changes
  const fetchBookings = async () => {
    if (!selectedHotelId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await bookingApi.getHotelBookings(selectedHotelId);
      setBookings(data || []);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to fetch bookings for this hotel.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedHotelId) {
      fetchBookings();
    }
  }, [selectedHotelId]);

  const handleCheckIn = async (bookingId) => {
    setActionLoadingId(bookingId);
    setError(null);
    try {
      await bookingApi.checkIn(bookingId);
      await fetchBookings();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to check in guest.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCheckOut = async (bookingId) => {
    setActionLoadingId(bookingId);
    setError(null);
    try {
      await bookingApi.checkOut(bookingId);
      await fetchBookings();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to check out guest.');
    } finally {
      setActionLoadingId(null);
    }
  };

  const filteredBookings = bookings.filter((b) => {
    if (filterStatus === 'ALL') return true;
    return b.status === filterStatus;
  });

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
            <Clock className="w-3.5 h-3.5" /> In House
          </span>
        );
      case 'CHECKED_OUT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-700 border border-slate-200">
            Completed
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200">
            Cancelled
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
      <div className="max-w-5xl mx-auto">
        
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-extrabold text-slate-900">Front Desk & Reception</h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200">
                Manager Ops
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Verify reservations, check-in arriving guests, and process check-outs
            </p>
          </div>

          {/* Hotel Selector */}
          <div className="flex items-center gap-2">
            <div className="relative">
              <Hotel className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <select
                value={selectedHotelId}
                onChange={(e) => setSelectedHotelId(e.target.value)}
                className="text-xs font-semibold pl-9 pr-8 py-2 bg-white border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 shadow-sm"
              >
                {hotels.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.name} ({h.city})
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={fetchBookings}
              className="p-2 text-slate-500 hover:text-indigo-600 bg-white rounded-xl border border-slate-200 shadow-sm transition-colors"
              title="Refresh Bookings"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mt-4 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Filter Pills */}
        <div className="mt-6 flex flex-wrap gap-2">
          {['ALL', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT'].map((status) => (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                filterStatus === status
                  ? 'bg-indigo-600 text-white shadow-sm'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {status === 'ALL' ? 'All Bookings' : status.replace('_', ' ')}
            </button>
          ))}
        </div>

        {/* Bookings Table / List */}
        <div className="mt-6">
          {loading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div key={i} className="bg-white rounded-2xl p-6 border border-slate-200 animate-pulse">
                  <div className="h-5 bg-slate-200 rounded w-1/4 mb-3"></div>
                  <div className="h-4 bg-slate-100 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : filteredBookings.length === 0 ? (
            <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
              <ShieldCheck className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-slate-800">No bookings matching filter</h3>
              <p className="text-xs text-slate-500 mt-1">There are no reservations in this category for the selected hotel.</p>
            </div>
          ) : (
            <div className="space-y-3.5">
              {filteredBookings.map((b) => {
                const isConfirmed = b.status === 'CONFIRMED';
                const isCheckedIn = b.status === 'CHECKED_IN';

                return (
                  <div
                    key={b.id}
                    className="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-xs font-bold text-indigo-600">
                          #BOOK-{b.id}
                        </span>
                        <span className="text-xs font-bold text-slate-800">
                          Room #{b.roomId}
                        </span>
                        {getStatusBadge(b.status)}
                      </div>

                      <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500">
                        <span>
                          Customer ID: <strong className="text-slate-700">#{b.customerId}</strong>
                        </span>
                        <span>
                          Dates: <strong className="text-slate-700">{b.checkInDate}</strong> to <strong className="text-slate-700">{b.checkOutDate}</strong>
                        </span>
                        <span>
                          Paid: <strong className="text-emerald-600 font-extrabold">₹{b.finalAmount}</strong>
                        </span>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex items-center gap-2 shrink-0">
                      {isConfirmed && (
                        <button
                          onClick={() => handleCheckIn(b.id)}
                          disabled={actionLoadingId === b.id}
                          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 disabled:opacity-50"
                        >
                          <UserCheck className="w-4 h-4" />
                          {actionLoadingId === b.id ? 'Checking In...' : 'Check In Guest'}
                        </button>
                      )}

                      {isCheckedIn && (
                        <button
                          onClick={() => handleCheckOut(b.id)}
                          disabled={actionLoadingId === b.id}
                          className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm transition-all flex items-center gap-1.5 disabled:opacity-50"
                        >
                          <LogOut className="w-4 h-4" />
                          {actionLoadingId === b.id ? 'Checking Out...' : 'Check Out Guest'}
                        </button>
                      )}

                      {!isConfirmed && !isCheckedIn && (
                        <span className="text-xs font-medium text-slate-400 italic">No action needed</span>
                      )}
                    </div>
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
