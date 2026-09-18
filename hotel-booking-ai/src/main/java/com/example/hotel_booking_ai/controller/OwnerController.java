package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.dto.HotelRequest;
import com.example.hotel_booking_ai.dto.ManagerCreateRequest;
import com.example.hotel_booking_ai.entity.Customer;
import com.example.hotel_booking_ai.entity.Hotel;
import com.example.hotel_booking_ai.repository.BookingRepository;
import com.example.hotel_booking_ai.repository.HotelRepository;
import com.example.hotel_booking_ai.repository.RoomRepository;
import com.example.hotel_booking_ai.service.CustomerService;
import com.example.hotel_booking_ai.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final HotelService hotelService;
    private final CustomerService customerService;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public OwnerController(
            HotelService hotelService,
            CustomerService customerService,
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {
        this.hotelService = hotelService;
        this.customerService = customerService;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(
            @Valid @RequestBody HotelRequest request,
            Authentication authentication) {
        Customer owner = customerService.getCustomerByEmail(authentication.getName());
        return hotelService.createHotel(request, owner.getId());
    }

    @GetMapping("/hotels")
    public List<Hotel> getMyHotels(Authentication authentication) {
        Customer owner = customerService.getCustomerByEmail(authentication.getName());
        return hotelService.getHotelsByOwner(owner.getId());
    }

    @PostMapping("/managers")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createManager(
            @Valid @RequestBody ManagerCreateRequest request) {
        // Validate hotel exists
        hotelService.getHotelById(request.hotelId());
        Customer manager = customerService.createManager(request);

        return Map.of(
                "id", manager.getId(),
                "name", manager.getName(),
                "email", manager.getEmail(),
                "role", manager.getRole(),
                "hotelId", manager.getHotelId()
        );
    }

    @GetMapping("/reports")
    public Map<String, Object> getReports(Authentication authentication) {
        Customer owner = customerService.getCustomerByEmail(authentication.getName());
        List<Hotel> hotels = hotelService.getHotelsByOwner(owner.getId());

        int totalHotels = hotels.size();
        int totalRooms = hotels.stream()
                .mapToInt(h -> roomRepository.findByHotelId(h.getId()).size())
                .sum();

        var ownerBookings = hotels.stream()
                .flatMap(h -> bookingRepository.findByHotelIdOrderByCreatedAtDesc(h.getId()).stream())
                .toList();

        int totalBookings = ownerBookings.size();
        BigDecimal totalRevenue = ownerBookings.stream()
                .filter(b -> !"CANCELLED".equals(b.getStatus()) && !"PENDING_PAYMENT".equals(b.getStatus()))
                .map(b -> b.getFinalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalHotels", totalHotels,
                "totalRooms", totalRooms,
                "totalBookings", totalBookings,
                "totalRevenue", totalRevenue
        );
    }
}
