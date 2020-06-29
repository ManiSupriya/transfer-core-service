package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLessCashGenReq {

    private String accountNumber;
    private BigDecimal amount;
    private String mobileNo;
    private BigDecimal fees;
}
