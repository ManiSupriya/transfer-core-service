package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
	@NotNull(message = "Account Number is manadatory field")
    @Pattern(regexp = "^[0-9]{12}$", message = "accountNum should have only twelve nummbers")
    private String accountNo;
    @DecimalMin(value = "100.00")
    @Digits(integer=4, fraction=2)
    private BigDecimal amount;
    private String otp;
    private String challengeToken;
    private int dpPublicKeyIndex;
    private String dpRandomNumber; 
}
