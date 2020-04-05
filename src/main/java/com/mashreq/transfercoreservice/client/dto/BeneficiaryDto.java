package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author shahbazkh
 * @date 3/16/20
 */
@Data
public class BeneficiaryDto {
    private Long id;
    private String accountNumber;
    private String nickname;
    private String category;
    private String serviceType;
    private String serviceTypeCode;
    private String status;
    private Instant createdDate;
    private Instant activeAfter;
    private String activeAfterDuration;

    // bill specific
    private String salikPinCode;
    private String creditcardHolderName;

    // Fields for beneficiary enquiry
    private String billRefNo;
    private BigDecimal dueAmount;
    private String outstandingAmount;
    private String currentBalance;
    private String balanceAmount;

    //For fund transfer
    private String beneficiaryCurrency;

    //LOCAL AND INTERNATIONAL
    private String fullName;
    private String bankName;
    private String swiftCode;
    private String routingCode;

    private String finalName;
    private String bankCountry;
    private String bankCountryISO;
    private String bankCity;
    private String bankBranchName;
    private String bankRoutingCode;
    private String bankState;

    private String beneficiaryAccountType;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

}


