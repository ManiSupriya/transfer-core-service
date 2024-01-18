package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.ConditionalRequired;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ConditionalRequired(fieldName = "beneficiaryId", dependentFieldName = "serviceType", noneMatch = "WYMA", message = "Beneficiary ID is mandatory")
@ConditionalRequired(fieldName = "chargeBearer", dependentFieldName = "serviceType", anyMatch = {"INFT", "LOCAL"}, message = "charge bearer cannot be empty")
public class FundTransferEligibiltyRequestDTO {

    @Account
    private String fromAccount;

    @NotBlank(message = "Account Number cannot be empty")
    private String toAccount;

    @TransactionAmount
    @NotNull(message = "amount cannot be empty")
    private BigDecimal amount;

    @ValueOfEnum(enumClass = ServiceType.class, message = "Not a valid value for service Type")
    private String serviceType;

    private String currency;

    @ValueOfEnum(enumClass = ChargeBearer.class, message = "Not a valid charge bearer", isRequired = false)
    private String chargeBearer;

    @Pattern(regexp = "^$|[a-zA-Z0-9-]+",message="Not a valid Deal number")
    private String dealNumber;

    private String beneficiaryId;
    @NotBlank(message = "txnCurrency cannot be empty")
    private String txnCurrency;
    private String additionalField;
    private String finalBene;
    private AdditionalFields beneRequiredFields;
    private BigDecimal dealRate;
    private String cardNo;
    private String destinationAccountCurrency;
}
