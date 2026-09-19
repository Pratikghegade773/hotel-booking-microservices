import React from 'react';
import { Users, Sparkles, CheckCircle2, XCircle } from 'lucide-react';

export default function RoomCard({ room, onBook }) {
  const isAvailable = room.isAvailable !== false;
  const hasDiscount = room.discountAmount && Number(room.discountAmount) > 0;

  const typeColorMap = {
    DELUXE: 'bg-amber-50 text-amber-700 border-amber-200',
    SUITE: 'bg-purple-50 text-purple-700 border-purple-200',
    STANDARD: 'bg-blue-50 text-blue-700 border-blue-200',
  };

  const badgeColor = typeColorMap[room.type?.toUpperCase()] || 'bg-slate-100 text-slate-700 border-slate-200';

  return (
    <div className={`p-4 rounded-xl border transition-all ${
      isAvailable ? 'bg-white border-slate-200 shadow-sm hover:border-indigo-200' : 'bg-slate-50 border-slate-200 opacity-60'
    }`}>
      <div className="flex items-start justify-between gap-2">
        <div>
          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-900 text-base">Room {room.roomNumber}</span>
            <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${badgeColor}`}>
              {room.type || 'STANDARD'}
            </span>
          </div>
          <p className="mt-1 flex items-center text-xs text-slate-500 gap-1">
            <Users className="w-3.5 h-3.5" />
            Up to {room.capacity || 2} Guests
          </p>
        </div>

        <div>
          {isAvailable ? (
            <span className="flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
              <CheckCircle2 className="w-3.5 h-3.5" /> Available
            </span>
          ) : (
            <span className="flex items-center gap-1 text-[11px] font-semibold text-rose-500">
              <XCircle className="w-3.5 h-3.5" /> Booked
            </span>
          )}
        </div>
      </div>

      {/* Pricing & CTA */}
      <div className="mt-4 pt-3 border-t border-slate-100 flex items-end justify-between">
        <div>
          <span className="text-[10px] text-slate-400 font-medium block">Price per night</span>
          <div className="flex items-baseline gap-2">
            {hasDiscount ? (
              <>
                <span className="text-base font-extrabold text-indigo-600">
                  ₹{room.finalPrice || room.basePrice}
                </span>
                <span className="text-xs text-slate-400 line-through">
                  ₹{room.basePrice}
                </span>
              </>
            ) : (
              <span className="text-base font-extrabold text-slate-900">
                ₹{room.basePrice}
              </span>
            )}
          </div>
          {hasDiscount && (
            <span className="inline-flex items-center gap-0.5 text-[10px] font-bold text-emerald-600">
              <Sparkles className="w-3 h-3" /> Save ₹{room.discountAmount}
            </span>
          )}
        </div>

        <button
          onClick={() => onBook(room)}
          disabled={!isAvailable}
          className={`px-4 py-2 text-xs font-bold rounded-xl transition-all shadow-sm ${
            isAvailable
              ? 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-indigo-100 hover:shadow-indigo-200'
              : 'bg-slate-200 text-slate-400 cursor-not-allowed'
          }`}
        >
          {isAvailable ? 'Book Room' : 'Unavailable'}
        </button>
      </div>
    </div>
  );
}
