package com.venuevista.dto;

import com.venuevista.entity.SeatType;

public record UpdateSeatRequest(
        SeatType seatType,
        Boolean active
) {}
