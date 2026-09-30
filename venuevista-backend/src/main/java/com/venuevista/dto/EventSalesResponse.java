package com.venuevista.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventSalesResponse(
        Long eventId,
        String title,
        Long venueId,
        String venueName,
        LocalDate date,
        Integer seats,
        Integer sold,
        Integer left,
        BigDecimal revenue,
        String status
) {}
