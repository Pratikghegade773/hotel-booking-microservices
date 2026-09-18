package com.example.aiservice.client;

import com.example.common.dto.RoomValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "hotel-inventory-service")
public interface HotelInventoryClient {

    @GetMapping("/api/hotels")
    List<Map<String, Object>> getHotels(@RequestParam(value = "city", required = false) String city);

    @GetMapping("/api/hotels/{id}")
    Map<String, Object> getHotelById(@PathVariable("id") Long id);

    @GetMapping("/api/internal/rooms/{id}")
    RoomValidationResponse getRoomById(@PathVariable("id") Long id);

    @GetMapping("/api/rooms")
    List<Map<String, Object>> getRooms(
            @RequestParam(value = "hotelId", required = false) Long hotelId,
            @RequestParam(value = "offerCode", required = false) String offerCode
    );
}
