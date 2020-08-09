package com.mashreq.transfercoreservice.middleware.enums;

import java.util.Arrays;
import java.util.Optional;

public enum YesNo {
    Y((byte) 1, true), N((byte) 0, false);

    private final boolean booleanVal;
    private final byte numberVal;

    YesNo(byte numberVal, boolean booleanVal) {
        this.numberVal = numberVal;
        this.booleanVal = booleanVal;
    }

    /**
     * factory method
     */
    public static YesNo from(String src) {
        return Arrays.asList(YesNo.values())
                .stream()
                .filter(item -> item.name().equalsIgnoreCase(src) || ("" + item.numVal()).equals(src))
                .findAny()
                .orElse(null);
    }

    /**
     * factory method with default
     */
    public static YesNo from(String src, YesNo defValue) {
        return Optional.ofNullable(from(src)).orElse(defValue);
    }

    /**
     * boolean val
     */
    public boolean val() {
        return booleanVal;
    }

    /**
     * numeric val
     */
    public byte numVal() {
        return numberVal;
    }
}
