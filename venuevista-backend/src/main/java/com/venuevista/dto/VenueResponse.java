package com.venuevista.dto;

import java.math.BigDecimal;

public record VenueResponse(
        Long id,
        String name,
        String city,
        String type,
        Integer capacity,
        BigDecimal price,
        String description,
        String c1,
        String c2
) {}
