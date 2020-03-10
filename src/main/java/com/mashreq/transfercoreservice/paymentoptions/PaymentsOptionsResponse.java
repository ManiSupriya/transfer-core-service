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
 * @date 2/20/20
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentsOptionsResponse {

    @JsonProperty("source")
    PaymentOptionPayLoad source;

    @JsonProperty("destination")
    PaymentOptionPayLoad destination;

}

