package com.venuevista.dto;

import java.time.Instant;
import java.util.List;

public record HoldResponse(
        String holdGroup,
        Instant expiresAt,
        List<SeatDTO> seats,
        QuoteResponse pricing
) {}
