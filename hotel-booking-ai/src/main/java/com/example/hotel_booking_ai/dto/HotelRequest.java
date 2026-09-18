package com.example.hotel_booking_ai.dto;

import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank String city,
        String address,
        String description
) {}
