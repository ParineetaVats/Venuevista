package com.venuevista.dto;

import java.util.List;
import java.util.Map;

public record AdminSeatingResponse(
        Long venueId,
        Long eventId,
        Map<String, Object> stats,
        List<AdminSeatDTO> seats,
        List<String> skipped
) {
    public AdminSeatingResponse(Long venueId, Long eventId, Map<String, Object> stats, List<AdminSeatDTO> seats) {
        this(venueId, eventId, stats, seats, null);
    }
}
