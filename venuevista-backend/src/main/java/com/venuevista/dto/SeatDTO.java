package com.venuevista.dto;

import com.venuevista.entity.SeatType;

import java.math.BigDecimal;

public record SeatDTO(
        Long id,
        String code,
        String row,
        Integer number,
        SeatType type,
        BigDecimal price,
        String status,
        Boolean heldByMe
) {}
