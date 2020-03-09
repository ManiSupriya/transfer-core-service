package com.mashreq.transfercoreservice.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 3/8/20
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FundTransferRequestDTO {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String purposeCode;
    private String dealNumber;
    private String finTxnNo;
}
