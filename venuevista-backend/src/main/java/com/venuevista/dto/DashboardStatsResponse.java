package com.venuevista.dto;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long events,
        long venues,
        long users,
        long bookings,
        long ticketsSold,
        BigDecimal totalRevenue
) {}
