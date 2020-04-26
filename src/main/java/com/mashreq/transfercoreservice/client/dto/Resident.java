package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Resident {
    R("Resident"), N("Non resident");

    private final String description;

    Resident(String description) {
        this.description = description;
    }

    /**
     * Factory method
     */
    @JsonCreator
    public static Resident from(String src) {
        if (src == null) {
            return null;
        }
        return Resident.valueOf(src.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
