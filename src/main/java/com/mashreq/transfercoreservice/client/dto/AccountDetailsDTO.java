package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
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

    @JsonProperty("branch")
    private String branchCode;

    @JsonIgnore
    private boolean noDebit;

    @JsonIgnore
    private boolean noCredit;
}
