package com.example.paymentservice.controller;

import com.example.common.dto.PaymentOrderCreationRequest;
import com.example.common.dto.PaymentOrderCreationResponse;
import com.example.paymentservice.dto.PaymentVerifyRequest;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentOrderCreationResponse createPaymentOrder(@Valid @RequestBody PaymentOrderCreationRequest request) {
        return paymentService.createPaymentOrder(request);
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        Payment confirmed = paymentService.verifyPayment(request);
        return Map.of(
                "message", "Payment verified successfully",
                "bookingId", confirmed.getBookingId(),
                "status", confirmed.getStatus(),
                "amount", confirmed.getAmount(),
                "paymentId", confirmed.getRazorpayPaymentId()
        );
    }

    @GetMapping("/booking/{bookingId}")
    public Payment getPaymentByBooking(@PathVariable Long bookingId) {
        return paymentService.getPaymentByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for booking id: " + bookingId));
    }
}
