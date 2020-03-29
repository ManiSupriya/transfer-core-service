package com.mashreq.transfercoreservice.fundtransfer.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 */
@Data
@Builder
public class FundTransferResponseDTO {

    private String status;
    private BigDecimal paidAmount;
    private String accountTo;
    private String mwReferenceNo;
    private String mwResponseDescription;
    private String mwResponseCode;
    private String financialTransactionNo;
}
