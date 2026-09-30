package com.venuevista.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventCategory {
    CONCERTS("Concerts"),
    SPORTS("Sports"),
    CONFERENCES("Conferences"),
    WORKSHOPS("Workshops"),
    COMEDY("Comedy"),
    THEATRE("Theatre"),
    FESTIVALS("Festivals"),
    CORPORATE_EVENTS("Corporate Events");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static EventCategory fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim();
        for (EventCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(clean) || cat.displayName.equalsIgnoreCase(clean)) {
                return cat;
            }
        }
        // Handle "corporate-events" or similar path variables if needed
        String normalized = clean.replace("-", " ").replace("_", " ");
        for (EventCategory cat : values()) {
            if (cat.displayName.equalsIgnoreCase(normalized) || cat.name().equalsIgnoreCase(normalized)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown EventCategory: " + value);
    }
}
