package com.example.common.dto;

import java.math.BigDecimal;

public record PaymentOrderCreationResponse(
        Long bookingId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency,
        String keyId
) {}
