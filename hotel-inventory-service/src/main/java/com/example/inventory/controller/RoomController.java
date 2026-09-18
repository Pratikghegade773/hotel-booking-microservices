package com.example.inventory.controller;

import com.example.inventory.dto.RoomWithOfferResponse;
import com.example.inventory.entity.Room;
import com.example.inventory.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomWithOfferResponse> getAvailableRooms(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String offerCode) {
        return roomService.getAvailableRoomsWithOffers(hotelId, offerCode);
    }

    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }
}
