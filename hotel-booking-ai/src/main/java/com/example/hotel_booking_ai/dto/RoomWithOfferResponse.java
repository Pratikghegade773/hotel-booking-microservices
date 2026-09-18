package com.example.hotel_booking_ai.dto;

import java.math.BigDecimal;

public record RoomWithOfferResponse(
        Long id,
        Long hotelId,
        String roomNumber,
        String type,
        BigDecimal basePrice,
        BigDecimal discountAmount,
        BigDecimal offerPrice,
        Integer capacity,
        Boolean isAvailable
) {}
