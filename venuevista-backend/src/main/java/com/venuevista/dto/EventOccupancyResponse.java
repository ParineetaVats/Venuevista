package com.venuevista.dto;

public record EventOccupancyResponse(
        Long eventId,
        String name,
        Integer seats,
        Integer sold,
        double occupancyPercent
) {}
