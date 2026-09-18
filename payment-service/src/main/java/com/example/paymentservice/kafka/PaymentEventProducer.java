package com.example.paymentservice.kafka;

import com.example.common.event.PaymentCompletedEvent;
import com.example.common.event.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String PAYMENT_TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent for booking: {}", event.bookingId());
        try {
            kafkaTemplate.send(PAYMENT_TOPIC, String.valueOf(event.bookingId()), event);
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompletedEvent to Kafka: {}", e.getMessage());
        }
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.warn("Publishing PaymentFailedEvent for booking: {}", event.bookingId());
        try {
            kafkaTemplate.send(PAYMENT_TOPIC, String.valueOf(event.bookingId()), event);
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailedEvent to Kafka: {}", e.getMessage());
        }
    }
}
