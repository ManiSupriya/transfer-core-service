package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author shahbazkh
 * @date 1/23/20
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardDetailsDTO {

    @JsonProperty("cardNo")
    private String cardNo;

    @JsonProperty("cardType")
    private String cardType;

    @JsonProperty("cardStatus")
    private String cardStatus;

    @JsonProperty("cardHolderName")
    private String cardHolderName;

    @JsonProperty("plasticTypeDescription")
    private String plasticTypeDescription;

    @JsonProperty("currentBalance")
    private BigDecimal currentBalance;

    @JsonProperty("availableCreditLimit")
    private BigDecimal availableCreditLimit;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("primaryCard")
    private Boolean primaryCard;

    @JsonProperty("segment")
    private String segment;

    @JsonProperty("encryptedCardNumber")
    private String encryptedCardNumber;

    public String getCardNo() {
        return StringUtils.isBlank(cardNo) ? null : cardNo.substring(cardNo.length() - 4);
    }
}