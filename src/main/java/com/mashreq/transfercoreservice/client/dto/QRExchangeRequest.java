package com.mashreq.transfercoreservice.client.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QRExchangeRequest {

    @NotNull
    private Long benId;
    @NotEmpty
    private String senderAcNum;
    @NotEmpty
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid source currency. 3 uppercase alphabets allowed")
    private String sourceCcy;
    @NotEmpty
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid destination currency. 3 uppercase alphabets allowed")
    private String destinationCcy;
    @NotEmpty
    @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid transaction currency. 3 uppercase alphabets allowed")
    private String transactionCcy;
    @NotNull
    @Digits(integer = 12, fraction = 3, message = "Invalid amount. Max 12 digit and 3 decimal points")
    @Positive
    private BigDecimal transactionAmt;
    @Pattern(regexp = "|INFT|QR", message = "Should be either INFT or QR")
    String initiatedFrom;

}