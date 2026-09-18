package com.example.hotel_booking_ai.controller;

import com.example.hotel_booking_ai.dto.OfferRequest;
import com.example.hotel_booking_ai.entity.Offer;
import com.example.hotel_booking_ai.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public List<Offer> getActiveOffers() {
        return offerService.getActiveOffers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Offer createOffer(@Valid @RequestBody OfferRequest request) {
        return offerService.createOffer(request);
    }
}
