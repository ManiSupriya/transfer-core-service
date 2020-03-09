package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
public class SearchAccountDto {
    private String customerName;
    private String customerCif;
    private String accountName;
    private SearchAccountTypeDto accountType;
    private String currency;
    private String number;
    private String creationDate;
    private String currentBalance;
    private String availableBalance;
    private String status;
    private String branch;
    private String branchName;
    private String operatingInstruction;
    private boolean jointAccount;
    private boolean dormant;
    private boolean frozen;
    private boolean closed;
    private boolean noDebitForCompliance;
    private boolean noDebit;
    private boolean noCredit;
}