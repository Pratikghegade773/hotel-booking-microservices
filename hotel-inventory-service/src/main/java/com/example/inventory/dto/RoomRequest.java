package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RoomRequest(
        @NotNull Long hotelId,
        @NotBlank String roomNumber,
        @NotBlank String type,
        @NotNull BigDecimal basePrice,
        Integer capacity,
        Boolean isAvailable
) {}
