import React, { useState } from 'react';
import { CheckCircle, CreditCard, ShieldAlert, Sparkles, X } from 'lucide-react';
import { bookingApi } from '../api/bookingApi';

export default function PaymentModal({ orderData, onClose, onComplete }) {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const handleSimulatePayment = async () => {
    setLoading(true);
    setError(null);

    try {
      await bookingApi.verifyPayment({
        bookingId: orderData.bookingId,
        razorpayOrderId: orderData.razorpayOrderId,
        razorpayPaymentId: `pay_sim_${Date.now()}`,
        razorpaySignature: 'mock_sig', // Mock signature accepted by backend in dev
      });

      setSuccess(true);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Payment verification failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl border border-slate-100 text-center relative">
        
        {!success && (
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-slate-600 rounded-lg"
          >
            <X className="w-5 h-5" />
          </button>
        )}

        {!success ? (
          <div>
            <div className="w-12 h-12 rounded-2xl bg-indigo-50 border border-indigo-100 text-indigo-600 flex items-center justify-center mx-auto mb-4">
              <CreditCard className="w-6 h-6" />
            </div>

            <h3 className="text-lg font-bold text-slate-900">Razorpay Checkout</h3>
            <p className="text-xs text-slate-500 mt-1">Payment Gateway Simulation</p>

            <div className="my-5 p-3.5 bg-slate-50 rounded-xl text-left text-xs space-y-2 border border-slate-100 font-mono">
              <div className="flex justify-between">
                <span className="text-slate-400">Booking ID:</span>
                <span className="font-bold text-slate-700">#{orderData.bookingId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Order ID:</span>
                <span className="font-bold text-slate-700 truncate max-w-[150px]">{orderData.razorpayOrderId}</span>
              </div>
              <div className="flex justify-between pt-1 border-t border-slate-200 text-slate-900 text-sm font-sans font-extrabold">
                <span>Amount:</span>
                <span className="text-indigo-600">₹{orderData.finalAmount}</span>
              </div>
            </div>

            {error && (
              <p className="text-xs text-rose-600 mb-4 bg-rose-50 p-2.5 rounded-lg border border-rose-200">
                {error}
              </p>
            )}

            <button
              onClick={handleSimulatePayment}
              disabled={loading}
              className="w-full py-3 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-lg shadow-indigo-200 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <span>Processing Payment...</span>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  Simulate Successful Payment
                </>
              )}
            </button>
            <p className="text-[10px] text-slate-400 mt-3">
              Triggers asynchronous Kafka event: payment.completed → booking CONFIRMED
            </p>
          </div>
        ) : (
          <div className="py-3">
            <div className="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-3 shadow-inner">
              <CheckCircle className="w-8 h-8" />
            </div>

            <h3 className="text-lg font-bold text-slate-900">Payment Verified!</h3>
            <p className="text-xs text-slate-600 mt-1">
              Your booking #{orderData.bookingId} has been confirmed.
            </p>

            <button
              onClick={onComplete}
              className="mt-6 w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-md shadow-emerald-200 transition-all"
            >
              View My Bookings
            </button>
          </div>
        )}

      </div>
    </div>
  );
}
