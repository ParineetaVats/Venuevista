package com.venuevista.dto;

import com.venuevista.entity.SeatType;

public record AddSeatRequest(
        String rowLabel,
        Integer count,
        SeatType seatType
) {}
