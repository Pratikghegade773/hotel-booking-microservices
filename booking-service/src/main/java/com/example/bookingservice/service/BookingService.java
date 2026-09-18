package com.example.bookingservice.service;

import com.example.bookingservice.client.OfferClient;
import com.example.bookingservice.client.PaymentClient;
import com.example.bookingservice.client.RoomInventoryClient;
import com.example.bookingservice.dto.BookingOrderRequest;
import com.example.bookingservice.dto.BookingOrderResponse;
import com.example.bookingservice.entity.Booking;
import com.example.bookingservice.kafka.BookingEventProducer;
import com.example.bookingservice.repository.BookingRepository;
import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import com.example.common.dto.PaymentOrderCreationRequest;
import com.example.common.dto.PaymentOrderCreationResponse;
import com.example.common.dto.RoomValidationResponse;
import com.example.common.event.BookingCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RoomInventoryClient roomInventoryClient;
    private final OfferClient offerClient;
    private final PaymentClient paymentClient;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(
            BookingRepository bookingRepository,
            RoomInventoryClient roomInventoryClient,
            OfferClient offerClient,
            PaymentClient paymentClient,
            BookingEventProducer bookingEventProducer) {
        this.bookingRepository = bookingRepository;
        this.roomInventoryClient = roomInventoryClient;
        this.offerClient = offerClient;
        this.paymentClient = paymentClient;
        this.bookingEventProducer = bookingEventProducer;
    }

    @Transactional
    public BookingOrderResponse createBookingOrder(BookingOrderRequest request, Long customerId, String customerEmail) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        RoomValidationResponse room = roomInventoryClient.getRoomById(request.roomId());
        if (room == null || !Boolean.TRUE.equals(room.isAvailable())) {
            throw new IllegalArgumentException("Room is currently marked unavailable or does not exist");
        }

        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                request.roomId(), request.checkInDate(), request.checkOutDate()
        );
        if (hasOverlap) {
            throw new IllegalArgumentException("Room is already booked for the selected dates");
        }

        long nights = Math.max(1, ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate()));
        BigDecimal totalAmount = room.basePrice().multiply(BigDecimal.valueOf(nights));

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.offerCode() != null && !request.offerCode().isBlank()) {
            try {
                DiscountCalculationResponse discountRes = offerClient.calculateDiscount(
                        new DiscountCalculationRequest(totalAmount, request.offerCode())
                );
                discountAmount = discountRes.discountAmount();
            } catch (Exception e) {
                log.warn("Failed to calculate discount from offer-service: {}", e.getMessage());
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        Booking booking = new Booking(
                customerId,
                room.id(),
                room.hotelId(),
                request.checkInDate(),
                request.checkOutDate(),
                totalAmount,
                discountAmount,
                finalAmount,
                "PENDING_PAYMENT"
        );
        Booking savedBooking = bookingRepository.save(booking);

        PaymentOrderCreationResponse paymentOrder = paymentClient.createPaymentOrder(
                new PaymentOrderCreationRequest(savedBooking.getId(), finalAmount, customerEmail)
        );

        return new BookingOrderResponse(
                savedBooking.getId(),
                savedBooking.getRoomId(),
                savedBooking.getHotelId(),
                savedBooking.getCheckInDate(),
                savedBooking.getCheckOutDate(),
                savedBooking.getTotalAmount(),
                savedBooking.getDiscountAmount(),
                savedBooking.getFinalAmount(),
                savedBooking.getStatus(),
                paymentOrder.razorpayOrderId(),
                paymentOrder.currency(),
                paymentOrder.keyId()
        );
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, Long customerId, String role) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!booking.getCustomerId().equals(customerId) && !"OWNER".equals(role) && !"MANAGER".equals(role)) {
            throw new IllegalArgumentException("You are not authorized to cancel this booking");
        }

        if ("CHECKED_IN".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel booking after check-in has occurred");
        }

        booking.setStatus("CANCELLED");
        Booking updated = bookingRepository.save(booking);

        try {
            roomInventoryClient.updateAvailability(booking.getRoomId(), true);
        } catch (Exception e) {
            log.warn("Could not update room availability in hotel-inventory-service: {}", e.getMessage());
        }

        bookingEventProducer.publishBookingCancelled(new BookingCancelledEvent(
                updated.getId(),
                updated.getCustomerId(),
                updated.getRoomId(),
                updated.getHotelId(),
                "User requested cancellation",
                Instant.now()
        ));

        return updated;
    }

    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
    }

    public List<Booking> getHotelBookings(Long hotelId) {
        return bookingRepository.findByHotelIdOrderByCreatedAtDesc(hotelId);
    }

    public List<Booking> getBookingsByHotelIds(List<Long> hotelIds) {
        return bookingRepository.findByHotelIdInOrderByCreatedAtDesc(hotelIds);
    }

    @Transactional
    public Booking checkIn(Long bookingId, Long managerHotelId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (managerHotelId != null && !booking.getHotelId().equals(managerHotelId)) {
            throw new IllegalArgumentException("You are not authorized to manage bookings for this hotel");
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only CONFIRMED bookings can be checked in. Current status: " + booking.getStatus());
        }

        booking.setStatus("CHECKED_IN");
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkOut(Long bookingId, Long managerHotelId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (managerHotelId != null && !booking.getHotelId().equals(managerHotelId)) {
            throw new IllegalArgumentException("You are not authorized to manage bookings for this hotel");
        }

        if (!"CHECKED_IN".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Only CHECKED_IN bookings can be checked out. Current status: " + booking.getStatus());
        }

        booking.setStatus("CHECKED_OUT");
        Booking saved = bookingRepository.save(booking);

        try {
            roomInventoryClient.updateAvailability(booking.getRoomId(), true);
        } catch (Exception e) {
            log.warn("Could not update room availability upon checkout: {}", e.getMessage());
        }

        return saved;
    }
}
