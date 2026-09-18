package com.example.userservice.dto;

public record AuthResponse(
        String token,
        Long id,
        String name,
        String email,
        String role,
        Long hotelId
) {}
