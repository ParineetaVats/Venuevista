package com.venuevista.dto;

import java.math.BigDecimal;

public record NamedRevenueResponse(
        Long id,
        String name,
        BigDecimal revenue
) {}
