package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import com.mashreq.transfercoreservice.annotations.ValueOfEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 4/21/20
 */

@Data
public class FlexRuleEngineRequestDTO {

    @Account
    private String customerAccountNo;

    @NotEmpty
    private String transactionCurrency;

    @TransactionAmount
    private BigDecimal transactionAmount;

    @ValueOfEnum(isRequired = true, enumClass = FlexRuleEngineCountryType.class, message = "Noat a valid country choice")
    private FlexRuleEngineCountryType countryType;
}
