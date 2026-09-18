package com.example.aiservice.client;

import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "offer-service")
public interface OfferClient {

    @GetMapping("/api/offers")
    List<Map<String, Object>> getActiveOffers();

    @PostMapping("/api/offers/calculate")
    DiscountCalculationResponse calculateDiscount(@RequestBody DiscountCalculationRequest request);
}
