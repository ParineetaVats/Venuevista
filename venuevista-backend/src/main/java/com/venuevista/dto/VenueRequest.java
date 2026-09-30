package com.venuevista.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VenueRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Venue type is required")
        String type,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be non-negative")
        BigDecimal price,

        String description,
        String c1,
        String c2
) {}
