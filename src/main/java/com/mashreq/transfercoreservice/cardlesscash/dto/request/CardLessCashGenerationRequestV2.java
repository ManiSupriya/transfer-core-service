package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mashreq.twofa.client.dto.TwoFa;

import java.math.BigDecimal;

/**
 * This class is to hold the detail for the CLC Generated request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CardLessCashGenerationRequestV2 {
	@NotNull(message = "Account Number is manadatory field")
    @Pattern(regexp = "^\\d{12}$", message = "accountNum should have only twelve nummbers")
    private String accountNo;
    @DecimalMin(value = "100.00", message = "Amount should be multiples of hundred")
    @Digits(integer=4, fraction=2)
    private BigDecimal amount;
    @NotEmpty(message = "transaction Type cannot be empty")
    private String transactionType;
    @NotEmpty(message = "source of transaction cannot be empty")
    private String sourceType;
    @NotEmpty(message = "source Identifier should be either account or card")
    private String sourceIdentifier;
    @NotEmpty(message = "currency code cannot be empty")
    private String currencyCode;
    private String beneficiaryName;
    private TwoFa twofa;

}
