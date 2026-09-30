package com.venuevista.dto;

public record TimeSeriesResponse(
        String period,
        String label,
        Object value
) {}
