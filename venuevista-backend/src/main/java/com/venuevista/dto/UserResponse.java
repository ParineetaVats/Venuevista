package com.venuevista.dto;

import com.venuevista.entity.Role;

import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role,
        String city,
        String interest,
        Integer guests,
        BigDecimal budget
) {}
