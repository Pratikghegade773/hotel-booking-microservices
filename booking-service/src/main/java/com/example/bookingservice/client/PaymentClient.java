package com.example.bookingservice.client;

import com.example.common.dto.PaymentOrderCreationRequest;
import com.example.common.dto.PaymentOrderCreationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments/orders")
    PaymentOrderCreationResponse createPaymentOrder(@RequestBody PaymentOrderCreationRequest request);
}
