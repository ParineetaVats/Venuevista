package com.venuevista.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuoteRequest(
        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotEmpty(message = "Seat IDs cannot be empty")
        @Size(min = 1, max = 8, message = "You must select between 1 and 8 seats")
        List<Long> seatIds
) {}
