package com.mashreq.transfercoreservice.transactionqueue;

import com.mashreq.transfercoreservice.fundtransfer.dto.ChargeBearer;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@Table(name = "transaction_history")
@AllArgsConstructor
@NoArgsConstructor
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
    private LocalDateTime valueDate;
    private String countryCode;
    private String transferPurpose;
    private String rejectedReason;
    private String transactionRefNo;
    private String transactionCategory;
    private Instant createdDate;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "service_fee_paid_by")
    private ChargeBearer chargeBearer;
    @Column(name = "total_debit_amount")
    private String debitAmount;
    @Column(name = "exchange_rate")
    private String exchangeRate;
    @Column(name = "payment_note")
    private String paymentNote;
}
