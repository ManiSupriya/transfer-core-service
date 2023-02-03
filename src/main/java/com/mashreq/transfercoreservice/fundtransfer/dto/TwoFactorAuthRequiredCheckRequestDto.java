package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.mashreq.mobcommons.annotations.Account;
import com.mashreq.transfercoreservice.annotations.TransactionAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthRequiredCheckRequestDto {
	@NotBlank
	private String beneficiaryId;
	
	@TransactionAmount
    @NotNull(message = "amount cannot be empty")
    private BigDecimal amount;
    
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    @NotNull(message = "account currency cannot be empty")
    private String accountCurrency;
    
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    @NotBlank(message = "txnCurrency cannot be empty")
    private String txnCurrency;
    
    @Account
    @NotBlank(message = "account number should be present")
    private String fromAccount;
    
    @Pattern(regexp = "^$|[a-zA-Z0-9-]+",message="Not a valid Deal number")
    private String dealNumber;
}
