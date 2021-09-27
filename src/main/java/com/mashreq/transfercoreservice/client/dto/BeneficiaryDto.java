package com.mashreq.transfercoreservice.client.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shahbazkh
 * @date 3/16/20
 */
@Data
@NoArgsConstructor
public class BeneficiaryDto {

    private Long id;
    private String accountNumber;
    private String nickname;
    private String category;
    private String serviceType;
    private String serviceTypeCode;
    private String status;
    private String createdDate;
    private String activeAfter;
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

    //LOCAL AND INTERNATIONAL
    private String fullName;
    private String finalName;
    private String swiftCode;
    private String routingCode;

    private String beneficiaryAccountType;

    private String bankName;
    private String bankCountry;
    private String bankState;
    private String bankCity;
    private String bankBranchName;
    private String bankCountryISO;

    // Beneficiary address for international beneficiary
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    //QRI
    private String beneficiaryMobile;

    //QRPK
    private String bankCode;
    private String documentNumber;
    private String documentType;


    //Insta rem

    private String beneficiaryCity;
    private String beneficiaryState;
    private String beneficiaryPostalCode;
    private String beneficiaryRelationship;

    private boolean newVersion;

}


