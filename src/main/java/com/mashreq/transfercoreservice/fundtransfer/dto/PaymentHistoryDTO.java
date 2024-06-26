package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentHistoryDTO {

    private String cif;

    private Long userId;
    private Long beneficiaryId;

    private String beneficiaryTypeCode;

    private String accountTo;
    private String toCurrency;
    private BigDecimal paidAmount;

    private String accountFrom;
    private String encryptedCardFrom;
    private String encryptedCardFromFourdigit;
    private String fromCurrency;

    private String billRefNo;
    private BigDecimal dueAmount;

    private String notes;
    private String mwReferenceNo;
    private String mwResponseDescription;
    private String mwResponseCode;
    private String channel;

    private String status;

    private String ipAddress;
    private String financialTransactionNo;
    private String transactionRefNo;
    private String hostRefNo;


}
