package com.example.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long bookingId,
        String razorpayOrderId,
        String razorpayPaymentId,
        BigDecimal amount,
        String status,
        Instant timestamp
) {}
