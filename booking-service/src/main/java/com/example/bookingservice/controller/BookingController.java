package com.example.bookingservice.controller;

import com.example.bookingservice.dto.BookingOrderRequest;
import com.example.bookingservice.dto.BookingOrderResponse;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.service.BookingService;
import com.example.common.constant.SecurityHeaders;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
            @RequestHeader(SecurityHeaders.USER_ID) Long customerId,
            @RequestHeader(SecurityHeaders.USER_EMAIL) String customerEmail) {
        return bookingService.createBookingOrder(request, customerId, customerEmail);
    }

    @GetMapping("/my-bookings")
    public List<Booking> getMyBookings(@RequestHeader(SecurityHeaders.USER_ID) Long customerId) {
        return bookingService.getCustomerBookings(customerId);
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/{id}/cancel")
    public Booking cancelBooking(
            @PathVariable Long id,
            @RequestHeader(SecurityHeaders.USER_ID) Long customerId,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, defaultValue = "CUSTOMER") String role) {
        return bookingService.cancelBooking(id, customerId, role);
    }
}
