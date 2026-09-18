package com.example.hotel_booking_ai.dto;

public record AuthResponse(
        String token,
        Long id,
        String name,
        String email,
        String role,
        Long hotelId
) {}
