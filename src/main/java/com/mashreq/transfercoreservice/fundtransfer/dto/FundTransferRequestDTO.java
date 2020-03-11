package com.mashreq.transfercoreservice.fundtransfer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashreq.transfercoreservice.annotations.Account;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Data
public class FundTransferRequestDTO {

    @Account
    private String fromAccount;

    @Account
    private String toAccount;

    @TransactionAmount
    private BigDecimal amount;

    @NotBlank
    private String serviceType;

    @NotBlank(message = "Currency Cannot be empty")
    @Size(max = 3, min = 3, message = "Size should be 3")
    private String currency;

    @NotBlank(message = "Purpose code cannot be empty")
    private String purposeCode;

    private String dealNumber;

    @NotBlank(message = "Financial Transaction Number cannot be empty")
    private String finTxnNo;

    @NotBlank(message = "Beneficiary Id cannot be empty")
    private String beneficiaryId;
}
