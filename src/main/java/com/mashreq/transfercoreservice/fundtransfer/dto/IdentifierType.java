package com.mashreq.transfercoreservice.fundtransfer.dto;


public enum IdentifierType {

    ACCOUNT("account"),
    IBAN("iban");

    private String name;

    IdentifierType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
