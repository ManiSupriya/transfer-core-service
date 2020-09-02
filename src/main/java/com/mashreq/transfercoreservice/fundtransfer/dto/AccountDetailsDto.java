package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.util.List;

import com.mashreq.transfercoreservice.client.dto.SearchAccountDto;

import lombok.Data;

@Data
public class AccountDetailsDto {
    private String customerName;
    private String chequeBookName;
    private String onlineBanking;
    private String salaryAccount;
    private String alternativeAccount;
    private boolean chequeBookAllowed;
    private List<SearchAccountDto> connectedAccounts;
    private String iban;
    private List<ComplianceDto> compliances;
}
