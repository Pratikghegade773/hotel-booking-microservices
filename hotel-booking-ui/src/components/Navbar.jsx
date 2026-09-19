import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Hotel, User, LogOut, LogIn, Calendar, ShieldAlert, Sparkles, LayoutDashboard } from 'lucide-react';

export default function Navbar() {
  const { user, logout, isOwner, isManager } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand Logo */}
        <Link to="/" className="flex items-center gap-2.5">
          <div className="bg-indigo-600 text-white p-2 rounded-xl shadow-md shadow-indigo-200">
            <Hotel className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl font-bold bg-gradient-to-r from-indigo-600 to-indigo-800 bg-clip-text text-transparent">
              HotelBooking.ai
            </span>
            <span className="hidden sm:inline-block ml-2 text-xs font-semibold px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200">
              Microservices
            </span>
          </div>
        </Link>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-600">
          <Link to="/" className="hover:text-indigo-600 transition-colors">
            Explore Hotels
          </Link>

          {user && (
            <Link to="/my-bookings" className="flex items-center gap-1.5 hover:text-indigo-600 transition-colors">
              <Calendar className="w-4 h-4 text-slate-400" />
              My Bookings
            </Link>
          )}

          {isManager && (
            <Link to="/manager/bookings" className="flex items-center gap-1.5 text-amber-700 hover:text-amber-800 font-semibold transition-colors">
              <ShieldAlert className="w-4 h-4" />
              Manager Desk
            </Link>
          )}

          {isOwner && (
            <Link to="/owner/dashboard" className="flex items-center gap-1.5 text-indigo-700 hover:text-indigo-800 font-semibold transition-colors">
              <LayoutDashboard className="w-4 h-4" />
              Owner Dashboard
            </Link>
          )}
        </nav>

        {/* User / Auth Actions */}
        <div className="flex items-center gap-3">
          {user ? (
            <div className="flex items-center gap-3">
              <div className="hidden sm:flex flex-col text-right">
                <span className="text-xs font-bold text-slate-900 leading-tight">{user.name}</span>
                <span className="text-[10px] font-semibold tracking-wider uppercase text-indigo-600">
                  {user.role}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="p-2 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                title="Logout"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-3.5 py-1.5 text-sm font-medium text-slate-700 hover:text-indigo-600 transition-colors"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="px-4 py-1.5 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm shadow-indigo-200 transition-all"
              >
                Register
              </Link>
            </div>
          )}
        </div>

      </div>
    </header>
  );
}
