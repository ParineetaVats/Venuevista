package com.venuevista.dto;

import com.venuevista.entity.SeatType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TicketResponse(
        String ticketId,
        String bookingReference,
        String seatCode,
        SeatType seatType,
        BigDecimal price,
        Long eventId,
        String eventTitle,
        LocalDate date,
        String time,
        String venueName,
        String city,
        String status
) {}
