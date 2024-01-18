package com.mashreq.transfercoreservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CoreCurrencyConversionRequestDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]{12}$", message = "Please input 12 digits Account number.")
    private String accountNumber;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    private String accountCurrency;

    @DecimalMin("0.0")
    private BigDecimal transactionAmount;
    @DecimalMin("0.0")
    private BigDecimal accountCurrencyAmount;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{3}$")
    private String transactionCurrency;

    private String dealNumber;

    private String productCode;
}
