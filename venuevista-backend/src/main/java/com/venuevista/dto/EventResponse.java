package com.venuevista.dto;

import com.venuevista.entity.EventCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventResponse(
        Long id,
        String title,
        EventCategory category,
        LocalDate date,
        String time,
        Long venueId,
        VenueResponse venue,
        BigDecimal price,
        Integer seats,
        Integer left,
        Boolean featured,
        String organizer,
        String desc
) {}
