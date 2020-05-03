package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    M("Male"), F("Female");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    /**
     * Factory method
     */
    public static Gender from(String src) {
        if (src == null) {
            return null;
        }
        return Gender.valueOf(src.toUpperCase());
    }

    @JsonValue
    public String description() {
        return description;
    }

}
