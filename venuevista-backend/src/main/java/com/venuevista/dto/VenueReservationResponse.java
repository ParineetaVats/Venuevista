package com.venuevista.dto;

import com.venuevista.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record VenueReservationResponse(
        Long id,
        Long venueId,
        String venueName,
        LocalDate reservationDate,
        BigDecimal amount,
        BookingStatus status,
        Instant createdAt
) {}
