package com.example.bookingservice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookingOrderRequest(
        @NotNull Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        String offerCode
) {}
