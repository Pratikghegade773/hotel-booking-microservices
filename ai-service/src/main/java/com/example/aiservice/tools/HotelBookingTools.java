package com.example.aiservice.tools;

import com.example.aiservice.client.HotelInventoryClient;
import com.example.aiservice.client.OfferClient;
import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import com.example.common.dto.RoomValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class HotelBookingTools {

    private static final Logger log = LoggerFactory.getLogger(HotelBookingTools.class);

    private final HotelInventoryClient hotelInventoryClient;
    private final OfferClient offerClient;

    public HotelBookingTools(HotelInventoryClient hotelInventoryClient, OfferClient offerClient) {
        this.hotelInventoryClient = hotelInventoryClient;
        this.offerClient = offerClient;
    }

    @Tool(description = "Search for hotels in a specific city. Leave city empty to search all hotels.")
    public List<Map<String, Object>> searchHotelsByCity(String city) {
        log.info("AI Tool invoked: searchHotelsByCity(city={})", city);
        try {
            return hotelInventoryClient.getHotels(city);
        } catch (Exception e) {
            log.warn("Failed to query hotels: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Tool(description = "Check the availability, room type, capacity, and base price of a specific room ID.")
    public RoomValidationResponse checkRoomDetails(Long roomId) {
        log.info("AI Tool invoked: checkRoomDetails(roomId={})", roomId);
        try {
            return hotelInventoryClient.getRoomById(roomId);
        } catch (Exception e) {
            log.warn("Failed to query room {}: {}", roomId, e.getMessage());
            return null;
        }
    }

    @Tool(description = "Calculate the discounted amount and final price for a given total amount and promotional coupon code.")
    public DiscountCalculationResponse calculateDiscount(Double amount, String offerCode) {
        log.info("AI Tool invoked: calculateDiscount(amount={}, offerCode={})", amount, offerCode);
        try {
            return offerClient.calculateDiscount(new DiscountCalculationRequest(BigDecimal.valueOf(amount), offerCode));
        } catch (Exception e) {
            log.warn("Failed to calculate discount for code {}: {}", offerCode, e.getMessage());
            return new DiscountCalculationResponse(BigDecimal.valueOf(amount), BigDecimal.ZERO, BigDecimal.valueOf(amount), offerCode, false);
        }
    }

    @Tool(description = "Retrieve all currently active promotional offers, coupons, and discount percentages.")
    public List<Map<String, Object>> getActiveOffers() {
        log.info("AI Tool invoked: getActiveOffers()");
        try {
            return offerClient.getActiveOffers();
        } catch (Exception e) {
            log.warn("Failed to fetch offers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
