package com.venuevista.dto;

import java.math.BigDecimal;

public record AnalyticsOverviewResponse(
        BigDecimal totalRevenue,
        long ticketsSold,
        BigDecimal avgTicketPrice,
        double bookingConversionPercent,
        double seatOccupancyPercent,
        long totalBookings
) {}
