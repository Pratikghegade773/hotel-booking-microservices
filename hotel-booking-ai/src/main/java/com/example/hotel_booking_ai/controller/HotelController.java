package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.entity.Hotel;
import com.example.hotel_booking_ai.service.HotelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public List<Hotel> getHotels(@RequestParam(required = false) String city) {
        return hotelService.getHotelsByCity(city);
    }

    @GetMapping("/{id}")
    public Hotel getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }
}
