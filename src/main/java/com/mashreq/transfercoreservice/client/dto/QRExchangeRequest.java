package com.mashreq.transfercoreservice.client.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

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