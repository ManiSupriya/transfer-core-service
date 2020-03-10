package com.mashreq.transfercoreservice.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashreq.transfercoreservice.annotations.Account;
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

    @NotBlank
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String serviceType;

    @NotBlank
    private String currency;

    @NotBlank
    private String purposeCode;

    private String dealNumber;

    @NotBlank
    private String finTxnNo;

    @NotBlank
    private Long beneficiaryId;
}
