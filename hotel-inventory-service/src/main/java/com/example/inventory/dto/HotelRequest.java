package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank String city,
        String address,
        String description
) {}
