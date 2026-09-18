package com.example.common.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DiscountCalculationRequest(
        @NotNull BigDecimal amount,
        String offerCode
) {}
