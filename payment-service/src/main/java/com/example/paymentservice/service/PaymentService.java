package com.example.paymentservice.service;

import com.example.common.dto.PaymentOrderCreationRequest;
import com.example.common.dto.PaymentOrderCreationResponse;
import com.example.common.event.PaymentCompletedEvent;
import com.example.common.event.PaymentFailedEvent;
import com.example.paymentservice.dto.PaymentVerifyRequest;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.kafka.PaymentEventProducer;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            RazorpayService razorpayService,
            PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.razorpayService = razorpayService;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public PaymentOrderCreationResponse createPaymentOrder(PaymentOrderCreationRequest request) {
        String razorpayOrderId = razorpayService.createOrder(request.bookingId(), request.amount());

        Payment payment = new Payment(
                request.bookingId(),
                razorpayOrderId,
                request.amount(),
                "CREATED"
        );
        paymentRepository.save(payment);

        return new PaymentOrderCreationResponse(
                payment.getBookingId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                razorpayService.getKeyId()
        );
    }

    @Transactional
    public Payment verifyPayment(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order: " + request.razorpayOrderId()));

        boolean isValid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!isValid) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);

            paymentEventProducer.publishPaymentFailed(new PaymentFailedEvent(
                    payment.getBookingId(),
                    payment.getRazorpayOrderId(),
                    "Invalid Razorpay payment signature",
                    Instant.now()
            ));

            throw new IllegalArgumentException("Invalid Razorpay payment signature");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setStatus("SUCCESS");
        Payment saved = paymentRepository.save(payment);

        paymentEventProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                saved.getBookingId(),
                saved.getRazorpayOrderId(),
                saved.getRazorpayPaymentId(),
                saved.getAmount(),
                saved.getStatus(),
                Instant.now()
        ));

        return saved;
    }

    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}
