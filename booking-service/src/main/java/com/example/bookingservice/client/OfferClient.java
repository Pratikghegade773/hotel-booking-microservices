package com.example.bookingservice.client;

import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "offer-service")
public interface OfferClient {

    @PostMapping("/api/offers/calculate")
    DiscountCalculationResponse calculateDiscount(@RequestBody DiscountCalculationRequest request);
}
