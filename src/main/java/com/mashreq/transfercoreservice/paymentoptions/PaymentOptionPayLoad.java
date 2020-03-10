package com.mashreq.transfercoreservice.paymentoptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashreq.transfercoreservice.client.dto.AccountDetailsDTO;
import com.mashreq.transfercoreservice.client.dto.CardDetailsDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author shahbazkh
 * @date 3/10/20
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentOptionPayLoad {

    @JsonProperty("accounts")
    List<AccountDetailsDTO> accounts;

    @JsonProperty("cards")
    List<CardDetailsDTO> creditCards;

    @JsonProperty("suggestedCard")
    CardDetailsDTO defaultCard;

    @JsonProperty("suggestedAccount")
    AccountDetailsDTO defaultAccount;
}
