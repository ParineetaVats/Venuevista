package com.venuevista.service;

import com.venuevista.dto.QuoteResponse;
import com.venuevista.dto.SeatDTO;
import com.venuevista.entity.Seat;
import com.venuevista.entity.SeatType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    @Value("${venuevista.pricing.convenience-fee-per-ticket:40}")
    private BigDecimal convenienceFeePerTicket;

    @Value("${venuevista.pricing.tax-rate:0.05}")
    private BigDecimal taxRate;

    public BigDecimal calculateSeatPrice(BigDecimal basePrice, SeatType seatType) {
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal multiplier = switch (seatType) {
            case VIP -> new BigDecimal("2.0");
            case PREMIUM -> new BigDecimal("1.5");
            case STANDARD -> new BigDecimal("1.0");
        };
        return basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    public QuoteResponse calculateQuote(Long eventId, BigDecimal basePrice, List<Seat> seats) {
        int qty = seats.size();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<SeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat : seats) {
            BigDecimal seatPrice = calculateSeatPrice(basePrice, seat.getSeatType());
            subtotal = subtotal.add(seatPrice);
            seatDTOs.add(new SeatDTO(
                    seat.getId(),
                    seat.getSeatCode(),
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    seat.getSeatType(),
                    seatPrice,
                    "AVAILABLE",
                    false
            ));
        }

        BigDecimal fee = convenienceFeePerTicket.multiply(BigDecimal.valueOf(qty)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(taxRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(fee).add(tax);

        return new QuoteResponse(eventId, seatDTOs, qty, subtotal, fee, tax, total);
    }
}
