package com.example.offerservice.service;

import com.example.common.dto.DiscountCalculationRequest;
import com.example.common.dto.DiscountCalculationResponse;
import com.example.offerservice.dto.OfferRequest;
import com.example.offerservice.entity.Offer;
import com.example.offerservice.repository.OfferRepository;
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
        if (offerRepository.existsByCodeIgnoreCase(request.code())) {
            throw new IllegalArgumentException("Offer with code already exists: " + request.code());
        }

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

    public DiscountCalculationResponse calculateDiscount(DiscountCalculationRequest request) {
        BigDecimal amount = request.amount();
        String offerCode = request.offerCode();

        if (offerCode == null || offerCode.isBlank() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new DiscountCalculationResponse(amount, BigDecimal.ZERO, amount, offerCode, false);
        }

        Optional<Offer> offerOpt = getValidOffer(offerCode);
        if (offerOpt.isEmpty()) {
            return new DiscountCalculationResponse(amount, BigDecimal.ZERO, amount, offerCode, false);
        }

        Offer offer = offerOpt.get();
        BigDecimal percentage = BigDecimal.valueOf(offer.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discount = amount.multiply(percentage).setScale(2, RoundingMode.HALF_UP);

        if (offer.getMaxDiscountAmount() != null && discount.compareTo(offer.getMaxDiscountAmount()) > 0) {
            discount = offer.getMaxDiscountAmount();
        }

        BigDecimal finalAmount = amount.subtract(discount).max(BigDecimal.ZERO);
        return new DiscountCalculationResponse(amount, discount, finalAmount, offerCode, true);
    }
}
