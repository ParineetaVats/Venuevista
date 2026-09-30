package com.venuevista.dto;

import com.venuevista.entity.SeatType;

public record AdminSeatDTO(
        Long id,
        String code,
        String row,
        Integer number,
        SeatType type,
        boolean active,
        String status
) {}
