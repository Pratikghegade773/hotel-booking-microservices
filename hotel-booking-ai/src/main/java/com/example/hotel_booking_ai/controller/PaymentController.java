package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.dto.PaymentVerifyRequest;
import com.example.hotel_booking_ai.entity.Booking;
import com.example.hotel_booking_ai.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BookingService bookingService;

    public PaymentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        Booking confirmedBooking = bookingService.verifyPayment(request);
        return Map.of(
                "message", "Payment verified and booking confirmed successfully",
                "bookingId", confirmedBooking.getId(),
                "status", confirmedBooking.getStatus(),
                "finalAmount", confirmedBooking.getFinalAmount()
        );
    }
}
