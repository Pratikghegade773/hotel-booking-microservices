package com.example.offerservice.controller;

import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import com.example.offerservice.dto.OfferRequest;
import com.example.offerservice.entity.Offer;
import com.example.offerservice.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/calculate")
    public DiscountCalculationResponse calculateDiscount(@Valid @RequestBody DiscountCalculationRequest request) {
        return offerService.calculateDiscount(request);
    }
}
