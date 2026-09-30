package com.venuevista.dto;

import java.math.BigDecimal;

public record SalesSummaryResponse(
        long totalTicketsSold,
        long todayTickets,
        long weekTickets,
        long monthTickets,
        BigDecimal totalRevenue,
        BigDecimal todayRevenue,
        BigDecimal weekRevenue,
        BigDecimal monthRevenue,
        BigDecimal grossCollected
) {}
