package com.example.common.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentOrderCreationRequest(
        @NotNull Long bookingId,
        @NotNull BigDecimal amount,
        String customerEmail
) {}
