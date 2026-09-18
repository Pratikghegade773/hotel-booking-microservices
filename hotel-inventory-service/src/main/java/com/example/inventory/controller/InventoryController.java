package com.example.inventory.controller;

import com.example.common.constant.SecurityHeaders;
import com.example.inventory.dto.HotelRequest;
import com.example.inventory.dto.RoomRequest;
import com.example.inventory.entity.Hotel;
import com.example.inventory.entity.Room;
import com.example.inventory.service.HotelService;
import com.example.inventory.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final HotelService hotelService;
    private final RoomService roomService;

    public InventoryController(HotelService hotelService, RoomService roomService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(
            @Valid @RequestBody HotelRequest request,
            @RequestHeader(SecurityHeaders.USER_ID) Long ownerId) {
        return hotelService.createHotel(request, ownerId);
    }

    @GetMapping("/hotels/my")
    public List<Hotel> getMyHotels(@RequestHeader(SecurityHeaders.USER_ID) Long ownerId) {
        return hotelService.getHotelsByOwner(ownerId);
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public Room addRoom(
            @Valid @RequestBody RoomRequest request,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        if ("MANAGER".equals(role) && managerHotelId != null) {
            if (!managerHotelId.equals(request.hotelId())) {
                throw new IllegalArgumentException("You can only add rooms for your assigned hotel: " + managerHotelId);
            }
        }
        return roomService.createRoom(request);
    }

    @PutMapping("/rooms/{id}")
    public Room updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        Room existing = roomService.getRoomById(id);
        if ("MANAGER".equals(role) && managerHotelId != null) {
            if (!managerHotelId.equals(existing.getHotelId())) {
                throw new IllegalArgumentException("You can only update rooms for your assigned hotel");
            }
        }
        return roomService.updateRoom(id, request);
    }

    @PatchMapping("/rooms/{id}/availability")
    public Room updateRoomAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isAvailable,
            @RequestHeader(value = SecurityHeaders.USER_ROLE, required = false) String role,
            @RequestHeader(value = SecurityHeaders.HOTEL_ID, required = false) Long managerHotelId) {
        Room existing = roomService.getRoomById(id);
        if ("MANAGER".equals(role) && managerHotelId != null) {
            if (!managerHotelId.equals(existing.getHotelId())) {
                throw new IllegalArgumentException("You can only update rooms for your assigned hotel");
            }
        }
        return roomService.updateAvailability(id, isAvailable);
    }

    @GetMapping("/summary")
    public Map<String, Object> getOwnerSummary(@RequestParam Long ownerId) {
        List<Hotel> hotels = hotelService.getHotelsByOwner(ownerId);
        int totalHotels = hotels.size();
        int totalRooms = hotels.stream()
                .mapToInt(h -> roomService.getRoomsByHotel(h.getId()).size())
                .sum();
        List<Long> hotelIds = hotels.stream().map(Hotel::getId).toList();

        return Map.of(
                "totalHotels", totalHotels,
                "totalRooms", totalRooms,
                "hotelIds", hotelIds
        );
    }
}
