package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.dto.HotelRequest;
import com.example.hotel_booking_ai.entity.Hotel;
import com.example.hotel_booking_ai.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public Hotel createHotel(HotelRequest request, Long ownerId) {
        Hotel hotel = new Hotel(
                request.name(),
                request.city(),
                request.address(),
                request.description(),
                ownerId
        );
        return hotelRepository.save(hotel);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<Hotel> getHotelsByCity(String city) {
        if (city == null || city.isBlank()) {
            return hotelRepository.findAll();
        }
        return hotelRepository.findByCityIgnoreCase(city);
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + id));
    }

    public List<Hotel> getHotelsByOwner(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId);
    }
}
