package com.example.bookingservice.kafka;

import com.example.common.event.BookingCancelledEvent;
import com.example.common.event.BookingConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);
    private static final String BOOKING_TOPIC = "booking-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Publishing BookingConfirmedEvent for booking id: {}", event.bookingId());
        try {
            kafkaTemplate.send(BOOKING_TOPIC, String.valueOf(event.bookingId()), event);
        } catch (Exception e) {
            log.error("Failed to publish BookingConfirmedEvent to Kafka: {}", e.getMessage());
        }
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing BookingCancelledEvent for booking id: {}", event.bookingId());
        try {
            kafkaTemplate.send(BOOKING_TOPIC, String.valueOf(event.bookingId()), event);
        } catch (Exception e) {
            log.error("Failed to publish BookingCancelledEvent to Kafka: {}", e.getMessage());
        }
    }
}
