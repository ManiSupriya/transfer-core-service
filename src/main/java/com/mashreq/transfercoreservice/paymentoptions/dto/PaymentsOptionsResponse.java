package com.mashreq.transfercoreservice.paymentoptions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author shahbazkh
 * @date 2/20/20
 */

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentsOptionsResponse {

    @JsonProperty("source")
    PaymentOptionPayLoad source;

    @JsonProperty("destination")
    PaymentOptionPayLoad destination;

    @JsonProperty("financialTxnNumber")
    private String finTxnNo;

}

