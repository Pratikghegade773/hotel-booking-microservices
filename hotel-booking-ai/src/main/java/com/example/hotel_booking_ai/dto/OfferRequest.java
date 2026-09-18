package com.example.hotel_booking_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferRequest(
        @NotBlank String code,
        String title,
        String description,
        @NotNull Double discountPercentage,
        BigDecimal maxDiscountAmount,
        LocalDate validFrom,
        LocalDate validTo,
        Boolean isActive
) {}
