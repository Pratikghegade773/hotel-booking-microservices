package com.example.inventory.controller;

import com.example.common.dto.RoomValidationResponse;
import com.example.inventory.entity.Room;
import com.example.inventory.service.RoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/rooms")
public class InternalRoomController {

    private final RoomService roomService;

    public InternalRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{id}")
    public RoomValidationResponse getRoomForValidation(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return new RoomValidationResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomNumber(),
                room.getType(),
                room.getBasePrice(),
                room.getCapacity(),
                room.getIsAvailable()
        );
    }

    @PatchMapping("/{id}/availability")
    public RoomValidationResponse updateAvailability(@PathVariable Long id, @RequestParam Boolean isAvailable) {
        Room room = roomService.updateAvailability(id, isAvailable);
        return new RoomValidationResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomNumber(),
                room.getType(),
                room.getBasePrice(),
                room.getCapacity(),
                room.getIsAvailable()
        );
    }
}
