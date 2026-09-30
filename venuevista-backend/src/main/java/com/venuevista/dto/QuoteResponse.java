package com.venuevista.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
        Long eventId,
        List<SeatDTO> seats,
        Integer quantity,
        BigDecimal subtotal,
        BigDecimal convenienceFee,
        BigDecimal tax,
        BigDecimal total
) {}
