package com.example.hotel_booking_ai.repository;

import com.example.hotel_booking_ai.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByCityIgnoreCase(String city);
    List<Hotel> findByOwnerId(Long ownerId);
}
