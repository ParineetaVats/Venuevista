package com.venuevista.dto;

public record CategoryDistributionResponse(
        String type,
        long count,
        double percent
) {}
