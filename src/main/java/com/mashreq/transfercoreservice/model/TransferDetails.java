package com.mashreq.transfercoreservice.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferDetails {
    private long transfers;
    private BigDecimal amount;

    public TransferDetails(Long transfers, BigDecimal amount) {
        this.transfers = transfers;
        this.amount = amount;
    }
}