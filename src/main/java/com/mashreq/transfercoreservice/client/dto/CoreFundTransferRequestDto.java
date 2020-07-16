package com.mashreq.transfercoreservice.client.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CoreFundTransferRequestDto {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String purposeCode;
    private String dealNumber;
}
