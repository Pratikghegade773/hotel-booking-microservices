package com.example.common.dto;

import java.math.BigDecimal;

public record DiscountCalculationResponse(
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String offerCode,
        boolean applied
) {}
