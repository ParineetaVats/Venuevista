package com.venuevista.dto;

import java.math.BigDecimal;
import java.util.List;

public record EventSeatsResponse(
        Long eventId,
        Long venueId,
        BigDecimal basePrice,
        List<SeatDTO> seats
) {}
