package com.example.hotel_booking_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RoomRequest(
        @NotNull Long hotelId,
        @NotBlank String roomNumber,
        @NotBlank String type, // STANDARD, DELUXE, SUITE
        @NotNull BigDecimal basePrice,
        Integer capacity,
        Boolean isAvailable
) {}
