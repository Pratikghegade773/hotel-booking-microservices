package com.example.bookingservice.repository;

import com.example.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.roomId = :roomId
          AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING_PAYMENT')
          AND b.checkInDate < :checkOutDate
          AND b.checkOutDate > :checkInDate
    """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    List<Booking> findByHotelIdInOrderByCreatedAtDesc(List<Long> hotelIds);
}
