package com.venuevista.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SeatType {
    VIP("VIP"),
    PREMIUM("Premium"),
    STANDARD("Standard");

    private final String displayName;

    SeatType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SeatType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return STANDARD;
        }
        String clean = value.trim();
        for (SeatType type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.displayName.equalsIgnoreCase(clean)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SeatType: " + value);
    }
}
