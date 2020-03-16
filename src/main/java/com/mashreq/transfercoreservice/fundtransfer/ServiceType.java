package com.mashreq.transfercoreservice.fundtransfer;

import com.mashreq.transfercoreservice.annotations.ValidEnum;

/**
 * @author shahbazkh
 * @date 3/12/20
 */
public enum ServiceType implements ValidEnum {

    OWN_ACCOUNT("own-account"),
    WITHIN_MASHREQ("within-mashreq"),
    LOCAL("local"),
    INTERNATIONAL("international"),
    CHARITY_ACCOUNT("charity-account");

    private String name;

    ServiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
