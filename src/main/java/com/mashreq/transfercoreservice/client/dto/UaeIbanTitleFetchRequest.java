package com.mashreq.transfercoreservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by KrishnaKo on 05/01/2024
 */
public record UaeIbanTitleFetchRequest( @JsonProperty("WebServiceData")
                                            List<IbanRecord> ibanList) {
}
