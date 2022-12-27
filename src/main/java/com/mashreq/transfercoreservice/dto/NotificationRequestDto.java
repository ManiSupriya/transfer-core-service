package com.mashreq.transfercoreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by KrishnaKo on 24/11/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NotificationRequestDto {
    private String channel;
    private String channelTraceId;
    private String fromAccount;
    private String toAccount;
    private String productId;
    private BigDecimal amount;
    private String purposeCode;
    private String purposeDesc;
    private String chargeBearer;
    private String dealNumber;
    private BigDecimal dealRate;
    private String finTxnNo;
    private String sourceCurrency;
    private String destinationCurrency;
    private String sourceBranchCode;
    private String beneficiaryFullName;
    private String awInstBICCode;
    private String awInstName;
    private String beneficiaryAddressOne;
    private String beneficiaryAddressTwo;
    private String beneficiaryAddressThree;
    private String transactionCode;
    private String internalAccFlag;
    private BigDecimal srcAmount;
    private BigDecimal exchangeRate;
    private String limitTransactionRefNo;
    private String transactionReferenceNo;
    private String responseStatus;
    /**added for note to beneficiary field*/
    private String paymentNote;
    // below fields are applicable for credit card
    private String cardNo;
    private String expiryDate;
    private String sourceISOCurrency;
    private String destinationISOCurrency;
    private String acwthInst1;
    private String acwthInst2;
    private String acwthInst5;
    private String transferType;
    private String sourceOfFund;
    private String status;
    private String txnCurrency;
    private String NotificationType;
    private String finalBene;
    private String serviceType;
    private String intermediaryBankSwiftCode;
    private String beneficiaryBankCountry;
    private String bankFees;
    private String accountClass;
    private BigDecimal srcCcyAmt;
    private String exchangeRateDisplayTxt;
    private String postingGroup;
    private String limitVersionUuid;
    private BigDecimal limitUsageAmount;

}
