import React, { useState } from 'react';
import { MapPin, ChevronDown, ChevronUp, BedDouble } from 'lucide-react';
import RoomCard from './RoomCard';

export default function HotelCard({ hotel, rooms, onBookRoom }) {
  const [expanded, setExpanded] = useState(false);
  const hotelRooms = rooms.filter((r) => r.hotelId === hotel.id);

  return (
    <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition-shadow">
      <div className="p-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-xl font-bold text-slate-900">{hotel.name}</h3>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                Active
              </span>
            </div>
            <p className="mt-1 flex items-center text-sm font-medium text-slate-500">
              <MapPin className="w-4 h-4 mr-1 text-indigo-500 shrink-0" />
              {hotel.city} {hotel.address ? `• ${hotel.address}` : ''}
            </p>
          </div>

          <button
            onClick={() => setExpanded(!expanded)}
            className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded-xl transition-colors shrink-0"
          >
            <BedDouble className="w-4 h-4" />
            {hotelRooms.length} Room{hotelRooms.length !== 1 ? 's' : ''}
            {expanded ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
          </button>
        </div>

        <p className="mt-3 text-sm text-slate-600 line-clamp-2 leading-relaxed">
          {hotel.description || 'Experience comfort and exceptional service at this premier destination.'}
        </p>
      </div>

      {/* Expandable Rooms Container */}
      {expanded && (
        <div className="bg-slate-50/70 border-t border-slate-100 p-5 space-y-3">
          <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
            Available Rooms at {hotel.name}
          </h4>
          {hotelRooms.length === 0 ? (
            <div className="text-center py-6 text-sm text-slate-400">
              No rooms listed for this hotel yet.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
              {hotelRooms.map((room) => (
                <RoomCard key={room.id} room={room} onBook={onBookRoom} />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
