package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is to hold the detail for the CLC Generated request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLessCashGenerationRequest {

    private String accountNo;
    private BigDecimal amount;
    private String mobileNo;
    private BigDecimal fees;
    private String otp;
    private String challengeToken;
}
