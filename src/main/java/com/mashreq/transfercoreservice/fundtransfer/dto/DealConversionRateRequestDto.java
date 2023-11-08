package com.mashreq.transfercoreservice.fundtransfer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
@Data
public class DealConversionRateRequestDto {
	@NotBlank
    @Pattern(regexp = "^[0-9]{12}$", message = "Please input 12 digits Account number.")
    private String accountNumber;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    private String accountCurrency;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    private String transactionCurrency;
    
    private BigDecimal transactionAmount;

    private String conversionType;
    
    private String dealNumber;
    
    private String transactionType;
}
