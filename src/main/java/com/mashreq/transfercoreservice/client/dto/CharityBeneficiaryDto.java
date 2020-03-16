package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CharityBeneficiaryDto {
    private Long id;
    private String organization;
    private String name;
    private String code;
    private String cause;
    private String accountNumber;
    private String logo;
    private BigDecimal charityTarget;
    private String currencyCode;
    private String branchCode;
    private String countryIsoCode;
    private String status;
}
