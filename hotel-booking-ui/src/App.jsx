import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import AiChatWidget from './components/AiChatWidget';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import MyBookingsPage from './pages/MyBookingsPage';
import OwnerDashboard from './pages/OwnerDashboard';
import ManagerPage from './pages/ManagerPage';
import { useAuth } from './context/AuthContext';

// Protected Route helper
function ProtectedRoute({ children, roleRequired }) {
  const { user, loading, isOwner, isManager } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (roleRequired === 'OWNER' && !isOwner) {
    return <Navigate to="/" replace />;
  }

  if (roleRequired === 'MANAGER' && !isManager) {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default function App() {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-900 selection:bg-indigo-500 selection:text-white relative">
      <Navbar />

      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute>
                <MyBookingsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/owner/dashboard"
            element={
              <ProtectedRoute roleRequired="OWNER">
                <OwnerDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/manager/bookings"
            element={
              <ProtectedRoute roleRequired="MANAGER">
                <ManagerPage />
              </ProtectedRoute>
            }
          />

          {/* Catch-all fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      {/* Floating AI Concierge Chatbot */}
      <AiChatWidget />
    </div>
  );
}
