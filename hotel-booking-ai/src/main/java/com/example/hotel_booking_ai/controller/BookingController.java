package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.dto.BookingOrderRequest;
import com.example.hotel_booking_ai.dto.BookingOrderResponse;
import com.example.hotel_booking_ai.entity.Booking;
import com.example.hotel_booking_ai.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create-order")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingOrderResponse createBookingOrder(
            @Valid @RequestBody BookingOrderRequest request,
            Authentication authentication) {
        return bookingService.createBookingOrder(request, authentication.getName());
    }

    @GetMapping("/my-bookings")
    public List<Booking> getMyBookings(Authentication authentication) {
        return bookingService.getCustomerBookings(authentication.getName());
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        return bookingService.cancelBooking(id, authentication.getName());
    }
}
