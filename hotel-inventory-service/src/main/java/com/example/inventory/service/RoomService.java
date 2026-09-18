package com.example.inventory.service;

import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import com.example.inventory.client.OfferClient;
import com.example.inventory.dto.RoomRequest;
import com.example.inventory.dto.RoomWithOfferResponse;
import com.example.inventory.entity.Room;
import com.example.inventory.repository.HotelRepository;
import com.example.inventory.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final OfferClient offerClient;

    public RoomService(
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            OfferClient offerClient) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.offerClient = offerClient;
    }

    public Room createRoom(RoomRequest request) {
        if (!hotelRepository.existsById(request.hotelId())) {
            throw new IllegalArgumentException("Hotel not found with id: " + request.hotelId());
        }

        Room room = new Room(
                request.hotelId(),
                request.roomNumber(),
                request.type(),
                request.basePrice(),
                request.capacity() != null ? request.capacity() : 2,
                request.isAvailable() != null ? request.isAvailable() : true
        );
        return roomRepository.save(room);
    }

    public Room updateRoom(Long roomId, RoomRequest request) {
        Room room = getRoomById(roomId);

        room.setRoomNumber(request.roomNumber());
        room.setType(request.type());
        room.setBasePrice(request.basePrice());
        if (request.capacity() != null) {
            room.setCapacity(request.capacity());
        }
        if (request.isAvailable() != null) {
            room.setIsAvailable(request.isAvailable());
        }

        return roomRepository.save(room);
    }

    public Room updateAvailability(Long roomId, Boolean isAvailable) {
        Room room = getRoomById(roomId);
        room.setIsAvailable(isAvailable);
        return roomRepository.save(room);
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    public List<RoomWithOfferResponse> getAvailableRoomsWithOffers(Long hotelId, String offerCode) {
        List<Room> rooms = hotelId != null
                ? roomRepository.findByHotelIdAndIsAvailableTrue(hotelId)
                : roomRepository.findAll().stream().filter(Room::getIsAvailable).toList();

        return rooms.stream().map(room -> {
            BigDecimal discount = BigDecimal.ZERO;
            if (offerCode != null && !offerCode.isBlank()) {
                try {
                    DiscountCalculationResponse calc = offerClient.calculateDiscount(
                            new DiscountCalculationRequest(room.getBasePrice(), offerCode)
                    );
                    discount = calc.discountAmount();
                } catch (Exception e) {
                    log.warn("Could not calculate discount from offer-service: {}", e.getMessage());
                }
            }
            BigDecimal offerPrice = room.getBasePrice().subtract(discount).max(BigDecimal.ZERO);

            return new RoomWithOfferResponse(
                    room.getId(),
                    room.getHotelId(),
                    room.getRoomNumber(),
                    room.getType(),
                    room.getBasePrice(),
                    discount,
                    offerPrice,
                    room.getCapacity(),
                    room.getIsAvailable()
            );
        }).toList();
    }
}
