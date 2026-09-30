package com.venuevista.dto;

import com.venuevista.entity.PaymentMethod;
import jakarta.validation.constraints.*;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "Event ID is required")
        Long eventId,

        @NotEmpty(message = "Seat IDs are required")
        @Size(min = 1, max = 8, message = "You can book between 1 and 8 seats")
        List<Long> seatIds,

        @NotBlank(message = "Customer name is required")
        @Size(min = 2, message = "Customer name must be at least 2 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Invalid customer email format")
        String customerEmail,

        @NotBlank(message = "Customer phone is required")
        @Pattern(regexp = "^\\d{10}$", message = "Customer phone must be a 10-digit number")
        String customerPhone,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        PaymentDetailsRequest paymentDetails
) {}
