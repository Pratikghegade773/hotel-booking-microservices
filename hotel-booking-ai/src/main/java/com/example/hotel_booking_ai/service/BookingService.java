package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.dto.BookingOrderRequest;
import com.example.hotel_booking_ai.dto.BookingOrderResponse;
import com.example.hotel_booking_ai.dto.PaymentVerifyRequest;
import com.example.hotel_booking_ai.entity.Booking;
import com.example.hotel_booking_ai.entity.Customer;
import com.example.hotel_booking_ai.entity.Payment;
import com.example.hotel_booking_ai.entity.Room;
import com.example.hotel_booking_ai.repository.BookingRepository;
import com.example.hotel_booking_ai.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final RoomService roomService;
    private final OfferService offerService;
    private final RazorpayService razorpayService;

    public BookingService(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            CustomerService customerService,
            RoomService roomService,
            OfferService offerService,
            RazorpayService razorpayService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.customerService = customerService;
        this.roomService = roomService;
        this.offerService = offerService;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public BookingOrderResponse createBookingOrder(BookingOrderRequest request, String customerEmail) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.checkInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        Customer customer = customerService.getCustomerByEmail(customerEmail);
        Room room = roomService.getRoomById(request.roomId());

        if (!Boolean.TRUE.equals(room.getIsAvailable())) {
            throw new IllegalArgumentException("Room is currently marked unavailable");
        }

        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                request.roomId(), request.checkInDate(), request.checkOutDate()
        );
        if (hasOverlap) {
            throw new IllegalArgumentException("Room is already booked for the selected dates");
        }

        long nights = Math.max(1, ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate()));
        BigDecimal totalAmount = room.getBasePrice().multiply(BigDecimal.valueOf(nights));
        BigDecimal discountAmount = offerService.calculateDiscount(totalAmount, request.offerCode());
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        Booking booking = new Booking(
                customer.getId(),
                room.getId(),
                room.getHotelId(),
                request.checkInDate(),
                request.checkOutDate(),
                totalAmount,
                discountAmount,
                finalAmount,
                "PENDING_PAYMENT"
        );
        Booking savedBooking = bookingRepository.save(booking);

        String razorpayOrderId = razorpayService.createOrder(savedBooking.getId(), finalAmount);

        Payment payment = new Payment(savedBooking.getId(), razorpayOrderId, finalAmount, "CREATED");
        paymentRepository.save(payment);

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
                razorpayOrderId,
                "INR",
                razorpayService.getKeyId()
        );
    }

    @Transactional
    public Booking verifyPayment(PaymentVerifyRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for order: " + request.razorpayOrderId()));

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.bookingId()));

        boolean isValid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!isValid) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            throw new IllegalArgumentException("Invalid Razorpay payment signature");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String customerEmail) {
        Customer customer = customerService.getCustomerByEmail(customerEmail);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!booking.getCustomerId().equals(customer.getId()) && !"OWNER".equals(customer.getRole()) && !"MANAGER".equals(customer.getRole())) {
            throw new IllegalArgumentException("You are not authorized to cancel this booking");
        }

        if ("CHECKED_IN".equals(booking.getStatus()) || "CHECKED_OUT".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel booking after check-in has occurred");
        }

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getCustomerBookings(String customerEmail) {
        Customer customer = customerService.getCustomerByEmail(customerEmail);
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
    }
}
