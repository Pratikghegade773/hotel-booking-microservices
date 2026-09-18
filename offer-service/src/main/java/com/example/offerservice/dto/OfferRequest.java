package com.example.offerservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferRequest(
        @NotBlank String code,
        String title,
        String description,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double discountPercentage,
        BigDecimal maxDiscountAmount,
        LocalDate validFrom,
        LocalDate validTo,
        Boolean isActive
) {}
