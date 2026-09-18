package com.example.bookingservice.client;

import com.example.common.dto.RoomValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "hotel-inventory-service")
public interface RoomInventoryClient {

    @GetMapping("/api/internal/rooms/{id}")
    RoomValidationResponse getRoomById(@PathVariable("id") Long id);

    @PatchMapping("/api/internal/rooms/{id}/availability")
    RoomValidationResponse updateAvailability(@PathVariable("id") Long id, @RequestParam("isAvailable") Boolean isAvailable);

    @GetMapping("/api/inventory/summary")
    Map<String, Object> getOwnerSummary(@RequestParam("ownerId") Long ownerId);
}
