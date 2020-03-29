package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.transfercoreservice.annotations.ValidEnum;

public enum ChargeBearer implements ValidEnum {

    O("O"),
    B("B"),
    U("U");

    private String name;

    ChargeBearer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
