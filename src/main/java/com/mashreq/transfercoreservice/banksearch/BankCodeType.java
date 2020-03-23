package com.mashreq.transfercoreservice.banksearch;

import com.mashreq.transfercoreservice.annotations.ValidEnum;

/**
 * @author shahbazkh
 * @date 3/23/20
 */
public enum BankCodeType implements ValidEnum {
    IBAN("iban"),
    SWIFT("swift"),
    ROUTE_CODE("routing-code");

    BankCodeType(final String name) {
        this.name = name;
    }

    private String name;

    @Override
    public String getName() {
        return this.name;
    }
}
