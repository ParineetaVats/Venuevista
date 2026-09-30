package com.venuevista.dto;

public record PaymentDetailsRequest(
        String bank,
        String upiId,
        String cardLast4
) {}
