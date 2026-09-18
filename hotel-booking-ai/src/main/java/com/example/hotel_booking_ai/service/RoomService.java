package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.dto.RoomRequest;
import com.example.hotel_booking_ai.dto.RoomWithOfferResponse;
import com.example.hotel_booking_ai.entity.Room;
import com.example.hotel_booking_ai.repository.HotelRepository;
import com.example.hotel_booking_ai.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final OfferService offerService;

    public RoomService(
            RoomRepository roomRepository,
            HotelRepository hotelRepository,
            OfferService offerService) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.offerService = offerService;
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
            BigDecimal discount = offerService.calculateDiscount(room.getBasePrice(), offerCode);
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
