package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.entity.Booking;
import com.example.hotel_booking_ai.entity.Room;
import com.example.hotel_booking_ai.repository.BookingRepository;
import com.example.hotel_booking_ai.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManagerService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public ManagerService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    public List<Booking> getHotelBookings(Long hotelId) {
        return bookingRepository.findByHotelIdOrderByCreatedAtDesc(hotelId);
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

        // Ensure room is marked available
        roomRepository.findById(booking.getRoomId()).ifPresent(room -> {
            room.setIsAvailable(true);
            roomRepository.save(room);
        });

        return saved;
    }
}
