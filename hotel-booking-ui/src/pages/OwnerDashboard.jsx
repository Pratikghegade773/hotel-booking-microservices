import React, { useState, useEffect } from 'react';
import { 
  Building2, BedDouble, CalendarCheck, IndianRupee, Plus, 
  RefreshCw, CheckCircle2, XCircle, AlertCircle, Sparkles, X
} from 'lucide-react';
import { bookingApi } from '../api/bookingApi';
import { inventoryApi } from '../api/inventoryApi';
import { useAuth } from '../context/AuthContext';

export default function OwnerDashboard() {
  const { user } = useAuth();

  const [reports, setReports] = useState({
    totalHotels: 0,
    totalRooms: 0,
    totalBookings: 0,
    totalRevenue: 0,
  });

  const [hotels, setHotels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modals
  const [showAddHotelModal, setShowAddHotelModal] = useState(false);
  const [showAddRoomModal, setShowAddRoomModal] = useState(false);

  // Form states
  const [hotelForm, setHotelForm] = useState({ name: '', city: '', address: '', description: '' });
  const [roomForm, setRoomForm] = useState({ hotelId: '', roomNumber: '', type: 'DELUXE', basePrice: '', capacity: 2 });
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [reportsData, myHotels, allRooms] = await Promise.all([
        bookingApi.getOwnerReports().catch(() => ({ totalHotels: 0, totalRooms: 0, totalBookings: 0, totalRevenue: 0 })),
        inventoryApi.getMyHotels().catch(() => []),
        inventoryApi.getRooms().catch(() => []),
      ]);

      setReports(reportsData);
      setHotels(myHotels || []);
      setRooms(allRooms || []);
    } catch (err) {
      console.error(err);
      setError('Unable to load owner dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleCreateHotel = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFeedback(null);
    try {
      await inventoryApi.createHotel(hotelForm);
      setFeedback({ type: 'success', message: 'Hotel registered successfully!' });
      setShowAddHotelModal(false);
      setHotelForm({ name: '', city: '', address: '', description: '' });
      await fetchDashboardData();
    } catch (err) {
      console.error(err);
      setFeedback({ type: 'error', message: err.response?.data?.message || 'Failed to create hotel.' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddRoom = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFeedback(null);
    try {
      await inventoryApi.createRoom({
        ...roomForm,
        hotelId: Number(roomForm.hotelId),
        basePrice: Number(roomForm.basePrice),
        capacity: Number(roomForm.capacity),
        isAvailable: true,
      });
      setFeedback({ type: 'success', message: `Room ${roomForm.roomNumber} created successfully!` });
      setShowAddRoomModal(false);
      setRoomForm({ hotelId: '', roomNumber: '', type: 'DELUXE', basePrice: '', capacity: 2 });
      await fetchDashboardData();
    } catch (err) {
      console.error(err);
      setFeedback({ type: 'error', message: err.response?.data?.message || 'Failed to add room.' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleAvailability = async (roomId, currentAvailability) => {
    try {
      await inventoryApi.updateRoomAvailability(roomId, !currentAvailability);
      await fetchDashboardData();
    } catch (err) {
      console.error(err);
      alert('Failed to update room status.');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-2xl font-extrabold text-slate-900">Owner Dashboard</h1>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
                Business Hub
              </span>
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Manage your hotel properties, room inventory, and track real-time booking revenue
            </p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={fetchDashboardData}
              className="p-2 text-slate-500 hover:text-indigo-600 bg-white rounded-xl border border-slate-200 shadow-sm transition-colors"
              title="Refresh Stats"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
            <button
              onClick={() => setShowAddHotelModal(true)}
              className="px-3.5 py-2 bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4 text-indigo-600" /> Add Property
            </button>
            <button
              onClick={() => {
                if (hotels.length > 0 && !roomForm.hotelId) {
                  setRoomForm(prev => ({ ...prev, hotelId: hotels[0].id }));
                }
                setShowAddRoomModal(true);
              }}
              disabled={hotels.length === 0}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white text-xs font-bold rounded-xl shadow-sm shadow-indigo-200 transition-all flex items-center gap-1.5"
            >
              <Plus className="w-4 h-4" /> Add Room
            </button>
          </div>
        </div>

        {/* Feedback Alert */}
        {feedback && (
          <div className={`mt-4 p-3.5 rounded-xl border text-xs flex items-center justify-between ${
            feedback.type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-rose-50 border-rose-200 text-rose-800'
          }`}>
            <span>{feedback.message}</span>
            <button onClick={() => setFeedback(null)}><X className="w-4 h-4" /></button>
          </div>
        )}

        {/* Analytics Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-3">
              <Building2 className="w-5 h-5" />
            </div>
            <span className="text-xs font-semibold text-slate-500">Properties</span>
            <div className="text-2xl font-black text-slate-900 mt-1">{reports.totalHotels}</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center mb-3">
              <BedDouble className="w-5 h-5" />
            </div>
            <span className="text-xs font-semibold text-slate-500">Total Rooms</span>
            <div className="text-2xl font-black text-slate-900 mt-1">{reports.totalRooms}</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center mb-3">
              <CalendarCheck className="w-5 h-5" />
            </div>
            <span className="text-xs font-semibold text-slate-500">Total Bookings</span>
            <div className="text-2xl font-black text-slate-900 mt-1">{reports.totalBookings}</div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-3">
              <IndianRupee className="w-5 h-5" />
            </div>
            <span className="text-xs font-semibold text-slate-500">Gross Revenue</span>
            <div className="text-2xl font-black text-emerald-600 mt-1">₹{reports.totalRevenue || 0}</div>
          </div>
        </div>

        {/* Owned Hotels & Rooms List */}
        <div className="mt-10">
          <h2 className="text-lg font-bold text-slate-900 mb-4">My Hotel Properties & Inventory</h2>

          {loading ? (
            <div className="space-y-4">
              {[1, 2].map((i) => (
                <div key={i} className="bg-white rounded-2xl p-6 border border-slate-200 animate-pulse">
                  <div className="h-5 bg-slate-200 rounded w-1/4 mb-3"></div>
                  <div className="h-4 bg-slate-100 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          ) : hotels.length === 0 ? (
            <div className="bg-white rounded-2xl p-10 text-center border border-slate-200">
              <Building2 className="w-10 h-10 text-slate-300 mx-auto mb-2" />
              <h3 className="text-sm font-bold text-slate-800">No properties registered yet</h3>
              <p className="text-xs text-slate-500 mt-1">Click 'Add Property' above to create your first hotel listing.</p>
            </div>
          ) : (
            <div className="space-y-6">
              {hotels.map((hotel) => {
                const hotelRooms = rooms.filter((r) => r.hotelId === hotel.id);

                return (
                  <div key={hotel.id} className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-lg font-bold text-slate-900">{hotel.name}</h3>
                          <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
                            ID #{hotel.id}
                          </span>
                        </div>
                        <p className="text-xs text-slate-500 mt-0.5">{hotel.city} • {hotel.address || 'No address specified'}</p>
                      </div>

                      <button
                        onClick={() => {
                          setRoomForm({ hotelId: hotel.id, roomNumber: '', type: 'DELUXE', basePrice: '', capacity: 2 });
                          setShowAddRoomModal(true);
                        }}
                        className="px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-bold rounded-xl transition-colors flex items-center gap-1 w-fit"
                      >
                        <Plus className="w-3.5 h-3.5" /> Add Room to this Hotel
                      </button>
                    </div>

                    {/* Rooms Table */}
                    <div className="p-6">
                      <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
                        Room Inventory ({hotelRooms.length})
                      </h4>

                      {hotelRooms.length === 0 ? (
                        <p className="text-xs text-slate-400 italic py-2">No rooms added to this hotel yet.</p>
                      ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                          {hotelRooms.map((r) => (
                            <div key={r.id} className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/60 flex items-center justify-between">
                              <div>
                                <div className="flex items-center gap-1.5">
                                  <span className="text-xs font-bold text-slate-800">Room {r.roomNumber}</span>
                                  <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-white text-slate-600 border border-slate-200">
                                    {r.type}
                                  </span>
                                </div>
                                <span className="text-xs font-extrabold text-indigo-600 mt-1 block">
                                  ₹{r.basePrice}/night
                                </span>
                              </div>

                              <button
                                onClick={() => handleToggleAvailability(r.id, r.isAvailable)}
                                className={`px-2.5 py-1 text-[10px] font-bold rounded-lg border transition-colors flex items-center gap-1 ${
                                  r.isAvailable !== false
                                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'
                                    : 'bg-rose-50 text-rose-700 border-rose-200 hover:bg-rose-100'
                                }`}
                              >
                                {r.isAvailable !== false ? (
                                  <>
                                    <CheckCircle2 className="w-3 h-3" /> Available
                                  </>
                                ) : (
                                  <>
                                    <XCircle className="w-3 h-3" /> Closed
                                  </>
                                )}
                              </button>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>

      {/* Add Hotel Modal */}
      {showAddHotelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 relative">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-900">Register New Property</h3>
              <button onClick={() => setShowAddHotelModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateHotel} className="mt-4 space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Hotel Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Grand Horizon Palace"
                  value={hotelForm.name}
                  onChange={(e) => setHotelForm({ ...hotelForm, name: e.target.value })}
                  className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">City</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Mumbai, Goa"
                  value={hotelForm.city}
                  onChange={(e) => setHotelForm({ ...hotelForm, city: e.target.value })}
                  className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Address</label>
                <input
                  type="text"
                  placeholder="e.g. Marine Drive, South Mumbai"
                  value={hotelForm.address}
                  onChange={(e) => setHotelForm({ ...hotelForm, address: e.target.value })}
                  className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows={3}
                  placeholder="Luxury 5-star seaside hotel featuring world-class amenities..."
                  value={hotelForm.description}
                  onChange={(e) => setHotelForm({ ...hotelForm, description: e.target.value })}
                  className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddHotelModal(false)}
                  className="px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm shadow-indigo-200 transition-all disabled:opacity-50"
                >
                  {submitting ? 'Creating...' : 'Create Hotel'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Room Modal */}
      {showAddRoomModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-100 relative">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <h3 className="text-base font-bold text-slate-900">Add Room to Inventory</h3>
              <button onClick={() => setShowAddRoomModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleAddRoom} className="mt-4 space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Assign to Hotel</label>
                <select
                  required
                  value={roomForm.hotelId}
                  onChange={(e) => setRoomForm({ ...roomForm, hotelId: e.target.value })}
                  className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                >
                  <option value="">Select Hotel</option>
                  {hotels.map((h) => (
                    <option key={h.id} value={h.id}>{h.name} ({h.city})</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Room Number</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 101, 204"
                    value={roomForm.roomNumber}
                    onChange={(e) => setRoomForm({ ...roomForm, roomNumber: e.target.value })}
                    className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Room Type</label>
                  <select
                    value={roomForm.type}
                    onChange={(e) => setRoomForm({ ...roomForm, type: e.target.value })}
                    className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  >
                    <option value="DELUXE">DELUXE</option>
                    <option value="SUITE">SUITE</option>
                    <option value="STANDARD">STANDARD</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Price per Night (₹)</label>
                  <input
                    type="number"
                    required
                    min="1"
                    placeholder="e.g. 3500"
                    value={roomForm.basePrice}
                    onChange={(e) => setRoomForm({ ...roomForm, basePrice: e.target.value })}
                    className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Max Guests</label>
                  <input
                    type="number"
                    min="1"
                    max="10"
                    value={roomForm.capacity}
                    onChange={(e) => setRoomForm({ ...roomForm, capacity: e.target.value })}
                    className="w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowAddRoomModal(false)}
                  className="px-4 py-2 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold rounded-xl shadow-sm shadow-indigo-200 transition-all disabled:opacity-50"
                >
                  {submitting ? 'Adding...' : 'Save Room'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
