package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.ConditionalRequired;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ConditionalRequired(fieldName = "beneficiaryId", dependentFieldName = "serviceType", noneMatch = "WYMA", message = "Beneficiary ID is mandatory")
@ConditionalRequired(fieldName = "purposeCode", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "Purpose code cannot be empty")
@ConditionalRequired(fieldName = "purposeDesc", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "Purpose Description code cannot be empty")
@ConditionalRequired(fieldName = "chargeBearer", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "charge bearer cannot be empty")
public class FundTransferEligibiltyRequestDTO {

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

    private String beneficiaryId;

    private String txnCurrency;
    private String additionalField;
    private String finalBene;
    AdditionalFields beneRequiredFields;
    private BigDecimal dealRate;
    private String cardNo;

}
