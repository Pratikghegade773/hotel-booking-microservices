package com.example.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingConfirmedEvent(
        Long bookingId,
        Long customerId,
        Long roomId,
        Long hotelId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal finalAmount,
        String customerEmail,
        Instant timestamp
) {}
