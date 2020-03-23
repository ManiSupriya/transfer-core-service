package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.ConditionalRequired;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import com.mashreq.transfercoreservice.fundtransfer.ServiceType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Data
@ConditionalRequired(fieldName = "beneficiaryId", dependentFieldName = "serviceType", noneMatch = "own-account", message = "Beneficiary ID is mandatory")
@ConditionalRequired(fieldName = "purposeCode", dependentFieldName = "serviceType", anyMatch = "international", message = "Purpost code cannot be empty")
public class FundTransferRequestDTO {

    @Account
    private String fromAccount;

    @Account
    private String toAccount;

    @TransactionAmount
    private BigDecimal amount;

    @ValueOfEnum(enumClass = ServiceType.class, message = "Not a valid value for service Type")
    private String serviceType;

    @NotBlank(message = "Currency Cannot be empty")
    @Size(max = 3, min = 3, message = "Size should be 3")
    private String currency;

    private String purposeCode;

    private String dealNumber;

    @NotBlank(message = "Financial Transaction Number cannot be empty")
    private String finTxnNo;

    private String beneficiaryId;

    private String beneficaryCurrency;
}
