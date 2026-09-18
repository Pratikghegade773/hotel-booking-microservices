package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.dto.RoomRequest;
import com.example.hotel_booking_ai.entity.Booking;
import com.example.hotel_booking_ai.entity.Customer;
import com.example.hotel_booking_ai.entity.Room;
import com.example.hotel_booking_ai.service.CustomerService;
import com.example.hotel_booking_ai.service.ManagerService;
import com.example.hotel_booking_ai.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final RoomService roomService;
    private final ManagerService managerService;
    private final CustomerService customerService;

    public ManagerController(
            RoomService roomService,
            ManagerService managerService,
            CustomerService customerService) {
        this.roomService = roomService;
        this.managerService = managerService;
        this.customerService = customerService;
    }

    private Customer getAuthenticatedUser(Authentication authentication) {
        return customerService.getCustomerByEmail(authentication.getName());
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addRoom(
            @Valid @RequestBody RoomRequest request,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        // If manager, verify hotelId matches
        if ("MANAGER".equals(user.getRole()) && user.getHotelId() != null) {
            if (!user.getHotelId().equals(request.hotelId())) {
                throw new IllegalArgumentException("You can only add rooms for your assigned hotel id: " + user.getHotelId());
            }
        }
        return roomService.createRoom(request);
    }

    @PutMapping("/rooms/{id}")
    public Room updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        Room existing = roomService.getRoomById(id);
        if ("MANAGER".equals(user.getRole()) && user.getHotelId() != null) {
            if (!user.getHotelId().equals(existing.getHotelId())) {
                throw new IllegalArgumentException("You can only update rooms for your assigned hotel");
            }
        }
        return roomService.updateRoom(id, request);
    }

    @PatchMapping("/rooms/{id}/availability")
    public Room updateRoomAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isAvailable,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        Room existing = roomService.getRoomById(id);
        if ("MANAGER".equals(user.getRole()) && user.getHotelId() != null) {
            if (!user.getHotelId().equals(existing.getHotelId())) {
                throw new IllegalArgumentException("You can only update rooms for your assigned hotel");
            }
        }
        return roomService.updateAvailability(id, isAvailable);
    }

    @GetMapping("/bookings")
    public List<Booking> getHotelBookings(
            @RequestParam(required = false) Long hotelId,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        Long targetHotelId = "MANAGER".equals(user.getRole()) ? user.getHotelId() : hotelId;
        if (targetHotelId == null) {
            throw new IllegalArgumentException("Hotel ID is required");
        }
        return managerService.getHotelBookings(targetHotelId);
    }

    @PostMapping("/bookings/{id}/check-in")
    public Booking checkIn(
            @PathVariable Long id,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        Long managerHotelId = "MANAGER".equals(user.getRole()) ? user.getHotelId() : null;
        return managerService.checkIn(id, managerHotelId);
    }

    @PostMapping("/bookings/{id}/check-out")
    public Booking checkOut(
            @PathVariable Long id,
            Authentication authentication) {
        Customer user = getAuthenticatedUser(authentication);
        Long managerHotelId = "MANAGER".equals(user.getRole()) ? user.getHotelId() : null;
        return managerService.checkOut(id, managerHotelId);
    }
}
