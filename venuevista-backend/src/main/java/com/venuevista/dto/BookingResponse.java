package com.venuevista.dto;

import com.venuevista.entity.BookingStatus;
import com.venuevista.entity.EventCategory;
import com.venuevista.entity.PaymentMethod;
import com.venuevista.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record BookingResponse(
        String bookingReference,
        BookingStatus status,
        String displayStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String transactionId,
        Long eventId,
        String eventTitle,
        EventCategory category,
        LocalDate date,
        String time,
        Long venueId,
        String venueName,
        String city,
        List<BookingSeatDTO> seats,
        Integer quantity,
        BigDecimal subtotal,
        BigDecimal convenienceFee,
        BigDecimal tax,
        BigDecimal total,
        String customerName,
        String customerEmail,
        String customerPhone,
        Instant createdAt,
        Instant updatedAt
) {}
