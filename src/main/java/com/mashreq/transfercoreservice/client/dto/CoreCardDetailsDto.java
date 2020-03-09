package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */
@Data
public class CoreCardDetailsDto {
    private String cardIssuer;
    private String cifId;
    private String customerName;
    private String customerCreationDate;
    private String vipFlag;
    private String sourceBranch;
    private String cardNo;
    private String cardType;
    private String cardStatus;
    private String cardHolderName;
    private String cardAccountNumber;
    private BigDecimal paymentsMethod;
    private String accountType;
    private String accountTypeDescription;
    private BigDecimal currentBalance;
    private BigDecimal unbilledAmount;
    private String lastBillingDate;
    private String paymentDueDate;
    private BigDecimal lastPaymentAmount;
    private BigDecimal totalCreditLimit;
    private BigDecimal availableCreditLimit;
    private BigDecimal availableCashLimit;
    private BigDecimal cashLimit;
    private BigDecimal totalPaymentDue;
    private BigDecimal minimumPaymentDue;
    private BigDecimal presentMonthsMinDue;
    private BigDecimal pastDueAmount;
    private BigDecimal totalDue;
    private BigDecimal lastStatementBalance;
    private BigDecimal pointsBalance;
    private BigDecimal odAmt30Days;
    private BigDecimal odAmt60Days;
    private BigDecimal odAmt90Days;
    private BigDecimal odAmt120Days;
    private BigDecimal odAmt150Days;
    private BigDecimal odAmt180Days;
    private BigDecimal odAmt210Days;
    private String cardCurrency;
    private String primaryCard;
    private String issuanceDate;
    private String expiryDate;
    private String previousExpiryDate;
    private String recoveryAccount;
    private String encryptedCardNumber;
    private String plasticTypeDescription;
}
