package com.mashreq.transfercoreservice.cardlesscash.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is to hold the detail for blocking the CLC request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLessCashBlockRequest {
	@NotNull(message = "Reference Number is manadatory field")
    private String referenceNumber;
	@NotNull(message = "Account Number is manadatory field")
    @Pattern(regexp = "^[0-9]{12}$", message = "accountNum should have only twelve nummbers")
    private String accountNumber;
}
