package com.mashreq.transfercoreservice.client.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
@NoArgsConstructor
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
    private String unclearedBalance;
    private String chequeBook;
    private String status;
    private String branch;
    private String operatingInstruction;
    private boolean jointAccount;
    private boolean dormant;
    private boolean frozen;
    private boolean closed;
    private boolean noDebitForCompliance;
    private boolean noDebit;
    private boolean noCredit;
    private String totalOverdraft;
    private String holdAmount;
    private String freezeAmount;
    private String overDraft;
    private String overdraftStartDate;
    private String overdraftExpiryDate;
    private String tempOverDraft;
    private String accountClosedDate;
}