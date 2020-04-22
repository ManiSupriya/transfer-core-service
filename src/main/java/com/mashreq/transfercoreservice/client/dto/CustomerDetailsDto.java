package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashreq.esbcore.bindings.customer.mbcdm.RelCustType;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDetailsDto {
    private String name;
    private String birthday;
    private Resident residentialStatus;
    private String companyName;
    private String designation;
    private Gender gender;
    private boolean blacklisted;
    private boolean dormant;
    private boolean frozen;
    private String chargeGroup;
    private String kycRefNumber;
    private String branchCode;
    private String branchName;
    private String nationalNumber;
    private String nationalNumberExpiryDate;
    private String passportNumber;
    private String passportExpiryDate;
    private String visaNumber;
    private String visaExpiryDate;
    private String mobile;
    private String encryptedMobile;
    private String primaryEmail;
    private String secondaryEmail;
    private String primaryPhoneNumber;
    private List<AddressTypeDto> address;
    private String salary;
    private String salaryCurrency;
    private String kycExpiryDate;
    private String kycLastUpdateDate;
    private String relationshipManager;
    private String relationshipManagerMobileNumber;
    private String relationshipType;
    private boolean jointFlag;
    private String poBox;
    private String prefact;
    private String organization;
    private String department;
    private String passportIssueDate;
    private String passportIssueCountry;
    private String otherIncome;
    private String visaIssueDate;
    private String visaIssuePlace;
    private String dateOfEmployment;
    private String employerDesc;
    private String passportIssuePlace;
    private String nationalityCode;
    private String preferredMobileNumber;
    private List<RelCustType> relatedCustomer;
    private String residenceCountry;
}
