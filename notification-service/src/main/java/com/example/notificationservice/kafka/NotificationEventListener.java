package com.example.notificationservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void onBookingEvent(Map<String, Object> event) {
        log.info("📧 [Notification Service] Received booking event: {}", event);

        Object bookingId = event.get("bookingId");
        Object customerId = event.get("customerId");
        Object finalAmount = event.get("finalAmount");

        if (event.containsKey("checkInDate")) {
            log.info(">> Sending BOOKING CONFIRMATION EMAIL to customer {}: Booking #{} confirmed for INR {}",
                    customerId, bookingId, finalAmount);
        } else if (event.containsKey("reason")) {
            log.info(">> Sending BOOKING CANCELLATION NOTICE to customer {}: Booking #{} cancelled. Reason: {}",
                    customerId, bookingId, event.get("reason"));
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void onPaymentEvent(Map<String, Object> event) {
        log.info("💳 [Notification Service] Received payment event: {}", event);

        Object bookingId = event.get("bookingId");
        Object amount = event.get("amount");
        String status = (String) event.get("status");

        if ("SUCCESS".equalsIgnoreCase(status)) {
            log.info(">> Sending PAYMENT RECEIPT: Payment of INR {} successful for Booking #{}", amount, bookingId);
        } else {
            log.warn(">> Sending PAYMENT FAILURE ALERT for Booking #{}: Payment failed", bookingId);
        }
    }
}
