package com.example.hotel_booking_ai.service;

import com.example.hotel_booking_ai.dto.OfferRequest;
import com.example.hotel_booking_ai.entity.Offer;
import com.example.hotel_booking_ai.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public Offer createOffer(OfferRequest request) {
        Offer offer = new Offer(
                request.code().toUpperCase(),
                request.title(),
                request.description(),
                request.discountPercentage(),
                request.maxDiscountAmount(),
                request.validFrom() != null ? request.validFrom() : LocalDate.now(),
                request.validTo() != null ? request.validTo() : LocalDate.now().plusYears(1),
                request.isActive() != null ? request.isActive() : true
        );
        return offerRepository.save(offer);
    }

    public List<Offer> getActiveOffers() {
        return offerRepository.findByIsActiveTrue();
    }

    public Optional<Offer> getValidOffer(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return offerRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .filter(offer -> {
                    LocalDate now = LocalDate.now();
                    boolean afterStart = offer.getValidFrom() == null || !now.isBefore(offer.getValidFrom());
                    boolean beforeEnd = offer.getValidTo() == null || !now.isAfter(offer.getValidTo());
                    return afterStart && beforeEnd;
                });
    }

    public BigDecimal calculateDiscount(BigDecimal amount, String offerCode) {
        if (offerCode == null || offerCode.isBlank() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        Optional<Offer> offerOpt = getValidOffer(offerCode);
        if (offerOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Offer offer = offerOpt.get();
        BigDecimal percentage = BigDecimal.valueOf(offer.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discount = amount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

        if (offer.getMaxDiscountAmount() != null && discount.compareTo(offer.getMaxDiscountAmount()) > 0) {
            discount = offer.getMaxDiscountAmount();
        }

        return discount;
    }
}
