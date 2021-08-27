package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.ConditionalRequired;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import com.mashreq.transfercoreservice.paylater.enums.FTOrderType;
import com.mashreq.transfercoreservice.paylater.enums.SIFrequencyType;

import lombok.Data;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Data
@ConditionalRequired(fieldName = "beneficiaryId", dependentFieldName = "serviceType", noneMatch = "WYMA", message = "Beneficiary ID is mandatory")
@ConditionalRequired(fieldName = "purposeCode", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "Purpose code cannot be empty")
@ConditionalRequired(fieldName = "purposeDesc", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "Purpose Description code cannot be empty")
@ConditionalRequired(fieldName = "chargeBearer", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "charge bearer cannot be empty")
@ConditionalRequired(fieldName = "productCode", dependentFieldName = "serviceType", anyMatch = "QRT", message = "Product Code is mandatory")
@ConditionalRequired(fieldName = "startDate", dependentFieldName = "orderType", anyMatch = {"PL","SI"}, message = "start date is mandatory")
@ConditionalRequired(fieldName = "endDate", dependentFieldName = "orderType", anyMatch = "SI", message = "end date is mandatory")
@ConditionalRequired(fieldName = "frequency", dependentFieldName = "orderType", anyMatch = "SI", message = "frequency is mandatory")
public class FundTransferRequestDTO {

    @Account
    private String fromAccount;

    @NotBlank(message = "Account Number cannot be empty")
    private String toAccount;

    @TransactionAmount
    private BigDecimal amount;

    @TransactionAmount
    private BigDecimal srcAmount;

    @ValueOfEnum(enumClass = ServiceType.class, message = "Not a valid value for service Type")
    private String serviceType;

    private String currency;

    private String purposeCode;

    private String purposeDesc;

    @ValueOfEnum(enumClass = ChargeBearer.class, message = "Not a valid charge bearer", isRequired = false)
    private String chargeBearer;

    @Pattern(regexp = "^$|[a-zA-Z0-9-]+",message="Not a valid Deal number")
    private String dealNumber;

    @NotBlank(message = "Financial Transaction Number cannot be empty")
    @Pattern(regexp = "^[a-zA-Z0-9-]+",message="Not a valid transaction number")
    private String finTxnNo;

    private String beneficiaryId;

    private String productCode;
    private String otp;
    private String challengeToken;
    private int dpPublicKeyIndex;
    private String dpRandomNumber;
    private String txnCurrency;
    private String additionalField;
    private String finalBene;
    AdditionalFields beneRequiredFields;
    private BigDecimal dealRate;
    private String cardNo;
    /** added as a part of SI and pay later implementation */
    @ValueOfEnum(enumClass = FTOrderType.class, message = "Not a valid value for order type", isRequired = false)
    private String orderType= "PN";
    @ValueOfEnum(enumClass = SIFrequencyType.class, message = "Invalid frequency type", isRequired = false)
    private String frequency;
    @Pattern(regexp = "|^(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$", message = "yyyy-MM-dd only allowed")
    private String startDate;
    @Pattern(regexp = "^(19|20)\\d\\d[- /.](0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])$", message = "yyyy-MM-dd only allowed")
    private String endDate;
    
    private String paymentNote;
    
    private String promoCode;
    
}
