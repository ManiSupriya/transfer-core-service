package com.mashreq.transfercoreservice.client.dto;

public enum RiskGroupType {
    G1("Medium Risk Countries"),
    G2("High-Risk Countries"),
    G3("Sanctioned Countries");

    private String description;
    private RiskGroupType(String value) {
        this.description = value;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeName() {
        return this.name();
    }
}
