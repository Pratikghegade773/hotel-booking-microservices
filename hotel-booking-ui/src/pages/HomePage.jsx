import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, MapPin, Sparkles, Tag, AlertCircle, RefreshCw } from 'lucide-react';
import { inventoryApi } from '../api/inventoryApi';
import { offerApi } from '../api/offerApi';
import { useAuth } from '../context/AuthContext';
import HotelCard from '../components/HotelCard';
import BookingModal from '../components/BookingModal';
import PaymentModal from '../components/PaymentModal';

export default function HomePage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [hotels, setHotels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchCity, setSearchCity] = useState('');
  const [activeCity, setActiveCity] = useState('');

  const [selectedRoom, setSelectedRoom] = useState(null);
  const [createdOrder, setCreatedOrder] = useState(null);

  const popularCities = ['All', 'Mumbai', 'Goa', 'Bengaluru', 'Delhi', 'Jaipur'];

  const fetchData = async (city = '') => {
    setLoading(true);
    setError(null);
    try {
      const [hotelsData, roomsData, offersData] = await Promise.all([
        inventoryApi.getHotels(city || undefined),
        inventoryApi.getRooms(),
        offerApi.getActiveOffers().catch(() => []),
      ]);
      setHotels(hotelsData || []);
      setRooms(roomsData || []);
      setOffers(offersData || []);
    } catch (err) {
      console.error('Failed to load data:', err);
      setError('Unable to load hotels. Make sure the API Gateway (localhost:8080) and Inventory Service are running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(activeCity === 'All' ? '' : activeCity);
  }, [activeCity]);

  const handleSearch = (e) => {
    e.preventDefault();
    setActiveCity(searchCity.trim());
  };

  const handleSelectCityFilter = (city) => {
    if (city === 'All') {
      setActiveCity('');
      setSearchCity('');
    } else {
      setActiveCity(city);
      setSearchCity(city);
    }
  };

  const handleBookRoom = (room) => {
    if (!user) {
      navigate('/login');
      return;
    }
    setSelectedRoom(room);
  };

  return (
    <div className="min-h-screen bg-slate-50 pb-20">
      {/* Hero Section */}
      <section className="bg-gradient-to-b from-indigo-900 via-indigo-800 to-indigo-700 text-white pt-14 pb-20 px-4 sm:px-6 lg:px-8 shadow-inner">
        <div className="max-w-4xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/30 border border-indigo-400/30 text-indigo-200 text-xs font-semibold mb-4 backdrop-blur-sm">
            <Sparkles className="w-3.5 h-3.5 text-amber-300" />
            Distributed Microservices Architecture • Spring AI Concierge
          </div>
          <h1 className="text-3xl sm:text-5xl font-extrabold tracking-tight">
            Smart Hotel Reservations, <br className="hidden sm:inline" />
            Powered by Event-Driven AI
          </h1>
          <p className="mt-3 text-sm sm:text-base text-indigo-100/90 max-w-2xl mx-auto">
            Book top-rated hotels, apply instant discount coupons, and simulate real-time Saga payments coordinated via Kafka and Spring Cloud.
          </p>

          {/* Search Form */}
          <form
            onSubmit={handleSearch}
            className="mt-8 max-w-2xl mx-auto bg-white p-2 rounded-2xl shadow-xl flex flex-col sm:flex-row items-center gap-2 text-slate-800"
          >
            <div className="flex items-center gap-2.5 px-3 py-2 w-full sm:w-auto sm:flex-1">
              <MapPin className="w-5 h-5 text-indigo-600 shrink-0" />
              <input
                type="text"
                placeholder="Where are you going? (e.g. Mumbai, Goa)"
                value={searchCity}
                onChange={(e) => setSearchCity(e.target.value)}
                className="w-full text-sm font-medium placeholder:text-slate-400 focus:outline-none bg-transparent"
              />
            </div>
            <button
              type="submit"
              className="w-full sm:w-auto px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm rounded-xl transition-all shadow-md shadow-indigo-200 flex items-center justify-center gap-2"
            >
              <Search className="w-4 h-4" />
              Search Hotels
            </button>
          </form>

          {/* Quick Filter Tags */}
          <div className="mt-4 flex flex-wrap items-center justify-center gap-2">
            <span className="text-xs text-indigo-200 font-medium">Popular:</span>
            {popularCities.map((city) => (
              <button
                key={city}
                type="button"
                onClick={() => handleSelectCityFilter(city)}
                className={`text-xs px-3 py-1 rounded-full font-semibold transition-all ${
                  (activeCity === city || (!activeCity && city === 'All'))
                    ? 'bg-white text-indigo-900 shadow-sm'
                    : 'bg-indigo-800/60 hover:bg-indigo-800 text-indigo-100'
                }`}
              >
                {city}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Main Content Area */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 -mt-6">
        
        {/* Active Promos Banner */}
        {offers && offers.length > 0 && (
          <div className="bg-white rounded-2xl p-4 shadow-sm border border-slate-200/80 mb-8 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-amber-50 text-amber-600 rounded-xl">
                <Tag className="w-5 h-5" />
              </div>
              <div>
                <h4 className="text-sm font-bold text-slate-900">Active Promotional Offers</h4>
                <p className="text-xs text-slate-500">Apply any coupon at checkout for instant savings</p>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {offers.map((offer) => (
                <div
                  key={offer.id || offer.code}
                  className="px-3 py-1.5 rounded-lg border border-indigo-200 bg-indigo-50 text-indigo-900 text-xs font-mono font-bold flex items-center gap-1.5 shadow-sm"
                >
                  <span>{offer.code}</span>
                  <span className="text-indigo-600 font-sans font-normal text-[11px]">
                    ({offer.discountPercentage}% OFF)
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-sm flex items-center justify-between gap-3 mb-6">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-rose-600 shrink-0" />
              <span>{error}</span>
            </div>
            <button
              onClick={() => fetchData(activeCity)}
              className="px-3 py-1.5 bg-rose-600 text-white rounded-lg text-xs font-semibold hover:bg-rose-700 flex items-center gap-1"
            >
              <RefreshCw className="w-3.5 h-3.5" /> Retry
            </button>
          </div>
        )}

        {/* Header bar */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-xl font-extrabold text-slate-900">
              {activeCity ? `Hotels in ${activeCity}` : 'All Featured Hotels'}
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Showing {hotels.length} hotel{hotels.length !== 1 ? 's' : ''} available for online booking
            </p>
          </div>
        </div>

        {/* Hotels Grid / List */}
        {loading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="bg-white rounded-2xl p-6 border border-slate-200 animate-pulse">
                <div className="h-6 bg-slate-200 rounded w-1/3 mb-3"></div>
                <div className="h-4 bg-slate-100 rounded w-1/4 mb-4"></div>
                <div className="h-4 bg-slate-100 rounded w-2/3"></div>
              </div>
            ))}
          </div>
        ) : hotels.length === 0 ? (
          <div className="bg-white rounded-2xl p-12 text-center border border-slate-200">
            <div className="w-12 h-12 bg-slate-100 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-3">
              <MapPin className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-slate-800">No hotels found in this city</h3>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
              We couldn't find any hotels matching "{activeCity}". Try searching for Mumbai or Goa, or clear the filter.
            </p>
            <button
              onClick={() => handleSelectCityFilter('All')}
              className="mt-4 px-4 py-2 bg-indigo-50 text-indigo-600 font-bold text-xs rounded-xl hover:bg-indigo-100 transition-colors"
            >
              Show All Hotels
            </button>
          </div>
        ) : (
          <div className="space-y-5">
            {hotels.map((hotel) => (
              <HotelCard
                key={hotel.id}
                hotel={hotel}
                rooms={rooms}
                onBookRoom={handleBookRoom}
              />
            ))}
          </div>
        )}
      </div>

      {/* Booking Modal */}
      {selectedRoom && (
        <BookingModal
          room={selectedRoom}
          onClose={() => setSelectedRoom(null)}
          onSuccess={(orderResponse) => {
            setSelectedRoom(null);
            setCreatedOrder(orderResponse);
          }}
        />
      )}

      {/* Payment Modal */}
      {createdOrder && (
        <PaymentModal
          orderData={createdOrder}
          onClose={() => setCreatedOrder(null)}
          onComplete={() => {
            setCreatedOrder(null);
            navigate('/my-bookings');
          }}
        />
      )}
    </div>
  );
}
