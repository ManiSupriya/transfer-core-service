package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoreAccountDetailsDTO {
    private String customerName;
    private String chequeBookName;
    private String onlineBanking;
    private String salaryAccount;
    private String alternativeAccount;
    private List<SearchAccountDto> connectedAccounts;
    private String iban;
//    private List<ComplianceDto> compliances;
}
