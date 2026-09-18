package com.example.hotel_booking_ai.repository;

import com.example.hotel_booking_ai.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByCodeIgnoreCaseAndIsActiveTrue(String code);
    List<Offer> findByIsActiveTrue();
}
