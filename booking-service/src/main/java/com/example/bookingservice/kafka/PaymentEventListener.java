package com.example.bookingservice.kafka;

import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.repository.BookingRepository;
import com.example.common.event.BookingConfirmedEvent;
import com.example.common.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;

    public PaymentEventListener(BookingRepository bookingRepository, BookingEventProducer bookingEventProducer) {
        this.bookingRepository = bookingRepository;
        this.bookingEventProducer = bookingEventProducer;
    }

    @KafkaListener(topics = "payment-events", groupId = "booking-service-group")
    @Transactional
    public void onPaymentEvent(Map<String, Object> event) {
        log.info("Received payment event: {}", event);

        Object bookingIdObj = event.get("bookingId");
        if (bookingIdObj == null) {
            return;
        }
        Long bookingId = ((Number) bookingIdObj).longValue();
        String status = (String) event.get("status");

        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                log.info("Booking {} transitioned to CONFIRMED following payment success", bookingId);

                bookingEventProducer.publishBookingConfirmed(new BookingConfirmedEvent(
                        booking.getId(),
                        booking.getCustomerId(),
                        booking.getRoomId(),
                        booking.getHotelId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getFinalAmount(),
                        null,
                        Instant.now()
                ));
            } else if ("FAILED".equalsIgnoreCase(status)) {
                booking.setStatus("FAILED");
                bookingRepository.save(booking);
                log.warn("Booking {} transitioned to FAILED following payment failure", bookingId);
            }
        });
    }
}
