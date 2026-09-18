package com.example.common.event;

import java.time.Instant;

public record PaymentFailedEvent(
        Long bookingId,
        String razorpayOrderId,
        String reason,
        Instant timestamp
) {}
