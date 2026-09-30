package com.venuevista.dto;

import com.venuevista.entity.EventCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Category is required")
        EventCategory category,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Time is required")
        String time,

        @NotNull(message = "Venue ID is required")
        Long venueId,

        @NotNull(message = "Base price is required")
        BigDecimal price,

        Boolean featured,

        @NotBlank(message = "Organizer is required")
        String organizer,

        String description
) {}
