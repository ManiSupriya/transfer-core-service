package com.mashreq.transfercoreservice.transactionqueue;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "transaction_history")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountFrom;
    private String accountTo;
    private Long beneficiaryId;
    private String transactionTypeCode;
    private String billRefNo;
    private String channel;
    private String cif;
    private BigDecimal dueAmount;
    private String encryptedCardFrom;
    private String cardFromFourDigit;
    private String fromCurrency;
    private String ipAddress;
    private String mwResponseDescription;
    private String mwResponseCode;
    private BigDecimal paidAmount;
    private String status;
    private String toCurrency;
    private Long userId;
    private String financialTransactionNo;
    private String hostReferenceNo;
    private String dealNumber;
    private String valueDate;
    private String countryCode;
    private String transferPurpose;
    private String rejectedReason;
    private String transactionRefNo;
    private String transactionCategory;
}
