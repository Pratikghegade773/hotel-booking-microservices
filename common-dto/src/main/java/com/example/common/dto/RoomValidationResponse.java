package com.example.common.dto;

import java.math.BigDecimal;

public record RoomValidationResponse(
        Long id,
        Long hotelId,
        String roomNumber,
        String type,
        BigDecimal basePrice,
        Integer capacity,
        Boolean isAvailable
) {}
