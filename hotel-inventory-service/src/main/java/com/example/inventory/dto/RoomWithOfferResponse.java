package com.example.inventory.dto;

import java.math.BigDecimal;

public record RoomWithOfferResponse(
        Long id,
        Long hotelId,
        String roomNumber,
        String type,
        BigDecimal basePrice,
        BigDecimal discountAmount,
        BigDecimal finalPrice,
        Integer capacity,
        Boolean isAvailable
) {}
