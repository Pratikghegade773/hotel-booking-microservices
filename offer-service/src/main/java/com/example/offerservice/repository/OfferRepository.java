package com.example.offerservice.repository;

import com.example.offerservice.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByCodeIgnoreCaseAndIsActiveTrue(String code);
    List<Offer> findByIsActiveTrue();
    boolean existsByCodeIgnoreCase(String code);
}
