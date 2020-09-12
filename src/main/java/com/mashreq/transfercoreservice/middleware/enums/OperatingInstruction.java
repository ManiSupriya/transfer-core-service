package com.mashreq.transfercoreservice.middleware.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperatingInstruction {

    S("Single"), J("Jointly"), E("Either-Anyone or Survivor"),
    F("Former or Survivor"), M("Mandate Holder");

    private final String title;

    OperatingInstruction(String s) {
        this.title = s;
    }

    /**
     * Factory method
     */
    @JsonCreator
    public static OperatingInstruction from(String instructionCode) {
        return Arrays.asList(OperatingInstruction.values())
                .stream()
                .filter(item -> item.name().equalsIgnoreCase(instructionCode))
                .findAny()
                .orElse(null);
    }

    /**
     * Value
     */
    @JsonValue
    public String value() {
        return name();
    }

    @Override
    public String toString() {
        return "OperatingInstruction{"
                + "name='" + name() + '\''
                + "title='" + title + '\''
                + '}';
    }
}

