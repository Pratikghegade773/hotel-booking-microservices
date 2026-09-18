package com.example.bookingservice.controller;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.service.BookingService;
import com.example.common.constant.SecurityHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/bookings")
public class ManagerBookingController {

    private final BookingService bookingService;

    public ManagerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getHotelBookings(
            @RequestParam(required = false) Long hotelId,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        Long targetHotelId = "MANAGER".equals(role) ? managerHotelId : hotelId;
        if (targetHotelId == null) {
            throw new IllegalArgumentException("Hotel ID is required");
        }
        return bookingService.getHotelBookings(targetHotelId);
    }

    @PostMapping("/{id}/check-in")
    public Booking checkIn(
            @PathVariable Long id,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        Long assignedHotel = "MANAGER".equals(role) ? managerHotelId : null;
        return bookingService.checkIn(id, assignedHotel);
    }

    @PostMapping("/{id}/check-out")
    public Booking checkOut(
            @PathVariable Long id,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        Long assignedHotel = "MANAGER".equals(role) ? managerHotelId : null;
        return bookingService.checkOut(id, assignedHotel);
    }
}
