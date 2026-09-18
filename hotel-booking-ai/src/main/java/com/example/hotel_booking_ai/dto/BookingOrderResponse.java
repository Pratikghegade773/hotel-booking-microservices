package com.example.hotel_booking_ai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingOrderResponse(
        Long bookingId,
        Long roomId,
        Long hotelId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String status,
        String razorpayOrderId,
        String currency,
        String razorpayKeyId
) {}
