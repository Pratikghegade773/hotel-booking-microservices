package com.example.bookingservice.controller;

import com.example.bookingservice.client.RoomInventoryClient;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.example.common.constant.SecurityHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerReportController {

    private final RoomInventoryClient roomInventoryClient;
    private final BookingRepository bookingRepository;

    public OwnerReportController(RoomInventoryClient roomInventoryClient, BookingRepository bookingRepository) {
        this.roomInventoryClient = roomInventoryClient;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/reports")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getReports(@RequestHeader(SecurityHeaders.USER_ID) Long ownerId) {
        Map<String, Object> inventorySummary = roomInventoryClient.getOwnerSummary(ownerId);
        int totalHotels = ((Number) inventorySummary.getOrDefault("totalHotels", 0)).intValue();
        int totalRooms = ((Number) inventorySummary.getOrDefault("totalRooms", 0)).intValue();
        List<Object> rawIds = (List<Object>) inventorySummary.getOrDefault("hotelIds", Collections.emptyList());

        List<Long> hotelIds = rawIds.stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        List<Booking> ownerBookings = hotelIds.isEmpty()
                ? Collections.emptyList()
                : bookingRepository.findByHotelIdInOrderByCreatedAtDesc(hotelIds);

        int totalBookings = ownerBookings.size();
        BigDecimal totalRevenue = ownerBookings.stream()
                .filter(b -> !"CANCELLED".equals(b.getStatus()) && !"PENDING_PAYMENT".equals(b.getStatus()) && !"FAILED".equals(b.getStatus()))
                .map(Booking::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalHotels", totalHotels,
                "totalRooms", totalRooms,
                "totalBookings", totalBookings,
                "totalRevenue", totalRevenue
        );
    }
}
