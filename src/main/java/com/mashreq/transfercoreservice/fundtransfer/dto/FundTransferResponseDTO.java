package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FundTransferResponseDTO {

    private String status;
    private BigDecimal paidAmount;
    private String accountTo;
    private String mwReferenceNo;
    private String mwResponseDescription;
    private String mwResponseCode;
    private String transactionRefNo;
    private boolean promoApplied;
    private String promoCode;
}
