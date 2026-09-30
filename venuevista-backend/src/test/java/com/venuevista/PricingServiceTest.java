package com.venuevista;

import com.venuevista.dto.QuoteResponse;
import com.venuevista.entity.Seat;
import com.venuevista.entity.SeatType;
import com.venuevista.entity.Venue;
import com.venuevista.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        ReflectionTestUtils.setField(pricingService, "convenienceFeePerTicket", new BigDecimal("40"));
        ReflectionTestUtils.setField(pricingService, "taxRate", new BigDecimal("0.05"));
    }

    @Test
    void testSeatPriceRounding() {
        // Base 1000, VIP -> 2000
        BigDecimal vipPrice = pricingService.calculateSeatPrice(new BigDecimal("1000"), SeatType.VIP);
        assertThat(vipPrice).isEqualTo(new BigDecimal("2000"));

        // Base 1499, Premium (1.5x) -> 2248.5 -> 2249 (HALF_UP)
        BigDecimal premPrice = pricingService.calculateSeatPrice(new BigDecimal("1499"), SeatType.PREMIUM);
        assertThat(premPrice).isEqualTo(new BigDecimal("2249"));
    }

    @Test
    void testQuoteCalculation() {
        // Base 1000, 2 VIP seats -> subtotal 4000, fee 80, tax 200 (4000*0.05), total 4280
        Venue venue = new Venue();
        Seat s1 = new Seat(venue, "A", 1, SeatType.VIP);
        Seat s2 = new Seat(venue, "A", 2, SeatType.VIP);

        QuoteResponse quote = pricingService.calculateQuote(100L, new BigDecimal("1000"), List.of(s1, s2));

        assertThat(quote.quantity()).isEqualTo(2);
        assertThat(quote.subtotal()).isEqualTo(new BigDecimal("4000"));
        assertThat(quote.convenienceFee()).isEqualTo(new BigDecimal("80"));
        assertThat(quote.tax()).isEqualTo(new BigDecimal("200"));
        assertThat(quote.total()).isEqualTo(new BigDecimal("4280"));
    }
}
