package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import com.mashreq.transactionauth.twofa.TwoFaBaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * This class is to hold the detail for the CLC Generated request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CardLessCashGenerationRequest extends TwoFaBaseModel {
	@NotNull(message = "Account Number is manadatory field")
    @Pattern(regexp = "^[0-9]{12}$", message = "accountNum should have only twelve nummbers")
    private String accountNo;
    @DecimalMin(value = "100.00", message = "Amount should be multiples of hundred")
    @Digits(integer=4, fraction=2)
    private BigDecimal amount;

}
