package com.venuevista.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VenueReservationRequest(
        @NotNull(message = "Reservation date is required")
        LocalDate reservationDate
) {}
