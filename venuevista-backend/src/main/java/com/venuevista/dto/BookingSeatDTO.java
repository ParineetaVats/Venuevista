package com.venuevista.dto;

import com.venuevista.entity.SeatType;

import java.math.BigDecimal;

public record BookingSeatDTO(
        Long seatId,
        String code,
        String row,
        Integer number,
        SeatType type,
        BigDecimal price
) {}
