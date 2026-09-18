package com.example.common.event;

import java.time.Instant;

public record BookingCancelledEvent(
        Long bookingId,
        Long customerId,
        Long roomId,
        Long hotelId,
        String reason,
        Instant timestamp
) {}
