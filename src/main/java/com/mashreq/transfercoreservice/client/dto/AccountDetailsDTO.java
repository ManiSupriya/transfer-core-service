package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDetailsDTO {

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("accountNickName")
    private String accountName;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("accountNumber")
    private String number;

    @JsonProperty("availableBalance")
    private BigDecimal availableBalance;

    @JsonProperty("status")
    private String status;

    @JsonProperty("schemeType")
    private String schemeType;

    @JsonProperty("segment")
    private String segment;
}
