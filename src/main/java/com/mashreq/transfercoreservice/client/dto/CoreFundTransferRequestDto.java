package com.mashreq.transfercoreservice.client.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
public class CoreFundTransferRequestDto {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private String purposeCode;
    private String dealNumber;
}
